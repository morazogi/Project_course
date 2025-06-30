package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidUserPresenter;
import ServiceLayer.BidService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/bidpay/:id")
public class BidPaymentUI extends VerticalLayout implements BeforeEnterObserver {

    private String bidId;
    private final IToken  tokenSvc;
    private final BidUserPresenter pres;

    /* ───────────────────────────────────────────────────────── */
    @Autowired
    public BidPaymentUI(BidService bidSvc, IToken tokenSvc, UserService userSvc) {
        this.tokenSvc = tokenSvc;

        String tok = ensureToken();
        pres = new BidUserPresenter(tokenSvc.extractUsername(tok), tok, bidSvc, userSvc);

        connectToWebSocket(tok);
    }

    /* ensure guest token if user is not logged in */
    private String ensureToken() {
        String tok = (String) UI.getCurrent().getSession().getAttribute("token");
        if (tok == null || tok.isBlank()) {
            tok = tokenSvc.generateToken("Guest");
            UI.getCurrent().getSession().setAttribute("token", tok);
        }
        return tok;
    }

    @Override public void beforeEnter(BeforeEnterEvent e) {
        RouteParameters p = e.getRouteParameters();
        bidId = p.get("id").orElse("");
        build();
    }

    /* build UI once per navigation      */
    private void build() {
        String token = (String) UI.getCurrent().getSession().getAttribute("token");

        /* same field set & order as PurchaseCartUI2 */
        TextField name  = new TextField("Card holder");
        TextField card  = new TextField("Card number");
        TextField exp   = new TextField("Expiry (MM/YY)");
        TextField cvv   = new TextField("CVV");
        TextField state = new TextField("State");
        TextField city  = new TextField("City");
        TextField addr  = new TextField("Street / No.");
        TextField idNum = new TextField("Buyer ID");
        TextField zip   = new TextField("ZIP");

        Span info = new Span();

        Button pay = new Button("Pay now", e -> {
            try {
                pres.pay(bidId, token,
                        name.getValue(), card.getValue(), exp.getValue(), cvv.getValue(),
                        state.getValue(), city.getValue(), addr.getValue(),
                        idNum.getValue(), zip.getValue());
                info.setText("Payment complete ✔");
            } catch (Exception ex) {
                info.setText(ex.getMessage());
            }
        });

        add(new H1("Bid Payment"), new Hr(),
                new HorizontalLayout(name, card, exp, cvv, idNum),
                new HorizontalLayout(state, city, addr, zip),
                pay, info);

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    private void connectToWebSocket(String token) {
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
