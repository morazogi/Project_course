package DomainLayer.DomainServices;

import utils.Notifications;

import java.util.List;

public interface IToNotify {
    void sendNotificationToStoreOwners(String token, String storeName, String message);
    void sendNotificationToStore(String token, String storeName, String message) throws Exception;
    void sendNotificationToUser(String storeId, String userId, String message) throws Exception;
    void sendAllUserNotifications(String token);
    List<Notifications> getUserNotifications(String token);
    List<String> getStoreNotifications(String storeId);
}
