package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidUserPresenter;
import ServiceLayer.BidService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

import org.springframework.beans.factory.annotation.Autowired;

@Route("/bidpay/:id")
public class BidPaymentUI extends VerticalLayout implements BeforeEnterObserver {

    private String bidId;
    private final BidUserPresenter bidUserPresenter;

    @Autowired
    public BidPaymentUI(BidService bidService, IToken tokenService, UserService userService) {
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        this.bidUserPresenter = new BidUserPresenter(token!=null ? tokenService.extractUsername(token):"unknown", token, bidService, userService);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent e){
        bidId = e.getRouteParameters().get("id").orElse("");
        build();
    }

    private void build(){
        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        add(new H1("Pay for Bid"));

        TextField name           = new TextField("name");
        TextField cardNumber     = new TextField("card number");
        TextField expirationDate = new TextField("expiration date");
        TextField cvv            = new TextField("cvv");
        TextField state          = new TextField("state");
        TextField city           = new TextField("city");
        TextField address        = new TextField("address");
        TextField id             = new TextField("id");
        TextField zip            = new TextField("zip");

        Span info = new Span();

        Button btn = new Button("Submit payment", e->{
            try{
                bidUserPresenter.pay(bidId, token, name.getValue(), cardNumber.getValue(),
                        expirationDate.getValue(), cvv.getValue(),
                        state.getValue(), city.getValue(),
                        address.getValue(), id.getValue(), zip.getValue());
                info.setText("Payment successful!");
            }catch(Exception ex){ info.setText(ex.getMessage()); }
        });

        add(name, cardNumber, expirationDate, cvv, state, city, address, zip, id, btn, info);
        setPadding(true); setAlignItems(Alignment.CENTER);
    }
}
