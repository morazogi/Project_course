package UILayer;

import DomainLayer.IToken;
import ServiceLayer.BidService;
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

    private final BidService bidService;
    private final IToken tokenService;
    private String bidId;

    @Autowired
    public BidPaymentUI(BidService bidService, IToken tokenService) {
        this.bidService = bidService; this.tokenService = tokenService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent e){
        bidId = e.getRouteParameters().get("id").orElse("");
        build();
    }

    private void build(){
        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        add(new H1("Pay for Bid"));

        TextField pay  = new TextField("payment method");
        TextField card = new TextField("card number");
        TextField exp  = new TextField("expiration");
        TextField cvv  = new TextField("cvv");
        TextField state= new TextField("state");
        TextField city = new TextField("city");
        TextField st   = new TextField("street");
        TextField home = new TextField("home");

        Span info = new Span();

        Button btn = new Button("Submit payment", e->{
            try{
                bidService.pay(bidId, token, pay.getValue(), card.getValue(),
                        exp.getValue(), cvv.getValue(),
                        state.getValue(), city.getValue(),
                        st.getValue(), home.getValue());
                info.setText("Payment successful!");
            }catch(Exception ex){ info.setText(ex.getMessage()); }
        });

        add(pay, card, exp, cvv, state, city, st, home, btn, info);
        setPadding(true); setAlignItems(Alignment.CENTER);
    }
}
