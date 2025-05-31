package DomainLayer;

import jakarta.persistence.*;

import java.util.Map;

@Entity
@Table(name = "manager_permissions")
public class ManagerPermissions {
    public static final String PERM_MANAGE_INVENTORY = "manageInventory";
    public static final String PERM_MANAGE_STAFF = "manageStaff";
    public static final String PERM_VIEW_STORE = "viewStore";
    public static final String PERM_UPDATE_POLICY = "updatePolicy";
    public static final String PERM_ADD_PRODUCT = "addProduct";
    public static final String PERM_REMOVE_PRODUCT = "removeProduct";
    public static final String PERM_UPDATE_PRODUCT = "updateProduct";
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "manage_inventory")
    private boolean manageInventory;

    @Column(name = "manage_staff")
    private boolean manageStaff;

    @Column(name = "view_store")
    private boolean viewStore;

    @Column(name = "update_policy")
    private boolean updatePolicy;

    @Column(name = "add_product")
    private boolean addProduct;

    @Column(name = "remove_product")
    private boolean removeProduct;

    @Column(name = "update_product")
    private boolean updateProduct;

    public ManagerPermissions() {}

    public ManagerPermissions(boolean[] perm) {
        this.manageInventory = perm[0];
        this.manageStaff = perm[1];
        this.viewStore = perm[2];
        this.updatePolicy = perm[3];
        this.addProduct = perm[4];
        this.removeProduct = perm[5];
        this.updateProduct = perm[6];
    }

    public boolean isManageInventory() { return manageInventory; }
    public boolean isManageStaff() { return manageStaff; }
    public boolean isViewStore() { return viewStore; }
    public boolean isUpdatePolicy() { return updatePolicy; }
    public boolean isAddProduct() { return addProduct; }
    public boolean isRemoveProduct() { return removeProduct; }
    public boolean isUpdateProduct() { return updateProduct; }

    public void setPermissions(boolean[] perm) {
        this.manageInventory = perm[0];
        this.manageStaff = perm[1];
        this.viewStore = perm[2];
        this.updatePolicy = perm[3];
        this.addProduct = perm[4];
        this.removeProduct = perm[5];
        this.updateProduct = perm[6];
    }

    public boolean getPermission(String permission) {
        switch (permission) {
            case PERM_MANAGE_INVENTORY:
                return manageInventory;
            case PERM_MANAGE_STAFF:
                return manageStaff;
            case PERM_VIEW_STORE:
                return viewStore;
            case PERM_UPDATE_POLICY:
                return updatePolicy;
            case PERM_ADD_PRODUCT:
                return addProduct;
            case PERM_REMOVE_PRODUCT:
                return removeProduct;
            case PERM_UPDATE_PRODUCT:
                return updateProduct;
            default:
                return false;
        }
    }

    public Map<String, Boolean> getPermissions() {
        return Map.of(
                PERM_MANAGE_INVENTORY, manageInventory,
                PERM_MANAGE_STAFF, manageStaff,
                PERM_VIEW_STORE, viewStore,
                PERM_UPDATE_POLICY, updatePolicy,
                PERM_ADD_PRODUCT, addProduct,
                PERM_REMOVE_PRODUCT, removeProduct,
                PERM_UPDATE_PRODUCT, updateProduct
        );
    }
}
