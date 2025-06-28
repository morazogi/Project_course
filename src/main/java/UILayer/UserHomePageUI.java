/* ────────────────────────────────────────────────────────────────
   src/main/java/UILayer/UserHomePageUI.java
   ──────────────────────────────────────────────────────────────── */
package UILayer;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.PermissionsPresenter;
import PresentorLayer.ProductPresenter;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/** Store-manager dashboard (registered user). */
@Route("/userhomepage")
public class UserHomePageUI extends VerticalLayout {

    /* ── collaborators ─────────────────────────────────────────── */
    private final IToken                    tokenService;
    private final UserRepository            userRepository;
    private final ProductRepository         productRepo;
    private final UserService               userService;
    private final OwnerManagerService       ownerMgrService;
    private final ButtonPresenter           buttonPresenter;
    private final UserConnectivityPresenter userConn;
    private final PermissionsPresenter      pp;

    /* ── UI elements we need to refresh ────────────────────────── */
    private final ComboBox<String> storeDropdown = new ComboBox<>("Store");
    private final VerticalLayout   storeContent  = new VerticalLayout();

    /* ── session state ─────────────────────────────────────────── */
    private List<Store> myStores = List.of();   // filled in ctor
    private String      username;

    /* ------------------------------------------------------------ */
    @Autowired
    public UserHomePageUI(UserService          userService,
                          OwnerManagerService  ownerMgrService,
                          IToken               tokenService,
                          UserRepository       userRepository,
                          RegisteredService    registeredService,
                          StoreRepository      storeRepository,
                          ProductRepository    productRepository) {

        /* ── save collaborators ─────────────────────────────── */
        this.userService     = userService;
        this.ownerMgrService = ownerMgrService;
        this.tokenService    = tokenService;
        this.userRepository  = userRepository;
        this.productRepo     = productRepository;
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);
        this.userConn        = new UserConnectivityPresenter(
                userService, registeredService, ownerMgrService,
                tokenService, userRepository);
        this.pp              = new PermissionsPresenter(
                ownerMgrService, tokenService, userRepository);

        /* ── security gate ──────────────────────────────────── */
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);
        username = tokenService.extractUsername(token);

        RegisteredUser user;
        try { user = userRepository.getById(username); }
        catch (Exception e) {
            UI.getCurrent().navigate("");
            return;
        }

        /* ── personal stores list (once) ─────────────────────── */
        try { myStores = userConn.getUserStoresName(token); }
        catch (Exception e) {
            Notification.show(e.getMessage());          // fallback: empty list
            myStores = List.of();
        }

        /* ── header ──────────────────────────────────────────── */
        H1 title = new H1("🛍️ Store Manager Dashboard");
        HorizontalLayout header = new HorizontalLayout(
                new H4("👤 Hello, " + user.getUsername()),
                buttonPresenter.signOutButton(token)
        );
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        /* ── quick-access buttons ────────────────────────────── */
        HorizontalLayout quick = new HorizontalLayout(
                new Button("add store",             ev -> UI.getCurrent().navigate("/addstore")),
                new Button("add new product to store",
                        ev -> UI.getCurrent().navigate("/addnewproduct")),
                new Button("Discounts",             ev -> UI.getCurrent().navigate("/discount-management")),
                new Button("Bids manager",          ev -> UI.getCurrent().navigate("/bidmanager")),
                new Button("Auctions manager",      ev -> UI.getCurrent().navigate("/auctionManagerUI")),
                new Button("Roles",                 ev -> UI.getCurrent().navigate("/roles"))
        );

        /* ── search / misc buttons ───────────────────────────── */
        HorizontalLayout searches = new HorizontalLayout(
                new Button("Search store",   ev -> UI.getCurrent().navigate("/searchstore")),
                new Button("Search product", ev -> UI.getCurrent().navigate("/searchproduct")),
                new Button("Edit store",     ev -> UI.getCurrent().navigate("/edit-store")),
                new Button("Shopping cart",  ev -> UI.getCurrent().navigate("/shoppingcart"))
        );

        /* ── assemble static part of the page ────────────────── */
        add(header, new Hr(), title, quick, searches, storeContent);

        /* ── per-store sections (dynamic) ────────────────────── */
        buildStoreSections(token, user);

        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
    }

    /* ────────────────────────────────────────────────────────── */
    /** Build a collapsible section for every store the user owns. */
    private void buildStoreSections(String token, RegisteredUser user) {

        LinkedList<Store> stores;
        try { stores = userConn.getUserStoresName(token); }
        catch (Exception e) {
            add(new Span(e.getMessage() + "\npermissions:"));
            return;
        }

        for (Store store : stores) {

            /* container that holds everything for this store */
            VerticalLayout storeLayout = new VerticalLayout();
            storeLayout.setPadding(false);
            storeLayout.setSpacing(false);

            /* ──────────── first row: store name only ─────────── */
            Span storeNameLbl = new Span(store.getName());
            HorizontalLayout nameRow = new HorizontalLayout(storeNameLbl);
            nameRow.setWidthFull();
            nameRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            nameRow.setAlignItems(Alignment.BASELINE);
            storeLayout.add(nameRow);

            /* ────────── second row: action buttons ───────────── */
            Button viewBtn = new Button("📦 View products");
            HorizontalLayout actionRow = new HorizontalLayout(viewBtn);
            actionRow.setWidthFull();
            actionRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            actionRow.setAlignItems(Alignment.BASELINE);

            /* product list container (hidden by default) */
            VerticalLayout productList = new VerticalLayout();
            productList.setPadding(false);
            productList.setSpacing(false);
            productList.setVisible(false);

            /* ── product toggle logic ─────────────────────────── */
            viewBtn.addClickListener(ev -> {
                if (!productList.isVisible()) {
                    productList.removeAll();
                    for (Map.Entry<String,Integer> e : store.getProducts().entrySet()) {
                        String productId = e.getKey();
                        int    qty       = e.getValue();
                        productRepo.findById(productId).ifPresent(p -> {
                            String line = String.format(
                                    "%s | $%.2f | %s | qty %d",
                                    p.getName(), p.getPrice(),
                                    p.getDescription()==null? "no description": p.getDescription(),
                                    qty);
                            productList.add(new Span(line));
                        });
                    }
                    productList.setVisible(true);
                    viewBtn.setText("📦 Hide products");
                } else {
                    productList.setVisible(false);
                    viewBtn.setText("📦 View products");
                }
            });

            /* ── permissions-driven manage-inventory section ─── */
            Map<String, Boolean> rawPerms = pp.getPremissions(
                    user.getUsername(), store.getId(), user.getUsername());

            if (rawPerms != null) {

                boolean canManageInventory =
                        Boolean.TRUE.equals(rawPerms.get("PERM_MANAGE_INVENTORY"));

                /* force-disable MANAGE_STAFF, VIEW_STORE, UPDATE_POLICY */
                boolean[] permsArray = {
                        canManageInventory,
                        false,                // MANAGE_STAFF  ❌
                        false,                // VIEW_STORE    ❌
                        false,                // UPDATE_POLICY ❌
                        Boolean.TRUE.equals(rawPerms.get("PERM_ADD_PRODUCT")),
                        Boolean.TRUE.equals(rawPerms.get("PERM_REMOVE_PRODUCT")),
                        Boolean.TRUE.equals(rawPerms.get("PERM_UPDATE_PRODUCT")),
                        Boolean.TRUE.equals(rawPerms.get("PERM_OPEN_STORE")),
                        Boolean.TRUE.equals(rawPerms.get("PERM_CLOSE_STORE")),
                };

                ManagerPermissions perms =
                        new ManagerPermissions(permsArray,
                                user.getUsername(), store.getId());

                /* build the permissions UI once, hidden by default */
                VerticalLayout manageInvContainer = new VerticalLayout();
                manageInvContainer.setPadding(false);
                manageInvContainer.setSpacing(false);
                manageInvContainer.setVisible(false);

                PermissionButtonsUI permUI = new PermissionButtonsUI(
                        new ProductPresenter(userService, tokenService, userRepository),
                        userConn, token, store, perms, ownerMgrService);

                manageInvContainer.add(permUI);

                /* toggle button (only if allowed) */
                if (canManageInventory) {
                    Button invBtn = new Button("🛠️ Show inventory tools");
                    invBtn.addClickListener(e -> {
                        boolean nowVisible = manageInvContainer.isVisible();
                        manageInvContainer.setVisible(!nowVisible);
                        invBtn.setText(!nowVisible
                                ? "🛠️ Hide inventory tools"
                                : "🛠️ Show inventory tools");
                    });
                    actionRow.add(invBtn);
                }

                storeLayout.add(actionRow, productList, manageInvContainer);
            } else {
                /* no permissions info → just add basic rows */
                storeLayout.add(actionRow, productList);
            }

            /* finally push this store’s section to the page */
            add(storeLayout);
        }
    }

    /* ────────────────────────────────────────────────────────── */
    /** Open (or switch) the live WebSocket channel for server pushes. */
    public void connectToWebSocket(String token) {
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
