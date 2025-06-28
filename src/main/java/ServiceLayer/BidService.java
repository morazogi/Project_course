package ServiceLayer;

import DomainLayer.BidSale;
import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.OrderRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BidService {

    private final Map<String,BidSale> bids = new ConcurrentHashMap<>();

    private final PaymentService   paymentService;
    private final ShippingService  shippingService;
    private final IToken           tokenService;
    private final StoreRepository  storeRepo;
    private final ProductRepository productRepo;
    private final OrderRepository  orderRepo;

    public BidService(PaymentService   paymentService,
                      ShippingService  shippingService,
                      IToken           tokenService,
                      StoreRepository  storeRepo,
                      ProductRepository productRepo,
                      OrderRepository  orderRepo) {

        this.paymentService = paymentService;
        this.shippingService= shippingService;
        this.tokenService   = tokenService;
        this.storeRepo      = storeRepo;
        this.productRepo    = productRepo;
        this.orderRepo      = orderRepo;
    }

    /*──────────────────── creation ────────────────────*/
    public BidSale start(String storeId,
                         String productId,
                         double startPrice,
                         double minInc,
                         int minutes) {

        BidSale b = new BidSale(storeId, productId, startPrice,
                minInc, LocalDateTime.now().plusMinutes(minutes));
        bids.put(b.getId(), b);
        return b;
    }

    /*──────────────────── bidding ─────────────────────*/
    public void place(String bidId, String bidder, double amount) {

        /* ★ NEW – guests may look but not bid */
        if (bidder == null || bidder.startsWith("Guest")) {
            throw new RuntimeException("You must be logged-in to place a bid.");
        }

        bids.get(bidId).bid(bidder, amount, LocalDateTime.now());
    }

    /*──────────────────── view / housekeeping ─────────*/
    public List<BidSale> open() {
        LocalDateTime now = LocalDateTime.now();

        /* move finished bids to payment stage */
        bids.values().forEach(b -> {
            if (!b.isAwaitingPayment() && now.isAfter(b.getEndAt()))
                b.markAwaitingPayment();
        });

        return new ArrayList<>(bids.values());
    }

    /*──────────────────── payment ─────────────────────*/
    /*──────────────────── payment ─────────────────────*/
    @Transactional
    public void pay(String bidId,
                    String token,
                    String name, String card, String exp, String cvv,
                    String state, String city, String address,
                    String id,    String zip) {

        /* ──────── basic card-data sanity ──────── */
        if (!card.matches("\\d{13,19}"))
            throw new RuntimeException("Invalid card number");
        if (!exp.matches("(0[1-9]|1[0-2])\\/\\d{2}"))
            throw new RuntimeException("Invalid expiry – use MM/YY");
        if (!cvv.matches("\\d{3,4}"))
            throw new RuntimeException("Invalid CVV");


        BidSale b = bids.get(bidId);
        if (b == null)              throw new RuntimeException("bid not found");
        if (!b.isAwaitingPayment()) throw new RuntimeException("not payable");

        String buyer = tokenService.extractUsername(token);
        if (!buyer.equals(b.getWinner())) throw new RuntimeException("not your bid");

        /* ──────── sanity before we touch external services ──────── */
        Store   store   = storeRepo.getById(b.getStoreId());
        if (store == null) throw new RuntimeException("store not found");
        if (!store.isOpenNow())
            throw new RuntimeException("Cannot purchase – store '" + store.getName() + "' is closed");

        Product product = productRepo.getById(b.getProductId());
        if (product == null) throw new RuntimeException("product missing");

        /* ──────── reserve stock first ──────── */
        boolean reserved = false;
        String  shippingTx = null;
        String  paymentTx  = null;
        try {
            if (!store.reserveProduct(product.getId(), 1))
                throw new RuntimeException("out of stock");
            reserved = true;
            storeRepo.update(store);                              // persist reservation

            /* 1. arrange shipping, then 2. charge card */
            shippingTx = shippingService.processShipping(
                    token, state, city, address, name, zip);
            paymentTx  = paymentService.processPayment(
                    token, name, card, exp, cvv, id);

            /* 3. commit sale */
            store.sellProduct(product.getId(), 1);
            product.setQuantity(product.getQuantity() - 1);
            storeRepo.update(store);
            productRepo.save(product);

            /* 4. record order & remove bid */
            orderRepo.save(new DomainLayer.Order("bid:" + bidId,
                    store.getId(), buyer, new Date()));
            bids.remove(bidId);

        } catch (Exception ex) {

            /* rollback everything we managed to do */
            if (reserved) {
                try {
                    store.unreserveProduct(product.getId(), 1);
                    storeRepo.update(store);
                } catch (Exception ignored) {}
            }
            if (shippingTx != null) {
                try { shippingService.cancelShipping(token, shippingTx); } catch (Exception ignored) {}
            }
            if (paymentTx != null)  {
                try { paymentService.cancelPayment(token, paymentTx);   } catch (Exception ignored) {}
            }

            String msg = (ex.getMessage() == null || ex.getMessage().isBlank())
                    ? "Failed to pay for bid" : ex.getMessage();
            throw new RuntimeException(msg);
        }
    }

}
