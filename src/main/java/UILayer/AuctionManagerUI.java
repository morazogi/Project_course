package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionManagerPresenter;
import PresentorLayer.Offer;
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

@Route("/auctionManagerUI")
public class AuctionManagerUI extends VerticalLayout {

    private final AuctionManagerPresenter presenter;
    private final Span statusMessage = new Span();
    private final VerticalLayout offerDisplayLayout = new VerticalLayout();

    @Autowired
    public AuctionManagerUI(IToken tokenService) {
        this.presenter = new AuctionManagerPresenter();

        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        // === Auction Creation ===
        TextField itemIdField = new TextField("Item ID");
        TextField itemNameField = new TextField("Item Name");
        TextField itemPriceField = new TextField("Starting Price");
        TextField itemDescriptionField = new TextField("Description");

        Button createAuctionButton = new Button("Create Auction", e -> {
            try {
                presenter.createAuction(token,
                        itemIdField.getValue().trim(),
                        itemNameField.getValue().trim(),
                        itemPriceField.getValue().trim(),
                        itemDescriptionField.getValue().trim());

                Notification.show("Auction created.");
                statusMessage.setText("");
            } catch (Exception ex) {
                statusMessage.setText("Error: " + ex.getMessage());
            }
        });

        // === Offer Handling ===
        TextField counterPriceField = new TextField("Counter Price");

        Button refreshOffersButton = new Button("Refresh Offers", e -> renderOffers());

        Button acceptButton = new Button("Accept", e -> {
            statusMessage.setText(presenter.respondToOffer(token, "accept", null));
            // to add a nice message and update down to the data base
        });

        Button declineButton = new Button("Decline", e -> {
            statusMessage.setText(presenter.respondToOffer(token, "decline", null));
            // to add a nice message and update down to the data base
        });

        Button counterButton = new Button("Counter", e -> {
            statusMessage.setText(presenter.respondToOffer(token, "counter", counterPriceField.getValue()));
        });

        add(
                new H1("Create New Auction"),
                new HorizontalLayout(itemIdField, itemNameField),
                new HorizontalLayout(itemPriceField, itemDescriptionField),
                createAuctionButton,
                statusMessage,

                new H1("Customer Offers"),
                refreshOffersButton,
                offerDisplayLayout,
                counterPriceField,
                new HorizontalLayout(acceptButton, declineButton, counterButton)
        );

        renderOffers();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    private void renderOffers() {
        offerDisplayLayout.removeAll();
        if (presenter.getOffers().isEmpty()) {
            offerDisplayLayout.add(new Span("No offers yet."));
        } else {
            for (Offer offer : presenter.getOffers()) {
                offerDisplayLayout.add(new Span(offer.toString()));
            }
        }
    }
}
