package DomainLayer.DomainServices;


import java.util.ArrayList;
import java.util.List;

import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.NotificationRepository;
import DomainLayer.IToken;


import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.Notifications;


public class ToNotify {
    private NotificationRepository notificationRepo;
    private IToken tokenService;
    private NotificationWebSocketHandler notificationWebSocketHandler;
    private final ObjectMapper mapper = new ObjectMapper();
    private UserRepository userRepository;
    private StoreRepository storeRepository;

    public ToNotify(NotificationRepository notificationRepo, IToken tokenService, NotificationWebSocketHandler notificationWebSocketHandler, UserRepository userRepository, StoreRepository storeRepository) {
        this.tokenService = tokenService;
        this.notificationRepo = notificationRepo;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }

    public List<Notifications> getUserNotifications(String token) {
        String receiverUsername = tokenService.extractUsername(token);
        List<Notifications> notifications = notificationRepo.getAll(); //.findByUserID(receiverUsername);
        ArrayList<Notifications> messages = new ArrayList<>();
        for (Notifications notification : notifications) {
            messages.add(notification);
            System.out.println(notification);
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

    public void sendNotificationToStore(String token, String storeName, String message) throws Exception {
        try {
            List<RegisteredUser> users = userRepository.getAll();
            for (RegisteredUser user : users) {
                List<String> managedStores = user.getManagedStores();
                List<String> managedStoresfg = managedStores;
                List<Store> stores = storeRepository.getAll();
                String storeId = "";
                for(Store store : stores) {
                    if (store.getName().equals(storeName)) {
                        storeId = store.getId();
                    }
                }
                if (managedStoresfg.contains(storeId) || user.getOwnedStores().contains(storeId)) {
                    notificationWebSocketHandler.sendNotificationToClient(user.getUsername(), message);
                }
            }
        } catch (Exception e) {

        }
    }

    public void sendNotificationToUser(String storeId, String userId, String message) throws Exception {
        try {
            Notifications notification = new Notifications(message, userId, storeId);
                notificationWebSocketHandler.sendNotificationToClient(userId, message);
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
                List<Store> stores = storeRepository.getAll();
                String storeId = "";
                for(Store store : stores) {
                        storeId = store.getId();
                    if (notification.getUserId().equals(username) && (notification.getStoreId().equals("") || managedStoresfg.contains(notification.getStoreId()) || user.getOwnedStores().contains(storeId))) {
                        notificationWebSocketHandler.sendNotificationToClient(username, notification.getMessage());
                    }

                }
            }
        } catch (Exception e) {

        }
    }


}   