package ServiceLayer;

import DomainLayer.DomainServices.UserConnectivity;
import DomainLayer.IToken;
import DomainLayer.DomainServices.*;
import java.util.List;

import InfrastructureLayer.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class RegisteredService {

    private final IToken tokenService;
    private final UserConnectivity userConnectivity;
    private final Rate       rateService;
    private final History    history;
    private final OpenStore  opener;
    private final ToNotify   notifyService;
    private final GuestRepository guestRepository;

    public RegisteredService(IToken tokenService,
                             StoreRepository storeRepository,
                             UserRepository userRepository,
                             ProductRepository productRepository,
                             OrderRepository orderRepository,
                             NotificationRepository notificationRepository, GuestRepository guestRepository,
                             NotificationWebSocketHandler notificationWebSocketHandler) {
        this.tokenService = tokenService;
        this.userConnectivity = new UserConnectivity(tokenService, userRepository, guestRepository);
        this.rateService = new Rate(tokenService, storeRepository, userRepository, productRepository);
        this.history = new History(tokenService, orderRepository , userRepository);
        this.opener = new OpenStore(tokenService, storeRepository, userRepository);
        this.notifyService = new ToNotify(notificationRepository, tokenService, notificationWebSocketHandler, userRepository, storeRepository);
        this.guestRepository = guestRepository;
    }


    @Transactional
    public String logoutRegistered(String token) throws Exception {
        String username = tokenService.extractUsername(token);
        try {
            userConnectivity.logout(username, token);
            EventLogger.logEvent(username, "LOGOUT");
            return tokenService.generateToken("Guest");
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(username, "LOGOUT_FAILED" );
            throw new RuntimeException("Invalid token");
        }
    }

    @Transactional
    public String openStore(String token, String storeName) throws Exception {
        String username = tokenService.extractUsername(token);
        try {
            EventLogger.logEvent(username, "OPEN_STORE");
            return opener.openStore(token, storeName);
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(username, "OPEN_STORE_FAILED");
            throw new RuntimeException("Invalid token");
        }
    }

    /* ───────────────────── rating operations ───────────────────────── */

    @Transactional
    public boolean rateStore(String token, String storeId, int rate) throws Exception {
        try {
            EventLogger.logEvent(tokenService.extractUsername(token), "RATE_STORE");
            return rateService.rateStore(token, storeId, rate);
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "RATE_STORE_FAILED");
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "Could not rate store" : e.getMessage();
            throw new RuntimeException(msg);
        }
    }

    @Transactional
    public boolean rateProduct(String token, String productId, int rate) throws Exception {
        try {
            EventLogger.logEvent(tokenService.extractUsername(token), "RATE_PRODUCT");
            return rateService.rateProduct(token, productId, rate);
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "RATE_PRODUCT_FAILED");
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "Could not rate product" : e.getMessage();
            throw new RuntimeException(msg);
        }
    }

    @Transactional
    public boolean rateStoreAndProduct(String token,
                                       String storeId,
                                       String productId,
                                       int storeRate,
                                       int productRate) throws Exception {
        try {
            EventLogger.logEvent(tokenService.extractUsername(token), "RATE_STORE_AND_PRODUCT");
            boolean okStore   = rateService.rateStore  (token, storeId,   storeRate);
            boolean okProduct = rateService.rateProduct(token, productId, productRate);
            return okStore && okProduct;
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "RATE_STORE_AND_PRODUCT_FAILED");
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "Could not rate" : e.getMessage();
            throw new RuntimeException(msg);
        }
    }

    /* ───────────────────── history / notification ──────────────────── */

    @Transactional
    public List<String> getUserOrderHistory(String token) throws Exception {
        String username = tokenService.extractUsername(token);
        try {
            EventLogger.logEvent(username, "GET_ORDER_HISTORY");
            return history.getOrderHistory(token);
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(username, "GET_ORDER_HISTORY_FAILED");
            throw new RuntimeException("Invalid token");
        }
    }

    @Transactional
    public void sendNotificationToStore(String token, String storeId, String message) throws Exception {
        String username = tokenService.extractUsername(token);
        try {
            EventLogger.logEvent(username, "SEND_NOTIFICATION_TO_STORE");
            notifyService.sendNotificationToStore(token, storeId, message);
        } catch (Exception e) {
            EventLogger.logEvent(username, "SEND_NOTIFICATION_TO_STORE_FAILED " + e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    /* ───────────────────── misc accessor ───────────────────────────── */

    public IToken getTokenService() { return tokenService; }
}
