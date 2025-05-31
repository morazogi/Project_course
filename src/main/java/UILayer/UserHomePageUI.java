package UILayer;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.PermissionsPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Route("/userhomepage")
public class UserHomePageUI extends VerticalLayout {

    private final IToken tokenService;
    private final IUserRepository userRepository;
    private final ButtonPresenter buttonPresenter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PermissionsPresenter pp;

    @Autowired
    public UserHomePageUI(OwnerManagerService ownerManager, IToken tokenService, IUserRepository userRepository, RegisteredService registeredService, IStoreRepository storeRepository) {
        // Get current user from session
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.buttonPresenter = new ButtonPresenter(registeredService);
        this.pp = new PermissionsPresenter(ownerManager, tokenService, userRepository);
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
        Store store = new Store();
        store.setId("saqw");
        store.addOwner(user.getID(), user.getID());
        boolean[] arr = new boolean[7];
        arr[0] = true;
        arr[1] = true;
        store.addManager(user.getID(), user.getID(), arr);
        store.setName("name");
        System.out.println(store.userIsManager(user.getID()));
        try {
            storeRepository.addStore(store.getId(), mapper.writeValueAsString(store));
            System.out.println("Serialized store JSON:\n" + mapper.writeValueAsString(store));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Header bar
        H1 title = new H1("🛍️ Store Manager Dashboard");
        Button homeBtn = buttonPresenter.homePageButton();
        Button signOutBtn = buttonPresenter.signOutButton(token);

        HorizontalLayout header = new HorizontalLayout(
                new H4("👤 Hello, " + user.getUsername()),
                homeBtn,
                signOutBtn
        );
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        ComboBox<String> storeDropdown = new ComboBox<>("Store");
        storeDropdown.setItems("Store 1", "Store 2", "Store 3"); // TODO add stores as a list or use it as an input?
        storeDropdown.setPlaceholder("Select Store");

        add(header, new Hr(), title, storeDropdown);


        // in case storeDropdown.getValue() == null
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
            map1 = this.pp.getPremissions(user.getID(), store.getName(), user.getID());; //user.getManagerPermissions();

        boolean[] permsArray=  {
            map1.get("PERM_MANAGE_INVENTORY"),
            map1.get("PERM_MANAGE_STAFF"),
            map1.get("PERM_VIEW_STORE"),
            map1.get("PERM_UPDATE_POLICY"),
            map1.get("PERM_ADD_PRODUCT"),
            map1.get("PERM_REMOVE_PRODUCT"),
            map1.get("PERM_UPDATE_PRODUCT")};

        // if it doesnt work to check maybe to go throw that path stright to the store and in it to the mannager for premissions
        // work over the store name -> store ID

        ManagerPermissions perms = new ManagerPermissions(permsArray);
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

        if(map1 != null)
            add(buttonLayout1, buttonLayout2);

        if (!hasAnyPermission) {
            add(new Paragraph("⚠️ You currently don’t have permissions for any store management actions. Contact the store owner to update your role."));
        }

        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
    }
}
