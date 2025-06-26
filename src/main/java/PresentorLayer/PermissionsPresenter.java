package PresentorLayer;

import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.UserRepository;
import ServiceLayer.OwnerManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.notification.Notification;

import java.util.HashMap;
import java.util.Map;

public class PermissionsPresenter {
    // handles PermissionsPresenter activity with the ui layer and adapt between ui to domain
    OwnerManagerService manager;
    private final ObjectMapper mapper = new ObjectMapper();
    private IToken tokenService;
    private UserRepository userRepository;

    //getManagerPermissions
    public PermissionsPresenter(OwnerManagerService manager, IToken tokenService, UserRepository userRepository){
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

    public Map<String, Boolean> getPremissions(String token, String storeId) {
        if(storeId == null || storeId.isEmpty()){
            return null;
        }
        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
        }
        if (user == null) {
            return new HashMap<String, Boolean>();
        }
        return this.manager.getManagerPermissions(user.getUsername(), storeId, user.getUsername());
    }
}