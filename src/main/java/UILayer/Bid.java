package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidUserPresenter;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.BidService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/Bid")
public class Bid extends VerticalLayout {

    private final BidUserPresenter presenter;
    private final Div board = new Div();
    private final Span msg  = new Span();
    private final ButtonPresenter btns;

    /*──────────────────────────────────────────────────────────────────────*/
    @Autowired
    public Bid(IToken tokenSvc,
               BidService bidSvc,
               UserService userSvc,
               RegisteredService regSvc) {

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        presenter    = new BidUserPresenter(token!=null? tokenSvc.extractUsername(token):"guest",
                token, bidSvc, userSvc);
        btns         = new ButtonPresenter(regSvc, tokenSvc);

        add(new HorizontalLayout(new H1("Active Bids"), btns.homePageButton(token)),
                board, new Hr());

        TextField   store = new TextField("Store Name");
        TextField   prod  = new TextField("Product Name");
        NumberField price = new NumberField("Your Bid ($)");

        Button send = new Button("Place Bid", e -> {
            if(price.getValue()==null){ msg.setText("Enter a number"); return; }
            msg.setText(presenter.placeBid(store.getValue(), prod.getValue(), price.getValue()));
            refresh();
        });

        add(new H1("Submit / Raise a Bid"),
                new HorizontalLayout(store, prod, price, send),
                msg);

        refresh();
        setPadding(true); setAlignItems(Alignment.CENTER);
    }

    private void refresh(){ board.removeAll(); board.add(presenter.getBidsComponent()); }
}
