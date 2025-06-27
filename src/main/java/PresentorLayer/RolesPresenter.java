package PresentorLayer;
import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import ServiceLayer.RolesService;
import ServiceLayer.UserService;
import DomainLayer.Store;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RolesPresenter {

    private final String username;
    private final String token;
    private final RolesService rolesService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final IToken tokenService;
    private final UserRepository userRepository;

    public RolesPresenter(String username,
                          String token,
                          RolesService rolesService,
                          UserService userService,
                          IToken tokenService,
                          UserRepository userRepository) {
        this.username = username;
        this.token = token;
        this.rolesService = rolesService;
        this.userService = userService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public String appointOwner(String storeName, String targetUsername) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            rolesService.appointStoreOwner(username, storeId, targetUsername);
            return "Owner appointed successfully.";
        } catch (Exception e) {
            return "Failed to appoint owner: " + e.getMessage();
        }
    }

    public String removeOwner(String storeName, String targetUsername) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            rolesService.removeStoreOwner(username, storeId, targetUsername);
            return "Owner removed successfully.";
        } catch (Exception e) {
            return "Failed to remove owner: " + e.getMessage();
        }
    }

    public String appointManager(String storeName, String targetUsername, boolean[] permissions) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            rolesService.appointStoreManager(username, storeId, userRepository.getById(targetUsername).getShoppingCart().getUserId(), permissions);
            return "Manager appointed successfully.";
        } catch (Exception e) {
            return "Failed to appoint manager: " + e.getMessage();
        }
    }

    public String removeManager(String storeName, String targetUsername) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            rolesService.removeStoreManager(username, storeId, targetUsername);
            return "Manager removed successfully.";
        } catch (Exception e) {
            return "Failed to remove manager: " + e.getMessage();
        }
    }

    public String updatePermissions(String storeName, String managerUsername, boolean[] newPermissions) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            rolesService.updateManagerPermissions(username, storeId, managerUsername, newPermissions);
            return "Permissions updated.";
        } catch (Exception e) {
            return "Failed to update permissions: " + e.getMessage();
        }
    }

    public String viewStoreRoles(String storeName) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            return rolesService.getStoreRoleInfo(username, storeId);
        } catch (Exception e) {
            return "Failed to view roles: " + e.getMessage();
        }
    }

    public String relinquishOwnership(String storeName) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            rolesService.relinquishOwnership(username, storeId);
            return "You relinquished your ownership.";
        } catch (Exception e) {
            return "Failed to relinquish ownership: " + e.getMessage();
        }
    }

    public String relinquishManagement(String storeName) {
        try {
            String storeId = userService.searchStoreByName(token, storeName)
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Store not found")).getId();

            rolesService.relinquishManagement(username, storeId);
            return "You relinquished your management role.";
        } catch (Exception e) {
            return "Failed to relinquish management: " + e.getMessage();
        }
    }
}