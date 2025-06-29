package PresentorLayer;

import DomainLayer.Auction;
import DomainLayer.Product;
import DomainLayer.Store;
import ServiceLayer.AuctionService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Handles all manager-side actions for auctions:
 *  • creating a new auction
 *  • viewing / accepting / declining / counter-offering on bids
 */
public class AuctionManagerPresenter {

    private final String           managerId;
    private final String           token;          // session/JWT (needed for store lookup)
    private final AuctionService   auctionService;
    private final UserService      userService;
    private final ObjectMapper     mapper = new ObjectMapper();

    /* ------------------------------------------------------------ */

    public AuctionManagerPresenter(String managerId,
                                   String token,
                                   AuctionService auctionService,
                                   UserService userService) {

        this.managerId      = managerId;
        this.token          = token;
        this.auctionService = auctionService;
        this.userService    = userService;
    }

    /* ------------------------------------------------------------
       1.  Create a new auction for a product in a given store
       ------------------------------------------------------------ */
    public void createAuction(String sessionToken,
                              String storeName,
                              String productName,
                              String price,
                              String description /* << not yet used but kept */)
            throws Exception {

        double startPrice = Double.parseDouble(price);

        /* locate the store by name */
        List<Store> stores = userService.searchStoreByName(sessionToken, storeName);
        if (stores.isEmpty())
            throw new RuntimeException("Store '" + storeName + "' not found");
        String storeId = stores.get(0).getId();

        /* locate product within that store */
        String productId = null;
        for (Product p : userService.getProductsInStore(storeId))
            if (p.getName().equalsIgnoreCase(productName)) {
                productId = p.getId();
                break;
            }
        if (productId == null)
            throw new RuntimeException("Product '" + productName + "' not found in that store");

        auctionService.create(storeId, productId, managerId, startPrice);
    }

    /* ------------------------------------------------------------
       2.  Display pending offers with readable names
       ------------------------------------------------------------ */
    public List<Offer> getOffers() {
        return auctionService.list().stream()
                .filter(Auction::isWaitingConsent)
                .map(a -> {
                    String prodName  = a.getProductId();
                    String storeName = a.getStoreId();
                    try {
                        Product p = userService.getProductById(a.getProductId()).orElse(null);
                        if (p != null) {
                            prodName  = p.getName();
                            storeName = mapper.readValue(
                                    userService.getStoreById(token, p.getStoreId()),
                                    Store.class).getName();
                        }
                    } catch (Exception ignored) {}
                    return new Offer(a.getId(),          /* NEW */
                            a.getLastParty(),  /* buyer */
                            storeName, prodName,
                            a.getCurrentPrice());
                })
                .toList();
    }


    /* ------------------------------------------------------------
       3.  Accept / decline / counter an offer
       ------------------------------------------------------------ */
    /* ── replace the entire respondToOffer method ─────────────────── */
    public String respondToOffer(String sessionToken,
                                 String action,
                                 String counterPriceStr,
                                 String auctionId) {

        if (auctionId == null || auctionId.isBlank())
            return "Select an offer first.";

        Auction a = auctionService.list().stream()
                .filter(Auction::isWaitingConsent)
                .filter(x -> x.getId().equals(auctionId))
                .findFirst().orElse(null);

        if (a == null)
            return "That offer is no longer pending.";

        try {
            return switch (action) {
                case "accept" -> {
                    auctionService.accept(a.getId(), managerId);
                    yield "Accepted – waiting for buyer’s payment.";
                }
                case "decline" -> {
                    auctionService.decline(a.getId(), managerId);
                    yield "Offer declined – auction reopened to customers.";
                }
                case "counter" -> {
                    if (counterPriceStr == null || counterPriceStr.isBlank())
                        yield "Enter a counter-price first.";
                    double counter = Double.parseDouble(counterPriceStr);
                    auctionService.offer(a.getId(), managerId, counter);
                    yield "Counter-offer sent: $" + counter;
                }
                default -> "Unknown action.";
            };
        } catch (IllegalArgumentException ex) {
            /* Domain layer will throw “same party” or other turn errors */
            String msg = ex.getMessage();
            return (msg != null && !msg.isBlank())
                    ? msg
                    : "Action not allowed – wait for the other side.";
        }
    }


    public String removeAuction(String auctionId) {
        try {
            auctionService.cancel(auctionId, managerId);
            return "Auction removed.";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public List<Offer> listAllAuctions() {
        return auctionService.list().stream()
                .map(a -> {
                    String prodName  = a.getProductId();
                    String storeName = a.getStoreId();
                    try {
                        Product p = userService.getProductById(a.getProductId()).orElse(null);
                        if (p != null) {
                            prodName  = p.getName();
                            storeName = mapper.readValue(
                                    userService.getStoreById(token, p.getStoreId()),
                                    Store.class).getName();
                        }
                    } catch (Exception ignored) {}
                    return new Offer(a.getId(),        /* re-use constructor */
                            a.getLastParty(),
                            storeName, prodName,
                            a.getCurrentPrice());
                })
                .toList();
    }

}
