/* ────────────────────────────────────────────────────────────────
   src/test/java/DomainLayer/DomainServices/PaymentConnectivityTest.java
   ──────────────────────────────────────────────────────────────── */
package DomainLayer.DomainServices;

import DomainLayer.IPayment;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentConnectivityTest {

    @Mock IPayment           proxy;
    @Mock UserRepository     userRepo;
    @Mock GuestRepository    guestRepo;
    @Mock ProductRepository  productRepo;
    @Mock StoreRepository    storeRepo;
    @Mock DiscountRepository discountRepo;

    private PaymentConnectivity svc;

    @BeforeEach
    void setUp() {
        svc = new PaymentConnectivity(proxy, userRepo, productRepo,
                storeRepo, discountRepo, guestRepo);
    }

    /* ───────────────────────────── tests ───────────────────────── */

    @Test
    void cancelPayment_registeredUser_delegatesToProxy() throws Exception {
        when(userRepo.getById("alice")).thenReturn(mock(RegisteredUser.class));
        /* “doReturn” avoids checked-exception complaint */
        doReturn("OK").when(proxy).cancelPayment("123");

        String res = svc.cancelPayment("alice", "123");
        assertEquals("OK", res);
        verify(proxy).cancelPayment("123");
    }

    @Test
    void cancelPayment_userNotFound_throwsRuntime() {
        when(userRepo.getById("alice"))
                .thenThrow(new RuntimeException("no row"));

        assertThrows(RuntimeException.class,
                () -> svc.cancelPayment("alice", "123"));
    }
}
