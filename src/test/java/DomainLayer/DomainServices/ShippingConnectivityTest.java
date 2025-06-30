/* ────────────────────────────────────────────────────────────────
   src/test/java/DomainLayer/DomainServices/ShippingConnectivityTest.java
   ──────────────────────────────────────────────────────────────── */
package DomainLayer.DomainServices;

import DomainLayer.IShipping;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.GuestRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.EventLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Exhaustively exercises every branch in ShippingConnectivity.
 */
@ExtendWith(MockitoExtension.class)
class ShippingConnectivityTest {

    /* collaborators */
    @Mock IShipping        proxyShipping;
    @Mock UserRepository   userRepository;
    @Mock GuestRepository  guestRepository;
    @Mock RegisteredUser   registeredUser;
    @Mock Guest            guestUser;

    private ShippingConnectivity sut;

    @BeforeEach
    void setUp() {
        sut = new ShippingConnectivity(proxyShipping, userRepository, guestRepository);
    }

    /* ════════════════════════════════════════════════════════════
       processShipping : SUCCESS PATHS
       ════════════════════════════════════════════════════════════ */

    @Test
    void processShipping_registeredUser_success_delegatesToProxy() throws Exception {
        when(userRepository.getById("alice")).thenReturn(registeredUser);
        when(proxyShipping.processShipping(eq("IL"), eq("TLV"),
                eq("Ben-Gurion Blvd 1"),
                isNull(), eq("Alice"), eq("12345")))
                .thenReturn("SHIP-123");

        String ref = sut.processShipping("alice", "IL", "TLV",
                "Ben-Gurion Blvd 1", "Alice", "12345");

        assertEquals("SHIP-123", ref);
        verify(proxyShipping).processShipping(eq("IL"), eq("TLV"),
                eq("Ben-Gurion Blvd 1"),
                isNull(), eq("Alice"), eq("12345"));
        verify(userRepository).getById("alice");
        verifyNoInteractions(guestRepository);
    }

    @Test
    void processShipping_guestUser_success_delegatesToProxy() throws Exception {
        String username = "Guest42";
        when(guestRepository.getById(username)).thenReturn(guestUser);
        when(proxyShipping.processShipping(anyString(), anyString(),
                anyString(), isNull(),
                anyString(), anyString()))
                .thenReturn("SHIP-GUEST");

        assertEquals("SHIP-GUEST",
                sut.processShipping(username, "IL", "Haifa",
                        "Herzl Ave 10", "Bob", "54321"));
        verify(guestRepository).getById(username);
        verify(proxyShipping).processShipping(eq("IL"), eq("Haifa"),
                eq("Herzl Ave 10"),
                isNull(), eq("Bob"), eq("54321"));
        verifyNoInteractions(userRepository);
    }

    /* ════════════════════════════════════════════════════════════
       processShipping : ERROR  BRANCHES
       ════════════════════════════════════════════════════════════ */

    @Test
    void processShipping_registeredUser_missing_logsAndThrows() {
        try (MockedStatic<EventLogger> logger = mockStatic(EventLogger.class)) {
            when(userRepository.getById("missingUser"))
                    .thenThrow(new RuntimeException("db 404"));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> sut.processShipping("missingUser", "IL", "Eilat",
                            "Coral Beach", "Nobody", "00000"));

            assertEquals("User not found", ex.getMessage());
            logger.verify(() -> EventLogger.logEvent(eq("missingUser"),
                    startsWith("PROCESS_SHIPPING - USER_NOT_FOUND")));
        }
    }

    @Test
    void processShipping_guestUser_missing_logsAndThrows() {
        try (MockedStatic<EventLogger> logger = mockStatic(EventLogger.class)) {
            String username = "GuestMissing";
            when(guestRepository.getById(username))
                    .thenThrow(new RuntimeException("gone"));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> sut.processShipping(username, "IL", "Beer-Sheva",
                            "University St 1", "Ghost", "99999"));

            assertEquals("User not found", ex.getMessage());
            logger.verify(() -> EventLogger.logEvent(eq(username),
                    startsWith("PROCESS_SHIPPING - USER_NOT_FOUND")));
        }
    }

    @Test
    void processShipping_proxyFails_propagatesException() throws Exception {
        when(userRepository.getById("alice")).thenReturn(registeredUser);
        when(proxyShipping.processShipping(any(), any(), any(), any(), any(), any()))
                .thenThrow(new Exception("gateway down"));

        Exception ex = assertThrows(
                Exception.class,
                () -> sut.processShipping("alice", "IL", "Jerusalem",
                        "King David St 3", "Alice", "11111"));

        assertEquals("gateway down", ex.getMessage());
    }

    /* ════════════════════════════════════════════════════════════
       cancelShipping : SUCCESS PATHS
       ════════════════════════════════════════════════════════════ */

    @Test
    void cancelShipping_registeredUser_success_delegatesToProxy() throws Exception {
        when(userRepository.getById("alice")).thenReturn(registeredUser);
        when(proxyShipping.cancelShipping("SHIP-123")).thenReturn("CANCEL-OK");

        assertEquals("CANCEL-OK", sut.cancelShipping("alice", "SHIP-123"));
        verify(proxyShipping).cancelShipping("SHIP-123");
        verify(userRepository).getById("alice");
        verifyNoInteractions(guestRepository);
    }

    @Test
    void cancelShipping_guestUser_success_delegatesToProxy() throws Exception {
        String username = "Guest77";
        when(guestRepository.getById(username)).thenReturn(guestUser);
        when(proxyShipping.cancelShipping("G-001")).thenReturn("CANCEL-G-OK");

        assertEquals("CANCEL-G-OK", sut.cancelShipping(username, "G-001"));
        verify(proxyShipping).cancelShipping("G-001");
        verify(guestRepository).getById(username);
        verifyNoInteractions(userRepository);
    }




    @Test
    void cancelShipping_proxyFails_rethrowsRuntimeException() throws Exception {
        when(userRepository.getById("alice")).thenReturn(registeredUser);
        when(proxyShipping.cancelShipping("FAIL"))
                .thenThrow(new RuntimeException("service down"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sut.cancelShipping("alice", "FAIL"));

        assertEquals("service down", ex.getMessage());
    }
}
