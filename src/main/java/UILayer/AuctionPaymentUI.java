package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionPresenter;
import ServiceLayer.AuctionService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
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
    private final AuctionPresenter pres;

    /*──────────────────────────────────────────────────────────────────────*/
    @Autowired
    public AuctionPaymentUI(AuctionService aucSvc, IToken tokenSvc, UserService userSvc) {
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        pres = new AuctionPresenter(
                token!=null ? tokenSvc.extractUsername(token):"guest",
                token, aucSvc, userSvc);
    }

    @Override public void beforeEnter(BeforeEnterEvent e){
        RouteParameters p = e.getRouteParameters();
        auctionId = p.get("id").orElse("");
        build();
    }

    private void build(){
        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        TextField name   = new TextField("Card Holder");
        TextField card   = new TextField("Card Number");        card.setPlaceholder("xxxx xxxx xxxx xxxx");
        TextField exp    = new TextField("Expiry (MM/YY)");
        TextField cvv    = new TextField("CVV");

        TextField state  = new TextField("State");
        TextField city   = new TextField("City");
        TextField addr   = new TextField("Street / No.");
        TextField zip    = new TextField("ZIP");
        TextField idNum  = new TextField("Buyer ID");

        Span info = new Span();
        Button pay = new Button("Pay now", e -> {
            try {
                pres.pay(auctionId, token,
                        name.getValue(), card.getValue(), exp.getValue(), cvv.getValue(),
                        state.getValue(), city.getValue(), addr.getValue(), idNum.getValue(), zip.getValue());
                info.setText("Payment successful ✔");
            } catch(Exception ex){ info.setText(ex.getMessage()); }
        });
        connectToWebSocket(token);

        add(new H1("Auction Payment"), new Hr(),
                name, card, exp, cvv,
                state, city, addr, zip, idNum,
                pay, info);

        setPadding(true); setAlignItems(Alignment.CENTER);
    }
    public void connectToWebSocket(String token) {
        UI.getCurrent().getPage().executeJs("""
                window._shopWs?.close();
                window._shopWs = new WebSocket('ws://'+location.host+'/ws?token='+$0);
                window._shopWs.onmessage = ev => {
                  const txt = (()=>{try{return JSON.parse(ev.data).message}catch(e){return ev.data}})();
                  const n = document.createElement('vaadin-notification');
                  n.renderer = r => r.textContent = txt;
                  n.duration = 5000;
                  n.position = 'top-center';
                  document.body.appendChild(n);
                  n.opened = true;
                };
                """, token);
    }
}
