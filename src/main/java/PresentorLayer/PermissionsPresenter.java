package PresentorLayer;

import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Roles.RegisteredUser;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;

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

    public Map<String, Boolean> getPremissions(String token, String storeId) {
        if(storeId == null || storeId.isEmpty()){
            return null;
        }
        String username = tokenService.extractUsername(token);

        String jsonUser = userRepository.getUser(username);
        RegisteredUser user = null;
        try {
            user = mapper.readValue(jsonUser, RegisteredUser.class);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
        return this.manager.getManagerPermissions(user.getID(), storeId, user.getID());
    }
}