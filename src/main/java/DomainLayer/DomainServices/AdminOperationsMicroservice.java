package DomainLayer.DomainServices;

import DomainLayer.IStoreRepository;
import DomainLayer.IUserRepository;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;

/**
 * Microservice handling system administrator operations
 */
public class AdminOperationsMicroservice {
    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final QueryMicroservice notificationService;
    private final String SYSTEM_ADMIN_ROLE = "1";
    private ObjectMapper mapper = new ObjectMapper();

    public AdminOperationsMicroservice(IUserRepository userRepository, IStoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.notificationService = new QueryMicroservice();
    }

    private Store getStoreById(String storeId) {
        if (storeRepository == null) {
            return null;
        }
        try {
            Store store = mapper.readValue(storeRepository.getStore(storeId), Store.class);
            if (storeRepository.getStore(storeId) == null) {
                throw new IllegalArgumentException("Store does not exist");
            }
            return store;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Closes a store by system administrator
     * This operation:
     * 1. Closes the store
     * 2. Sends notifications to store owners and managers
     * 3. Revokes all appointments in the store
     *
     * @param adminId ID of the system administrator
     * @param storeId ID of the store to close
     * @return true if successful, false otherwise
     */
    public boolean adminCloseStore(String adminId, String storeId) {
        // Verify admin permissions
        if (!isSystemAdmin(adminId)) {
            return false;
        }

        try {
            // Get store owners and managers for notifications
            Store store = getStoreById(storeId);
            String founderID = store.getFounder();
            LinkedList<String> storeStaff = new LinkedList<>();
            storeStaff.add(founderID);
            storeStaff.addAll(store.getAllSubordinates(founderID));
            // Close the store
            if (store.closeByAdmin()) {
                // Send notifications to all store staff
                for (String staffMember : storeStaff) {
                    /*notificationService.sendNotification(staffMember,
                        "Store Closure Notice",
                        "Store " + storeId + " has been closed by system administrator.","admin"
                    );*/
                    RegisteredUser user = mapper.readValue(userRepository.getUser(staffMember), RegisteredUser.class);
                    user.getManagedStores().remove(storeId);
                    user.getOwnedStores().remove(storeId);
                }
                store.terminateOwnership(founderID);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Removes a marketplace member by system administrator
     * This operation:
     * 1. Removes the user's membership
     * 2. Revokes all their roles in the marketplace
     *
     * @param adminId ID of the system administrator
     * @param userId ID of the user to remove
     * @return true if successful, false otherwise
     */
    public boolean suspendMember(String adminId, String userId) {
        // Verify admin permissions
        if (!isSystemAdmin(adminId)) {
            return false;
        }
        try {
            // Get all stores where user has roles
            RegisteredUser user = mapper.readValue(userRepository.getUser(userId), RegisteredUser.class);
            LinkedList<String> userManagedStores = user.getManagedStores();
            LinkedList<String> userOwnedStores = user.getOwnedStores();
            // Revoke all roles in all stores
            for (String StoreID : userManagedStores) {
                getStoreById(StoreID).terminateManagment(userId);
            }
            for (String StoreID : userOwnedStores) {
                getStoreById(StoreID).terminateOwnership(userId);
            }

            // Remove user membership
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean unSuspendMember(String adminId, String userId) {
        if (!isSystemAdmin(adminId)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the given user ID belongs to a system administrator
     *
     * @param userId ID to check
     * @return true if user is system admin, false otherwise
     */
    private boolean isSystemAdmin(String userId) {
        return SYSTEM_ADMIN_ROLE.equals(userId);
    }
}