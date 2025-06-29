package ServiceLayer;

import DomainLayer.DomainServices.ToNotify;
import DomainLayer.DomainServices.NotificationWebSocketHandler;
import InfrastructureLayer.NotificationRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationWebSocketHandler handler;
    @Mock NotificationRepository      repo;
    @Mock TokenService                tokenService;
    @Mock UserRepository              userRepo;
    @Mock StoreRepository             storeRepo;
    @Mock ToNotify                    toNotify;        // delegate mock

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(handler, repo, tokenService, userRepo, storeRepo);
        // replace real ToNotify with mock
        ReflectionTestUtils.setField(service, "toNotify", toNotify);
    }

    @Test
    void notifyUser_emptyStoreId_callsSendToUser() throws Exception {
        service.notifyUser("u1", "hello", "");

        verify(toNotify).sendNotificationToUser("", "u1", "hello");
        verify(toNotify, never()).sendNotificationToStore(anyString(), anyString(), anyString());
    }

    @Test
    void notifyUser_storeId_callsSendToStore() throws Exception {
        service.notifyUser("u2", "sale", "s1");

        verify(toNotify).sendNotificationToStore("", "s1", "sale");
        verify(toNotify, never()).sendNotificationToUser(anyString(), anyString(), anyString());
    }

    @Test
    void sendNotificationsForUser_delegates() {
        service.sendNotificationsForUser("token-123");
        verify(toNotify).sendAllUserNotifications("token-123");
    }

    @Test
    void notifyUser_delegateThrows_isSwallowed() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(toNotify).sendNotificationToUser("", "u3", "msg");

        assertDoesNotThrow(() -> service.notifyUser("u3", "msg", ""));
    }
}
