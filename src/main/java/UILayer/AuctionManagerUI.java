package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionManagerPresenter;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.Offer;
import ServiceLayer.AuctionService;
import ServiceLayer.UserService;
import ServiceLayer.RegisteredService;
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
    private final ButtonPresenter buttonPresenter;

    @Autowired
    public AuctionManagerUI(IToken tokenService,
                            AuctionService auctionService,
                            UserService userService) {
    public AuctionManagerUI(IToken tokenService, RegisteredService registeredService) {
        this.presenter = new AuctionManagerPresenter();
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        String manager = token != null ? tokenService.extractUsername(token) : "unknown";

        this.presenter = new AuctionManagerPresenter(manager, auctionService, userService);

        TextField storeNameField = new TextField("Store Name");
        TextField productNameField = new TextField("Product Name");
        TextField itemPriceField = new TextField("Starting Price");
        TextField itemDescriptionField = new TextField("Description");

        Button createAuctionButton = new Button("Create Auction", e -> {
            try {
                presenter.createAuction(token,
                        storeNameField.getValue().trim(),
                        productNameField.getValue().trim(),
                        itemPriceField.getValue().trim(),
                        itemDescriptionField.getValue().trim());

                Notification.show("Auction created.");
                statusMessage.setText("");
            } catch (Exception ex) {
                statusMessage.setText("Error: " + ex.getMessage());
            }
        });

        TextField counterPriceField = new TextField("Counter Price");

        Button refreshOffersButton = new Button("Refresh Offers", e -> renderOffers());

        Button acceptButton = new Button("Accept", e -> {
            statusMessage.setText(presenter.respondToOffer(token, "accept", null));
            renderOffers();
        });

        Button declineButton = new Button("Decline", e -> {
            statusMessage.setText(presenter.respondToOffer(token, "decline", null));
            renderOffers();
        });

        Button counterButton = new Button("Counter", e -> {
            statusMessage.setText(presenter.respondToOffer(token, "counter", counterPriceField.getValue()));
            renderOffers();
        });

        add(
                new H1("Create New Auction"),
                new HorizontalLayout(storeNameField, productNameField),
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
