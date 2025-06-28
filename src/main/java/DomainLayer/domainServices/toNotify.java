package DomainLayer.DomainServices;


import java.util.ArrayList;
import java.util.List;

import DomainLayer.IUserRepository;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.NotificationRepository;
import DomainLayer.IToken;


import InfrastructureLayer.UserRepository;
import ServiceLayer.OwnerManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.stereotype.Repository;
import utils.Notifications;


public class ToNotify {
    private NotificationRepository notificationRepo;
    private IToken tokenService;
    private NotificationWebSocketHandler notificationWebSocketHandler;
    private final ObjectMapper mapper = new ObjectMapper();
    private UserRepository userRepository;

    public ToNotify(NotificationRepository notificationRepo, IToken tokenService, NotificationWebSocketHandler notificationWebSocketHandler, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.notificationRepo = notificationRepo;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.userRepository = userRepository;
    }

    public List<Notifications> getUserNotifications(String token) {
        String receiverUsername = tokenService.extractUsername(token);
        List<Notifications> notifications = notificationRepo.getAll(); //.findByUserID(receiverUsername);
        ArrayList<Notifications> messages = new ArrayList<>();
        for (Notifications notification : notifications) {
            messages.add(notification);
        }
        return messages;
    }

    public List<String> getStoreNotifications(String StoreId) {
        List<Notifications> notifications = notificationRepo.findByStoreID(StoreId);
        ArrayList<String> messages = new ArrayList<>();
        for (Notifications notification : notifications) {
            messages.add(notification.getMessage());
        }
        return messages;
    }

    public void sendNotificationToStore(String token, String storeId, String message) throws Exception {
        try {
            List<RegisteredUser> users = userRepository.getAll();
            for (RegisteredUser user : users) {
                List<String> managedStores = user.getManagedStores();
                List<String> managedStoresfg = managedStores;
                if (managedStoresfg.contains(storeId)) {
                    notificationWebSocketHandler.sendNotificationToClient(user.getUsername(), message);
                }
            }
        } catch (Exception e) {

        }
    }

    public void sendNotificationToUser(String storeId, String userId, String message) throws Exception {
        try {
            Notifications notification = new Notifications(message, userId, storeId);
            if(tokenService.getToken(userId).equals("")) {
                notificationRepo.save(notification);
            } else {
                notificationWebSocketHandler.sendNotificationToClient(userId, notification.getUserId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize notification", e);
        }
    }

    public void sendAllUserNotifications(String token) {
        try {
            List<Notifications> notifications = getUserNotifications(token);
            for (Notifications notification : notifications) {
                String username = tokenService.extractUsername(token);
                RegisteredUser user = null;
                try {
                    user = userRepository.getById(username);
                } catch (Exception e) {
                }
                List<String> managedStores = user.getManagedStores();
                List<String> managedStoresfg = managedStores;
                if (notification.getStoreId().equals(username) && (notification.getMessage().equals("") || managedStoresfg.contains(notification.getMessage()))) {
                    notificationWebSocketHandler.sendNotificationToClient(username, notification.getUserId());
                    notificationRepo.delete(notification);
                }
            }
        } catch (Exception e) {

        }
    }


}   