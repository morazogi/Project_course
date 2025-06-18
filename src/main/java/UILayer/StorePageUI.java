package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.ManagerPermissions;
import DomainLayer.Store;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.PermissionsPresenter;
import PresentorLayer.ProductPresenter;
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

    @Autowired
    public StorePageUI(UserService configuredUserService, IToken configuredTokenService, UserRepository configuredUserRepository, OwnerManagerService ownerManagerService, RegisteredService registeredService) {
        productPresenter = new ProductPresenter(configuredUserService, configuredTokenService,configuredUserRepository);
        permissionsPresenter = new PermissionsPresenter(ownerManagerService, configuredTokenService, configuredUserRepository);
        buttonPresenter = new ButtonPresenter(registeredService, configuredTokenService);
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
        add(buttonPresenter.homePageButton(token), productPresenter.getStorePage(token, (String) UI.getCurrent().getSession().getAttribute("storeId")));

        Map<String, Boolean> perms = permissionsPresenter.getPremissions(token, (String) UI.getCurrent().getSession().getAttribute("storeId"));
        HorizontalLayout buttonLayout1 = new HorizontalLayout();
        HorizontalLayout buttonLayout2 = new HorizontalLayout();

        if (perms != null) {
            if (perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY) != null && perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
                buttonLayout1.add(new Button("📦 Manage Inventory"));
            }
            if (perms.get(ManagerPermissions.PERM_MANAGE_STAFF) != null && perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
                buttonLayout1.add(new Button("👥 Manage Staff"));
            }
            if (perms.get(ManagerPermissions.PERM_ADD_PRODUCT) != null && perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
                buttonLayout2.add(new Button("➕ Add Product", e -> {UI.getCurrent().navigate("/addnewproduct");}));
            }
            if (perms.get(ManagerPermissions.PERM_REMOVE_PRODUCT) != null && perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
                buttonLayout2.add(new Button("❌ Remove Product"));
            }
            if (perms.get(ManagerPermissions.PERM_UPDATE_PRODUCT) != null && perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
                buttonLayout2.add(new Button("✏️ Update Product"));
            }
            if (perms.get(ManagerPermissions.PERM_UPDATE_POLICY) != null && perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
                buttonLayout2.add(new Button("📝 Update Policy"));
            }
            add(buttonLayout1, buttonLayout2);
        }
    }
}