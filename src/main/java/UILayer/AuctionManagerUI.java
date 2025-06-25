package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionManagerPresenter;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.Offer;
import ServiceLayer.AuctionService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/auctionManagerUI")
public class AuctionManagerUI extends VerticalLayout {

    private final AuctionManagerPresenter presenter;
    private final ButtonPresenter         buttonPresenter;

    /* UI state */
    private final Span            statusMessage     = new Span();
    private final VerticalLayout  offerDisplayArea  = new VerticalLayout();

    /* ------------------------------------------------------------ */

    @Autowired
    public AuctionManagerUI(IToken tokenSvc,
                            AuctionService auctionSvc,
                            UserService userSvc,
                            RegisteredService regSvc) {

        String token    = (String) UI.getCurrent().getSession().getAttribute("token");
        String manager  = token != null ? tokenSvc.extractUsername(token)
                : "unknown";

        this.presenter       = new AuctionManagerPresenter(manager, token, auctionSvc, userSvc);
        this.buttonPresenter = new ButtonPresenter(regSvc, tokenSvc);

        /* ── Create-auction form ───────────────────────────────── */
        TextField storeField   = new TextField("Store name");
        TextField productField = new TextField("Product name");
        TextField priceField   = new TextField("Starting price");
        TextField descField    = new TextField("Description");

        Button createBtn = new Button("Create auction", e -> {
            try {
                presenter.createAuction(token,
                        storeField.getValue(), productField.getValue(),
                        priceField.getValue(), descField.getValue());
                Notification.show("Auction created.");
                statusMessage.setText("");
            } catch (Exception ex) {
                statusMessage.setText("Error: " + ex.getMessage());
            }
        });

        /* ── Customer-offer controls ───────────────────────────── */
        TextField counterField = new TextField("Counter-offer ($)");
        Button refreshBtn = new Button("Refresh", ev -> renderOffers());

        Button acceptBtn  = new Button("Accept",  ev -> {
            statusMessage.setText(presenter.respondToOffer(token, "accept",  null));
            renderOffers();
        });
        Button declineBtn = new Button("Decline", ev -> {
            statusMessage.setText(presenter.respondToOffer(token, "decline", null));
            renderOffers();
        });
        Button counterBtn = new Button("Counter", ev -> {
            statusMessage.setText(presenter.respondToOffer(token, "counter",
                    counterField.getValue()));
            renderOffers();
        });

        /* ── Layout ────────────────────────────────────────────── */
        add(
                new HorizontalLayout(
                        new H1("Auction Manager"),
                        buttonPresenter.homePageButton(token)),

                /* create auction section */
                new H1("Create new auction"),
                new HorizontalLayout(storeField, productField),
                new HorizontalLayout(priceField, descField),
                createBtn, statusMessage,

                /* offers section */
                new H1("Customer offers"),
                refreshBtn,
                offerDisplayArea,
                counterField,
                new HorizontalLayout(acceptBtn, declineBtn, counterBtn)
        );
        connectToWebSocket(token);
        renderOffers();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    /* ------------------------------------------------------------------ */
    /** Rebuild the offers list with readable “store / product” strings. */
    private void renderOffers() {
        offerDisplayArea.removeAll();
        var offers = presenter.getOffers();
        if (offers.isEmpty()) {
            offerDisplayArea.add(new Span("No offers yet."));
            return;
        }
        for (Offer o : offers)
            offerDisplayArea.add(new Span(o.toString()));
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
