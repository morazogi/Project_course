package DomainLayer.domainServices;

import DomainLayer.Store;
import DomainLayer.Roles.Owner;
import DomainLayer.Roles.Manager;
import DomainLayer.IStoreRepository;
import DomainLayer.IUserRepository;

public class InventoryManagementMicroservice {
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;

    public InventoryManagementMicroservice() {
        // Initialize repositories (would be injected in a real implementation)
    }

    /**
     * Set the repositories for this microservice
     * @param storeRepository Repository for stores
     * @param userRepository Repository for users
     */
    public void setRepositories(IStoreRepository storeRepository, IUserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Helper method to check if a user has permission to perform an action on a store
     * @param userId ID of the user (owner or manager)
     * @param storeId ID of the store
     * @param permissionType Type of permission to check (for managers)
     * @return true if the user has permission, false otherwise
     */
    private boolean checkPermission(String userId, String storeId, String permissionType) {
        // Get the store
        Store store = getStoreById(storeId);
        if (store == null) {
            return false;
        }

        // Check if user is an owner
        Owner owner = getOwnerById(userId);
        if (owner != null && owner.getStoreId().equals(storeId)) {
            return true; // Owners have all permissions
        }

        // Check if user is a manager with the required permission
        Manager manager = getManagerById(userId);
        if (manager != null && manager.getStoreId().equals(storeId)) {
            return manager.hasPermission(permissionType);
        }

        return false;
    }

    // Helper methods to get entities from repositories
    private Store getStoreById(String storeId) {
        if (storeRepository == null) {
            return null;
        }
        return storeRepository.getStoreById(storeId);
    }

    private Owner getOwnerById(String ownerId) {
        if (userRepository == null) {
            return null;
        }
        return userRepository.getOwnerById(ownerId);
    }

    private Manager getManagerById(String managerId) {
        if (userRepository == null) {
            return null;
        }
        return userRepository.getManagerById(managerId);
    }
    /**
     * Add a new product to the store inventory
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param productName Name of the product
     * @param description Description of the product
     * @param price Price of the product
     * @param quantity Initial quantity of the product
     * @param category Category of the product
     * @return Product ID if successful, null otherwise
     */
    public String addProduct(String ownerId, String storeId, String productName, String description, double price, int quantity, String category) {
        // Check if owner has permission
        if (!checkPermission(ownerId, storeId, null)) {
            return null; // No permission
        }

        // Implementation would call domain layer
        return "product-id"; // Placeholder for actual implementation
    }

    /**
     * Remove a product from the store inventory
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param productId ID of the product to remove
     * @return true if successful, false otherwise
     */
    public boolean removeProduct(String ownerId, String storeId, String productId) {
        // Check if owner has permission
        if (!checkPermission(ownerId, storeId, null)) {
            return false; // No permission
        }

        // Implementation would call domain layer
        return true; // Placeholder for actual implementation
    }

    /**
     * Update product details
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param productId ID of the product to update
     * @param productName New name (null if unchanged)
     * @param description New description (null if unchanged)
     * @param price New price (-1 if unchanged)
     * @param category New category (null if unchanged)
     * @return true if successful, false otherwise
     */
    public boolean updateProductDetails(String ownerId, String storeId, String productId, String productName, String description, double price, String category) {
        // Check if owner has permission
        if (!checkPermission(ownerId, storeId, null)) {
            return false; // No permission
        }

        // Implementation would call domain layer
        return true; // Placeholder for actual implementation
    }

    /**
     * Update product quantity
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param productId ID of the product
     * @param newQuantity New quantity
     * @return true if successful, false otherwise
     */
    public boolean updateProductQuantity(String ownerId, String storeId, String productId, int newQuantity) {
        // Check if owner has permission
        if (!checkPermission(ownerId, storeId, null)) {
            return false; // No permission
        }

        // Implementation would call domain layer
        return true; // Placeholder for actual implementation
    }

    /**
     * Manager function to add a product (if permitted)
     * @param managerId ID of the manager
     * @param storeId ID of the store
     * @param productName Name of the product
     * @param description Description of the product
     * @param price Price of the product
     * @param quantity Initial quantity of the product
     * @param category Category of the product
     * @return Product ID if successful, null otherwise
     */
    public String managerAddProduct(String managerId, String storeId, String productName, String description, double price, int quantity, String category) {
        // Check if manager has permission to add products
        if (!checkPermission(managerId, storeId, "addNewProduct")) {
            return null; // No permission
        }

        // Implementation would call domain layer
        return "product-id"; // Placeholder for actual implementation
    }

    /**
     * Manager function to update product details (if permitted)
     * @param managerId ID of the manager
     * @param storeId ID of the store
     * @param productId ID of the product to update
     * @param productName New name (null if unchanged)
     * @param description New description (null if unchanged)
     * @param price New price (-1 if unchanged)
     * @param category New category (null if unchanged)
     * @return true if successful, false otherwise
     */
    public boolean managerUpdateProductDetails(String managerId, String storeId, String productId, String productName, String description, double price, String category) {
        // Check if manager has permission to update product details
        boolean hasPermission = true;

        // Check different permissions based on what's being updated
        if (productName != null || description != null) {
            hasPermission = hasPermission && checkPermission(managerId, storeId, "changeProductDescription");
        }

        if (price >= 0) {
            hasPermission = hasPermission && checkPermission(managerId, storeId, "changeProductPrice");
        }

        if (!hasPermission) {
            return false; // No permission
        }

        // Implementation would call domain layer
        return true; // Placeholder for actual implementation
    }

    /**
     * Manager function to update product quantity (if permitted)
     * @param managerId ID of the manager
     * @param storeId ID of the store
     * @param productId ID of the product
     * @param newQuantity New quantity
     * @return true if successful, false otherwise
     */
    public boolean managerUpdateProductQuantity(String managerId, String storeId, String productId, int newQuantity) {
        // Check if manager has permission to change product quantity
        if (!checkPermission(managerId, storeId, "changeProductQuantity")) {
            return false; // No permission
        }

        // Implementation would call domain layer
        return true; // Placeholder for actual implementation
    }

    /**
     * Manager function to remove a product (if permitted)
     * @param managerId ID of the manager
     * @param storeId ID of the store
     * @param productId ID of the product to remove
     * @return true if successful, false otherwise
     */
    public boolean managerRemoveProduct(String managerId, String storeId, String productId) {
        // Check if manager has permission to remove products
        if (!checkPermission(managerId, storeId, "removeProductFromInventory")) {
            return false; // No permission
        }

        // Implementation would call domain layer
        return true; // Placeholder for actual implementation
    }
}
