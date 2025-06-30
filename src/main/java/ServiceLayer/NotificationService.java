package ServiceLayer;

import DomainLayer.DomainServices.ToNotify;
import DomainLayer.DomainServices.NotificationWebSocketHandler;
import InfrastructureLayer.NotificationRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.Notifications;

import java.util.List;

@Service
public class NotificationService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ToNotify toNotify;

    @Autowired
    public NotificationService(NotificationWebSocketHandler handler,
                               NotificationRepository repo,
                               TokenService tokenService,
                               UserRepository userRepository,
                               StoreRepository storeRepository) {
        this.toNotify = new ToNotify(repo,tokenService, handler, userRepository, storeRepository);
    }

    public void notifyUser(String userId, String message, String storeId) {
        try {
            if (storeId.equals("")) {
                toNotify.sendNotificationToUser(storeId, userId, message);
            } else {
                toNotify.sendNotificationToStore("", storeId, message);
            }
        } catch (Exception e) {
            // Log exception for debugging
            e.printStackTrace();
        }
    }

    public void sendNotificationsForUser(String token) {
//        List<Notifications> usernotifications = toNotify.getUserNotifications(token);
//        String receiverUsername = tokenService.extractUsername(token);
//        for (Notifications notification : usernotifications) {
//            handler.sendNotificationToClient(receiverUsername, notification.getUserId());
//            System.out.println(notification);
//        }
        toNotify.sendAllUserNotifications(token);
    }


}
