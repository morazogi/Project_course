package ServiceLayer;

import DomainLayer.MicroServices.*;


import java.util.List;
import java.util.Map;

/**
 * Service layer for owner and manager operations
 * This class implements the requirements for store owners and managers
 */
public class OwnerManagerService {

    // Microservices that will be used
    private final InventoryManagementMicroservice inventoryService;
    private final PurchasePolicyMicroservice purchasePolicyService;
    private final DiscountPolicyMicroservice discountPolicyService;
    private final StoreManagementMicroservice storeManagementService;
    private final NotificationMicroservice notificationService;
    private final PurchaseHistoryMicroservice purchaseHistoryService;

    public OwnerManagerService() {
        // Initialize microservices
        this.inventoryService = new InventoryManagementMicroservice();
        this.purchasePolicyService = new PurchasePolicyMicroservice();
        this.discountPolicyService = new DiscountPolicyMicroservice();
        this.storeManagementService = new StoreManagementMicroservice();
        this.notificationService = new NotificationMicroservice();
        this.purchaseHistoryService = new PurchaseHistoryMicroservice();
    }

    // ==================== 1. Inventory Management Functions ====================

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
        return inventoryService.addProduct(ownerId, storeId, productName, description, price, quantity, category);
    }

    /**
     * Remove a product from the store inventory
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param productId ID of the product to remove
     * @return true if successful, false otherwise
     */
    public boolean removeProduct(String ownerId, String storeId, String productId) {
        return inventoryService.removeProduct(ownerId, storeId, productId);
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
        return inventoryService.updateProductDetails(ownerId, storeId, productId, productName, description, price, category);
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
        return inventoryService.updateProductQuantity(ownerId, storeId, productId, newQuantity);
    }

    // ==================== 2. Purchase and Discount Policy Functions ====================

    /**
     * Define a new purchase policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param policyType Type of policy (e.g., "MinAge", "MaxQuantity")
     * @param policyParams Parameters for the policy
     * @return Policy ID if successful, null otherwise
     */
    public String definePurchasePolicy(String ownerId, String storeId, String policyType, Map<String, Object> policyParams) {
        return purchasePolicyService.definePurchasePolicy(ownerId, storeId, policyType, policyParams);
    }

    /**
     * Update an existing purchase policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param policyId ID of the policy to update
     * @param policyParams New parameters for the policy
     * @return true if successful, false otherwise
     */
    public boolean updatePurchasePolicy(String ownerId, String storeId, String policyId, Map<String, Object> policyParams) {
        return purchasePolicyService.updatePurchasePolicy(ownerId, storeId, policyId, policyParams);
    }

    /**
     * Remove a purchase policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param policyId ID of the policy to remove
     * @return true if successful, false otherwise
     */
    public boolean removePurchasePolicy(String ownerId, String storeId, String policyId) {
        return purchasePolicyService.removePurchasePolicy(ownerId, storeId, policyId);
    }

    /**
     * Define a new discount policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param discountType Type of discount (e.g., "Percentage", "Fixed")
     * @param discountParams Parameters for the discount
     * @return Discount ID if successful, null otherwise
     */
    public String defineDiscountPolicy(String ownerId, String storeId, String discountType, Map<String, Object> discountParams) {
        return discountPolicyService.defineDiscountPolicy(ownerId, storeId, discountType, discountParams);
    }

    /**
     * Update an existing discount policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param discountId ID of the discount to update
     * @param discountParams New parameters for the discount
     * @return true if successful, false otherwise
     */
    public boolean updateDiscountPolicy(String ownerId, String storeId, String discountId, Map<String, Object> discountParams) {
        return discountPolicyService.updateDiscountPolicy(ownerId, storeId, discountId, discountParams);
    }

    /**
     * Remove a discount policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param discountId ID of the discount to remove
     * @return true if successful, false otherwise
     */
    public boolean removeDiscountPolicy(String ownerId, String storeId, String discountId) {
        return discountPolicyService.removeDiscountPolicy(ownerId, storeId, discountId);
    }

    // ==================== 3. Owner Appointment Functions ====================

    /**
     * Appoint a registered user as a store owner
     * @param appointerId ID of the appointing owner
     * @param storeId ID of the store
     * @param userId ID of the user to appoint
     * @return true if successful, false otherwise
     */
    public boolean appointStoreOwner(String appointerId, String storeId, String userId) {
        return storeManagementService.appointStoreOwner(appointerId, storeId, userId);
    }

    /**
     * Accept or reject an owner appointment
     * @param userId ID of the user receiving the appointment
     * @param appointmentId ID of the appointment
     * @param accept true to accept, false to reject
     * @return true if successful, false otherwise
     */
    public boolean respondToOwnerAppointment(String userId, String appointmentId, boolean accept) {
        return storeManagementService.respondToOwnerAppointment(userId, appointmentId, accept);
    }

    // ==================== 4. Owner Removal Functions ====================

    /**
     * Remove a store owner
     * @param removerId ID of the removing owner
     * @param storeId ID of the store
     * @param ownerId ID of the owner to remove
     * @return true if successful, false otherwise
     */
    public boolean removeStoreOwner(String removerId, String storeId, String ownerId) {
        return storeManagementService.removeStoreOwner(removerId, storeId, ownerId);
    }

    // ==================== 5. Ownership Relinquishment Functions ====================

    /**
     * Relinquish ownership of a store
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    public boolean relinquishOwnership(String ownerId, String storeId) {
        return storeManagementService.relinquishOwnership(ownerId, storeId);
    }

    // ==================== 6. Manager Appointment Functions ====================

    /**
     * Appoint a registered user as a store manager
     * @param appointerId ID of the appointing owner
     * @param storeId ID of the store
     * @param userId ID of the user to appoint
     * @param permissions Array of permissions (view, edit inventory, edit policies, etc.)
     * @return true if successful, false otherwise
     */
    public boolean appointStoreManager(String appointerId, String storeId, String userId, boolean[] permissions) {
        return storeManagementService.appointStoreManager(appointerId, storeId, userId, permissions);
    }

    /**
     * Accept or reject a manager appointment
     * @param userId ID of the user receiving the appointment
     * @param appointmentId ID of the appointment
     * @param accept true to accept, false to reject
     * @return true if successful, false otherwise
     */
    public boolean respondToManagerAppointment(String userId, String appointmentId, boolean accept) {
        return storeManagementService.respondToManagerAppointment(userId, appointmentId, accept);
    }

    // ==================== 7. Manager Permissions Functions ====================

    /**
     * Update manager permissions
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @param managerId ID of the manager
     * @param permissions Array of new permissions
     * @return true if successful, false otherwise
     */
    public boolean updateManagerPermissions(String ownerId, String storeId, String managerId, boolean[] permissions) {
        return storeManagementService.updateManagerPermissions(ownerId, storeId, managerId, permissions);
    }

    // ==================== 8. Manager Removal Functions ====================

    /**
     * Remove a store manager
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @param managerId ID of the manager to remove
     * @return true if successful, false otherwise
     */
    public boolean removeStoreManager(String ownerId, String storeId, String managerId) {
        return storeManagementService.removeStoreManager(ownerId, storeId, managerId);
    }

    // ==================== 9. Store Closure Functions ====================

    /**
     * Close a store
     * @param founderId ID of the store founder
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    public boolean closeStore(String founderId, String storeId) {
        return storeManagementService.closeStore(founderId, storeId);
    }

    // ==================== 10. Store Reopening Functions ====================

    /**
     * Reopen a closed store
     * @param founderId ID of the store founder
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    public boolean reopenStore(String founderId, String storeId) {
        return storeManagementService.reopenStore(founderId, storeId);
    }

    // ==================== 11. Store Role Information Functions ====================

    /**
     * Get information about store roles
     * @param ownerId ID of the requesting owner
     * @param storeId ID of the store
     * @return Map of role information if successful, null otherwise
     */
    public Map<String, Object> getStoreRoleInfo(String ownerId, String storeId) {
        return storeManagementService.getStoreRoleInfo(ownerId, storeId);
    }

    /**
     * Get manager permissions
     * @param ownerId ID of the requesting owner
     * @param storeId ID of the store
     * @param managerId ID of the manager
     * @return Array of permissions if successful, null otherwise
     */
    public boolean[] getManagerPermissions(String ownerId, String storeId, String managerId) {
        return storeManagementService.getManagerPermissions(ownerId, storeId, managerId);
    }

    // ==================== 12. Customer Inquiry Functions ====================

    /**
     * Get customer inquiries for a store
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @return List of inquiries if successful, null otherwise
     */
    public List<Map<String, Object>> getCustomerInquiries(String ownerId, String storeId) {
        return notificationService.getCustomerInquiries(ownerId, storeId);
    }

    /**
     * Respond to a customer inquiry
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @param inquiryId ID of the inquiry
     * @param response Response text
     * @return true if successful, false otherwise
     */
    public boolean respondToCustomerInquiry(String ownerId, String storeId, String inquiryId, String response) {
        return notificationService.respondToCustomerInquiry(ownerId, storeId, inquiryId, response);
    }

    // ==================== 13. Purchase History Functions ====================

    /**
     * Get purchase history for a store
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @param startDate Start date for the history (null for all time)
     * @param endDate End date for the history (null for current date)
     * @return List of purchase records if successful, null otherwise
     */
    public List<Map<String, Object>> getStorePurchaseHistory(String ownerId, String storeId, String startDate, String endDate) {
        return purchaseHistoryService.getStorePurchaseHistory(ownerId, storeId, startDate, endDate);
    }

    // ==================== Manager Functions ====================

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
        return inventoryService.managerAddProduct(managerId, storeId, productName, description, price, quantity, category);
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
        return inventoryService.managerUpdateProductDetails(managerId, storeId, productId, productName, description, price, category);
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
        return inventoryService.managerUpdateProductQuantity(managerId, storeId, productId, newQuantity);
    }

    /**
     * Manager function to remove a product (if permitted)
     * @param managerId ID of the manager
     * @param storeId ID of the store
     * @param productId ID of the product to remove
     * @return true if successful, false otherwise
     */
    public boolean managerRemoveProduct(String managerId, String storeId, String productId) {
        return inventoryService.managerRemoveProduct(managerId, storeId, productId);
    }
}
