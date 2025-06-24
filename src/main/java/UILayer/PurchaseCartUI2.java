package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import PresentorLayer.UserConnectivityPresenter;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

@Route("/purchasecartfinal")
public class PurchaseCartUI2 extends VerticalLayout {

    private final UserConnectivityPresenter userConn;
    private final IToken tokenService;
    private String      token;        // will be (guest)-initialised if missing

    @Autowired
    public PurchaseCartUI2(UserService userService,
                           RegisteredService registeredService,
                           OwnerManagerService ownerMgrService,
                           IToken tokenService,
                           UserRepository userRepo) {

        this.userConn    = new UserConnectivityPresenter(userService, registeredService,
                ownerMgrService, tokenService, userRepo);
        this.tokenService = tokenService;
        this.token        = ensureGuestToken();

        /* -------- payment / shipping fields -------- */
        TextField name   = new TextField("name");
        TextField card   = new TextField("card number");
        TextField exp    = new TextField("expiration date");
        TextField cvv    = new TextField("cvv");
        TextField state  = new TextField("state");
        TextField city   = new TextField("city");
        TextField addr   = new TextField("address");
        TextField id     = new TextField("id");
        TextField zip    = new TextField("zip");

        Span priceSpan   = new Span();
        Span error       = new Span();
        VerticalLayout productList = new VerticalLayout();
        productList.setPadding(false);

        /* -------- buttons -------- */
        Button calc = new Button("calculate price", e -> {
            try {
                productList.removeAll();
                Map<String,Integer> items = userConn.getCartProducts(token);
                items.forEach((p, q) -> productList.add(new Span(p + " × " + q)));
                double price = userConn.calculateCartPrice(token);
                priceSpan.setText("total price after discounts: " + price);
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        Button purchase = new Button("purchase cart", e -> {
            try {
                userConn.purchaseCart(token,
                        name.getValue(), card.getValue(),
                        exp.getValue(), cvv.getValue(),
                        state.getValue(), city.getValue(),
                        addr.getValue(), id.getValue(),
                        zip.getValue());
                Notification.show("✅ Purchase completed!");
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        /* -------- layout -------- */
        add(
                new H1("purchase cart"),
                calc,
                productList,
                priceSpan,
                new HorizontalLayout(name, card, exp, cvv, id),
                new HorizontalLayout(state, city, addr, zip),
                purchase,
                error
        );
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    /* ----- helper: guest token if missing ----- */
    private String ensureGuestToken() {
        String t = (String) UI.getCurrent().getSession().getAttribute("token");
        if (t == null) {
            String guestName = "Guest-" + UUID.randomUUID();
            t = tokenService.generateToken(guestName);
            UI.getCurrent().getSession().setAttribute("token", t);
        }
        return t;
    }
}
