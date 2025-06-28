package DomainLayer.DomainServices;

import DomainLayer.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import static DomainLayer.ManagerPermissions.*;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.hibernate.Hibernate;

public class InventoryManagementMicroservice {
    private StoreRepository storeRepository;
    private ProductRepository productRepository;
    private ObjectMapper mapper = new ObjectMapper();
    // Standard permission strings


    public InventoryManagementMicroservice(StoreRepository storeRepository, ProductRepository productRepository) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    /**
     * Set the repositories for this microservice
     *
     * @param storeRepository Repository for stores
     * @param userRepository  Repository for users
     */
    public void setRepositories(StoreRepository storeRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * Helper method to check if a user has permission to perform an action on a store
     *
     * @param userId         ID of the user (owner or manager)
     * @param storeId        ID of the store
     * @param permissionType Type of permission to check (for managers)
     * @return true if the user has permission, false otherwise
     */
    private boolean checkPermission(String userId, String storeId, String permissionType) {
        // Get the store
        Store store = getStoreById(storeId);
        if (store == null) {
            return false;
        }

        // Owners have all permissions
        if (store.userIsOwner(userId)) {
            return true;
        }

        // Check if manager has specific permission
        if (store.userIsManager(userId)) {
            return store.userHasPermissions(userId, permissionType);
        }

        return false;
    }

    // Helper methods to get entities from repositories
    private Store getStoreById(String storeId) {
        if (storeRepository == null) {
            return null;
        }
        Store store = storeRepository.getById(storeId);
        Hibernate.initialize(store);
        if (store == null) {
            throw new IllegalArgumentException("Store does not exist");
        }
        return store;
    }

    /**
     * Add a new product to the store inventory
     *
     * @param userId      ID of the user (owner or manager)
     * @param storeId     ID of the store
     * @param productName Name of the product
     * @param description Description of the product
     * @param price       Price of the product
     * @param quantity    Initial quantity of the product
     * @param category    Category of the product
     * @return Product ID if successful, null otherwise
     */
    public String addProduct(String userId, String storeId, String productName, String description, float price, int quantity, String category) {
        // Check if user has permission
        if (!checkPermission(userId, storeId, ManagerPermissions.PERM_ADD_PRODUCT)) {
            return null; // No permission
        }

        Store store = getStoreById(storeId);
        if (store == null) {
            return null;
        }
        Product product = new Product(storeId, productName, description, price, quantity, -1, category);
        productRepository.save(product);
        store.addProduct(product.getId(), product.getQuantity());
        try {
            storeRepository.update(store);
        } catch (Exception e) {
            return e.getMessage();
        }
        return product.getId();
    }

    /**
     * Remove a product from the store inventory
     *
     * @param userId    ID of the user (owner or manager)
     * @param storeId   ID of the store
     * @param productId ID of the product to remove
     * @return true if successful, false otherwise
     */
    public boolean removeProduct(String userId, String storeId, String productId) {
        // Check if user has permission
        if (!checkPermission(userId, storeId, PERM_REMOVE_PRODUCT)) {
            return false; // No permission
        }

        Store store = getStoreById(storeId);
        if (store == null) {
            return false;
        }

        if(store.removeProduct(productId)) {
            storeRepository.update(store);
            return true;
        }
        return false;
    }

    /**
     * Update product details
     *
     * @param userId      ID of the user (owner or manager)
     * @param storeId     ID of the store
     * @param productId   ID of the product to update
     * @param productName New name (null if unchanged)
     * @param description New description (null if unchanged)
     * @param price       New price (-1 if unchanged)
     * @param category    New category (null if unchanged)
     * @return true if successful, false otherwise
     */
    public boolean updateProductDetails(String userId, String storeId, String productId, String productName, String description, double price, String category) {
        // Check if user has permission
        if (!checkPermission(userId, storeId, PERM_UPDATE_PRODUCT)) {
            return false; // No permission
        }

        Store store = getStoreById(storeId);
        if (store == null) {
            return false;
        }

        if(store.updateProductDetails(productId, productName, description, price, category)) {
            Product olProdu = productRepository.getById(productId);
            Product updateProduc = new Product(storeId, productName, description, (float) price, olProdu.getQuantity(), olProdu.getRating(), category);
            updateProduc.setId(productId);
            productRepository.delete(olProdu);
            productRepository.update(updateProduc);
            storeRepository.update(store);
            return true;
        }
        return false;
    }

    /**
     * Update product quantity
     *
     * @param userId      ID of the user (owner or manager)
     * @param storeId     ID of the store
     * @param productId   ID of the product
     * @param newQuantity New quantity
     * @return true if successful, false otherwise
     */
    public boolean updateProductQuantity(String userId, String storeId, String productId, int newQuantity) {
        // Check if user has permission
        if (!checkPermission(userId, storeId, PERM_UPDATE_PRODUCT)) {
            return false; // No permission
        }

        Store store = getStoreById(storeId);
        if (store == null) {
            return false;
        }
        if(store.updateProductQuantity(productId, newQuantity)) {
            storeRepository.update(store);
            return true;
        }
        return false;
    }
}
