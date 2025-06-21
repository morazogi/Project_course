package PresentorLayer;

import ServiceLayer.AuctionService;
import ServiceLayer.UserService;
import DomainLayer.Product;
import DomainLayer.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class AuctionManagerPresenter {

    private final AuctionService auctionService;
    private final UserService userService;
    private final String managerId;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuctionManagerPresenter(String managerId,
                                   AuctionService auctionService,
                                   UserService userService) {
        this.managerId = managerId;
        this.auctionService = auctionService;
        this.userService = userService;
    }

    public void createAuction(String token,
                              String storeName,
                              String productName,
                              String price,
                              String description) throws Exception {
        double startPrice = Double.parseDouble(price);

        List<Store> stores = userService.searchStoreByName(token, storeName);
        if (stores.isEmpty()) throw new RuntimeException("store not found");
        String storeId = stores.get(0).getId();

        String productId = null;
        for (Product p : userService.getProductsInStore(storeId)) {
            if (p.getName().equalsIgnoreCase(productName)) {
                productId = p.getId();
                break;
            }
        }
        if (productId == null) throw new RuntimeException("product not found in that store");

        auctionService.create(storeId, productId, managerId, startPrice);
    }

    public List<Offer> getOffers() {
        return auctionService.list().stream()
                .filter(a -> a.isWaitingConsent())
                .map(a -> new Offer(a.getLastParty(), a.getProductId(), a.getCurrentPrice()))
                .toList();
    }

    public String respondToOffer(String token, String action, String counterPrice) {
        return auctionService.list().stream()
                .filter(a -> a.isWaitingConsent())
                .findFirst()
                .map(a -> switch (action) {
                    case "accept"  -> { auctionService.accept(a.getId(), managerId);
                        yield "Accepted – waiting for buyer’s payment."; }
                    case "decline" -> { auctionService.decline(a.getId(), managerId);
                        yield "Offer declined / auction closed."; }
                    case "counter" -> { auctionService.offer(a.getId(), managerId,
                            Double.parseDouble(counterPrice));
                        yield "Countered with $" + counterPrice; }
                    default        -> "Unknown action.";
                })
                .orElse("No pending offers.");
    }
}
