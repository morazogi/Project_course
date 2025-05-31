package InfrastructureLayer;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.stereotype.Repository;
import utils.Notifications;

@Repository
public class NotificationRepository implements DomainLayer.INotificationRepository {
    private Map<String, List<Notifications>> notifications = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void addNotification(String recieverId, String notification) throws JsonProcessingException {
        notifications.computeIfAbsent(recieverId, k -> new ArrayList<>()).add(mapper.readValue(notification, Notifications.class));
    }

    public void removeNotification(String recieverId, String notification){
        List<Notifications> recieverNotifications = notifications.get(recieverId);
        if (recieverNotifications != null) {
            recieverNotifications.removeIf(n -> n.getMessage().equals(notification));
        }
    }

    public void clearNotifications(String recieverId) {
        notifications.remove(recieverId);
    }

    public void clearAllNotifications() {
        notifications.clear();
    }

    public List<String> getMyNotification(String recieverId) {
        List<Notifications> recieverNotifications = notifications.get(recieverId);
        if (recieverNotifications != null) {
            return recieverNotifications.stream().map(Notifications::getMessage).toList();
        }
        return new ArrayList<>();
    }



    
}