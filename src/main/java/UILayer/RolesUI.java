package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.RolesPresenter;
import ServiceLayer.RegisteredService;
import ServiceLayer.RolesService;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/roles")
public class RolesUI extends VerticalLayout {

    private final RolesPresenter presenter;
    private final Span status = new Span();

    @Autowired
    public RolesUI(IToken tokenSvc,
                   RolesService rolesSvc,
                   UserService userSvc,
                   RegisteredService regSvc,
                   UserRepository userRepository) {

        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);

        String userId = token != null ? userRepository.getById(tokenSvc.extractUsername(token)).getShoppingCart().getUserId() : "unknown";
        //String username = token != null ? tokenSvc.extractUsername(token) : "unknown";

        this.presenter = new RolesPresenter(userId, token, rolesSvc, userSvc, tokenSvc, userRepository);
        ButtonPresenter btns = new ButtonPresenter(regSvc, tokenSvc);

        /* Input fields */
        TextField storeField = new TextField("Store Name");
        TextField targetUser = new TextField("Target Username");



        // ─── Appoint Manager ────────────────────
        Checkbox[] perms = new Checkbox[] {
                new Checkbox("MANAGE_INVENTORY"),
                new Checkbox("MANAGE_STAFF"),
                new Checkbox("VIEW_STORE"),
                new Checkbox("UPDATE_POLICY"),
                new Checkbox("ADD_PRODUCT"),
                new Checkbox("REMOVE_PRODUCT"),
                new Checkbox("UPDATE_PRODUCT"),
                new Checkbox("OPEN_STORE"),
                new Checkbox("CLOSE_STORE")
        };


        Button appointManager = new Button("Appoint Manager", e -> {
            boolean[] selected = new boolean[9];
            for (int i = 0; i < perms.length; i++)
                selected[i] = perms[i].getValue();

            String msg = presenter.appointManager(storeField.getValue(), targetUser.getValue(), selected);
            Notification.show(msg); status.setText(msg);
        });

        // ─── Appoint Owner ──────────────────────
        Button appointOwner = new Button("Appoint Owner", e -> {
            String msg = presenter.appointOwner(storeField.getValue(), targetUser.getValue());
            Notification.show(msg); status.setText(msg);
        });

        // ─── Remove Owner ───────────────────────
        Button removeOwner = new Button("Remove Owner", e -> {
            String msg = presenter.removeOwner(storeField.getValue(), targetUser.getValue());
            Notification.show(msg); status.setText(msg);
        });


        // ─── Remove Manager ─────────────────────
        Button removeManager = new Button("Remove Manager", e -> {
            String msg = presenter.removeManager(storeField.getValue(), targetUser.getValue());
            Notification.show(msg); status.setText(msg);
        });

        // ─── Update Permissions ─────────────────
        Button updatePerms = new Button("Update Permissions", e -> {
            boolean[] selected = new boolean[5];
            for (int i = 0; i < perms.length; i++)
                selected[i] = perms[i].getValue();

            String msg = presenter.updatePermissions(storeField.getValue(), targetUser.getValue(), selected);
            Notification.show(msg); status.setText(msg);
        });

        // ─── View Roles ─────────────────────────
        Button viewRoles = new Button("View Store Roles", e -> {
            String tree = presenter.viewStoreRoles(storeField.getValue());
            String html = "<pre>" + tree + "</pre>";
            status.getElement().setProperty("innerHTML", html);
        });
        // ─── Relinquish Roles ───────────────────
        Button giveUpOwner = new Button("Relinquish Ownership", e -> {
            String msg = presenter.relinquishOwnership(storeField.getValue());
            Notification.show(msg); status.setText(msg);
        });

        Button giveUpManager = new Button("Relinquish Management", e -> {
            String msg = presenter.relinquishManagement(storeField.getValue());
            Notification.show(msg); status.setText(msg);
        });

        /* Layout structure */
        add(
                new HorizontalLayout(new H1("Roles Management"), btns.homePageButton(token)),

                new HorizontalLayout(storeField, targetUser),

                appointOwner, removeOwner,
                appointManager, removeManager,
                updatePerms,
                new HorizontalLayout(perms),  // permissions checkboxes

                viewRoles,
                giveUpOwner, giveUpManager,

                status
        );

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
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