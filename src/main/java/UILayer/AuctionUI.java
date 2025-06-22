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
import org.springframework.beans.factory.annotation.Autowired;

@Route("/auction")
public class AuctionUI extends VerticalLayout {

    private final AuctionPresenter presenter;
    private final Div auctionsDisplay = new Div();
    private final Span message = new Span();

    @Autowired
    public AuctionUI(IToken tokenService,
                     AuctionService auctionService,
                     UserService userService) {
    public AuctionUI(IToken tokenService, RegisteredService registeredService) {

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        String username = token != null ? tokenService.extractUsername(token) : "guest";
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);

        this.presenter = new AuctionPresenter(username, token, auctionService, userService);

        add(new H1("Open Auctions"));
        this.presenter = new AuctionPresenter(username);

        // ── heading + current auctions ──────────────────────────────
        add(new HorizontalLayout(new H1("Open Auctions"), buttonPresenter.homePageButton(token)));
        add(auctionsDisplay);
        updateAuctionsDisplay();

        TextField storeField   = new TextField("Store Name");
        TextField productField = new TextField("Product Name");
        TextField offerField   = new TextField("Your Offer");

        Button send = new Button("Send Offer", e -> {
            try {
                double price = Double.parseDouble(offerField.getValue().trim());
                String r = presenter.placeOffer(storeField.getValue().trim(),
                        productField.getValue().trim(),
                        price);
                message.setText(r);
                updateAuctionsDisplay();
            } catch (NumberFormatException ex) {
                message.setText("Enter a valid number for the offer.");
            }
        });

        add(storeField, productField, offerField, send, message);

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    private void updateAuctionsDisplay() {
        auctionsDisplay.removeAll();
        auctionsDisplay.add(presenter.getAuctionsComponent());
    }
}
