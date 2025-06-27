package UILayer;

import DomainLayer.IToken;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import ServiceLayer.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/** Guest catalogue – now auto-creates a guest token and fully supports add-to-cart. */
@Route("/guesthomepage")
public class GuestHomePageUI extends VerticalLayout {

    private final UserService userService;
    private final IToken      tokenService;
    private final String      token;          // guest token we just created/loaded

    /* grid data */
    private Grid<ProductRow>             grid;
    private ListDataProvider<ProductRow> dataProvider;

    @Autowired
    public GuestHomePageUI(UserService userService,
                           RegisteredService registeredService,
                           OwnerManagerService ownerMgrService,
                           IToken tokenService,
                           UserRepository userRepo,
                           StoreRepository storeRepo,
                           ProductRepository productRepo,
                           NotificationService notificationService) {

        this.userService  = userService;
        this.tokenService = tokenService;
        this.token        = ensureGuestToken();

        ButtonPresenter buttons = new ButtonPresenter(registeredService, tokenService);
        connectToWebSocket(token);

        /* auth + cart buttons */
        add(new HorizontalLayout(
                buttons.loginButton(),
                new Button("Sign up", e -> UI.getCurrent().navigate("/signup")),
                new Button("Shopping cart", e -> UI.getCurrent().navigate("/purchasecartfinal"))
        ));

        /* inline filter */
        TextField filter = new TextField();
        filter.setPlaceholder("Filter by store or product…");
        filter.setClearButtonVisible(true);
        filter.setWidth("300px");
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));
        add(filter);

        /* grid */
        grid         = buildGrid();
        dataProvider = new ListDataProvider<>(loadRows(storeRepo, productRepo));
        grid.setItems(dataProvider);
        add(grid);

        setPadding(true);
        setSpacing(true);
    }

    /* ---------- helper: guarantee non-null token ---------- */
    private String ensureGuestToken() {
        UI.getCurrent().getSession().setAttribute("token",
                tokenService.generateToken("Guest"));
        return (String) UI.getCurrent().getSession().getAttribute("token");
    }

    /* ---------- filter ---------- */
    private void applyFilter(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        dataProvider.setFilter(r ->
                r.storeName().toLowerCase().contains(q) ||
                        r.productName().toLowerCase().contains(q));
    }

    /* ---------- load catalogue ---------- */
    private List<ProductRow> loadRows(StoreRepository storeRepo,
                                      ProductRepository productRepo) {
        List<ProductRow> rows = new ArrayList<>();
        for (Store s : storeRepo.getAll()) {
            String storeId     = s.getId();
            String storeName   = s.getName();
            double storeRating = s.getRating();
            for (Map.Entry<String,Integer> e : s.getProducts().entrySet()) {
                String productId = e.getKey();
                int    qty       = e.getValue();
                productRepo.findById(productId).ifPresent(p ->
                        rows.add(new ProductRow(storeId, storeName,
                                productId, p.getName(),
                                qty, p.getPrice(),
                                storeRating, p.getRating())));
            }
        }
        return rows;
    }

    /* ---------- grid ---------- */
    private Grid<ProductRow> buildGrid() {
        Grid<ProductRow> g = new Grid<>();
        g.addColumn(ProductRow::storeName)     .setHeader("Store").setAutoWidth(true);
        g.addColumn(ProductRow::productName)   .setHeader("Product");
        g.addColumn(ProductRow::quantity)      .setHeader("Qty");
        g.addColumn(ProductRow::price)         .setHeader("Price");
        g.addColumn(ProductRow::storeRating)   .setHeader("Store ★").setAutoWidth(true);
        g.addColumn(ProductRow::productRating) .setHeader("Product ★").setAutoWidth(true);
        g.addComponentColumn(row -> {
            Button add = new Button("Add to cart", e -> {
                String msg = userService.addToCart(token,
                        row.storeId(),
                        row.productId(),
                        1);
                Notification.show(msg);
            });
            return add;
        }).setHeader(new Span());
        g.setAllRowsVisible(true);
        return g;
    }

        /* DTO – now includes ratings */
        public record ProductRow(String storeId,   String storeName,
                                 String productId, String productName,
                                 int quantity,     float  price,
                                 double storeRating, double productRating) {}

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
