package DomainLayer; // Adjust package as per your structure

import jakarta.persistence.*;
import DomainLayer.ManagerPermissionsPK; // Import your new PK class
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;




@Entity
@Table(name = "manager_permissions")
public class ManagerPermissions {
    // ... (public static final String PERM_... constants)
    public static final String PERM_MANAGE_INVENTORY = "PERM_MANAGE_INVENTORY";
    public static final String PERM_MANAGE_STAFF = "PERM_MANAGE_STAFF";
    public static final String PERM_VIEW_STORE = "PERM_VIEW_STORE";
    public static final String PERM_UPDATE_POLICY = "PERM_UPDATE_POLICY";
    public static final String PERM_ADD_PRODUCT = "PERM_ADD_PRODUCT";
    public static final String PERM_REMOVE_PRODUCT = "PERM_REMOVE_PRODUCT";
    public static final String PERM_UPDATE_PRODUCT = "PERM_UPDATE_PRODUCT";

    @EmbeddedId
    private ManagerPermissionsPK id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "manager_permission_entries",
            // The join columns here refer to the columns in 'manager_permission_entries'
            // that form the foreign key back to 'manager_permissions'.
            // The 'referencedColumnName' implicitly points to the columns in the
            // owning entity's primary key (derived from ManagerPermissionsPK).
            joinColumns = {
                    @JoinColumn(name = "manager_id", referencedColumnName = "manager_id"),
                    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
            })
    @MapKeyColumn(name = "permission_name")
    @Column(name = "permission_value")
    private Map<String, Boolean> permissions;

    // Default constructor needed by JPA and potentially Jackson
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

    // Custom constructor: NOW ACCEPTS storeId
    public ManagerPermissions(boolean[] perm, String managerId, String storeId) {
        this(); // Call default constructor to initialize permissions map
        this.id = new ManagerPermissionsPK(managerId, storeId); // Initialize the composite ID
        setPermissionsFromAarray(perm);
    }

    // --- Getters and Setters ---

    // Getter for the composite ID
    public ManagerPermissionsPK getId() {
        return id;
    }

    // Setter for the composite ID (often not used externally, but JPA needs it)
    public void setId(ManagerPermissionsPK id) {
        this.id = id;
    }

    // Convenience getter for managerId (access through the composite ID)
    public String getManagerId() {
        return this.id != null ? this.id.getManagerId() : null;
    }

    // Convenience getter for storeId (access through the composite ID)
    public String getStoreId() {
        return this.id != null ? this.id.getStoreId() : null;
    }

    // (Other getters/setters for permissions map remain the same)
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public boolean getPermission(String permission) {
        return permissions.getOrDefault(permission, false);
    }

    public void setPermission(String permission, boolean value) {
        this.permissions.put(permission, value);
    }

    // Extra: allow setting via boolean array if needed
    public void setPermissionsFromAarray(boolean[] perm) {
        // Ensure perm array is long enough to avoid IndexOutOfBoundsException
        if (perm == null || perm.length < 7) {
            // Log a warning or throw an IllegalArgumentException
            System.err.println("Warning: Permissions array is too short or null.");
            return;
        }
        this.permissions.put(PERM_MANAGE_INVENTORY, perm[0]);
        this.permissions.put(PERM_MANAGE_STAFF, perm[1]);
        this.permissions.put(PERM_VIEW_STORE, perm[2]);
        this.permissions.put(PERM_UPDATE_POLICY, perm[3]);
        this.permissions.put(PERM_ADD_PRODUCT, perm[4]);
        this.permissions.put(PERM_REMOVE_PRODUCT, perm[5]);
        this.permissions.put(PERM_UPDATE_PRODUCT, perm[6]);
    }

    // Implement equals and hashCode for the entity, using the ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerPermissions that = (ManagerPermissions) o;
        return Objects.equals(id, that.id); // Only compare IDs for entity equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash based on ID
    }
}
