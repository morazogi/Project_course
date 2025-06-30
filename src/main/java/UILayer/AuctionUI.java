package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AuctionPresenter;
import PresentorLayer.AuctionPresenter.AuctionView;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.AuctionService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/auction")
public class AuctionUI extends VerticalLayout {

    private final AuctionPresenter presenter;
    private final ButtonPresenter  btns;

    private final VerticalLayout board = new VerticalLayout();
    private final Span   msg   = new Span();
    private String selectedAuctionId = null;

    /* ───────────────────────────────────────────────────────────── */
    @Autowired
    public AuctionUI(IToken tokenSvc,
                     AuctionService auctionSvc,
                     UserService userSvc,
                     RegisteredService regSvc) {

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        String user  = token != null ? tokenSvc.extractUsername(token) : "guest";

        this.presenter = new AuctionPresenter(user, token, auctionSvc, userSvc);
        this.btns      = new ButtonPresenter(regSvc, tokenSvc);

        add(new HorizontalLayout(new H1("Live Auctions"),
                        btns.homePageButton(token)),
                board, new Hr());

        /* offer / reply controls – no name fields needed */
        NumberField offer = new NumberField("Your Offer ($)");
        offer.setPlaceholder("e.g. 39.99");

        Button send = new Button("Send Offer", e -> {
            if (selectedAuctionId == null) { msg.setText("Select an auction first"); return; }
            if (offer.getValue() == null)  { msg.setText("Enter a price"); return; }
            msg.setText(presenter.placeOffer(selectedAuctionId, offer.getValue()));
            updateBoard();
        });

        Button accept  = new Button("Accept",  e -> {
            if (selectedAuctionId == null) { msg.setText("Select an auction first"); return; }
            msg.setText(presenter.respondToPending(selectedAuctionId, "accept"));
            updateBoard();
        });
        Button decline = new Button("Decline", e -> {
            if (selectedAuctionId == null) { msg.setText("Select an auction first"); return; }
            msg.setText(presenter.respondToPending(selectedAuctionId, "decline"));
            updateBoard();
        });

        connectToWebSocket(token);

        add(new H1("Make / Counter an Offer"),
                new HorizontalLayout(offer, send, accept, decline),
                msg);

        updateBoard();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    /* ───────────────────────────────────────────────────────────── */
    private void updateBoard() {

        board.removeAll();
        boolean stillExists = false;

        for (AuctionView v : presenter.listAuctions()) {

            Span row = new Span(
                    v.storeName + " – " + v.productName +
                            " | $" + v.price + " (" + v.lastParty + ")" +
                            (v.waitingConsent ? " [awaiting consent]" : ""));

            row.getStyle().set("cursor","pointer");
            row.addClickListener(ev -> {
                selectedAuctionId = v.id;
                msg.setText("Selected → " + v.storeName + " / " + v.productName);
            });

            if (v.payableByUser) {
                Span pay = new Span("  → Pay now");
                pay.getStyle().set("color","blue").set("cursor","pointer");
                pay.addClickListener(ev ->
                        UI.getCurrent().navigate("/auctionpay/" + v.id));
                row.add(pay);
            }
            if (v.id.equals(selectedAuctionId)) stillExists = true;
            board.add(row);
        }

        if (board.getComponentCount() == 0)
            board.add(new Span("No auctions available"));

        if (!stillExists) selectedAuctionId = null;   // clear if gone
    }

    private void connectToWebSocket(String token) {
        UI.getCurrent().getPage().executeJs("""
                window._shopWs?.close();
                window._shopWs = new WebSocket('ws://'+location.host+'/ws?token='+$0);
                window._shopWs.onmessage = ev => {
                  const txt = (()=>{try{return JSON.parse(ev.data).message}catch(e){return ev.data}})();
                  const n = document.createElement('vaadin-notification');
                  n.renderer = r -> r.textContent = txt;
                  n.duration = 5000;
                  n.position = 'top-center';
                  document.body.appendChild(n);
                  n.opened = true;
                };
                """, token);
    }
}
