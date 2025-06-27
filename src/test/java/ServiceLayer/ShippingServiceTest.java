package ServiceLayer;

import DomainLayer.IShipping;
import DomainLayer.IToken;
import DomainLayer.DomainServices.ShippingConnectivity;
import InfrastructureLayer.GuestRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit-tests for {@link ShippingService}.
 *
 * <p>We let the real {@code ShippingService} build its own {@link ShippingConnectivity}
 * and then swap that field for a Mockito mock so we can assert behaviour without
 * touching the database or the external shipping provider.</p>
 */
@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    // ─── Constructor dependencies ──────────────────────────────────────────────
    @Mock IShipping       proxyShipping;
    @Mock IToken          tokenService;
    @Mock UserRepository  userRepository;
    @Mock GuestRepository guestRepository;

    // ─── Internal collaborator we want to verify ───────────────────────────────
    @Mock ShippingConnectivity shippingConnectivity;

    private ShippingService service;

    @BeforeEach
    void setUp() {
        service = new ShippingService(
                proxyShipping, tokenService, userRepository, guestRepository);

        // Replace the real connectivity layer with a mock
        ReflectionTestUtils.setField(service, "shippingConnectivity", shippingConnectivity);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  processShipping
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    void processShipping_success_returnsShippingId() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("john");
        when(shippingConnectivity.processShipping(
                "john", "IL", "Tel-Aviv", "Herzl 1", "John Doe", "61000"))
                .thenReturn("ship-42");

        String id = service.processShipping(
                "tok", "IL", "Tel-Aviv", "Herzl 1", "John Doe", "61000");

        assertEquals("ship-42", id);
        verify(shippingConnectivity).processShipping(
                "john", "IL", "Tel-Aviv", "Herzl 1", "John Doe", "61000");
    }

    @Test
    void processShipping_failure_isReThrownAsRuntime() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("john");
        when(shippingConnectivity.processShipping(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("svc down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.processShipping("tok", "IL", "TA", "addr", "name", "zip"));

        assertEquals("svc down", ex.getMessage());
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  cancelShipping
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    void cancelShipping_success_returnsProviderResponse() {
        when(tokenService.extractUsername("tok")).thenReturn("john");
        when(shippingConnectivity.cancelShipping("john", "ship-42")).thenReturn("200");

        String resp = service.cancelShipping("tok", "ship-42");

        assertEquals("200", resp);
        verify(shippingConnectivity).cancelShipping("john", "ship-42");
    }

    @Test
    void cancelShipping_failure_isReThrown() {
        when(tokenService.extractUsername("tok")).thenReturn("john");
        when(shippingConnectivity.cancelShipping(anyString(), anyString()))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class,
                () -> service.cancelShipping("tok", "id"));
    }
}
