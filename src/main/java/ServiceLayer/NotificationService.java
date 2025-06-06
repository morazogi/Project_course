package ServiceLayer;

import InfrastructureLayer.NotificationRepository;
import InfrastructureLayer.NotificationWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.Notifications;

@Service
public class NotificationService {

    private final NotificationWebSocketHandler handler;
    private final NotificationRepository repo;
    private final TokenService tokenService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public NotificationService(NotificationWebSocketHandler handler,
                               NotificationRepository repo,
                               TokenService tokenService) {
        this.handler = handler;
        this.repo = repo;
        this.tokenService = tokenService;
    }

    public void notifyUser(String userId, String message, String storeId) {
        try {
            Notifications n = new Notifications(message, userId, storeId);
            String json = mapper.writeValueAsString(n);
            repo.addNotification(userId, json);
            handler.sendNotificationToClient(userId, json);
        } catch (Exception ignored) { }
    }
}
