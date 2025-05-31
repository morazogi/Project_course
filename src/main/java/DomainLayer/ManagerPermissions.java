package DomainLayer;

import java.util.Map;
import java.util.HashMap;

public class ManagerPermissions {
    public static final String PERM_MANAGE_INVENTORY = "PERM_MANAGE_INVENTORY";
    public static final String PERM_MANAGE_STAFF = "PERM_MANAGE_STAFF";
    public static final String PERM_VIEW_STORE = "PERM_VIEW_STORE";
    public static final String PERM_UPDATE_POLICY = "PERM_UPDATE_POLICY";
    public static final String PERM_ADD_PRODUCT = "PERM_ADD_PRODUCT";
    public static final String PERM_REMOVE_PRODUCT = "PERM_REMOVE_PRODUCT";
    public static final String PERM_UPDATE_PRODUCT = "PERM_UPDATE_PRODUCT";

    private Map<String, Boolean> permissions;

    // ✅ Default constructor needed by Jackson
    public ManagerPermissions() {
        this.permissions = new HashMap<>();
        this.permissions.put(PERM_MANAGE_INVENTORY, false);
        this.permissions.put(PERM_MANAGE_STAFF, false);
        this.permissions.put(PERM_VIEW_STORE, false);
        this.permissions.put(PERM_UPDATE_POLICY, false);
        this.permissions.put(PERM_ADD_PRODUCT, false);
        this.permissions.put(PERM_REMOVE_PRODUCT, false);
        this.permissions.put(PERM_UPDATE_PRODUCT, false);
    }

    // Custom constructor (still useful)
    public ManagerPermissions(boolean[] perm) {
        this();
        setPermissionsFromAarray(perm);
    }

    // ✅ Getter that Jackson will use
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    // ✅ Setter that Jackson will use
    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public boolean getPermission(String permission) {
        return permissions.getOrDefault(permission, false);
    }

    // Extra: allow setting via boolean array if needed
    public void setPermissionsFromAarray(boolean[] perm) {
        this.permissions.put(PERM_MANAGE_INVENTORY, perm[0]);
        this.permissions.put(PERM_MANAGE_STAFF, perm[1]);
        this.permissions.put(PERM_VIEW_STORE, perm[2]);
        this.permissions.put(PERM_UPDATE_POLICY, perm[3]);
        this.permissions.put(PERM_ADD_PRODUCT, perm[4]);
        this.permissions.put(PERM_REMOVE_PRODUCT, perm[5]);
        this.permissions.put(PERM_UPDATE_PRODUCT, perm[6]);
    }
}