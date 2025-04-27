package DomainLayer;

import DomainLayer.Roles.Owner;
import DomainLayer.Roles.Manager;

import java.util.LinkedList;

public interface IJobRepository {
    public LinkedList<User> getUsersByStore(String storeID);
    public LinkedList<Owner> getOwnersByStore(String storeID);
    public LinkedList<Manager> getManagersByStore(String storeID);
    public boolean isUserOwnerOfStore(String userID, String storeID);
    public boolean isUserManagerOfStore(String userID, String storeID);
    public void addOwner(String userID, String storeID, Owner owner);
    public void addManager(String userID, String storeID, Manager manager);
    public void removeOwner(String userID, String storeID, Owner owner);
    public void removeManager(String userID, String storeID, Manager manager);
}
