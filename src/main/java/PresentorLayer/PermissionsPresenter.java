package PresentorLayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class PermissionsPresenter {
    // handles PermissionsPresenter activity with the ui layer and adapt between ui to domain
    OwnerManagerService manager;
    private final ObjectMapper mapper = new ObjectMapper();
    private IToken tokenService;
    private IUserRepository userRepository;

    //getManagerPermissions
    public PermissionsPresenter(OwnerManagerService manager, IToken tokenService, IUserRepository userRepository){
        this.manager = manager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public Map<String, Boolean> getPremissions(String ownerId, String storeId, String managerId){
        if(storeId == null || storeId.isEmpty()){
            return null;
        }
        return this.manager.getManagerPermissions(ownerId, storeId, managerId);
    }
}