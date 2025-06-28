package ServiceLayer;
//import DomainLayer.EventLogger;
import DomainLayer.DomainServices.StoreManagementMicroservice;
import InfrastructureLayer.RolesRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service
public class RolesService {

    private final RolesRepository rolesRepository;

    public RolesService(StoreRepository storeRepository, UserRepository userRepository) {
        this.rolesRepository = new RolesRepository(new StoreManagementMicroservice(storeRepository, userRepository));
    }

    @Transactional
    public boolean appointStoreOwner(String appointerId, String storeId, String userId) {
        try {
            boolean result = rolesRepository.appointStoreOwner(appointerId, storeId, userId);
            EventLogger.logEvent(appointerId, "APPOINT_OWNER for store " + storeId);
            return result;
        } catch (Exception e) {
            EventLogger.logEvent(appointerId, "APPOINT_OWNER_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to appoint store owner");
        }
    }

    @Transactional
    public boolean removeStoreOwner(String removerId, String storeId, String ownerId) {
        try {
            boolean result = rolesRepository.removeStoreOwner(removerId, storeId, ownerId);
            EventLogger.logEvent(removerId, "REMOVE_OWNER from store " + storeId);
            return result;
        } catch (Exception e) {
            EventLogger.logEvent(removerId, "REMOVE_OWNER_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to remove store owner");
        }
    }

    @Transactional
    public boolean appointStoreManager(String appointerId, String storeId, String userId, boolean[] permissions) {
        try {
            boolean result = rolesRepository.appointStoreManager(appointerId, storeId, userId, permissions);
            EventLogger.logEvent(appointerId, "APPOINT_MANAGER for store " + storeId);
            return result;
        } catch (Exception e) {
            EventLogger.logEvent(appointerId, "APPOINT_MANAGER_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to appoint store manager" + e.getMessage());
        }
    }

    @Transactional
    public boolean removeStoreManager(String removerId, String storeId, String managerId) {
        try {
            boolean result = rolesRepository.removeStoreManager(removerId, storeId, managerId);
            EventLogger.logEvent(removerId, "REMOVE_MANAGER from store " + storeId);
            return result;
        } catch (Exception e) {
            EventLogger.logEvent(removerId, "REMOVE_MANAGER_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to remove store manager");
        }
    }

    @Transactional
    public boolean updateManagerPermissions(String ownerId, String storeId, String managerId, boolean[] permissions) {
        try {
            boolean result = rolesRepository.updateManagerPermissions(ownerId, storeId, managerId, permissions);
            EventLogger.logEvent(ownerId, "UPDATE_MANAGER_PERMISSIONS for store " + storeId);
            return result;
        } catch (Exception e) {
            EventLogger.logEvent(ownerId, "UPDATE_MANAGER_PERMISSIONS_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to update manager permissions");
        }
    }

    @Transactional
    public boolean relinquishOwnership(String ownerId, String storeId) {
        try {
            boolean result = rolesRepository.relinquishOwnership(ownerId, storeId);
            EventLogger.logEvent(ownerId, "RELINQUISH_OWNERSHIP for store " + storeId);
            return result;
        } catch (Exception e) {
            EventLogger.logEvent(ownerId, "RELINQUISH_OWNERSHIP_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to relinquish ownership");
        }
    }

    @Transactional
    public boolean relinquishManagement(String managerId, String storeId) {
        try {
            boolean result = rolesRepository.relinquishManagement(managerId, storeId);
            EventLogger.logEvent(managerId, "RELINQUISH_MANAGEMENT for store " + storeId);
            return result;
        } catch (Exception e) {
            EventLogger.logEvent(managerId, "RELINQUISH_MANAGEMENT_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to relinquish management");
        }
    }

    @Transactional
    public Map<String, Boolean> getManagerPermissions(String ownerId, String storeId, String managerId) {
        try {
            return rolesRepository.getManagerPermissions(ownerId, storeId, managerId);
        } catch (Exception e) {
            EventLogger.logEvent(ownerId, "GET_MANAGER_PERMISSIONS_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to get manager permissions");
        }
    }

    @Transactional
    public String getStoreRoleInfo(String requesterId, String storeId) {
        try {
            return rolesRepository.getStoreRoleInfo(requesterId, storeId);
        } catch (Exception e) {
            EventLogger.logEvent(requesterId, "GET_ROLE_INFO_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to retrieve store role info");
        }
    }

    public boolean isFounderOrOwner(String userId, String storeId) {
        try {
            return rolesRepository.isFounderOrOwner(userId, storeId);
        } catch (Exception e) {
            EventLogger.logEvent(userId, "IS_FOUNDER_OR_OWNER_FAILED " + e.getMessage());
            return false;
        }
    }
}