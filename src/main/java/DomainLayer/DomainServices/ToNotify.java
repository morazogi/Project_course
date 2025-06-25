package DomainLayer.DomainServices;


import java.util.ArrayList;
import java.util.List;
import InfrastructureLayer.NotificationRepository;
import DomainLayer.IToken;


import com.fasterxml.jackson.databind.ObjectMapper;
import utils.Notifications;


public class ToNotify {
    private NotificationRepository notificationRepo;
    private IToken tokenService;
    private NotificationWebSocketHandler notificationWebSocketHandler;
    private final ObjectMapper mapper = new ObjectMapper();

    public ToNotify(NotificationRepository notificationRepo, IToken tokenService, NotificationWebSocketHandler notificationWebSocketHandler) {
        this.tokenService = tokenService;
        this.notificationRepo = notificationRepo;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    public List<Notifications> getUserNotifications(String token) {
        String receiverUsername = tokenService.extractUsername(token);
        List<Notifications> notifications = notificationRepo.findByUserID(receiverUsername);
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
        String senderUsername = tokenService.extractUsername(token);
        Notifications notification = new Notifications(message, senderUsername, storeId);
        notificationRepo.save(notification);
    }

    public void sendNotificationToUser(String storeId, String userId, String message) throws Exception {
        try {
            Notifications notification = new Notifications(message, userId, storeId);
            if(tokenService.getToken(userId).equals("")) {
                notificationRepo.save(notification);
            } else {
                notificationWebSocketHandler.sendNotificationToClient(userId, notification.getMessage());
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
                notificationWebSocketHandler.sendNotificationToClient(username, notification.getMessage());
                notificationRepo.delete(notification);

            }
        } catch (Exception e) {

        }
    }

}   