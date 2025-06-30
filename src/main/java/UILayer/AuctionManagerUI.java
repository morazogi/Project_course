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

    /* selections */
    private String selectedOfferId   = null;  // waiting-consent offer
    private String selectedAuctionId = null;  // any auction for remove

    private final AuctionManagerPresenter presenter;
    private final ButtonPresenter         buttonPresenter;

    /* UI state areas */
    private final Span           statusMessage      = new Span();
    private final VerticalLayout offerDisplayArea   = new VerticalLayout();
    private final VerticalLayout auctionsDisplayAll = new VerticalLayout();

    /* ----------------------------------------------------------- */
    @Autowired
    public AuctionManagerUI(IToken tokenSvc,
                            AuctionService auctionSvc,
                            UserService userSvc,
                            RegisteredService regSvc) {

        String token   = (String) UI.getCurrent().getSession().getAttribute("token");
        String manager = token != null ? tokenSvc.extractUsername(token) : "unknown";

        this.presenter       = new AuctionManagerPresenter(manager, token, auctionSvc, userSvc);
        this.buttonPresenter = new ButtonPresenter(regSvc, tokenSvc);

        /* ── Create-auction form (unchanged) ───────────────────── */
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
            } catch (Exception ex) { statusMessage.setText("Error: " + ex.getMessage()); }
        });

        /* ── Controls for waiting-consent offers ───────────────── */
        TextField counterField = new TextField("Counter-offer ($)");
        Button refreshBtn = new Button("Refresh", ev -> renderLists());

        Button acceptBtn  = new Button("Accept",  ev -> {
            statusMessage.setText(
                    presenter.respondToOffer(token, "accept", null, selectedOfferId));
            renderLists();
        });
        Button declineBtn = new Button("Decline", ev -> {
            statusMessage.setText(
                    presenter.respondToOffer(token, "decline", null, selectedOfferId));
            renderLists();
        });
        Button counterBtn = new Button("Counter", ev -> {
            statusMessage.setText(
                    presenter.respondToOffer(token, "counter",
                            counterField.getValue(), selectedOfferId));
            renderLists();
        });

        /* ── Remove-auction button ─────────────────────────────── */
        Button removeBtn = new Button("Remove auction", ev -> {
            statusMessage.setText(presenter.removeAuction(selectedAuctionId));
            selectedAuctionId = null;
            renderLists();
        });

        /* ── Layout ───────────────────────────────────────────── */
        add(
                new HorizontalLayout(new H1("Auction Manager"),
                        buttonPresenter.homePageButton(token)),

                /* create section */
                new H1("Create new auction"),
                new HorizontalLayout(storeField, productField),
                new HorizontalLayout(priceField, descField),
                createBtn, statusMessage,

                /* waiting-consent section */
                new H1("Pending offers (select one)"),
                refreshBtn,
                offerDisplayArea,
                counterField,
                new HorizontalLayout(acceptBtn, declineBtn, counterBtn),

                /* all auctions section */
                new H1("All active auctions (select to remove)"),
                auctionsDisplayAll,
                removeBtn
        );

        connectToWebSocket(token);
        renderLists();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    /* ---------------------------------------------------------------- */
    private void renderLists() {

        /* 1. pending offers -------------------------------------- */
        offerDisplayArea.removeAll();
        for (Offer o : presenter.getOffers()) {
            Span row = new Span(o.toString());
            row.getStyle().set("cursor","pointer");
            row.addClickListener(ev -> {
                selectedOfferId = o.getAuctionId();
                statusMessage.setText("Offer selected ➜ " + o);
            });
            offerDisplayArea.add(row);
        }
        if (offerDisplayArea.getComponentCount()==0)
            offerDisplayArea.add(new Span("No pending offers."));

        /* 2. all auctions ---------------------------------------- */
        auctionsDisplayAll.removeAll();
        for (Offer o : presenter.listAllAuctions()) {
            Span row = new Span(o.toString());
            row.getStyle().set("cursor","pointer");
            row.addClickListener(ev -> {
                selectedAuctionId = o.getAuctionId();
                statusMessage.setText("Auction selected ➜ " + o);
            });
            auctionsDisplayAll.add(row);
        }
        if (auctionsDisplayAll.getComponentCount()==0)
            auctionsDisplayAll.add(new Span("No active auctions."));
    }

    /* ---------------------------------------------------------------- */
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
