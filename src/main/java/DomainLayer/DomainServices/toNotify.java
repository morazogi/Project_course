package DomainLayer.DomainServices;


import java.util.List;
import DomainLayer.INotificationRepository;
import DomainLayer.IToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import utils.Notifications;


public class toNotify {
    private INotificationRepository notificationRepo;
    private IToken tokenService;
    private ObjectMapper mapper = new ObjectMapper();

    public toNotify(INotificationRepository notificationRepo, IToken tokenService) {
        this.tokenService = tokenService;
        this.notificationRepo = notificationRepo;
    }

    public List<String> getUserNotifications(String token) {
        String receiver = tokenService.extractUsername(token);
        return notificationRepo.getMyNotification(receiver);
    }

    public List<String> getStoreNotifications(String StoreId) {
        return notificationRepo.getMyNotification(StoreId);
    }

    public void sendNotificationToStore(String token, String storeId, String message) throws Exception {
        String sender = tokenService.extractUsername(token);
        Notifications notification = new Notifications(sender, storeId, message);
        notificationRepo.addNotification(storeId, mapper.writeValueAsString(notification));
    }

    public void sendNotificationToUser(String storeId, String userId, String message) throws Exception {
        Notifications notification = new Notifications(storeId, userId, message);
        try {
            notificationRepo.addNotification(userId, mapper.writeValueAsString(notification));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize notification", e);
        }
    }
}   