package DomainLayer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "manager_permissions")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagerPermissions {

    public static final String PERM_MANAGE_INVENTORY = "PERM_MANAGE_INVENTORY";
    public static final String PERM_MANAGE_STAFF = "PERM_MANAGE_STAFF";
    public static final String PERM_VIEW_STORE = "PERM_VIEW_STORE";
    public static final String PERM_UPDATE_POLICY = "PERM_UPDATE_POLICY";
    public static final String PERM_ADD_PRODUCT = "PERM_ADD_PRODUCT";
    public static final String PERM_REMOVE_PRODUCT = "PERM_REMOVE_PRODUCT";
    public static final String PERM_UPDATE_PRODUCT = "PERM_UPDATE_PRODUCT";
    public static final String PERM_OPEN_STORE = "PERM_OPEN_STORE";
    public static final String PERM_CLOSE_STORE = "PERM_CLOSE_STORE";

    @EmbeddedId
    private ManagerPermissionsPK id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "manager_permission_entries",
            joinColumns = {
                    @JoinColumn(name = "store_id", referencedColumnName = "store_id"),
                    @JoinColumn(name = "manager_id", referencedColumnName = "manager_id")
            }
    )
    @MapKeyColumn(name = "permission_name")
    @Column(name = "permission_value")
    private Map<String, Boolean> permissions = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    public ManagerPermissions() {
        initializeDefaultPermissions();
    }

    public ManagerPermissions(boolean[] permArray, String managerId, String storeId) {
        this.id = new ManagerPermissionsPK(managerId, storeId);
        initializeDefaultPermissions();
        setPermissionsFromAarray(permArray);
    }

    private void initializeDefaultPermissions() {
        permissions.put(PERM_MANAGE_INVENTORY, false);
        permissions.put(PERM_MANAGE_STAFF, false);
        permissions.put(PERM_VIEW_STORE, false);
        permissions.put(PERM_UPDATE_POLICY, false);
        permissions.put(PERM_ADD_PRODUCT, false);
        permissions.put(PERM_REMOVE_PRODUCT, false);
        permissions.put(PERM_UPDATE_PRODUCT, false);
        permissions.put(PERM_OPEN_STORE, false);
        permissions.put(PERM_CLOSE_STORE, false);
    }

    public ManagerPermissionsPK getId() {
        return id;
    }

    public void setId(ManagerPermissionsPK id) {
        this.id = id;
    }

    public String getManagerId() {
        return id != null ? id.getManagerId() : null;
    }

    public String getStoreId() {
        return id != null ? id.getStoreId() : null;
    }

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

    public void setPermissionsFromAarray(boolean[] perm) {
        if (perm == null || perm.length < 9) {
            System.err.println("Permissions array is null or too short.");
            return;
        }
        this.permissions.put(PERM_MANAGE_INVENTORY, perm[0]);
        this.permissions.put(PERM_MANAGE_STAFF, perm[1]);
        this.permissions.put(PERM_VIEW_STORE, perm[2]);
        this.permissions.put(PERM_UPDATE_POLICY, perm[3]);
        this.permissions.put(PERM_ADD_PRODUCT, perm[4]);
        this.permissions.put(PERM_REMOVE_PRODUCT, perm[5]);
        this.permissions.put(PERM_UPDATE_PRODUCT, perm[6]);
        this.permissions.put(PERM_CLOSE_STORE, perm[5]);
        this.permissions.put(PERM_OPEN_STORE, perm[6]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManagerPermissions)) return false;
        ManagerPermissions that = (ManagerPermissions) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
