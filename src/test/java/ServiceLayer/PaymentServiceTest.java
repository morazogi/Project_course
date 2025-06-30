package ServiceLayer;

import DomainLayer.DomainServices.PaymentConnectivity;
import DomainLayer.DomainServices.PaymentConnectivity.*;
import DomainLayer.IPayment;
import DomainLayer.IToken;
import InfrastructureLayer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies that {@link PaymentService} delegates correctly to the internally
 * constructed {@link PaymentConnectivity} and converts exceptions into
 * RuntimeExceptions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    /* ------------- collaborators passed through ctor ------------- */
    @Mock UserRepository     userRepo;
    @Mock ProductRepository  productRepo;
    @Mock StoreRepository    storeRepo;
    @Mock DiscountRepository discountRepo;
    @Mock GuestRepository    guestRepo;
    @Mock
    IPayment paymentProxy;
    @Mock IToken             tokenService;          // only public field we interact with

    private PaymentService service;

    /* =============================================================
                             processPayment
       ============================================================= */
    @Test
    void processPayment_success_returnsReference() throws Exception {
        String token   = "tok";
        String user    = "alice";
        String payRef  = "pay-123";

        when(tokenService.extractUsername(token)).thenReturn(user);

        try (MockedConstruction<PaymentConnectivity> pcMock =
                     mockConstruction(PaymentConnectivity.class,
                             (mock, ctx) -> when(mock.processPayment(
                                     eq(user), eq("4111"), eq("12/25"),
                                     eq("777"), eq("Alice B."), eq("id-1")
                             )).thenReturn(payRef)))
        {
            service = new PaymentService(
                    userRepo, productRepo, paymentProxy,
                    tokenService, discountRepo, storeRepo, guestRepo);

            String result = service.processPayment(
                    token, "Alice B.", "4111", "12/25", "777", "id-1");

            assertEquals(payRef, result);

            PaymentConnectivity pc = pcMock.constructed().get(0);
            verify(pc).processPayment(
                    user, "4111", "12/25", "777", "Alice B.", "id-1");
        }
    }

    @Test
    void processPayment_failure_throwsRuntimeException() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");

        try (MockedConstruction<PaymentConnectivity> pcMock =
                     mockConstruction(PaymentConnectivity.class,
                             (mock, ctx) -> when(mock.processPayment(any(), any(), any(), any(), any(), any()))
                                     .thenThrow(new IllegalStateException("gateway down"))))
        {
            service = new PaymentService(
                    userRepo, productRepo, paymentProxy,
                    tokenService, discountRepo, storeRepo, guestRepo);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.processPayment("tok", "a", "n", "e", "b", "id"));

            assertEquals("gateway down", ex.getMessage());
        }
    }

    /* =============================================================
                               cancelPayment
       ============================================================= */
    @Test
    void cancelPayment_success_returnsOk() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");

        try (MockedConstruction<PaymentConnectivity> pcMock =
                     mockConstruction(PaymentConnectivity.class,
                             (mock, ctx) -> when(mock.cancelPayment("alice", "pay-123"))
                                     .thenReturn("OK")))
        {
            service = new PaymentService(
                    userRepo, productRepo, paymentProxy, tokenService,
                    discountRepo, storeRepo, guestRepo);

            String res = service.cancelPayment("tok", "pay-123");
            assertEquals("OK", res);

            verify(pcMock.constructed().get(0))
                    .cancelPayment("alice", "pay-123");
        }
    }

    @Test
    void cancelPayment_failure_bubblesAsRuntimeException() throws Exception {
        when(tokenService.extractUsername("tok")).thenReturn("alice");

        try (MockedConstruction<PaymentConnectivity> pcMock =
                     mockConstruction(PaymentConnectivity.class,
                             (mock, ctx) -> when(mock.cancelPayment(any(), any()))
                                     .thenThrow(new RuntimeException("boom"))))
        {
            service = new PaymentService(
                    userRepo, productRepo, paymentProxy, tokenService,
                    discountRepo, storeRepo, guestRepo);

            assertThrows(RuntimeException.class,
                    () -> service.cancelPayment("tok", "x"));
        }
    }
}
