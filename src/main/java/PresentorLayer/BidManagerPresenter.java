package PresentorLayer;

import DomainLayer.IToken;
import com.vaadin.flow.component.Component;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Span;

public class BidManagerPresenter {

    //private final BidService bidService;
    private final List<Offer> offerLogs = new ArrayList<>();

    public BidManagerPresenter() { //BidService bidService,
        //this.bidService = bidService;

    }

    public void startBid(String token, String productId, String productName, String startPrice, String minIncrease, String duration) {
        // Optional: validate and convert string values to numbers
        double price = Double.parseDouble(startPrice);
        double increase = Double.parseDouble(minIncrease);
        int durationMinutes = Integer.parseInt(duration);

        // TODO create from service layer going down
        // openBid(token, productId, productName, price, increase, durationMinutes);
        // in this class we also need to send the bid to the Bid ui
    }

    public void addOffer(String buyerName, String productName, double amount) {
        offerLogs.add(new Offer(buyerName, productName, amount));
    }

    public Component getOffers() {
        VerticalLayout layout = new VerticalLayout();
        for (Offer offer : offerLogs) {
            layout.add(new Span(offer.toString()));
        }
        return layout;
    }
}
