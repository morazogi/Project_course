package DomainLayer.Roles;

import DomainLayer.Store;

import java.util.HashMap;

public class Manager extends RegisteredUser {
    private Store store;
    private HashMap<String, Boolean> permissions;

    public Manager(String name, Store store, Owner superior, boolean[] permissions) {
        super(name);
        this.store = store;
        this.permissions = new HashMap<>();
        initializePermissions(permissions);
        // Register with store
        store.addManager(this, superior);
    }

    private void initializePermissions(boolean[] permissions) {
        this.permissions.put("addNewProduct", Boolean.valueOf(permissions[0]));
        this.permissions.put("changeProductQuantity", Boolean.valueOf(permissions[1]));
        this.permissions.put("changeProductPrice", Boolean.valueOf(permissions[2]));
        this.permissions.put("changeProductDescription", Boolean.valueOf(permissions[3]));
        this.permissions.put("removeProductFromInventory", Boolean.valueOf(permissions[4]));
    }

    public Store getStore() {
        return store;
    }

    public Owner getSuperior() {
        return store.getManagerSuperiorOwner(this.getID());
    }

    public void changePermissions(boolean[] permissions) {
        initializePermissions(permissions);
    }

    public boolean hasPermission(String permission) {
        return permissions.getOrDefault(permission, false);
    }

    public void addNewProduct(String name, int quantity) {
        if (hasPermission("addNewProduct")) {
            store.addNewProduct(name, quantity);
        } else {
            sendErrorMessage("you do not have the right permissions for this action\n");
        }
    }

    public void changeProductQuantity(String productID, int quantity) {
        if (hasPermission("changeProductQuantity")) {
            store.changeProductQuantity(productID, quantity);
        } else {
            sendErrorMessage("you do not have the right permissions for this action\n");
        }
    }

    public void removeProductFromInventory(String productID) {
        if (hasPermission("removeProductFromInventory")) {
            store.removeProduct(productID);
        } else {
            sendErrorMessage("you do not have the right permissions for this action\n");
        }
    }

    private void sendErrorMessage(String message) {
        // This would be implemented with a notification system in the future
        System.out.println("Error: " + message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Managing: \n");
        sb.append("User ID: ").append(this.getID()).append("\n");
        sb.append("Name: ").append(this.getName()).append("\n");
        Owner superior = getSuperior();
        if (superior != null) {
            sb.append("Superior ID: ").append(superior.getID()).append("\n");
        }
        sb.append("Permissions: \n")
            .append("  -  addNewProduct: ").append(permissions.get("addNewProduct")).append("\n")
            .append("  -  changeProductQuantity: ").append(permissions.get("changeProductQuantity")).append("\n")
            .append("  -  changeProductPrice: ").append(permissions.get("changeProductPrice")).append("\n")
            .append("  -  changeProductDescription: ").append(permissions.get("changeProductDescription")).append("\n")
            .append("  -  removeProductFromInventory: ").append(permissions.get("removeProductFromInventory")).append("\n");
        return sb.toString();
    }
}
