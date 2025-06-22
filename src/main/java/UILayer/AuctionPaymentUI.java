package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionPresenter;
import ServiceLayer.AuctionService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/auctionpay/:id")
public class AuctionPaymentUI extends VerticalLayout implements BeforeEnterObserver {

    private String auctionId;
    private AuctionPresenter auctionPresenter;

    @Autowired
    public AuctionPaymentUI(AuctionService auctionService, IToken tokenService, UserService userService) {
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        this.auctionPresenter = new AuctionPresenter(token != null ? tokenService.extractUsername(token) : "guest", token, auctionService, userService);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent e) {
        RouteParameters params = e.getRouteParameters();
        auctionId = params.get("id").orElse("");
        build();
    }

    private void build() {
        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        add(new H1("Pay for auction"));

        TextField name           = new TextField("name");
        TextField cardNumber     = new TextField("card number");
        TextField expirationDate = new TextField("expiration date");
        TextField cvv            = new TextField("cvv");
        TextField state          = new TextField("state");
        TextField city           = new TextField("city");
        TextField address        = new TextField("address");
        TextField id             = new TextField("id");
        TextField zip            = new TextField("zip");

        Span msg = new Span();

        Button pay = new Button("Submit payment", ev -> {
            try {
                auctionPresenter.pay(auctionId, token, name.getValue(), cardNumber.getValue(),
                        expirationDate.getValue(), cvv.getValue(),
                        state.getValue(), city.getValue(),
                        address.getValue(), id.getValue(), zip.getValue());
                msg.setText("Payment successful – thank you!");
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });

        add(name, cardNumber, expirationDate, cvv, state, city, address, id, zip, pay, msg);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}
