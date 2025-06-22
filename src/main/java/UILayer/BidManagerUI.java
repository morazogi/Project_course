package UILayer;

import DomainLayer.IToken;
import PresentorLayer.BidManagerPresenter;
import ServiceLayer.BidService;
import ServiceLayer.UserService;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.RegisteredService;
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

@Route("/bidmanager")
public class BidManagerUI extends VerticalLayout {

    private final BidManagerPresenter bidManagerPresenter;
    private final List<Span> offerLines = new ArrayList<>();
    private final ButtonPresenter buttonPresenter;
    private final BidManagerPresenter presenter;
    private final Span error = new Span();

    @Autowired
    public BidManagerUI(IToken tokenService,
                        BidService bidService,
                        UserService userService) {
    public BidManagerUI(IToken tokenService, RegisteredService registeredService) { //BidService bidService,
        this.bidManagerPresenter = new BidManagerPresenter(); //bidService,
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        String manager = token!=null ? tokenService.extractUsername(token):"unknown";

        presenter = new BidManagerPresenter(manager, bidService, userService);

        TextField store  = new TextField("Store Name");
        TextField prod   = new TextField("Product Name");
        TextField start  = new TextField("Starting Price");
        TextField inc    = new TextField("Minimum Increase");
        TextField dur    = new TextField("Duration (minutes)");

        Button startBtn  = new Button("Start Bid", e -> {
            try {
                presenter.startBid(token,
                        store.getValue(), prod.getValue(),
                        start.getValue(), inc.getValue(), dur.getValue());
                Notification.show("Bid created.");
                error.setText("");
            } catch(Exception ex){ error.setText(ex.getMessage()); }
        });

        add(new H1("Create Bid"),
                new HorizontalLayout(store, prod),
                new HorizontalLayout(start, inc, dur),
                startBtn, error);

        setPadding(true); setAlignItems(Alignment.CENTER);
    }
}
