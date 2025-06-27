package ServiceLayer;


import DomainLayer.*;
import DomainLayer.DomainServices.*;
import InfrastructureLayer.*;

import DomainLayer.DomainServices.InventoryManagementMicroservice;
import DomainLayer.DomainServices.PurchasePolicyMicroservice;
import InfrastructureLayer.CustomerInquiryRepository;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Service layer for owner and manager operations
 * This class implements the requirements for store owners and managers
 */
@Service
public class OwnerManagerService {

    // Microservices that will be used
    private final InventoryManagementMicroservice inventoryService;
    private final PurchasePolicyMicroservice purchasePolicyService;
    private final DiscountPolicyMicroservice discountPolicyService;
    private final StoreManagementMicroservice storeManagementService;
    private final QueryMicroservice notificationService;
    private final PurchaseHistoryMicroservice purchaseHistoryService;

    public OwnerManagerService(InfrastructureLayer.UserRepository userRepository, StoreRepository storeRepository, InfrastructureLayer.ProductRepository productRepository, InfrastructureLayer.OrderRepository orderRepository, InfrastructureLayer.DiscountRepository discountRepository) {
        // Initialize repositories
        ICustomerInquiryRepository inquiryRepository = new CustomerInquiryRepository();

        // Initialize existing microservices
        this.inventoryService = new InventoryManagementMicroservice(storeRepository, productRepository);
        this.purchasePolicyService = new PurchasePolicyMicroservice();
        this.discountPolicyService = new DiscountPolicyMicroservice(storeRepository,userRepository, productRepository, discountRepository);
        this.storeManagementService = new StoreManagementMicroservice(storeRepository, userRepository);
        this.notificationService = new QueryMicroservice(inquiryRepository);
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
    @Transactional
    public String addProduct(String ownerId, String storeId, String productName, String description, float price, int quantity, String category) {
        try {
            EventLogger.logEvent(ownerId, "ADD_PRODUCT_START");
            inventoryService.addProduct(ownerId, storeId, productName, description, (float) price, quantity, category);
            EventLogger.logEvent(ownerId, "ADD_PRODUCT_SUCCESS");
            return "Added product to store";
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "ADD_PRODUCT_FAILED", e.getMessage());
            return "Failed to add product to store" + e.getMessage();
        }
    }

    /**
     * Remove a product from the store inventory
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param productId ID of the product to remove
     */
    @Transactional
    public String removeProduct(String ownerId, String storeId, String productId) {
        try {
            EventLogger.logEvent(ownerId, "REMOVE_PRODUCT_START");
            boolean resul = inventoryService.removeProduct(ownerId, storeId, productId);
            EventLogger.logEvent(ownerId, "REMOVE_PRODUCT_SUCCESS");
            if (resul) {
                return "Removed product";
            } else {
                return "Failed to remove product";
            }
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "REMOVE_PRODUCT_FAILED", e.getMessage());
            return "Failed to remove product " + e.getMessage();
        }
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
     */
    @Transactional
    public String updateProductDetails(String ownerId, String storeId, String productId, String productName, String description, double price, String category) {
        try {
            EventLogger.logEvent(ownerId, "UPDATE_PRODUCT_DETAILS_START");
            boolean result = inventoryService.updateProductDetails(ownerId, storeId, productId, productName, description, price, category);
            EventLogger.logEvent(ownerId, "UPDATE_PRODUCT_DETAILS_SUCCESS");
            if (result) {
                return "Updated product details";
            }
            return "Failed to update product details";
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "UPDATE_PRODUCT_DETAILS_FAILED", e.getMessage());
            return "Return to update product details " + e.getMessage();
        }
    }

    /**
     * Update product quantity
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param productId ID of the product
     * @param newQuantity New quantity
     */
    @Transactional
    public String updateProductQuantity(String ownerId, String storeId, String productId, int newQuantity) {
        try {
            EventLogger.logEvent(ownerId, "UPDATE_PRODUCT_QUANTITY_START");
            boolean result = inventoryService.updateProductQuantity(ownerId, storeId, productId, newQuantity);
            EventLogger.logEvent(ownerId, "UPDATE_PRODUCT_QUANTITY_SUCCESS");
            if (result) {
                return "Updated product quantity";
            }
            return "Failed to update product quantity";
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "UPDATE_PRODUCT_QUANTITY_FAILED", e.getMessage());
            return "Failed to update product quantity";
        }
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
    @Transactional
    public String definePurchasePolicy(String ownerId, String storeId, String policyType, Map<String, Object> policyParams) {
        try {
            EventLogger.logEvent(ownerId, "DEFINE_PURCHASE_POLICY_START");
            String result = purchasePolicyService.definePurchasePolicy(ownerId, storeId, policyType, policyParams);
            EventLogger.logEvent(ownerId, "DEFINE_PURCHASE_POLICY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "DEFINE_PURCHASE_POLICY_FAILED", e.getMessage());
            return null;
        }
    }

    /**
     * Update an existing purchase policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param policyId ID of the policy to update
     * @param policyParams New parameters for the policy
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean updatePurchasePolicy(String ownerId, String storeId, String policyId, Map<String, Object> policyParams) {
        try {
            EventLogger.logEvent(ownerId, "UPDATE_PURCHASE_POLICY_START");
            boolean result = purchasePolicyService.updatePurchasePolicy(ownerId, storeId, policyId, policyParams);
            EventLogger.logEvent(ownerId, "UPDATE_PURCHASE_POLICY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "UPDATE_PURCHASE_POLICY_FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * Remove a purchase policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param policyId ID of the policy to remove
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean removePurchasePolicy(String ownerId, String storeId, String policyId) {
        try {
            EventLogger.logEvent(ownerId, "REMOVE_PURCHASE_POLICY_START");
            boolean result = purchasePolicyService.removePurchasePolicy(ownerId, storeId, policyId);
            EventLogger.logEvent(ownerId, "REMOVE_PURCHASE_POLICY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "REMOVE_PURCHASE_POLICY_FAILED", e.getMessage());
            return false;
        }
    }
    @Transactional
    public boolean defineDiscountPolicy(String ownerId, String storeId, String discountId,
                                        String Id,
                                        float level,
                                        float logicComposition,
                                        float numericalComposition,
                                        List<String> discountsId,
                                        float percentDiscount,
                                        String discounted,
                                        float conditional,
                                        float limiter,
                                        String conditionalDiscounted) {
        try {
            EventLogger.logEvent(ownerId, "DEFINE_DISCOUNT_POLICY_START");
            boolean result = discountPolicyService.addDiscountToDiscountPolicy(ownerId,storeId,discountId,
                                                                                level,
                                                                                logicComposition,
                                                                                numericalComposition,
                                                                                discountsId,
                                                                                percentDiscount,
                                                                                discounted,
                                                                                conditional,
                                                                                limiter,
                                                                                conditionalDiscounted);
            EventLogger.logEvent(ownerId, "DEFINE_DISCOUNT_POLICY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "DEFINE_DISCOUNT_POLICY_FAILED", e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean removeDiscountFromDiscountPolicy(String ownerId, String storeId, String discountId) {
        try {
            EventLogger.logEvent(ownerId, "UPDATE_DISCOUNT_POLICY_START");
            boolean result = discountPolicyService.removeDiscountFromDiscountPolicy(ownerId, storeId, discountId);
            EventLogger.logEvent(ownerId, "UPDATE_DISCOUNT_POLICY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "UPDATE_DISCOUNT_POLICY_FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * Remove a discount policy
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean removeDiscountPolicy(String ownerId, String storeId) {
        try {
            EventLogger.logEvent(ownerId, "REMOVE_DISCOUNT_POLICY_START");
            boolean result = discountPolicyService.removeDiscountPolicy(ownerId, storeId);
            EventLogger.logEvent(ownerId, "REMOVE_DISCOUNT_POLICY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "REMOVE_DISCOUNT_POLICY_FAILED", e.getMessage());
            return false;
        }
    }

    // ==================== 3. Owner Appointment Functions ====================

    /**
     * Appoint a registered user as a store owner
     * @param appointerId ID of the appointing owner
     * @param storeId ID of the store
     * @param userId ID of the user to appoint
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean appointStoreOwner(String appointerId, String storeId, String userId) {
        try {
            EventLogger.logEvent(appointerId, "APPOINT_STORE_OWNER_START");
            boolean result = storeManagementService.appointStoreOwner(appointerId, storeId, userId);
            EventLogger.logEvent(appointerId, "APPOINT_STORE_OWNER_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(appointerId, "APPOINT_STORE_OWNER_FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * Sends an ownership proposal to a specific user for a given store. The proposal outlines
     * the details of the ownership request.
     *
     * @param userId The unique identifier of the user to whom the proposal is being sent.
     * @param storeId The unique identifier of the store for which the ownership is being proposed.
     * @param proposalText The text containing the details of the ownership proposal.
     */
    @Transactional
    public void sendOwnershipProposal(String userId, String storeId, String proposalText) {
        try {
            EventLogger.logEvent(userId, "SEND_OWNERSHIP_PROPOSAL_START");
            String proposal = storeManagementService.sendOwnershipProposal(userId, storeId, proposalText);
            EventLogger.logEvent(userId, "SEND_OWNERSHIP_PROPOSAL_SUCCESS");
        } catch (Exception e) {
            ErrorLogger.logError(userId, "SEND_OWNERSHIP_PROPOSAL_FAILED", e.getMessage());
        }
    }

    /**
     * Accept or reject an owner appointment
     * @param userId ID of the user receiving the appointment
     * @param storeID ID of the appointment
     * @param accept true to accept, false to reject
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean respondToOwnerAppointment(String userId, String storeID, boolean accept) {
        try {
            EventLogger.logEvent(userId, "RESPOND_TO_OWNER_APPOINTMENT_START");
            boolean result = storeManagementService.respondToOwnerAppointment(userId, storeID, accept);
            EventLogger.logEvent(userId, "RESPOND_TO_OWNER_APPOINTMENT_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(userId, "RESPOND_TO_OWNER_APPOINTMENT_FAILED", e.getMessage());
            return false;
        }
    }


    // ==================== 4. Owner Removal Functions ====================
    /**
     * Remove a store owner
     * @param removerId ID of the removing owner
     * @param storeId ID of the store
     * @param ownerId ID of the owner to remove
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean removeStoreOwner(String removerId, String storeId, String ownerId) {
        try {
            EventLogger.logEvent(removerId, "REMOVE_STORE_OWNER_START");
            boolean result = storeManagementService.removeStoreOwner(removerId, storeId, ownerId);
            EventLogger.logEvent(removerId, "REMOVE_STORE_OWNER_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(removerId, "REMOVE_STORE_OWNER_FAILED", e.getMessage());
            return false;
        }
    }

    // ==================== 5. Ownership Relinquishment Functions ====================

    /**
     * Relinquish ownership of a store
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean relinquishOwnership(String ownerId, String storeId) {
        try {
            EventLogger.logEvent(ownerId, "RELINQUISH_OWNERSHIP_START");
            boolean result = storeManagementService.relinquishOwnership(ownerId, storeId);
            EventLogger.logEvent(ownerId, "RELINQUISH_OWNERSHIP_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "RELINQUISH_OWNERSHIP_FAILED", e.getMessage());
            return false;
        }
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
    @Transactional
    public boolean appointStoreManager(String appointerId, String storeId, String userId, boolean[] permissions) {
        try {
            EventLogger.logEvent(appointerId, "APPOINT_STORE_MANAGER_START");

            boolean result = storeManagementService.appointStoreManager(appointerId, storeId, userId, permissions);
            Map<String, Boolean> perms = getManagerPermissions(userId, storeId, userId);
            System.out.println("After appointment, perms = " + perms);
            EventLogger.logEvent(appointerId, "APPOINT_STORE_MANAGER_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(appointerId, "APPOINT_STORE_MANAGER_FAILED", e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean setFounder(String founderId, String storeId) {
        try {
            EventLogger.logEvent(founderId, "STORE_FOUNDER");
            boolean result = storeManagementService.appointStoreFounder(founderId, storeId);
            EventLogger.logEvent(founderId, "STORE_FOUNDER_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(founderId, "STORE_FOUNDER_FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * Sends a management proposal to the specified store on behalf of the user.
     *
     * @param userId the unique identifier of the user sending the proposal
     * @param storeId the unique identifier of the store to receive the proposal
     * @param proposalText the content of the management proposal being sent
     */
    @Transactional
    public void sendManagementProposal(String userId, String storeId, String proposalText) {
        try {
            EventLogger.logEvent(userId, "SEND_MANAGEMENT_PROPOSAL_START");
            storeManagementService.sendManagementProposal(userId, storeId, proposalText);
            EventLogger.logEvent(userId, "SEND_MANAGEMENT_PROPOSAL_SUCCESS");
        } catch (Exception e) {
            ErrorLogger.logError(userId, "SEND_MANAGEMENT_PROPOSAL_FAILED", e.getMessage());
        }
    }

    /**
     * Accept or reject a manager appointment
     * @param userId ID of the user receiving the appointment
     * @param appointmentId ID of the appointment
     * @param accept true to accept, false to reject
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean respondToManagerAppointment(String userId, String appointmentId, boolean accept) {
        try {
            EventLogger.logEvent(userId, "RESPOND_TO_MANAGER_APPOINTMENT_START");
            boolean result = storeManagementService.respondToManagerAppointment(userId, appointmentId, accept);
            EventLogger.logEvent(userId, "RESPOND_TO_MANAGER_APPOINTMENT_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(userId, "RESPOND_TO_MANAGER_APPOINTMENT_FAILED", e.getMessage());
            return false;
        }
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
    @Transactional
    public boolean updateManagerPermissions(String ownerId, String storeId, String managerId, boolean[] permissions) {
        try {
            EventLogger.logEvent(ownerId, "UPDATE_MANAGER_PERMISSIONS_START");
            boolean result = storeManagementService.updateManagerPermissions(ownerId, storeId, managerId, permissions);
            EventLogger.logEvent(ownerId, "UPDATE_MANAGER_PERMISSIONS_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "UPDATE_MANAGER_PERMISSIONS_FAILED", e.getMessage());
            return false;
        }
    }

    // ==================== 8. Manager Removal Functions ====================

    /**
     * Remove a store manager
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @param managerId ID of the manager to remove
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean removeStoreManager(String ownerId, String storeId, String managerId) {
        try {
            EventLogger.logEvent(ownerId, "REMOVE_STORE_MANAGER_START");
            boolean result = storeManagementService.removeStoreManager(ownerId, storeId, managerId);
            EventLogger.logEvent(ownerId, "REMOVE_STORE_MANAGER_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "REMOVE_STORE_MANAGER_FAILED", e.getMessage());
            return false;
        }
    }
    @Transactional
    public boolean relinquishManagement(String managerID, String storeId) {
        try {
            EventLogger.logEvent(managerID, "RELINQUISH_MANAGEMENT_START");
            boolean result = storeManagementService.relinquishManagement(managerID, storeId);
            EventLogger.logEvent(managerID, "RELINQUISH_MANAGEMENT_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(managerID, "RELINQUISH_MANAGEMENT_FAILED", e.getMessage());
            return false;
        }
    }


        // ==================== 9. Store Closure Functions ====================

    /**
     * Close a store
     * @param founderId ID of the store founder
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    @Transactional
    public String closeStore(String founderId, String storeId) {
        try {
            EventLogger.logEvent(founderId, "CLOSE_STORE_START");
            boolean result = storeManagementService.closeStore(founderId, storeId);
            EventLogger.logEvent(founderId, "CLOSE_STORE_SUCCESS");
            if (result) {
                return "Closed store";
            }
            return "Failed to close store";
        } catch (Exception e) {
            ErrorLogger.logError(founderId, "CLOSE_STORE_FAILED", e.getMessage());
            return "Failed to close store" + e.getMessage();
        }
    }

    // ==================== 10. Store Reopening Functions ====================

    /**
     * Reopen a closed store
     * @param founderId ID of the store founder
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    @Transactional
    public String reopenStore(String founderId, String storeId) {
        try {
            EventLogger.logEvent(founderId, "REOPEN_STORE_START");
            boolean result = storeManagementService.reopenStore(founderId, storeId);
            EventLogger.logEvent(founderId, "REOPEN_STORE_SUCCESS");
            if (result) {
                return "Opened store";
            }
            return "Failed to open store";
        } catch (Exception e) {
            ErrorLogger.logError(founderId, "REOPEN_STORE_FAILED", e.getMessage());
            return "Failed to open store" + e.getMessage();
        }
    }

    // ==================== 11. Store Role Information Functions ====================

    /**
     * Get information about store roles
     * @param ownerId ID of the requesting owner
     * @param storeId ID of the store
     * @return Map of role information if successful, null otherwise
     */
    @Transactional
    public String getStoreRoleInfo(String ownerId, String storeId) {
        try {
            EventLogger.logEvent(ownerId, "GET_STORE_ROLE_INFO_START");
            String result = storeManagementService.getStoreRoleInfo(ownerId, storeId);
            EventLogger.logEvent(ownerId, "GET_STORE_ROLE_INFO_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "GET_STORE_ROLE_INFO_FAILED", e.getMessage());
            return null;
        }
    }

    /**
     * Get manager permissions
     * @param ownerId ID of the requesting owner
     * @param storeId ID of the store
     * @param managerId ID of the manager
     * @return Array of permissions if successful, null otherwise
     */
    @Transactional
    public Map<String, Boolean> getManagerPermissions(String ownerId, String storeId, String managerId) {
        try {
            EventLogger.logEvent(ownerId, "GET_MANAGER_PERMISSIONS_START");
            Map<String, Boolean> result = storeManagementService.getManagerPermissions(ownerId, storeId, managerId);
            EventLogger.logEvent(ownerId, "GET_MANAGER_PERMISSIONS_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "GET_MANAGER_PERMISSIONS_FAILED", e.getMessage());
            return null;
        }
    }

    // ==================== 12. Customer Inquiry Functions ====================

    /**
     * Get customer inquiries for a store
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @return List of inquiries if successful, null otherwise
     */
    @Transactional
    public List<Map<String, Object>> getCustomerInquiries(String ownerId, String storeId) {
        try {
            EventLogger.logEvent(ownerId, "GET_CUSTOMER_INQUIRIES_START");
            List<Map<String, Object>> result = notificationService.getCustomerInquiries(ownerId, storeId);
            EventLogger.logEvent(ownerId, "GET_CUSTOMER_INQUIRIES_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "GET_CUSTOMER_INQUIRIES_FAILED", e.getMessage());
            return null;
        }
    }

    /**
     * Respond to a customer inquiry
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @param inquiryId ID of the inquiry
     * @param response Response text
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean respondToCustomerInquiry(String ownerId, String storeId, String inquiryId, String response) {
        try {
            EventLogger.logEvent(ownerId, "RESPOND_TO_CUSTOMER_INQUIRY_START");
            boolean result = notificationService.respondToCustomerInquiry(ownerId, storeId, inquiryId, response);
            EventLogger.logEvent(ownerId, "RESPOND_TO_CUSTOMER_INQUIRY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "RESPOND_TO_CUSTOMER_INQUIRY_FAILED", e.getMessage());
            return false;
        }
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
    @Transactional
    public List<String> getStorePurchaseHistory(String ownerId, String storeId, Date startDate, Date endDate) {
        try {
            EventLogger.logEvent(ownerId, "GET_STORE_PURCHASE_HISTORY_START");
            List<String> result = purchaseHistoryService.getStorePurchaseHistory(ownerId, storeId, startDate, endDate);
            EventLogger.logEvent(ownerId, "GET_STORE_PURCHASE_HISTORY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(ownerId, "GET_STORE_PURCHASE_HISTORY_FAILED", e.getMessage());
            return null;
        }
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
    @Transactional
    public String managerAddProduct(String managerId, String storeId, String productName, String description, float price, int quantity, String category) {
        try {
            EventLogger.logEvent(managerId, "MANAGER_ADD_PRODUCT_START");
            String result = inventoryService.addProduct(managerId, storeId, productName, description, (float) price, quantity, category);
            EventLogger.logEvent(managerId, "MANAGER_ADD_PRODUCT_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(managerId, "MANAGER_ADD_PRODUCT_FAILED", e.getMessage());
            return null;
        }
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
    @Transactional
    public boolean managerUpdateProductDetails(String managerId, String storeId, String productId, String productName, String description, double price, String category) {
        try {
            EventLogger.logEvent(managerId, "MANAGER_UPDATE_PRODUCT_DETAILS_START");
            boolean result = inventoryService.updateProductDetails(managerId, storeId, productId, productName, description, price, category);
            EventLogger.logEvent(managerId, "MANAGER_UPDATE_PRODUCT_DETAILS_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(managerId, "MANAGER_UPDATE_PRODUCT_DETAILS_FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * Manager function to update product quantity (if permitted)
     * @param managerId ID of the manager
     * @param storeId ID of the store
     * @param productId ID of the product
     * @param newQuantity New quantity
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean managerUpdateProductQuantity(String managerId, String storeId, String productId, int newQuantity) {
        try {
            EventLogger.logEvent(managerId, "MANAGER_UPDATE_PRODUCT_QUANTITY_START");
            boolean result = inventoryService.updateProductQuantity(managerId, storeId, productId, newQuantity);
            EventLogger.logEvent(managerId, "MANAGER_UPDATE_PRODUCT_QUANTITY_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(managerId, "MANAGER_UPDATE_PRODUCT_QUANTITY_FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * Manager function to remove a product (if permitted)
     * @param managerId ID of the manager
     * @param storeId ID of the store
     * @param productId ID of the product to remove
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean managerRemoveProduct(String managerId, String storeId, String productId) {
        try {
            EventLogger.logEvent(managerId, "MANAGER_REMOVE_PRODUCT_START");
            boolean result = inventoryService.removeProduct(managerId, storeId, productId);
            EventLogger.logEvent(managerId, "MANAGER_REMOVE_PRODUCT_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(managerId, "MANAGER_REMOVE_PRODUCT_FAILED", e.getMessage());
            return false;
        }
    }


    @Transactional
    public boolean hasInventoryPermission(String userId, String storeId) {

        /* ① founder / owner → always yes */
        if (storeManagementService.isFounderOrOwner(userId, storeId))
            return true;

        /* ② manager → check permission map */
        Map<String, Boolean> perms =
                storeManagementService.getManagerPermissions(userId, storeId, userId);

        if (perms == null) return false;
        Boolean allowed = perms.get(ManagerPermissions.PERM_MANAGE_INVENTORY);
        return allowed != null && allowed;
    }
    public boolean isFounderOrOwner(String userId, String storeId) {
        return storeManagementService.isFounderOrOwner(userId, storeId);
    }
}
