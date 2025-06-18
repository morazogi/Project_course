package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionPresenter;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.RegisteredService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Customer-side screen:
 * – lists all open auctions
 * – lets the user enter an Item-ID and a price to make / counter an offer
 */
@Route("/auction")
public class AuctionUI extends VerticalLayout {

    private final AuctionPresenter presenter;
    private final Div auctionsDisplay = new Div();
    private final Span message = new Span();
    private final IToken tokenService;
    private final ButtonPresenter buttonPresenter;


    public AuctionUI(IToken tokenService, RegisteredService registeredService) {

        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);

        this.tokenService = tokenService;
        String token = "";
        if (tokenService != null) {
            token = (String) UI.getCurrent().getSession().getAttribute("token");
        }
        String username = "";
        if(token != null) {
           username = tokenService.extractUsername(token);
        }

        this.presenter = new AuctionPresenter(username);

        // ── heading + current auctions ──────────────────────────────
        add(new HorizontalLayout(new H1("Open Auctions"), buttonPresenter.homePageButton(token)));
        add(auctionsDisplay);
        updateAuctionsDisplay();

        // ── offer input fields ─────────────────────────────────────
        TextField itemIdField   = new TextField("Item ID");
        TextField offerPriceField = new TextField("Your Offer");

        Button makeOfferButton = new Button("Send Offer", e -> {
            try {
                String itemId = itemIdField.getValue().trim();
                double price  = Double.parseDouble(offerPriceField.getValue().trim());

                String result = presenter.placeOffer(itemId, price);
                message.setText(result);
                updateAuctionsDisplay();

            } catch (NumberFormatException ex) {
                message.setText("Please enter a valid number for the offer.");
            }
        });

        add(itemIdField, offerPriceField, makeOfferButton, message);

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    /** refreshes the list of auctions on screen */
    private void updateAuctionsDisplay() {
        auctionsDisplay.removeAll();
        auctionsDisplay.add(presenter.getAuctionsComponent());
    }
}
