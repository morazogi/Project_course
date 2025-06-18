package PresentorLayer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

public class AuctionManagerPresenter {

    private final List<Offer> offers = new ArrayList<>();

    public void createAuction(String token, String id, String name, String price, String description) {
        // TODO: Connect to service layer and backend
        System.out.println("Creating auction: " + id + ", " + name + ", $" + price + ", " + description);
        // For now, simulate adding a new auction
    }


    public void addOffer(String buyer, String itemName, double offerAmount) {
        offers.add(new Offer(buyer, itemName, offerAmount));
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public String respondToOffer(String token, String action, String counterPrice) {
        switch (action) {
            case "accept":
                return "Offer accepted.";
            case "decline":
                return "Offer declined.";
            case "counter":
                return "Countered with: $" + counterPrice;
            default:
                return "Unknown action.";
        }
    }
}
