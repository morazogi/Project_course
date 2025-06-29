/* ────────────────────────────────────────────────────────────────
   src/test/java/ServiceLayer/RegisteredServiceTest.java
   ──────────────────────────────────────────────────────────────── */
package ServiceLayer;

import DomainLayer.DomainServices.*;
import DomainLayer.IToken;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisteredServiceTest {

    /* constructor args */
    @Mock IToken                 tokenService;
    @Mock StoreRepository        storeRepo;
    @Mock UserRepository         userRepo;
    @Mock ProductRepository      productRepo;
    @Mock OrderRepository        orderRepo;
    @Mock NotificationRepository notifRepo;
    @Mock GuestRepository        guestRepo;
    @Mock NotificationWebSocketHandler wsHandler;

    /* sub-components to be mocked & injected */
    @Mock UserConnectivity userConn;
    @Mock Rate             rateSvc;
    @Mock History          historySvc;
    @Mock OpenStore        openerSvc;
    @Mock ToNotify         notifySvc;

    private RegisteredService svc;

    @BeforeEach
    void setUp() {
        svc = new RegisteredService(tokenService, storeRepo, userRepo,
                productRepo, orderRepo, notifRepo,
                guestRepo, wsHandler);

        ReflectionTestUtils.setField(svc, "userConnectivity", userConn);
        ReflectionTestUtils.setField(svc, "rateService",       rateSvc);
        ReflectionTestUtils.setField(svc, "history",           historySvc);
        ReflectionTestUtils.setField(svc, "opener",            openerSvc);
        ReflectionTestUtils.setField(svc, "notifyService",     notifySvc);
    }

    /* ─────────────────────────── authentication ─────────────────────────── */

    @Test
    void logoutRegistered_success_returnsGuestToken() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");
        when(tokenService.generateToken("Guest")).thenReturn("GuestTok");

        String res = svc.logoutRegistered("tok");

        assertEquals("GuestTok", res);
        verify(userConn).logout("alice","tok");
    }

    /* ─────────────────────────── store open ─────────────────────────────── */

    @Test
    void openStore_happyPath_returnsId() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");
        when(openerSvc.openStore("tok","Mega")).thenReturn("store-1");

        String id = svc.openStore("tok","Mega");

        assertEquals("store-1", id);
    }

    /* ─────────────────────────── rating combo ───────────────────────────── */

    @Test
    void rateStoreAndProduct_bothTrue_returnsTrue() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");
        when(rateSvc.rateStore  ("tok","s",5)).thenReturn(true);
        when(rateSvc.rateProduct("tok","p",4)).thenReturn(true);

        assertTrue(svc.rateStoreAndProduct("tok","s","p",5,4));
    }

    @Test
    void rateStoreAndProduct_anyFalse_returnsFalse() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");
        when(rateSvc.rateStore  ("tok","s",5)).thenReturn(true);
        when(rateSvc.rateProduct("tok","p",4)).thenReturn(false);

        assertFalse(svc.rateStoreAndProduct("tok","s","p",5,4));
    }

    /* ─────────────────────────── history fetch ──────────────────────────── */

    @Test
    void getUserOrderHistory_returnsListFromHistory() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");
        when(historySvc.getOrderHistory("tok"))
                .thenReturn(List.of("o1","o2"));

        List<String> out = svc.getUserOrderHistory("tok");
        assertEquals(2, out.size());
        verify(historySvc).getOrderHistory("tok");
    }

    /* ─────────────────────────── notifications ──────────────────────────── */

    @Test
    void sendNotificationToStore_delegatesToNotifyService() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");

        svc.sendNotificationToStore("tok","Mega","Hello");

        verify(notifySvc).sendNotificationToStore("tok","Mega","Hello");
    }
}
