package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidUserPresenter;
import ServiceLayer.BidService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.RegisteredService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/Bid")
public class Bid extends VerticalLayout {

    private final BidUserPresenter presenter;
    private final Div disp = new Div();
    private final Span msg = new Span();
    private final ButtonPresenter buttonPresenter;

    @Autowired
    public Bid(IToken tokenService,
               BidService bidService,
               UserService userService,
               RegisteredService registeredService) {

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);

        presenter = new BidUserPresenter(token!=null ? tokenService.extractUsername(token):"guest", token, bidService, userService);
        add(new HorizontalLayout(new H1("Available Bids"), buttonPresenter.homePageButton(token)));

        add(new H1("Active Bids"), disp);  refresh();

        TextField store = new TextField("Store Name");
        TextField prod  = new TextField("Product Name");
        TextField amt   = new TextField("Your Bid");

        Button send = new Button("Place Bid", e -> {
            try {
                double price = Double.parseDouble(amt.getValue());
                msg.setText(presenter.placeBid(store.getValue(), prod.getValue(), price));
                refresh();
            } catch(NumberFormatException ex){ msg.setText("Invalid number"); }
        });

        add(store, prod, amt, send, msg);
        setPadding(true); setAlignItems(Alignment.CENTER);
    }

    private void refresh() {
        disp.removeAll(); disp.add(presenter.getBidsComponent());
    }
}
