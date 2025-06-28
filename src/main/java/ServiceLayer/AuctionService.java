package ServiceLayer;

import DomainLayer.Auction;
import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.OrderRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuctionService {

    /* in-memory auction board */
    private final Map<String, Auction> auctions = new ConcurrentHashMap<>();

    /* collaborators wired from Spring */
    private final PaymentService     paymentService;
    private final ShippingService    shippingService;
    private final IToken             tokenService;
    private final StoreRepository    storeRepository;
    private final ProductRepository  productRepository;
    private final OrderRepository    orderRepository;   // optional, keeps a sales history

    public AuctionService(PaymentService    paymentService,
                          ShippingService   shippingService,
                          IToken            tokenService,
                          StoreRepository   storeRepository,
                          ProductRepository productRepository,
                          OrderRepository   orderRepository) {

        this.paymentService   = paymentService;
        this.shippingService  = shippingService;
        this.tokenService     = tokenService;
        this.storeRepository  = storeRepository;
        this.productRepository= productRepository;
        this.orderRepository  = orderRepository;
    }

    /*────────────────────────── basic CRUD ──────────────────────────*/

    public Auction create(String storeId,
                          String productId,
                          String managerId,
                          double startPrice) {

        Auction a = new Auction(storeId, productId, managerId, startPrice);
        auctions.put(a.getId(), a);
        return a;
    }

    public List<Auction> list() {
        return new ArrayList<>(auctions.values());
    }

    /*────────────────────────── customer offer ─────────────────────*/

    /*────────────────────────── customer offer ─────────────────────*/
    public void offer(String auctionId, String buyer, double price) {

        /* ★ NEW – block guest offers */
        if (buyer == null || buyer.startsWith("Guest")) {
            throw new RuntimeException("You must be logged-in to make an offer.");
        }

        auctions.get(auctionId).offer(buyer, price);
    }

    /*────────────────────────── manager actions ────────────────────*/

    public void accept(String auctionId, String managerId) {
        Auction a = auctions.get(auctionId);
        a.accept(managerId);                                    // clears waitingConsent
        a.markAwaitingPayment(a.getLastParty(), a.getCurrentPrice());
    }

    public void decline(String auctionId, String managerId) {
        auctions.remove(auctionId);
    }

    /*────────────────────────── buyer payment ──────────────────────*/
    @Transactional
    public void pay(String auctionId,
                    String token,
                    String name, String cardNumber, String expDate, String cvv,
                    String state, String city, String address,
                    String id,    String zip) {

        if (!cardNumber.matches("\\d{13,19}"))
            throw new RuntimeException("Invalid card number");
        if (!expDate.matches("(0[1-9]|1[0-2])\\/\\d{2}"))
            throw new RuntimeException("Invalid expiry – use MM/YY");
        if (!cvv.matches("\\d{3,4}"))
            throw new RuntimeException("Invalid CVV");

        Auction a = auctions.get(auctionId);
        if (a == null)              throw new RuntimeException("auction not found");
        if (!a.isAwaitingPayment()) throw new RuntimeException("auction not payable");

        String buyer = tokenService.extractUsername(token);
        if (!buyer.equals(a.getWinner())) throw new RuntimeException("not your auction");

        /* ──────── sanity before we touch external services ──────── */
        Store   store   = storeRepository.getById(a.getStoreId());
        if (store == null) throw new RuntimeException("store not found");
        if (!store.isOpenNow())
            throw new RuntimeException("Cannot purchase – store '" + store.getName() + "' is closed");

        Product product = productRepository.getById(a.getProductId());
        if (product == null) throw new RuntimeException("product missing");

        /* ──────── reserve stock first ──────── */
        boolean reserved = false;
        String  shippingTx = null;
        String  paymentTx  = null;
        try {
            if (!store.reserveProduct(product.getId(), 1))
                throw new RuntimeException("out of stock");
            reserved = true;
            storeRepository.update(store);                         // persist reservation

            /* 1. arrange shipping, then 2. charge card */
            shippingTx = shippingService.processShipping(
                    token, state, city, address, name, zip);
            paymentTx  = paymentService.processPayment(
                    token, name, cardNumber, expDate, cvv, id);

            /* 3. commit sale */
            store.sellProduct(product.getId(), 1);
            product.setQuantity(product.getQuantity() - 1);
            storeRepository.update(store);
            productRepository.save(product);

            /* 4. optional order record & close auction */
            orderRepository.save(new DomainLayer.Order("auction:" + auctionId,
                    store.getId(), buyer, new Date()));
            auctions.remove(auctionId);

        } catch (Exception ex) {

            /* rollback everything we managed to do */
            if (reserved) {
                try {
                    store.unreserveProduct(product.getId(), 1);
                    storeRepository.update(store);
                } catch (Exception ignored) {}
            }
            if (shippingTx != null) {
                try { shippingService.cancelShipping(token, shippingTx); } catch (Exception ignored) {}
            }
            if (paymentTx != null)  {
                try { paymentService.cancelPayment(token, paymentTx);   } catch (Exception ignored) {}
            }

            String msg = (ex.getMessage() == null || ex.getMessage().isBlank())
                    ? "Failed to pay for auction" : ex.getMessage();
            throw new RuntimeException(msg);
        }
    }

}
