package UILayer;

import DomainLayer.IToken;
import DomainLayer.Product;
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

    private final UserService          userService;
    private final StoreRepository      storeRepo;
    private final ProductRepository    productRepo;
    private Grid<GuestHomePageUI.ProductRow> grid;
    private ListDataProvider<GuestHomePageUI.ProductRow> dataProvider;

    @Autowired
    public RegisteredUserHomePageUI(UserService userService,
                                    RegisteredService registeredService,
                                    OwnerManagerService ownerMgrService,
                                    IToken tokenService,
                                    UserRepository userRepo,
                                    StoreRepository storeRepo,
                                    ProductRepository productRepo) {

        this.userService = userService;
        this.storeRepo   = storeRepo;
        this.productRepo = productRepo;

        /* ----- guard ----- */
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        String username;
        try { username = tokenService.extractUsername(token); }
        catch (Exception e) { username = null; }
        if (username == null || username.startsWith("Guest")) {
            UI.getCurrent().navigate("/guesthomepage");
            return;
        }

        ButtonPresenter buttons = new ButtonPresenter(registeredService, tokenService);

        /* header */
        add(new HorizontalLayout(
                new H4("👋 Hello, " + username),
                buttons.signOutButton(token),
                new Button("Store dashboard", e -> UI.getCurrent().navigate("/userhomepage")),
                new Button("Shopping cart",  e -> UI.getCurrent().navigate("/purchasecartfinal"))
        ));

        /* filter */
        TextField filter = new TextField();
        filter.setPlaceholder("Filter by store or product…");
        filter.setClearButtonVisible(true);
        filter.setWidth("300px");
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));
        add(filter);

        /* grid */
        grid         = buildGrid();
        dataProvider = new ListDataProvider<>(loadRows());
        grid.setItems(dataProvider);
        add(grid);

        setPadding(true);
        setSpacing(true);
    }

    /* ---------- helpers ---------- */
    private void applyFilter(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        dataProvider.setFilter(r ->
                r.storeName().toLowerCase().contains(q) ||
                        r.productName().toLowerCase().contains(q));
    }

    private List<GuestHomePageUI.ProductRow> loadRows() {
        List<GuestHomePageUI.ProductRow> rows = new ArrayList<>();
        for (Store s : storeRepo.getAll()) {
            String storeId   = s.getId();
            String storeName = s.getName();
            for (Map.Entry<String,Integer> e : s.getProducts().entrySet()) {
                String productId = e.getKey();
                int qty          = e.getValue();
                productRepo.findById(productId).ifPresent(p ->
                        rows.add(new GuestHomePageUI.ProductRow(
                                storeId, storeName,
                                productId, p.getName(),
                                qty, p.getPrice())));
            }
        }
        return rows;
    }

    private Grid<GuestHomePageUI.ProductRow> buildGrid() {
        Grid<GuestHomePageUI.ProductRow> g = new Grid<>();
        g.addColumn(GuestHomePageUI.ProductRow::storeName).setHeader("Store").setAutoWidth(true);
        g.addColumn(GuestHomePageUI.ProductRow::productName).setHeader("Product");
        g.addColumn(GuestHomePageUI.ProductRow::quantity)   .setHeader("Qty");
        g.addColumn(GuestHomePageUI.ProductRow::price)      .setHeader("Price");
        g.addComponentColumn(row -> {
            Button btn = new Button("Add to cart", e -> {
                String token = (String) UI.getCurrent().getSession().getAttribute("token");
                String msg   = userService.addToCart(token,
                        row.storeId(),
                        row.productId(),
                        1);
                Notification.show(msg);
            });
            return btn;
        }).setHeader(new Span());
        g.setAllRowsVisible(true);
        return g;
    }
}
