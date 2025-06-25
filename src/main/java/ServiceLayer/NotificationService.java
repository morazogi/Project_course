package ServiceLayer;

import DomainLayer.DomainServices.ToNotify;
import DomainLayer.DomainServices.NotificationWebSocketHandler;
import InfrastructureLayer.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.Notifications;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationWebSocketHandler handler;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ToNotify toNotify;

    @Autowired
    public NotificationService(NotificationWebSocketHandler handler,
                               NotificationRepository repo,
                               TokenService tokenService) {
        this.handler = handler;
        this.toNotify = new ToNotify(repo,tokenService, handler);
    }

    public void notifyUser(String userId, String message, String storeId) {
        try {
            try {
                handler.sendNotificationToClient(userId, message);
            } catch (Exception e) {}
            toNotify.sendNotificationToUser(userId, message, storeId);
        } catch (Exception e) {
            // Log exception for debugging
            e.printStackTrace();
        }
    }

    public void sendNotificationsForUser(String token) {
        List<Notifications> usernotifications = toNotify.getUserNotifications(token);
        for (Notifications notification : usernotifications) {
            handler.sendNotificationToClient(notification.getUserId(), notification.getMessage());
        }
        toNotify.sendAllUserNotifications(token);
    }


}
