package infrastructureLayer;
import DomainLayer.IUserRepository;
import DomainLayer.Store;
import DomainLayer.User;
import DomainLayer.Roles.Owner;
import DomainLayer.Roles.Manager;
import DomainLayer.IStoreRepository;

import java.util.HashMap;
import java.util.Map;

public class UserRepository implements IUserRepository {
    //entry in the hashmap is of the form <username , (pass;json)>
    HashMap<String, String> rep = new HashMap<String, String>();
    HashMap<String, String> pass = new HashMap<String, String>();

    // Maps to store Owner and Manager objects by ID
    private Map<String, Owner> owners = new HashMap<>();
    private Map<String, Manager> managers = new HashMap<>();

    // Store repository reference for creating Owner and Manager objects
    private IStoreRepository storeRepository;

    public void setStoreRepository(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;

        // Set the store repository in Owner and Manager classes
        Owner.setStoreRepository(storeRepository);
        Manager.setStoreRepository(storeRepository);
    }

    public static void sendNewOwnershipRequest(int newOwnerId, Store myStore) {
        // Implementation
    }

    public String getUserPass(String username){
        return pass.get(username);
    }

    public boolean addUser(String username, String hashedPassword, String json) {
        if(rep.containsKey(username)){
            return false;
        }
        rep.put(username, json);
        pass.put(username, hashedPassword);
        return true;
    }

    public boolean isUserExist(String username) {
        return rep.containsKey(username);
    }

    public boolean update(String name, String s) {
        if(!rep.containsKey(s)){
            return false;
        }
        rep.replace(name, s);
        return true;
    }

    public String getUser(String username) {
        return rep.get(username);
    }

    @Override
    public Owner getOwnerById(String ownerId) {
        return owners.get(ownerId);
    }

    @Override
    public Manager getManagerById(String managerId) {
        return managers.get(managerId);
    }

    // Methods to add Owner and Manager objects to the repository
    public void addOwner(Owner owner) {
        owners.put(owner.getID(), owner);
    }

    public void removeOwner(String ownerId) {
        owners.remove(ownerId);
    }

    public void addManager(Manager manager) {
        managers.put(manager.getID(), manager);
    }

    public void removeManager(String managerId) {
        managers.remove(managerId);
    }
}
