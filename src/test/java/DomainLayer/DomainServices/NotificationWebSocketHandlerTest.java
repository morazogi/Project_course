/* ────────────────────────────────────────────────────────────────
   src/test/java/DomainLayer/DomainServices/NotificationWebSocketHandlerTest.java
   ──────────────────────────────────────────────────────────────── */
package DomainLayer.DomainServices;

import DomainLayer.IToken;
import InfrastructureLayer.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import utils.Notifications;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationWebSocketHandlerTest {

    /* mocks */
    @Mock IToken                 tokenService;
    @Mock NotificationRepository notificationRepo;
    @Mock WebSocketSession       sessionOpen;
    @Mock WebSocketSession       sessionClosed;
    @Mock Notifications          notif1;
    @Mock Notifications          notif2;

    private NotificationWebSocketHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = new NotificationWebSocketHandler(tokenService, notificationRepo);

        when(sessionOpen.getUri())
                .thenReturn(new URI("ws://localhost/ws?token=tok123"));
        when(sessionOpen.isOpen()).thenReturn(true);

        when(sessionClosed.getUri())
                .thenReturn(new URI("ws://localhost/ws?token=tokClosed"));
        when(sessionClosed.isOpen()).thenReturn(false);

        when(tokenService.extractUsername("tok123")).thenReturn("user1");
        when(tokenService.extractUsername("tokClosed")).thenReturn("userClosed");

        when(notif1.getMessage()).thenReturn("hello-1");
        when(notif2.getMessage()).thenReturn("hello-2");
        when(notificationRepo.findByUserID("user1"))
                .thenReturn(List.of(notif1, notif2));
    }

    @AfterEach
    void tearDown() {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, WebSocketSession> map =
                (ConcurrentHashMap<String, WebSocketSession>)
                        ReflectionTestUtils.getField(NotificationWebSocketHandler.class, "userSessions");
        if (map != null) map.clear();
    }

    /* ───────────────────────────── tests ───────────────────────────── */

    @Test
    void afterConnectionEstablished_sendsSavedNotifications_andStoresSession() throws Exception {
        handler.afterConnectionEstablished(sessionOpen);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(sessionOpen, times(2)).sendMessage(captor.capture());

        List<TextMessage> sent = captor.getAllValues();
        assertTrue(sent.stream().anyMatch(m -> "hello-1".equals(m.getPayload())));
        assertTrue(sent.stream().anyMatch(m -> "hello-2".equals(m.getPayload())));

        verify(notificationRepo).delete(notif1);
        verify(notificationRepo).delete(notif2);

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, WebSocketSession> map =
                (ConcurrentHashMap<String, WebSocketSession>)
                        ReflectionTestUtils.getField(NotificationWebSocketHandler.class, "userSessions");
        assertEquals(sessionOpen, map.get("user1"));
    }

    @Test
    void afterConnectionEstablished_missingToken_doesNothing() throws Exception {
        WebSocketSession noTokenSession = mock(WebSocketSession.class);
        when(noTokenSession.getUri()).thenReturn(new URI("ws://localhost/ws"));
        handler.afterConnectionEstablished(noTokenSession);

        verify(tokenService, never()).extractUsername(anyString());
        verify(notificationRepo, never()).findByUserID(anyString());

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, WebSocketSession> map =
                (ConcurrentHashMap<String, WebSocketSession>)
                        ReflectionTestUtils.getField(NotificationWebSocketHandler.class, "userSessions");
        assertTrue(map.isEmpty());
    }

    @Test
    void sendNotificationToClient_noActiveSession_persistsNotification() {
        handler.sendNotificationToClient("ghostUser", "payload-X");

        ArgumentCaptor<Notifications> captor = ArgumentCaptor.forClass(Notifications.class);
        verify(notificationRepo).save(captor.capture());
        assertEquals("payload-X", captor.getValue().getMessage());
    }

    /* ✨ add throws Exception here ✨ */
    @Test
    void sendNotificationToClient_closedSession_savesNotification() throws Exception {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, WebSocketSession> map =
                (ConcurrentHashMap<String, WebSocketSession>)
                        ReflectionTestUtils.getField(NotificationWebSocketHandler.class, "userSessions");
        map.put("userClosed", sessionClosed);

        handler.sendNotificationToClient("userClosed", "payload-Y");

        verify(sessionClosed, never()).sendMessage(any());
        verify(notificationRepo).save(any(Notifications.class));
    }

    @Test
    void afterConnectionClosed_removesSession() throws Exception {
        handler.afterConnectionEstablished(sessionOpen);
        handler.afterConnectionClosed(sessionOpen, null);

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, WebSocketSession> map =
                (ConcurrentHashMap<String, WebSocketSession>)
                        ReflectionTestUtils.getField(NotificationWebSocketHandler.class, "userSessions");
        assertFalse(map.containsKey("user1"));
    }
}
