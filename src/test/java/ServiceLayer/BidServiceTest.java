package ServiceLayer;

import DomainLayer.BidSale;
import DomainLayer.IToken;
import InfrastructureLayer.OrderRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock PaymentService   paymentService;
    @Mock ShippingService  shippingService;
    @Mock IToken           tokenService;
    @Mock StoreRepository  storeRepo;
    @Mock ProductRepository productRepo;
    @Mock OrderRepository  orderRepo;

    private BidService service;

    @BeforeEach
    void setUp() {
        service = new BidService(
                paymentService, shippingService, tokenService,
                storeRepo,     productRepo,      orderRepo);
    }

    /* ──────────────── basic flow ──────────────── */

    @Test
    void start_createsBidWithExpectedFields() {
        BidSale bid = service.start("store-1", "prod-1", 10.0, 1.0, 5);

        assertNotNull(bid.getId());
        assertEquals("store-1", bid.getStoreId());
        assertEquals("prod-1",  bid.getProductId());
    }

    @Test
    void place_withValidUser_isAccepted() {
        BidSale bid = service.start("s", "p", 5.0, 1.0, 1);
        assertDoesNotThrow(() -> service.place(bid.getId(), "alice", 6.0));
    }

    /* ──────────────── guard-clauses / errors ──────────────── */

    @Test
    void place_byGuest_throws() {
        BidSale bid = service.start("s", "p", 5.0, 1.0, 1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.place(bid.getId(), "Guest42", 6.0));
        assertEquals("You must be logged-in to place a bid.", ex.getMessage());
    }

    @Test
    void pay_invalidCardNumber_throwsFast() {
        BidSale bid = service.start("s", "p", 5.0, 1.0, 0);

        // force payable state
        bid.markAwaitingPayment();

        // No stubbing: execution fails before tokenService.extractUsername() is called.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.pay(bid.getId(), "token",
                        "Alice", "123", "10/30", "123",
                        "NY", "NY", "1 Main St", "123456789", "00000"));
        assertEquals("Invalid card number", ex.getMessage());
    }
}
