package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidManagerPresenter;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.BidService;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/bidmanager")
public class BidManagerUI extends VerticalLayout {

    private final BidManagerPresenter presenter;
    private final Span status = new Span();
    private final ButtonPresenter btns;

    @Autowired
    public BidManagerUI(IToken tokenSvc,
                        BidService bidSvc,
                        UserService userSvc,
                        OwnerManagerService ownerMgr,
                        RegisteredService regSvc) {

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        presenter = new BidManagerPresenter(token!=null ? tokenSvc.extractUsername(token):"manager",
                bidSvc, userSvc, ownerMgr);
        btns      = new ButtonPresenter(regSvc, tokenSvc);

        /* form */
        TextField  store = new TextField("Store Name");
        TextField  prod  = new TextField("Product Name");
        NumberField start= new NumberField("Start Price ($)");
        NumberField step = new NumberField("Min Increase ($)");
        IntegerField mins= new IntegerField("Duration (minutes)");
        mins.setMin(1);

        Button create = new Button("Launch Bid", e -> {
            try {
                presenter.startBid(token,
                        store.getValue(), prod.getValue(),
                        toStr(start), toStr(step), mins.getValue()==null?"":mins.getValue().toString());
                Notification.show("Bid launched 🚀");
                clear(store, prod, start, step, mins); status.setText("");
            } catch(Exception ex){ status.setText(ex.getMessage()); }
        });
        connectToWebSocket(token);

        add(new HorizontalLayout(new H1("Bid Manager"), btns.homePageButton(token)),
                new HorizontalLayout(store, prod),
                new HorizontalLayout(start, step, mins),
                create, status);

        setPadding(true); setAlignItems(Alignment.CENTER);
    }

    private static String toStr(NumberField f){ return f.getValue()==null? "": f.getValue().toString(); }
    private void clear(TextField a,TextField b,NumberField c,NumberField d,IntegerField e){
        a.clear(); b.clear(); c.clear(); d.clear(); e.clear();
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
