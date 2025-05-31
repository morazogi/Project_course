package DomainLayer;

public interface IUserRepository {
    /**
     *Easy signatures that should always be in any UserRepository implementation ever (we won't need to implement more tho)
     */
    public boolean addUser(String username , String password , String json);
    public String getUserPass(String username);
    public boolean isUserExist(String username);
    public boolean update(String name ,String s);
    public String getUser(String username);
    String getUserById(String userId);
}