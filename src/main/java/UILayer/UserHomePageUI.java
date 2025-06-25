package UILayer;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/** Store-manager dashboard (registered user). */
@Route("/userhomepage")
public class UserHomePageUI extends VerticalLayout {

    private final IToken tokenService;
    private final UserRepository userRepository;
    private final ButtonPresenter buttonPresenter;
    private final UserConnectivityPresenter userConn;
    private final PermissionsPresenter pp;

    /* UI elements we need to refresh */
    private final ComboBox<String> storeDropdown = new ComboBox<>("Store");
    private final VerticalLayout storeContent = new VerticalLayout();

    private List<Store> myStores = List.of();   // filled in ctor
    private String username;

    /* ------------------------------------------------------------ */
    @Autowired
    public UserHomePageUI(UserService userService,
                          OwnerManagerService ownerMgrService,
                          IToken tokenService,
                          UserRepository userRepository,
                          RegisteredService registeredService,
                          StoreRepository storeRepository) {

        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);
        this.userConn = new UserConnectivityPresenter(userService, registeredService,
                ownerMgrService, tokenService,
                userRepository);
        this.pp = new PermissionsPresenter(ownerMgrService, tokenService, userRepository);

        /* ------------ security gate ---------------------------------- */
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);
        username = tokenService.extractUsername(token);

        RegisteredUser user;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            UI.getCurrent().navigate("");
            return;
        }

        /* ------------ stores list (once) ----------------------------- */
        try {
            myStores = userConn.getUserStoresName(token);
        } catch (Exception e) {
            Notification.show(e.getMessage());          // fallback: empty list
            myStores = List.of();
        }

        /* ------------ header ---------------------------------------- */
        H1 title = new H1("🛍️ Store Manager Dashboard");
        HorizontalLayout header = new HorizontalLayout(
                new H4("👤 Hello, " + user.getUsername()),
                buttonPresenter.signOutButton(token)
        );
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        /* ------------ store dropdown -------------------------------- */
        storeDropdown.setItems(myStores.stream().map(Store::getName).toList());
        storeDropdown.setPlaceholder("Select / filter stores");
        storeDropdown.setClearButtonVisible(true);

//        /* when value chosen or custom text entered */
//        storeDropdown.addValueChangeListener(e -> refreshStoreContent());
//        storeDropdown.addCustomValueSetListener(e -> {
//            storeDropdown.setValue(e.getDetail());
//            refreshStoreContent();
//        });

        /* ------------ static action buttons ------------------------- */
        HorizontalLayout quick = new HorizontalLayout(
                new Button("add store", ev -> UI.getCurrent().navigate("/addstore")),
                new Button("add new product to store",
                        ev -> UI.getCurrent().navigate("/addnewproduct"))
        );

        HorizontalLayout searches = new HorizontalLayout(
                new Button("Search store", ev -> UI.getCurrent().navigate("/searchstore")),
                new Button("Search product", ev -> UI.getCurrent().navigate("/searchproduct")),
                new Button("Edit store", ev -> UI.getCurrent().navigate("/edit-store")),
                new Button("Shopping cart", ev -> UI.getCurrent().navigate("/shoppingcart"))
        );

        Map<String, Boolean> map1 = new HashMap<>();
        map1.put("PERM_MANAGE_INVENTORY", false);
        map1.put("PERM_MANAGE_STAFF", false);
        map1.put("PERM_VIEW_STORE", false);
        map1.put("PERM_UPDATE_POLICY", false);
        map1.put("PERM_ADD_PRODUCT", false);
        map1.put("PERM_REMOVE_PRODUCT", false);
        map1.put("PERM_UPDATE_PRODUCT", false);

        // Permissions and actions
        //if(storeDropdown.getValue() != null)
        LinkedList<Store> stores = new LinkedList<Store>();
        try {
            stores = userConn.getUserStoresName(token);
        } catch (Exception e) {
            add(new Span(e.getMessage() + "\npremissions:"));
        }
        for (Store storeName : stores) {
            add(new Span(storeName.getName()));
            map1 = this.pp.getPremissions(user.getUsername(), storeName.getId(), user.getUsername());
            ; //user.getManagerPermissions();

            if (map1 != null) {
                boolean[] permsArray = {
                        Boolean.TRUE.equals(map1.get("PERM_MANAGE_INVENTORY")),
                        Boolean.TRUE.equals(map1.get("PERM_MANAGE_STAFF")),
                        Boolean.TRUE.equals(map1.get("PERM_VIEW_STORE")),
                        Boolean.TRUE.equals(map1.get("PERM_UPDATE_POLICY")),
                        Boolean.TRUE.equals(map1.get("PERM_ADD_PRODUCT")),
                        Boolean.TRUE.equals(map1.get("PERM_REMOVE_PRODUCT")),
                        Boolean.TRUE.equals(map1.get("PERM_UPDATE_PRODUCT"))};

                // if it doesnt work to check maybe to go throw that path stright to the store and in it to the mannager for premissions
                // work over the store name -> store ID

                ManagerPermissions perms = new ManagerPermissions(permsArray, user.getUsername(), storeName.getId());

                if (map1 != null)
                    add(new PermissionButtonsUI(new ProductPresenter(userService, tokenService, userRepository), userConn, token, storeName, perms));

            }
        }



    /* ------------ assemble page --------------------------------- */
    add(header, new Hr(),title,

    storeDropdown,quick,searches,
    storeContent);

    setPadding(true);

    setSpacing(true);

    setAlignItems(Alignment.CENTER);
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

        /* initial population */
    //    refreshStoreContent();
  //  }

//    /* ------------------------------------------------------------ */
//    /** Re-build the storeContent layout according to filter box. */
//    private void refreshStoreContent() {
//        storeContent.removeAll();
//
//        String filter = Optional.ofNullable(storeDropdown.getValue())
//                .orElse("").trim().toLowerCase();
//
//        for (Store store : myStores) {
//            if (!store.getName().toLowerCase().contains(filter)) continue;
//
//            storeContent.add(new Span(store.getName()));
//
//            Map<String,Boolean> perms = pp.getPremissions(
//                    username, store.getId(), username);
//
//            if (perms == null) perms = Map.of();
//
//            HorizontalLayout top    = new HorizontalLayout();
//            HorizontalLayout bottom = new HorizontalLayout();
//            boolean hasAny = false;
//
//            if (Boolean.TRUE.equals(perms.get("PERM_VIEW_STORE"))) {
//                top.add(new Button("🏬 View Store"));
//                hasAny = true;
//            }
//            if (Boolean.TRUE.equals(perms.get("PERM_MANAGE_INVENTORY"))) {
//                top.add(new Button("📦 Manage Inventory"));
//                hasAny = true;
//            }
//            if (Boolean.TRUE.equals(perms.get("PERM_MANAGE_STAFF"))) {
//                top.add(new Button("👥 Manage Staff"));
//                hasAny = true;
//            }
//            if (Boolean.TRUE.equals(perms.get("PERM_ADD_PRODUCT"))) {
//                bottom.add(new Button("➕ Add Product"));
//                hasAny = true;
//            }
//            if (Boolean.TRUE.equals(perms.get("PERM_REMOVE_PRODUCT"))) {
//                bottom.add(new Button("❌ Remove Product"));
//                hasAny = true;
//            }
//            if (Boolean.TRUE.equals(perms.get("PERM_UPDATE_PRODUCT"))) {
//                bottom.add(new Button("✏️ Update Product"));
//                hasAny = true;
//            }
//            if (Boolean.TRUE.equals(perms.get("PERM_UPDATE_POLICY"))) {
//                bottom.add(new Button("📝 Update Policy"));
//                hasAny = true;
//            }
//
//            if (hasAny) storeContent.add(top, bottom);
//            else storeContent.add(new Paragraph(
//                    "⚠️ You currently don’t have permissions for this store."));
//        }
//    }
//}