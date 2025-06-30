package UILayer;

import DomainLayer.IToken;
import DomainLayer.ShoppingBag;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.UserRepository;
import PresentorLayer.UserConnectivityPresenter;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.*;
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

/**
 * UI for completing the purchase of all items currently in the cart.
 * <p>
 * **Input validation (unchanged logic)**
 * <ul>
 *     <li>ID – digits only.</li>
 *     <li>Card number – digits only.</li>
 *     <li>CVV – exactly 3 or 4 digits.</li>
 * </ul>
 * Client-side prevention now uses {@code setAllowedCharPattern} instead of
 * {@code setPreventInvalidInput} to avoid compilation issues on older Vaadin
 * versions. Server-side checks are still present and unchanged.
 */
@Route("/purchasecartfinal")
public class PurchaseCartUI2 extends VerticalLayout {

    private final UserConnectivityPresenter userConn;
    private final IToken tokenService;
    private final ProductRepository productRepo;
    private final ButtonPresenter btns;
    private String token;

    private final VerticalLayout productList = new VerticalLayout();
    private final Span priceSpan = new Span();
    private final Span error = new Span();

    @Autowired
    public PurchaseCartUI2(UserService userService,
                           RegisteredService registeredService,
                           OwnerManagerService ownerMgrService,
                           IToken tokenService,
                           UserRepository userRepo,
                           ProductRepository productRepo) {

        this.userConn     = new UserConnectivityPresenter(userService, registeredService, ownerMgrService, tokenService, userRepo);
        this.tokenService = tokenService;
        this.productRepo  = productRepo;
        this.token        = ensureGuestToken();
        this.btns         = new ButtonPresenter(registeredService, tokenService);
        connectToWebSocket(token);

        /* ──────────────────── input fields ──────────────────── */
        TextField name  = new TextField("name");
        TextField card  = new TextField("card number");
        TextField exp   = new TextField("expiration date");
        TextField cvv   = new TextField("cvv");
        TextField state = new TextField("state");
        TextField city  = new TextField("city");
        TextField addr  = new TextField("address");
        TextField id    = new TextField("id");
        TextField zip   = new TextField("zip");

        /*
         * Client-side restriction: only digits are allowed in numeric fields.
         * We use setAllowedCharPattern instead of setPreventInvalidInput to
         * support older Vaadin versions where the latter is absent.
         */
        card.setAllowedCharPattern("\\d");
        id.setAllowedCharPattern("\\d");
        cvv.setAllowedCharPattern("\\d");
        cvv.setMaxLength(4); // Still allow 3 or 4 digits – enforced server-side

        /* ─────────────────── buttons ─────────────────── */
        Button refresh = new Button("refresh cart", e -> refreshCart());

        Button purchase = new Button("purchase cart", e -> {
            error.setText(""); // clear previous error

            String idVal   = id.getValue().trim();
            String cardVal = card.getValue().trim();
            String cvvVal  = cvv.getValue().trim();

            /* ─────────────── server-side validation ─────────────── */
            if (!idVal.matches("\\d+")) {
                error.setText("ID must contain digits only");
                return;
            }
            if (!cardVal.matches("\\d+")) {
                error.setText("Card number must contain digits only");
                return;
            }
            if (!cvvVal.matches("\\d{3,4}")) {
                error.setText("CVV must be 3 or 4 digits");
                return;
            }

            try {
                userConn.purchaseCart(token,
                        name.getValue(),  cardVal,
                        exp.getValue(),   cvvVal,
                        state.getValue(), city.getValue(),
                        addr.getValue(),  idVal,
                        zip.getValue());

                Notification.show("✅ Purchase completed!");
                refreshCart();
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });

        /* ──────────────────── layout ──────────────────── */
        add(new HorizontalLayout(
                        new H1("your shopping cart"),
                        btns.homePageButton(token)),
                refresh,
                productList,
                priceSpan,
                new HorizontalLayout(name, card, exp, cvv, id),
                new HorizontalLayout(state, city, addr, zip),
                purchase,
                error);

        setPadding(true);
        setAlignItems(Alignment.CENTER);

        refreshCart();
    }

    /* ───────────────────────── cart helpers ───────────────────────── */
    private void refreshCart() {
        productList.removeAll();
        priceSpan.setText("");
        error.setText("");

        boolean cartChanged = false;

        for (ShoppingBag bag : userConn.getShoppingBags(token)) {
            String storeId = bag.getStoreId();
            for (Map.Entry<String, Integer> e : bag.getProducts().entrySet()) {
                String productId = e.getKey();
                int qty          = e.getValue();
                String productName;
                try {
                    productName = productRepo.getById(productId).getName();
                } catch (Exception ex) {
                    userConn.removeFromCart(token, storeId, productId, qty);
                    cartChanged = true;
                    continue;
                }

                Span label = new Span(productName + " × " + qty);

                Button rm = new Button("Remove 1", ev -> {
                    try {
                        userConn.removeFromCart(token, storeId, productId, 1);
                        refreshCart();
                    } catch (Exception ex) {
                        error.setText(ex.getMessage());
                    }
                });
                productList.add(new HorizontalLayout(label, rm));
            }
        }
        double price = userConn.calculateCartPrice(token);
        priceSpan.setText("total price after discounts: " + price);

        if (cartChanged) Notification.show("cart changed");
    }

    /* ───────────────────────── setup helpers ───────────────────────── */
    private String ensureGuestToken() {
        return (String) UI.getCurrent().getSession().getAttribute("token");
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