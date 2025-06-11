package PresentorLayer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds auction data in-memory and exposes a Vaadin Component for the UI.
 * Replace / extend with real service-layer calls when ready.
 */
public class AuctionPresenter {

    private final List<Auction> auctions = new ArrayList<>();
    private String name;

    public AuctionPresenter(String userName) {
        this.name = userName;
        // Demo data – delete when wired to backend
        auctions.add(new Auction("A01", "Gaming Laptop",   1000, "name1"));
        auctions.add(new Auction("A02", "Wireless Headset",  90, "name2"));
    }

    /*──────────────────── Public API for UI ────────────────────*/

    /** returns a Vaadin component that lists every auction neatly */
    public Component getAuctionsComponent() {
        Div container = new Div();
        if (auctions.isEmpty()) {
            container.add(new Span("No auctions available"));
        } else {
            for (Auction a : auctions) {
                container.add(new Span(
                        a.itemId + " – " + a.itemName +
                                " | Current Offer: $" + a.currentPrice +
                                " (by " + a.lastOfferedBy + ")"
                ));
                container.add(new Div());   // simple spacer
            }
        }
        return container;
    }

    /**
     * Customer attempts to place (or counter) an offer.
     * Returns a human-readable message for the UI.
     */
    public String placeOffer(String itemId, double price) {
        for (Auction a : auctions) {
            if (a.itemId.equals(itemId)) {

                // in a simple ping-pong model we allow ANY price;
                // business rules (must be higher / lower, etc.) go here
                this.auctions.add(new Auction(itemId, a.itemName, price, this.name));
                // here we need to sync it with the auction manager list we will do it throw the service layer since it will hold them both
                return "Offer submitted!";
            }
        }
        return "Auction with given Item ID not found.";
    }


    private static class Auction {
        final String itemId;
        final String itemName;
        double currentPrice;
        String lastOfferedBy;

        Auction(String id, String name, double startPrice, String provider) {
            this.itemId      = id;
            this.itemName    = name;
            this.currentPrice = startPrice;
            this.lastOfferedBy = provider;
        }
    }
}
