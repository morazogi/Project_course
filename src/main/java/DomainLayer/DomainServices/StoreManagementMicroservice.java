package DomainLayer.DomainServices;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import DomainLayer.Store;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import static DomainLayer.ManagerPermissions.PERM_MANAGE_STAFF;


public class StoreManagementMicroservice {
    // Add standard permission constants
    private StoreRepository storeRepository;
    private UserRepository userRepository;
    private ObjectMapper mapper = new ObjectMapper();

    public StoreManagementMicroservice(StoreRepository storeRepository,UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }
    /**
     * Set the repositories for this microservice
     * @param storeRepository Repository for stores
     * @param userRepository Repository for users
     */
    public void setRepositories(StoreRepository storeRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }
    // Helper methods to get entities from repositories
    private Store getStoreById(String storeId) {
        if (storeRepository == null) {
            return null;
        }
        Store store = storeRepository.getById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store does not exist");
        }
        return store;
    }
    private RegisteredUser getUserById(String userId) {
        if (userRepository == null) {
            return null;
        }
        RegisteredUser RegisteredUser = userRepository.getById(userId);
        if (RegisteredUser == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        return RegisteredUser;
    }
    // Add permission check method
    private boolean checkPermission(String userId, String storeId, String permissionType) {
        Store store = getStoreById(storeId);
        if (store == null) {
            return false;
        }

        // Owners have all permissions
        if (store.userIsOwner(userId) && userRepository.getById(userId).isOwnerOf(storeId)) {
            return true;
        }

        // Check if manager has specific permission
        if (store.userIsManager(userId) && userRepository.getById(userId).isManagerOf(storeId)) {
            return store.userHasPermissions(userId, permissionType);
        }

        return false;
    }
    /**
     * Appoint a user as a store owner
     * @param appointerId ID of the appointing owner
     * @param storeId ID of the store
     * @param userId ID of the user to appoint
     * @return true if successful, false otherwise
     */
    public boolean appointStoreOwner(String appointerId, String storeId, String userId) {

        if(!checkPermission(appointerId,storeId, PERM_MANAGE_STAFF)) {
            if (!(getStoreById(storeId) != null && getStoreById(storeId).getFounder().equals(userId))) {
                return false;
            }
        }
        Store store = getStoreById(storeId);
        synchronized (store) {
            store.addOwner(appointerId, userId);
            getUserById(userId).addOwnedStore(storeId);
            storeRepository.update(store);
            userRepository.update(getUserById(userId));
            return true;
        }
    }

    public String sendOwnershipProposal(String userId, String storeId, String proposalText) {
        Store store = getStoreById(storeId);
        RegisteredUser user = getUserById(userId);
        if (store == null || user == null) {
            throw new IllegalArgumentException("Store or user does not exist");
        }
        if (store.userIsOwner(userId) || store.userIsManager(userId)) {
            throw new IllegalArgumentException("User is already an owner or manager of the store");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Hi, would you like to become an owner of the store ").append(store.getId()).append("? \n");
        return sb.toString();
    }
    /**
     * Respond to an owner appointment
     * @param userId ID of the user responding
     * @param storeID ID of the appointment
     * @param accept Whether to accept the appointment
     * @return true if successful, false otherwise
     */
    public boolean respondToOwnerAppointment(String userId, String storeID, boolean accept) {
        if (accept) {
            appointStoreOwner(userId, storeID, userId);
            return true;
        } else return false;
    }
    /**
     * Remove a store owner
     * @param removerId ID of the removing owner
     * @param storeId ID of the store
     * @param ownerId ID of the owner to remove
     * @return true if successful, false otherwise
     */
    public boolean removeStoreOwner(String removerId, String storeId, String ownerId) {
        Store store = getStoreById(storeId);
        if(store.checkIfSuperior(removerId,ownerId)){
            synchronized (store) {
                LinkedList<String> firedUsers = store.getAllSubordinates(ownerId);
                store.terminateOwnership(ownerId);
                for (String s : firedUsers) {
                    RegisteredUser user = getUserById(s);
                    user.removeStore(storeId);
                    userRepository.update(user); // <-- update each fired user
                }

                RegisteredUser owner = getUserById(ownerId);
                owner.removeStore(storeId);
                userRepository.update(owner);

                storeRepository.update(store);
            }
            return true;
        }
        return false;
    }
    /**
     * Relinquish ownership of a store
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    public boolean relinquishOwnership(String ownerId, String storeId) {
        Store store = getStoreById(storeId);
        if(!store.isFounder(ownerId)&&store.userIsOwner(ownerId)){
            synchronized (store) {
                LinkedList<String> firedUsers = store.getAllSubordinates(ownerId);
                store.terminateOwnership(ownerId);
                for(String s:firedUsers){
                    getUserById(s).removeStore(storeId);
                }
                getUserById(ownerId).removeStore(storeId);
            }
            return true;
        }
        return false;
    }

        public boolean appointStoreManager(String appointerId, String storeId, String userId, boolean[] permissions) {
            if(!checkPermission(appointerId,storeId, PERM_MANAGE_STAFF)) {
                if (!(getStoreById(storeId) != null && getStoreById(storeId).getFounder().equals(userId))) {
                    return false;
                }
            }

            Store store = getStoreById(storeId); // Make sure getStoreById uses your StoreRepository
                //if (store.userIsOwner(userId) || store.userIsManager(userId)) {
                //    return false;
                //}
            store.addManager(appointerId, userId, permissions);
            // Changes to 'store' will be automatically flushed by the @Transactional context

                // --- IMPORTANT CHANGE HERE ---
                RegisteredUser managerUser = getUserById(userId);
                if (managerUser == null) {
                    // Handle case where user is not found (e.g., log error, throw exception, return false)
                    System.err.println("Error: User with ID " + userId + " not found for manager appointment.");
                    return false;
                }
            managerUser.addManagedStore(storeId); // Modify the user object in memory

                // Explicitly save the modified user object to persist changes
                // This 'save' operation will participate in the transaction started by the ServiceLayer
                userRepository.update(managerUser); // <--- ADD THIS LINE!
                storeRepository.update(store);
                return true;

        }
    /**
     * Sends a proposal to a user to appoint them as a manager for a store.
     * The method checks if the store and user exist, and ensures the user is not already an owner
     * or manager of the store before notifying them about the manager appointment proposal.
     *
     * @param userId ID of the user to be proposed as a manager
     * @param storeId ID of the store for which the management proposal is being made
     * @param proposalText Description or details of the proposal
     */
    public void sendManagementProposal(String userId, String storeId, String proposalText) {
        Store store = getStoreById(storeId);
        RegisteredUser user = getUserById(userId);
        if (store == null || user == null) {
            return;
        }
        if (store.userIsOwner(userId) || store.userIsManager(userId)) {
            return;
        }
        //user.getNotifiedAboutOffer(storeId,proposalText);
        // to do and understand
    }
    /**
     * Respond to a manager appointment
     * @param userId ID of the user responding
     * @param appointmentId ID of the appointment
     * @param accept Whether to accept the appointment
     * @return true if successful, false otherwise
     */
    public boolean respondToManagerAppointment(String userId, String appointmentId, boolean accept) {
        if (accept) {
            appointStoreManager(userId, appointmentId, userId, new boolean[]{true, true, true});
            return true;
        } else return false;
    }
    // Modify methods to use permission check
    public boolean updateManagerPermissions(String ownerId, String storeId, String managerId, boolean[] permissions) {
        if (!checkPermission(ownerId, storeId, PERM_MANAGE_STAFF)) {
            return false;
        }

        Store store = getStoreById(storeId);
        if (store.checkIfSuperior(ownerId, managerId)) {
            synchronized (store) {
                store.changeManagersPermissions(managerId, permissions);
            }
            return true;
        }
        return false;
    }
    /**
     * Remove a store manager
     * @param ownerId ID of the owner
     * @param storeId ID of the store
     * @param managerId ID of the manager to remove
     * @return true if successful, false otherwise
     */
    public boolean removeStoreManager(String ownerId, String storeId, String managerId) {
        Store store = getStoreById(storeId);
        if(store.checkIfSuperior(ownerId,managerId)){
            synchronized (store) {
                store.terminateManagment(managerId);
                getUserById(managerId).removeStore(storeId);
                userRepository.update(getUserById(managerId));
                storeRepository.update(store);
            }
            return true;
        }
        return false;
    }
    /**
     * Close a store
     * @param founderId ID of the store founder
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    public boolean closeStore(String founderId, String storeId) {
        Store store = getStoreById(storeId);
        if (!store.isFounder(founderId))           // unchanged
            throw new RuntimeException("User is not store founder");

        synchronized (store) {
            store.closeTheStore();                 // flip the flag
            storeRepository.update(store);        // ★ NEW – make it stick
        }
        return true;
    }
    /**
     * Reopen a store
     * @param founderId ID of the store founder
     * @param storeId ID of the store
     * @return true if successful, false otherwise
     */
    public boolean reopenStore(String founderId, String storeId) {
        Store store = getStoreById(storeId);
        if (!store.isFounder(founderId))
            throw new RuntimeException("User is not store founder");

        synchronized (store) {
            store.openTheStore();
            storeRepository.update(store);         // ★ NEW – keep DB in sync
        }
        return true;
    }
    /**
     * Get information about store roles
     * @param ownerId ID of the requesting owner
     * @param storeId ID of the store
     * @return Map of role information if successful, null otherwise
     */
    public String getStoreRoleInfo(String ownerId, String storeId) {
        Store store = getStoreById(storeId);
        if (store.userIsManager(ownerId)||store.userIsManager(ownerId)){
            return store.getRoles();
        }
        return "";
    }
    /**
     * Get manager permissions
     * @param ownerId ID of the requesting owner
     * @param storeId ID of the store
     * @param managerId ID of the manager
     * @return Array of permissions if successful, null otherwise
     */
    public Map<String, Boolean> getManagerPermissions(String ownerId, String storeId, String managerId) {
        Store store = getStoreById(storeId);
        // Allow if owner == manager
        if ((store.checkIfSuperior(ownerId, managerId) || ownerId.equals(managerId)) && store.userIsManager(managerId)) {
            return store.getPremissions(managerId);
        }
        return new HashMap<>();
    }
    public boolean relinquishManagement(String managerID, String storeId) {
        Store store = getStoreById(storeId);
        if(!store.isFounder(managerID)&&store.userIsManager(managerID)){
            synchronized (store) {
                store.terminateManagment(managerID);
                getUserById(managerID).removeStore(storeId);
            }
            return true;
        }
        return false;

    }

    public boolean appointStoreFounder(String founderId, String storeId) {
        throw new RuntimeException("store founder can't be appointed - error thrown in storeManagementMicroservice");
    }

    /**
     * Return <code>true</code> if the user is the store’s founder **or**
     * already appears in the owner list.
     *
     * @param userId  the user to check
     * @param storeId the store in question
     */
    public boolean isFounderOrOwner(String userId, String storeId) {
        Store store = getStoreById(storeId);      // throws if store not found
        return store.isFounder(userId) || store.userIsOwner(userId);
    }

    public boolean removeStoreOwnerWithUserSync(String removerId, String storeId, String ownerId) {
        Store store = getStoreById(storeId);
        if (store.checkIfSuperior(removerId, ownerId)) {
            synchronized (store) {
                LinkedList<String> subordinates = store.getAllSubordinates(ownerId);
                store.terminateOwnership(ownerId);
                userRepository.getById(ownerId).removeStore(storeId);
                for (String sid : subordinates) {
                    userRepository.getById(sid).removeStore(storeId);
                }
            }
            return true;
        }
        return false;
    }

    public boolean relinquishOwnershipWithUserSync(String ownerId, String storeId) {
        Store store = getStoreById(storeId);
        if (!store.isFounder(ownerId) && store.userIsOwner(ownerId)) {
            synchronized (store) {
                LinkedList<String> subordinates = store.getAllSubordinates(ownerId);
                store.terminateOwnership(ownerId);
                userRepository.getById(ownerId).removeStore(storeId);
                for (String sid : subordinates) {
                    userRepository.getById(sid).removeStore(storeId);
                }
            }
            return true;
        }
        return false;
    }

    public boolean removeManagerWithUserSync(String removerId, String storeId, String managerId) {
        Store store = getStoreById(storeId);
        if (store.checkIfSuperior(removerId, managerId)) {
            synchronized (store) {
                store.terminateManagment(managerId);
                userRepository.getById(managerId).removeStore(storeId);
            }
            return true;
        }
        return false;
    }

    public boolean relinquishManagerWithUserSync(String managerId, String storeId) {
        Store store = getStoreById(storeId);
        if (!store.isFounder(managerId) && store.userIsManager(managerId)) {
            synchronized (store) {
                store.terminateManagment(managerId);
                RegisteredUser manager = userRepository.getById(managerId);
                manager.removeStore(storeId);
                userRepository.update(manager);       // <-- added update for user
                storeRepository.update(store);        // <-- added update for store
            }
            return true;
        }
        return false;
    }

}