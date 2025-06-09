package DomainLayer.DomainServices;


import java.util.ArrayList;
import java.util.List;
import InfrastructureLayer.NotificationRepository;
import DomainLayer.IToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.aspectj.apache.bcel.generic.RET;
import utils.Notifications;


public class toNotify {
    private NotificationRepository notificationRepo;
    private IToken tokenService;

    public toNotify(NotificationRepository notificationRepo, IToken tokenService) {
        this.tokenService = tokenService;
        this.notificationRepo = notificationRepo;
    }

    public List<String> getUserNotifications(String token) {
        String receiverUsername = tokenService.extractUsername(token);
        List<Notifications> notifications = notificationRepo.findByUserID(receiverUsername);
        ArrayList<String> messages = new ArrayList<>();
        for (Notifications notification : notifications) {
            messages.add(notification.getMessage());
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
            notificationRepo.save(notification);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize notification", e);
        }
    }
}   