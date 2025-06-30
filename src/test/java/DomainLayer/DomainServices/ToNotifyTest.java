/* ────────────────────────────────────────────────────────────────
   src/test/java/DomainLayer/DomainServices/ToNotifyTest.java
   ──────────────────────────────────────────────────────────────── */
package DomainLayer.DomainServices;

import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.Notifications;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ToNotifyTest {

    @Mock NotificationRepository       notificationRepo;
    @Mock IToken                       tokenService;
    @Mock NotificationWebSocketHandler wsHandler;
    @Mock UserRepository               userRepo;
    @Mock StoreRepository              storeRepo;

    private ToNotify notify;

    @BeforeEach
    void init() {
        notify = new ToNotify(notificationRepo, tokenService,
                wsHandler, userRepo, storeRepo);
    }

    /* ─────────────────── getUserNotifications ─────────────────── */

    @Test
    void getUserNotifications_returnsAllFromRepo() {
        when(tokenService.extractUsername("tok")).thenReturn("bob");
        List<Notifications> stub = List.of(
                new Notifications("m1", "bob", ""),
                new Notifications("m2", "bob", "")
        );
        when(notificationRepo.getAll()).thenReturn(stub);

        List<Notifications> out = notify.getUserNotifications("tok");

        assertEquals(2, out.size());
        verify(tokenService).extractUsername("tok");
        verify(notificationRepo).getAll();
    }

    /* ─────────────────── getStoreNotifications ────────────────── */

    @Test
    void getStoreNotifications_mapsToMessages() {
        List<Notifications> stub = List.of(
                new Notifications("hi",  "u1", "s1"),
                new Notifications("bye", "u2", "s1")
        );
        when(notificationRepo.findByStoreID("s1")).thenReturn(stub);

        List<String> msgs = notify.getStoreNotifications("s1");

        assertIterableEquals(List.of("hi", "bye"), msgs);
    }

    /* ───────────── sendNotificationToStore (manager) ──────────── */

    @Test
    void sendNotificationToStore_managerMatch_callsWebSocket() throws Exception {
        RegisteredUser mgr = mock(RegisteredUser.class);
        when(mgr.getManagedStores()).thenReturn(List.of("store-1"));
        when(mgr.getUsername()).thenReturn("manager1");
        when(userRepo.getAll()).thenReturn(List.of(mgr));

        Store st = mock(Store.class);
        when(st.getId()).thenReturn("store-1");
        when(st.getName()).thenReturn("Mega");
        when(storeRepo.getAll()).thenReturn(List.of(st));

        notify.sendNotificationToStore("tok", "Mega", "hello");

        verify(wsHandler).sendNotificationToClient("manager1", "hello");
    }

    /* ─────────── sendNotificationToStoreOwners (owner) ────────── */

    @Test
    void sendNotificationToStoreOwners_ownerMatch_callsWebSocket() {
        RegisteredUser owner = mock(RegisteredUser.class);
        when(owner.getOwnedStores()).thenReturn(List.of("store-1"));
        when(owner.getManagedStores()).thenReturn(List.of());
        when(owner.getUsername()).thenReturn("owner1");
        when(userRepo.getAll()).thenReturn(List.of(owner));

        Store st = mock(Store.class);
        when(st.getId()).thenReturn("store-1");
        when(st.getName()).thenReturn("Mega");
        when(storeRepo.getAll()).thenReturn(List.of(st));

        notify.sendNotificationToStoreOwners("tok", "Mega", "msg");

        verify(wsHandler).sendNotificationToClient("owner1", "msg");
    }

    /* ─────────────────── sendNotificationToUser ───────────────── */

    @Test
    void sendNotificationToUser_alwaysCallsWebSocket() throws Exception {
        notify.sendNotificationToUser("s1", "alice", "hi");
        verify(wsHandler).sendNotificationToClient("alice", "hi");
    }
}
