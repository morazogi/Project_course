package UILayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.ManagerPermissions;
import DomainLayer.Roles.RegisteredUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/userhomepage")
public class UserHomePageUI extends VerticalLayout {

    private final IToken tokenService;
    private final IUserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public UserHomePageUI(IToken tokenService, IUserRepository userRepository) {
        // Get current user from session
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        String username = tokenService.extractUsername(token);
        String jsonUser = userRepository.getUser(username);
        RegisteredUser user = null;
        try {
            user = mapper.readValue(jsonUser, RegisteredUser.class);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
        if (user == null) {
            UI.getCurrent().navigate("");
            return;
        }

        // Header bar
        H1 title = new H1("🛍️ Store Manager Dashboard");
        Button homeBtn = new Button("🏠 Home", e -> UI.getCurrent().navigate(""));
        Button signOutBtn = new Button("🔐 Sign Out", e -> {
            UI.getCurrent().getSession().setAttribute("token", null);
            UI.getCurrent().navigate("");
        });

        HorizontalLayout header = new HorizontalLayout(
                new H4("👤 Hello, " + user.getUsername()),
                homeBtn,
                signOutBtn
        );
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        add(header, new Hr(), title);

        // Permissions and actions
        ManagerPermissions perms = null; //user.getManagerPermissions();
        boolean hasAnyPermission = false;
        HorizontalLayout buttonLayout1 = new HorizontalLayout();
        HorizontalLayout buttonLayout2 = new HorizontalLayout();

        if (perms.getPermission(ManagerPermissions.PERM_VIEW_STORE)) {
            buttonLayout1.add(new Button("🏬 View Store"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
            buttonLayout1.add(new Button("📦 Manage Inventory"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_MANAGE_STAFF)) {
            buttonLayout1.add(new Button("👥 Manage Staff"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_ADD_PRODUCT)) {
            buttonLayout2.add(new Button("➕ Add Product"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT)) {
            buttonLayout2.add(new Button("❌ Remove Product"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT)) {
            buttonLayout2.add(new Button("✏️ Update Product"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_UPDATE_POLICY)) {
            buttonLayout2.add(new Button("📝 Update Policy"));
            hasAnyPermission = true;
        }

        add(buttonLayout1, buttonLayout2);

        if (!hasAnyPermission) {
            add(new Paragraph("⚠️ You currently don’t have permissions for any store management actions. Contact the store owner to update your role."));
        }

        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
    }
}
