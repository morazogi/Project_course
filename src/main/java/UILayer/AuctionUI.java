package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionPresenter;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.AuctionService;
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

@Route("/auction")
public class AuctionUI extends VerticalLayout {

    private final AuctionPresenter presenter;
    private final ButtonPresenter  btns;
    private final Div board = new Div();
    private final Span msg  = new Span();

    /*──────────────────────────────────────────────────────────────────────*/

    @Autowired
    public AuctionUI(IToken tokenSvc,
                     AuctionService auctionSvc,
                     UserService userSvc,
                     RegisteredService regSvc) {

        String token   = (String) UI.getCurrent().getSession().getAttribute("token");
        String user    = token != null ? tokenSvc.extractUsername(token) : "guest";

        this.presenter = new AuctionPresenter(user, token, auctionSvc, userSvc);
        this.btns      = new ButtonPresenter(regSvc, tokenSvc);

        /* –– live board –– */
        add(new HorizontalLayout(new H1("Live Auctions"), btns.homePageButton(token)),
                board, new Hr());

        /* –– offer form –– */
        TextField   store  = new TextField("Store Name");
        TextField   prod   = new TextField("Product Name");
        NumberField offer  = new NumberField("Your Offer ($)");
        offer.setPlaceholder("e.g. 39.99");

        Button send = new Button("Send Offer", e -> {
            if (offer.getValue() == null){ msg.setText("Enter a price"); return; }
            msg.setText(presenter.placeOffer(store.getValue(), prod.getValue(),
                    offer.getValue()));
            updateBoard();
        });

        add(new H1("Make / Counter an Offer"),
                new HorizontalLayout(store, prod, offer, send),
                msg);
        updateBoard();

        setPadding(true); setAlignItems(Alignment.CENTER);
    }

    /*──────────────────────────────────────────────────────────────────────*/
    private void updateBoard(){
        board.removeAll();
        board.add(presenter.getAuctionsComponent());
    }
}
