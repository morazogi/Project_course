package InfrastructureLayer;


import DomainLayer.DomainServices.StoreManagementMicroservice;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.Map;

@Repository
public class RolesRepository {
    private final StoreManagementMicroservice storeManagementMicroservice;

    public RolesRepository(StoreManagementMicroservice storeManagementMicroservice) {
        this.storeManagementMicroservice = storeManagementMicroservice;
    }

    public boolean appointStoreOwner(String appointerId, String storeId, String userId) {
        return storeManagementMicroservice.appointStoreOwner(appointerId, storeId, userId);
    }

    public boolean removeStoreOwner(String removerId, String storeId, String ownerId) {
        return storeManagementMicroservice.removeStoreOwner(removerId, storeId, ownerId);
    }

    public boolean appointStoreManager(String appointerId, String storeId, String userId, boolean[] permissions) {
        return storeManagementMicroservice.appointStoreManager(appointerId, storeId, userId, permissions);
    }

    public boolean removeStoreManager(String removerId, String storeId, String managerId) {
        return storeManagementMicroservice.removeStoreManager(removerId, storeId, managerId);
    } //

    public boolean updateManagerPermissions(String ownerId, String storeId, String managerId, boolean[] permissions) {
        return storeManagementMicroservice.updateManagerPermissions(ownerId, storeId, managerId, permissions);
    }

    public boolean relinquishOwnership(String ownerId, String storeId) {
        return storeManagementMicroservice.relinquishOwnership(ownerId, storeId);
    }

    public boolean relinquishManagement(String managerId, String storeId) {
        return storeManagementMicroservice.relinquishManagerWithUserSync(managerId, storeId);
    }

    public Map<String, Boolean> getManagerPermissions(String ownerId, String storeId, String managerId) {
        return storeManagementMicroservice.getManagerPermissions(ownerId, storeId, managerId);
    }

    public String getStoreRoleInfo(String requesterId, String storeId) {
        return storeManagementMicroservice.getStoreRoleInfo(requesterId, storeId);
    }

    public boolean isFounderOrOwner(String userId, String storeId) {
        return storeManagementMicroservice.isFounderOrOwner(userId, storeId);
    }
}
