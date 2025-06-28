/* ────────────────────────────────────────────────────────────────
   src/main/java/UILayer/RegisteredUserHomePageUI.java
   ──────────────────────────────────────────────────────────────── */
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
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Logged-in catalogue with greeting, filter and “Add to cart”. */
@Route("/registeredhomepage")
public class RegisteredUserHomePageUI extends VerticalLayout {

    private final UserService       userService;
    private final StoreRepository   storeRepo;
    private final ProductRepository productRepo;

    private Grid<GuestHomePageUI.ProductRow>           grid;
    private ListDataProvider<GuestHomePageUI.ProductRow> dataProvider;

    /* ────────────────────────────────────────────────────────── */
    /* ────────────────────────────────────────────────────────── */
    @Autowired
    public RegisteredUserHomePageUI(UserService          userService,
                                    RegisteredService    registeredService,
                                    OwnerManagerService  ownerMgrService,
                                    IToken               tokenService,
                                    UserRepository       userRepo,
                                    StoreRepository      storeRepo,
                                    ProductRepository    productRepo) {

        this.userService = userService;
        this.storeRepo   = storeRepo;
        this.productRepo = productRepo;

        /* ---- access guard: redirect guests ---- */
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);
        String username;
        try { username = tokenService.extractUsername(token); }
        catch (Exception e) { username = null; }
        if (username == null || username.startsWith("Guest")) {
            UI.getCurrent().navigate("/guesthomepage");
            return;
        }

        /* ---- header bar ---------------------------------------------------- */
        ButtonPresenter buttons = new ButtonPresenter(registeredService, tokenService);

        HorizontalLayout header = new HorizontalLayout(
                new H4("👋 Hello, " + username),
                buttons.signOutButton(token),
                new Button("Store dashboard", e -> UI.getCurrent().navigate("/userhomepage")),
                new Button("Shopping cart",   e -> UI.getCurrent().navigate("/purchasecartfinal")),
                new Button("Rate my purchases", e -> UI.getCurrent().navigate("/rate")),

                /* ★ NEW buttons ↓↓↓ */
                new Button("Bid board",      e -> UI.getCurrent().navigate("/Bid")),
                new Button("Auctions",       e -> UI.getCurrent().navigate("/auction"))
        );

        /* **Admin console** button only for user “1” */
        if ("1".equals(username)) {
            header.add(new Button("Admin console", e -> UI.getCurrent().navigate("/admin")));
        }
        add(header);

        /* ---- inline filter ------------------------------------------------- */
        TextField filter = new TextField();
        filter.setPlaceholder("Filter by store or product…");
        filter.setClearButtonVisible(true);
        filter.setWidth("300px");
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));
        add(filter);

        /* ---- product grid -------------------------------------------------- */
        grid         = buildGrid();
        dataProvider = new ListDataProvider<>(loadRows());
        grid.setItems(dataProvider);
        add(grid);

        setPadding(true);
        setSpacing(true);
    }


    /* ────────────────────────────────────────────────────────── */
    private void applyFilter(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        dataProvider.setFilter(r ->
                r.storeName().toLowerCase().contains(q) ||
                        r.productName().toLowerCase().contains(q));
    }

    /* build list including ratings */
    /* build list including ratings */
    private List<GuestHomePageUI.ProductRow> loadRows() {

        List<GuestHomePageUI.ProductRow> rows = new ArrayList<>();

        for (Store s : storeRepo.getAll()) {

            /* ★ NEW – hide closed stores from regular shoppers */
            if (!s.isOpenNow()) {
                continue;               // don’t list this store
            }

            String  storeId     = s.getId();
            String  storeName   = s.getName();
            double  storeRating = s.getRating();

            for (Map.Entry<String,Integer> e : s.getProducts().entrySet()) {
                String productId = e.getKey();
                int    qty       = e.getValue();

                productRepo.findById(productId).ifPresent(p ->
                        rows.add(new GuestHomePageUI.ProductRow(
                                storeId,  storeName,
                                productId,p.getName(),
                                qty,      p.getPrice(),
                                storeRating, p.getRating())));
            }
        }
        return rows;
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

    /* grid definition */
    private Grid<GuestHomePageUI.ProductRow> buildGrid() {
        Grid<GuestHomePageUI.ProductRow> g = new Grid<>();
        g.addColumn(GuestHomePageUI.ProductRow::storeName)    .setHeader("Store").setAutoWidth(true);
        g.addColumn(GuestHomePageUI.ProductRow::productName)  .setHeader("Product");
        g.addColumn(GuestHomePageUI.ProductRow::quantity)     .setHeader("Quantity");
        g.addColumn(GuestHomePageUI.ProductRow::price)        .setHeader("Price");
        g.addColumn(row -> ratingLabel(row.storeRating()))
                .setHeader("Store ★").setAutoWidth(true);

        g.addColumn(row -> ratingLabel(row.productRating()))
                .setHeader("Product ★").setAutoWidth(true);

        g.addComponentColumn(row -> {
            Button add = new Button("Add to cart", e -> {
                String tkn = (String) UI.getCurrent().getSession().getAttribute("token");
                String msg = userService.addToCart(tkn, row.storeId(), row.productId(), 1);
                Notification.show(msg);
            });
            return add;
        }).setHeader(new Span());

        g.setAllRowsVisible(true);
        return g;
    }


    private static String ratingLabel(double rating) {
        return (rating <= 0) ? "Unrated" : String.format("%.1f", rating);
    }
}
