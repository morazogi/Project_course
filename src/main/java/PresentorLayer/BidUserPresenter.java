package PresentorLayer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

public class BidUserPresenter {

    private final List<Bid> bids = new ArrayList<>();

    public BidUserPresenter() {
        // Example bids
        //bids.add(new Bid("P01", "Product A", 100));
        //bids.add(new Bid("P02", "Product B", 150));
    }

    public List<Bid> getBids() {
        return bids;
    }

    // Return a Component listing bids + current price
    public Component getBidsComponent() {
        Div container = new Div();
        if (bids.isEmpty()) {
            container.add(new Span("No bids available"));
        } else {
            for (Bid bid : bids) {
                container.add(new Span(
                        bid.getProductId() + ": " + bid.getProductName() + " - Current Price: $" + bid.getCurrentPrice()
                ));
                container.add(new Div()); // spacing
            }
        }
        return container;
    }

    // Try to place a bid on a product; returns message if success or fail
    public String placeBid(String productId, double bidAmount) {
        for (Bid bid : bids) {
            if (bid.getProductId().equals(productId)) {
                if (bidAmount > bid.getCurrentPrice()) {
                    bid.setCurrentPrice(bidAmount);
                    // didnt rly know where to do it, but the class that handles it needs to call for the add bid function in the bidManagerPresentor
                    // so the bid will be shown in the ui layer and the database etc...
                    return "Bid placed successfully!";
                } else {
                    return "Bid amount must be higher than current price.";
                }
            }
        }
        return "Bid with product ID not found.";
    }
}
