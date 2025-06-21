package UILayer;

import DomainLayer.IToken;
import ServiceLayer.AuctionService;
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

    private final AuctionService auctionService;
    private final IToken tokenService;
    private String auctionId;

    @Autowired
    public AuctionPaymentUI(AuctionService auctionService, IToken tokenService) {
        this.auctionService = auctionService;
        this.tokenService = tokenService;
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

        TextField payMeth = new TextField("payment method");
        TextField card = new TextField("card number");
        TextField exp = new TextField("expiration");
        TextField cvv = new TextField("cvv");
        TextField state = new TextField("state");
        TextField city = new TextField("city");
        TextField street = new TextField("street");
        TextField home = new TextField("home");

        Span msg = new Span();

        Button pay = new Button("Submit payment", ev -> {
            try {
                auctionService.pay(auctionId, token,
                        payMeth.getValue(), card.getValue(), exp.getValue(), cvv.getValue(),
                        state.getValue(), city.getValue(), street.getValue(), home.getValue());
                msg.setText("Payment successful – thank you!");
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });

        add(payMeth, card, exp, cvv, state, city, street, home, pay, msg);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}
