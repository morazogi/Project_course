/* ────────────────────────────────────────────────────────────────
   src/test/java/ServiceLayer/AuctionServiceTest.java
   ──────────────────────────────────────────────────────────────── */
package ServiceLayer;

import DomainLayer.Auction;
import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.OrderRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    /* collaborators */
    @Mock PaymentService    paymentService;
    @Mock ShippingService   shippingService;
    @Mock IToken            tokenService;
    @Mock StoreRepository   storeRepo;
    @Mock ProductRepository productRepo;
    @Mock OrderRepository   orderRepo;

    /* frequently reused mocks */
    @Mock Store   store;
    @Mock Product product;

    private AuctionService service;

    @BeforeEach
    void setUp() {
        service = new AuctionService(
                paymentService, shippingService, tokenService,
                storeRepo,      productRepo,      orderRepo);
    }

    /* ────────────────────────── original smoke tests ───────────────────────── */

    @Test
    void create_thenOffer_succeeds() {
        Auction a = service.create("store", "prod", "manager", 15.0);

        assertNotNull(a.getId());
        assertDoesNotThrow(() -> service.offer(a.getId(), "bob", 16.0));
    }

    @Test
    void offer_byGuest_throws() {
        Auction a = service.create("store", "prod", "manager", 15.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.offer(a.getId(), "Guest_11", 16.0));
        assertEquals("You must be logged-in to make an offer.", ex.getMessage());
    }

    @Test
    void pay_invalidCardNumber_throwsFast() {
        Auction a = service.create("store", "prod", "manager", 15.0);
        a.markAwaitingPayment("alice", 20.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.pay(a.getId(), "token",
                        "Alice", "123", "10/30", "123",
                        "NY", "NY", "1 Main St", "123456789", "00000"));
        assertEquals("Invalid card number", ex.getMessage());
    }

    /* ─────────────────────────── new coverage tests ─────────────────────────── */

    /* ---------- accept / decline ---------- */

    @Test
    void accept_byManager_setsAwaitingPayment_andWinnerIsLastParty() {
        Auction a = service.create("s", "p", "mgr", 10);
        service.offer(a.getId(), "bob", 11);

        service.accept(a.getId(), "mgr");           // manager accepts

        assertTrue(a.isAwaitingPayment());
        assertEquals("bob", a.getWinner());
    }

    @Test
    void accept_byBuyerAfterCounterOffer_setsAwaitingPayment_andWinnerIsBuyer() {
        Auction a = service.create("s", "p", "mgr", 10);
        service.offer(a.getId(), "bob", 11);        // buyer offer
        service.offer(a.getId(), "mgr", 12);        // manager counter → lastParty = mgr

        service.accept(a.getId(), "bob");           // now buyer can accept

        assertTrue(a.isAwaitingPayment());
        assertEquals("bob", a.getWinner());
    }

    @Test
    void decline_resetsDialogue_withoutError() {
        Auction a = service.create("s", "p", "mgr", 10);
        service.offer(a.getId(), "bob", 11);

        assertDoesNotThrow(() -> service.decline(a.getId(), "mgr"));
        assertFalse(a.isAwaitingPayment());
    }

    /* ---------- pay() happy-path ---------- */

    @Test
    void pay_successfulFlow_clearsAuction_andPersistsEverything() {
        Auction a = service.create("store", "prod", "mgr", 10);
        a.markAwaitingPayment("alice", 12);

        /* stubbing */
        when(tokenService.extractUsername("T")).thenReturn("alice");
        when(storeRepo.getById("store")).thenReturn(store);
        when(productRepo.getById("prod")).thenReturn(product);
        when(store.isOpenNow()).thenReturn(true);
        when(product.getId()).thenReturn("prod");
        when(store.reserveProduct("prod", 1)).thenReturn(true);
        when(shippingService.processShipping(eq("T"), any(), any(), any(), any(), any()))
                .thenReturn("shipTx");
        when(paymentService.processPayment(eq("T"), any(), any(), any(), any(), any()))
                .thenReturn("payTx");
        when(product.getQuantity()).thenReturn(5);

        /* act */
        assertDoesNotThrow(() -> service.pay(a.getId(), "T",
                "Alice", "4111111111111111", "12/30", "123",
                "NY", "NY", "1 Main", "123456789", "00001"));

        assertTrue(service.list().isEmpty(), "auction board should be empty");

        /* verify sequence */
        InOrder io = inOrder(storeRepo, shippingService, paymentService, productRepo, orderRepo);
        io.verify(storeRepo).update(store);                       // after reservation
        io.verify(shippingService).processShipping(any(), any(), any(), any(), any(), any());
        io.verify(paymentService).processPayment(any(), any(), any(), any(), any(), any());
        io.verify(storeRepo).update(store);                       // after sale commit
        io.verify(productRepo).save(product);
        io.verify(orderRepo).save(any());
    }

    /* ---------- pay() guards & rollback paths ---------- */

    @Test
    void pay_auctionNotPayable_throws() {
        Auction a = service.create("s", "p", "mgr", 10);          // NOT awaiting payment

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.pay(a.getId(), "tok",
                        "bob", "4111111111111111", "12/30", "123",
                        "IL", "TLV", "Some", "123", "0000"));
        assertEquals("auction not payable", ex.getMessage());
    }

    @Test
    void pay_storeClosed_throws_andNoExternalCalls() {
        Auction a = service.create("s", "p", "mgr", 10);
        a.markAwaitingPayment("bob", 12);

        when(tokenService.extractUsername("tok")).thenReturn("bob");
        when(storeRepo.getById("s")).thenReturn(store);
        when(store.isOpenNow()).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.pay(a.getId(), "tok",
                        "bob", "4111111111111111", "12/30", "123",
                        "IL", "TLV", "Some", "123", "0000"));
        assertTrue(ex.getMessage().contains("closed"));
        verifyNoInteractions(shippingService, paymentService);
    }

    @Test
    void pay_outOfStock_rollsBackBeforeShipping() {
        Auction a = service.create("s", "p", "mgr", 10);
        a.markAwaitingPayment("bob", 12);

        when(tokenService.extractUsername("tok")).thenReturn("bob");
        when(storeRepo.getById("s")).thenReturn(store);
        when(productRepo.getById("p")).thenReturn(product);
        when(store.isOpenNow()).thenReturn(true);
        when(product.getId()).thenReturn("p");
        when(store.reserveProduct("p", 1)).thenReturn(false);     // <-- no stock

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.pay(a.getId(), "tok",
                        "bob", "4111111111111111", "12/30", "123",
                        "IL", "TLV", "Some", "123", "0000"));
        assertEquals("out of stock", ex.getMessage());
        verifyNoInteractions(shippingService, paymentService);
    }

    @Test
    void pay_shippingFails_rollsBackReservation_andCancelsShipping() {
        Auction a = service.create("s", "p", "mgr", 10);
        a.markAwaitingPayment("bob", 12);

        when(tokenService.extractUsername("tok")).thenReturn("bob");
        when(storeRepo.getById("s")).thenReturn(store);
        when(productRepo.getById("p")).thenReturn(product);
        when(store.isOpenNow()).thenReturn(true);
        when(product.getId()).thenReturn("p");
        when(store.reserveProduct("p", 1)).thenReturn(true);

        /* shipping blows up */
        when(shippingService.processShipping(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("postal strike!"));
        when(store.unreserveProduct("p", 1)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> service.pay(a.getId(), "tok",
                        "bob", "4111111111111111", "12/30", "123",
                        "IL", "TLV", "Some", "123", "0000"));

        verify(store).unreserveProduct("p", 1);
        verify(storeRepo, atLeastOnce()).update(store);
        verify(paymentService, never()).processPayment(any(), any(), any(), any(), any(), any());
        verify(paymentService, never()).cancelPayment(any(), any());
    }

    /* ---------- cancel() ---------- */

    @Test
    void cancel_byOtherUser_throws() {
        Auction a = service.create("s", "p", "mgr", 10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.cancel(a.getId(), "hacker"));
        assertEquals("Only the auction manager may remove it", ex.getMessage());
    }

    @Test
    void cancel_byManager_removesAuction() {
        Auction a = service.create("s", "p", "mgr", 10);

        assertEquals(1, service.list().size());
        service.cancel(a.getId(), "mgr");
        assertTrue(service.list().isEmpty());
    }
}
