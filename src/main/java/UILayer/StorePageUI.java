package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.ManagerPermissions;
import DomainLayer.Store;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.PermissionsPresenter;
import PresentorLayer.ProductPresenter;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Route("/store/:storeid")
public class StorePageUI extends VerticalLayout implements BeforeEnterObserver {

    private final ProductPresenter productPresenter;
    private final PermissionsPresenter permissionsPresenter;
    private final ButtonPresenter buttonPresenter;
    private final UserConnectivityPresenter userConnectivityPresenter;
    private final OwnerManagerService ownerManagerService;

    @Autowired
    public StorePageUI(UserService configuredUserService, IToken configuredTokenService, UserRepository configuredUserRepository, OwnerManagerService ownerManagerService, RegisteredService registeredService) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService,configuredUserRepository);
        permissionsPresenter = new PermissionsPresenter(ownerManagerService, configuredTokenService, configuredUserRepository);
        buttonPresenter = new ButtonPresenter(registeredService, configuredTokenService);
        userConnectivityPresenter = new UserConnectivityPresenter(configuredUserService, registeredService, ownerManagerService, configuredTokenService, configuredUserRepository);
        this.ownerManagerService = ownerManagerService;
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        RouteParameters parameters = beforeEnterEvent.getRouteParameters();
        if (parameters.get("storeid").isPresent()) {
            UI.getCurrent().getSession().setAttribute("storeId", parameters.get("storeid").get());
        } else {
            add(new Span("No fitting store"));
        }
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);
        add(buttonPresenter.homePageButton(token), productPresenter.getStorePage(token, (String) UI.getCurrent().getSession().getAttribute("storeId")));

        Map<String, Boolean> map1 = permissionsPresenter.getPremissions(token, (String) UI.getCurrent().getSession().getAttribute("storeId"));
        if (map1 != null) {
            boolean[] permsArray = {
                    Boolean.TRUE.equals(map1.get("PERM_MANAGE_INVENTORY")),
                    Boolean.TRUE.equals(map1.get("PERM_MANAGE_STAFF")),
                    Boolean.TRUE.equals(map1.get("PERM_VIEW_STORE")),
                    Boolean.TRUE.equals(map1.get("PERM_UPDATE_POLICY")),
                    Boolean.TRUE.equals(map1.get("PERM_ADD_PRODUCT")),
                    Boolean.TRUE.equals(map1.get("PERM_REMOVE_PRODUCT")),
                    Boolean.TRUE.equals(map1.get("PERM_UPDATE_PRODUCT")),
                    Boolean.TRUE.equals(map1.get("PERM_OPEN_STORE")),
                    Boolean.TRUE.equals(map1.get("PERM_CLOSE_STORE"))};

            // if it doesnt work to check maybe to go throw that path stright to the store and in it to the mannager for premissions
            // work over the store name -> store ID

            ManagerPermissions perms = new ManagerPermissions(permsArray, userConnectivityPresenter.getUsername(token), (String) UI.getCurrent().getSession().getAttribute("token"));

            if (map1 != null)
                add(new PermissionButtonsUI(productPresenter, userConnectivityPresenter, token, userConnectivityPresenter.getStore(token, (String) UI.getCurrent().getSession().getAttribute("storeId")), perms, ownerManagerService));
        }
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