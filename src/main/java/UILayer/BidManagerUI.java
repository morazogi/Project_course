package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidManagerPresenter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route("/bidmanager")
public class BidManagerUI extends VerticalLayout {

    private final BidManagerPresenter bidManagerPresenter;
    private final List<Span> offerLines = new ArrayList<>();

    @Autowired
    public BidManagerUI(IToken tokenService) { //BidService bidService,
        this.bidManagerPresenter = new BidManagerPresenter(); //bidService,

        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        // UI Fields
        TextField productId = new TextField("Product ID");
        TextField productName = new TextField("Product Name");
        TextField startingPrice = new TextField("Starting Price");
        TextField minBidIncrease = new TextField("Minimum Increase");
        TextField bidDuration = new TextField("Duration (minutes)");

        Span error = new Span("");

        Button startBid = new Button("Start Bid", e -> {
            try {
                bidManagerPresenter.startBid(
                        token,
                        productId.getValue(),
                        productName.getValue(),
                        startingPrice.getValue(),
                        minBidIncrease.getValue(),
                        bidDuration.getValue()
                );
                Notification.show("Bid started successfully!");
                error.setText("");
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        add(
                new H1("Start a Bid"),
                new HorizontalLayout(productId, productName),
                new HorizontalLayout(startingPrice, minBidIncrease, bidDuration),
                startBid,
                error,
                bidManagerPresenter.getOffers() // placeholder for future real offers
        );

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}
