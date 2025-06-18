package ServiceLayer;

import DomainLayer.DomainServices.AdminOperationsMicroservice;
import DomainLayer.DomainServices.QueryMicroservice;
import DomainLayer.IStoreRepository;
import DomainLayer.IUserRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import jakarta.transaction.Transactional;

public class AdminService {
    // Add new microservice
    private final AdminOperationsMicroservice adminService;
    private final QueryMicroservice notificationService;
    private final TokenService tokenService;

    public AdminService(UserRepository userRepository, StoreRepository storeRepository, QueryMicroservice notificationService, TokenService tokenService) {
        this.notificationService = notificationService;
        this.tokenService = tokenService;
        // Initialize new admin microservice
        this.adminService = new AdminOperationsMicroservice(userRepository, storeRepository);
    }

    // Add new admin operations section
    // ==================== 14. System Administrator Functions ====================

    /**
     * System administrator function to close a store
     * This will notify all store owners and managers and revoke their appointments
     *
     * @param adminId ID of the system administrator
     * @param storeId ID of the store to close
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean adminCloseStore(String adminId, String storeId) {
        try {
            EventLogger.logEvent(adminId, "ADMIN_CLOSE_STORE_START");
            boolean result = adminService.adminCloseStore(adminId, storeId);
            EventLogger.logEvent(adminId, "ADMIN_CLOSE_STORE_SUCCESS");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(adminId, "ADMIN_CLOSE_STORE_FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * System administrator function to remove a marketplace member
     * This will revoke all their roles across all stores
     *
     * @param adminId ID of the system administrator
     * @param userId ID of the user to remove
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean adminSuspendMember(String adminId, String userId) {
        try {
            EventLogger.logEvent(adminId, "ADMIN_REMOVE_MEMBER_START");
            boolean result = adminService.suspendMember(adminId, userId);
            if (result) {
                    tokenService.suspendUser(userId);
                    EventLogger.logEvent(adminId, "ADMIN_REMOVE_MEMBER_SUCCESS");
                return true;
            }
            ErrorLogger.logError(adminId, "ADMIN_REMOVE_MEMBER_FAILED", "Failed to remove member");
            return false;
        } catch (Exception e) {
            ErrorLogger.logError(adminId, "ADMIN_REMOVE_MEMBER_FAILED", e.getMessage());
            return false;
        }
    }
    @Transactional
    public boolean adminUnSuspendMember(String adminId, String userId) {
        try {
            EventLogger.logEvent(adminId, "ADMIN_REMOVE_MEMBER_START");
            boolean result = adminService.unSuspendMember(adminId, userId);
            if (result) {
                tokenService.unsuspendUser(userId);
                EventLogger.logEvent(adminId, "ADMIN_REMOVE_MEMBER_SUCCESS");
                return true;
            }
            ErrorLogger.logError(adminId, "ADMIN_REMOVE_MEMBER_FAILED", "Failed to remove member");
            return false;
        } catch (Exception e) {
            ErrorLogger.logError(adminId, "ADMIN_REMOVE_MEMBER_FAILED", e.getMessage());
            return false;
        }
    }
}
