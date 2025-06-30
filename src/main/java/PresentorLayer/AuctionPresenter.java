package PresentorLayer;

import ServiceLayer.AuctionService;
import ServiceLayer.UserService;
import DomainLayer.Auction;
import DomainLayer.Product;
import DomainLayer.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.List;

/**
 * All buyer-side behaviour for auctions:
 *  • listing auctions for the UI
 *  • placing offers (by names or by selected auction-id)
 *  • accepting / declining a manager counter-offer
 *  • redirecting to payment when the buyer wins
 */
public class AuctionPresenter {

    private final String username;
    private final String token;
    private final AuctionService auctionService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    /* ───────────────────────────────────────────────────────────── */
    public AuctionPresenter(String username,
                            String token,
                            AuctionService auctionService,
                            UserService userService) {
        this.username = username;
        this.token    = token;
        this.auctionService = auctionService;
        this.userService    = userService;
    }

    /* ─────────────────────────────────────────────────────────────
       0.  Legacy component builder (still used in manager pages)
       ------------------------------------------------------------ */
    public Component getAuctionsComponent() {
        Div container = new Div();
        List<Auction> list = auctionService.list();
        if (list.isEmpty()) {
            container.add(new Span("No auctions available"));
            return container;
        }

        for (Auction a : list) {
            String storeName = "?", productName = "?";
            try {
                Store s = mapper.readValue(
                        userService.getStoreById(token, a.getStoreId()), Store.class);
                storeName = s.getName();
                Product p = userService.getProductById(a.getProductId()).orElse(null);
                if (p != null) productName = p.getName();
            } catch (Exception ignored) {}

            container.add(new Span(
                    storeName + " – " + productName +
                            " | $" + a.getCurrentPrice() +
                            " (" + a.getLastParty() + ")" +
                            (a.isWaitingConsent() ? " [awaiting consent]" : "")
            ));

            if (a.isAwaitingPayment() && username.equals(a.getWinner())) {
                Span pay = new Span("  → Pay now");
                pay.getStyle().set("color","blue").set("cursor","pointer");
                String id = a.getId();
                pay.addClickListener(ev ->
                        UI.getCurrent().navigate("/auctionpay/" + id));
                container.add(pay);
            }
            container.add(new Div());
        }
        return container;
    }

    /* ─────────────────────────────────────────────────────────────
       1.  Offer by store / product names (used when no row selected)
       ------------------------------------------------------------ */
    public String placeOffer(String storeName, String productName, double price) {
        for (Auction a : auctionService.list()) {
            try {
                Store s = mapper.readValue(
                        userService.getStoreById(token, a.getStoreId()), Store.class);
                Product p = userService.getProductById(a.getProductId()).orElse(null);
                if (s.getName().equalsIgnoreCase(storeName)
                        && p != null
                        && p.getName().equalsIgnoreCase(productName)
                        && !a.isWaitingConsent()) {
                    auctionService.offer(a.getId(), username, price);
                    return "Offer submitted!";
                }
            } catch (Exception ignored) {}
        }
        return "Matching auction not found or awaiting consent.";
    }

    /* convenience pass-through for the payment UI */
    public void pay(String auctionId, String token,
                    String name, String card, String exp, String cvv,
                    String state,String city,String addr,
                    String id,String zip) {
        auctionService.pay(auctionId, token, name, card, exp, cvv,
                state, city, addr, id, zip);
    }

    /* ─────────────────────────────────────────────────────────────
       2.  Accept / decline by store & product names
       ------------------------------------------------------------ */
    public String respondToPending(String storeName, String productName, String action) {

        return auctionService.list().stream()
                .filter(Auction::isWaitingConsent)            // needs reply
                .filter(a -> !a.getLastParty().equals(username)) // reply must be from me
                .filter(a -> {                                // match names typed
                    try {
                        Product p = userService.getProductById(a.getProductId()).orElse(null);
                        Store   s = mapper.readValue(
                                userService.getStoreById(token, a.getStoreId()),
                                Store.class);
                        return p != null && s != null &&
                                s.getName().equalsIgnoreCase(storeName) &&
                                p.getName().equalsIgnoreCase(productName);
                    } catch (Exception e) { return false; }
                })
                .findFirst()
                .map(a -> switch (action) {
                    case "accept"  -> {
                        auctionService.accept(a.getId(), username);
                        yield "Accepted – proceed to payment.";
                    }
                    case "decline" -> {
                        auctionService.decline(a.getId(), username);
                        yield "Offer declined – auction still open.";
                    }
                    default        -> "Unknown action.";
                })
                .orElse("No pending offer matches those names.");
    }

    /* ─────────────────────────────────────────────────────────────
       3.  Row-selection support for the new UI
       ------------------------------------------------------------ */

    /** Immutable view-object for the UI. */
    public static class AuctionView {
        public final String  id, storeName, productName, lastParty;
        public final double  price;
        public final boolean waitingConsent, awaitingPayment, payableByUser;

        public AuctionView(String id, String store, String prod, double price,
                           String lastParty, boolean waiting, boolean awaitPay,
                           boolean payableByUser) {
            this.id = id;  this.storeName = store; this.productName = prod;
            this.price = price; this.lastParty = lastParty;
            this.waitingConsent = waiting; this.awaitingPayment = awaitPay;
            this.payableByUser  = payableByUser;
        }
    }

    /** Build a list of readable rows for the buyer UI. */
    public List<AuctionView> listAuctions() {
        return auctionService.list().stream().map(a -> {
            String store = a.getStoreId(), prod = a.getProductId();
            try {
                Store  s = mapper.readValue(
                        userService.getStoreById(token, a.getStoreId()),
                        Store.class);
                Product p = userService.getProductById(a.getProductId()).orElse(null);
                if (s != null) store = s.getName();
                if (p != null) prod  = p.getName();
            } catch (Exception ignored) {}
            return new AuctionView(a.getId(), store, prod, a.getCurrentPrice(),
                    a.getLastParty(), a.isWaitingConsent(),
                    a.isAwaitingPayment(),
                    a.isAwaitingPayment() && username.equals(a.getWinner()));
        }).toList();
    }

    /* ─────────────────────────────────────────────────────────────
       4.  Offer / reply when the UI provides a selected auction-id
       ------------------------------------------------------------ */

    /** Place an offer on a selected auction row. */
    public String placeOffer(String auctionId, double price) {
        Auction a = auctionService.list().stream()
                .filter(x -> x.getId().equals(auctionId))
                .findFirst().orElse(null);

        if (a == null)                return "Auction not found.";
        if (a.isAwaitingPayment())    return "Auction already closed.";
        if (a.isWaitingConsent() && username.equals(a.getLastParty()))
            return "You already sent an offer – wait for the counter-response.";


        auctionService.offer(auctionId, username, price);
        return "Offer submitted!";
    }

    /** Accept or decline a counter-offer on the selected row. */
    public String respondToPending(String auctionId, String action) {

        Auction a = auctionService.list().stream()
                .filter(x -> x.getId().equals(auctionId))
                .filter(Auction::isWaitingConsent)
                .findFirst().orElse(null);

        if (a == null) return "No pending offer selected.";

        if (username.equals(a.getLastParty()))
            return "Wait for the counter-offer before responding.";

        if ("accept".equals(action)) {
            auctionService.accept(a.getId(), username);
            return "Accepted – proceed to payment.";
        }
        if ("decline".equals(action)) {
            auctionService.decline(a.getId(), username);
            return "Offer declined – auction still open.";
        }
        return "Unknown action.";
    }
}
