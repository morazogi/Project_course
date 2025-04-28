package DomainLayer;

import DomainLayer.Roles.Owner;
import DomainLayer.Roles.Manager;

public interface IUserRepository {
    /**
     *Easy signatures that should always be in any UserRepository implementation ever (we won't need to implement more tho)
     */
    public boolean addUser(String username, String password , String json);
    public String getUserPass(String username);
    public boolean isUserExist(String username);
    public boolean update(String name ,String s);
    public String getUser(String username);

    /**
     * Get an Owner by ID
     * @param ownerId ID of the owner
     * @return Owner object if found, null otherwise
     */
    public Owner getOwnerById(String ownerId);

    /**
     * Get a Manager by ID
     * @param managerId ID of the manager
     * @return Manager object if found, null otherwise
     */
    public Manager getManagerById(String managerId);
}
