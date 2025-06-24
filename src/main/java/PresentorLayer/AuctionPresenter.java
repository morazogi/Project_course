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

public class AuctionPresenter {

    private final String username;
    private final String token;
    private final AuctionService auctionService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuctionPresenter(String username,
                            String token,
                            AuctionService auctionService,
                            UserService userService) {
        this.username = username;
        this.token = token;
        this.auctionService = auctionService;
        this.userService = userService;
    }

    public Component getAuctionsComponent() {
        Div container = new Div();
        List<Auction> list = auctionService.list();
        if (list.isEmpty()) {
            container.add(new Span("No auctions available"));
            return container;
        }

        for (Auction a : list) {
            String storeName = "?";
            String productName = "?";
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
                pay.getStyle().set("color", "blue").set("cursor", "pointer");
                String id = a.getId();
                pay.addClickListener(ev ->
                        UI.getCurrent().navigate("/auctionpay/" + id));
                container.add(pay);
            }
            container.add(new Div());
        }
        return container;
    }

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

    public void pay(String auctionId, String token, String name, String cardNumber, String expirationDate, String cvv, String state, String city, String address, String id, String zip) {
        auctionService.pay(auctionId, token, name, cardNumber, expirationDate, cvv, state, city, address, id, zip);
    }
}
