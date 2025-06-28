package ServiceLayer;

import DomainLayer.Auction;
import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.OrderRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
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

    public void offer(String auctionId, String buyer, double price) {
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

    public void pay(String auctionId,
                    String token,
                    String name,
                    String cardNumber,
                    String expDate,
                    String cvv,
                    String state,
                    String city,
                    String address,
                    String id,
                    String zip) {

        Auction a = auctions.get(auctionId);
        if (a == null)                    throw new RuntimeException("auction not found");
        if (!a.isAwaitingPayment())       throw new RuntimeException("auction not payable");

        String buyer = tokenService.extractUsername(token);
        if (!buyer.equals(a.getWinner())) throw new RuntimeException("not your auction");

        /* 1. charge card */
        paymentService.processPayment(
                token, name, cardNumber, expDate, cvv, id);

        /* 2. arrange shipping */
        shippingService.processShipping(
                token, state, city, address, name, zip);

        /* 3. update stock and store ledger */
        Store store = storeRepository.getById(a.getStoreId());
        Product product = productRepository.getById(a.getProductId());
        if (product == null) throw new RuntimeException("product not found");
        if (product.getQuantity() < 1)   throw new RuntimeException("out of stock");

        /* decrement */
        store.sellProduct(product.getId(), 1);
        product.setQuantity(product.getQuantity() - 1);

        storeRepository.update(store);
        productRepository.save(product);

        /* optional: record internal order */
        orderRepository.save(
                new DomainLayer.Order("auction:" + auctionId,
                        store.getId(),
                        buyer,
                        new Date()));

        /* 4. close auction */
        auctions.remove(auctionId);
    }
}
