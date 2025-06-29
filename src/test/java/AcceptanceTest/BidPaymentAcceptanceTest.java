package AcceptanceTest;

import DomainLayer.BidSale;
import DomainLayer.Order;
import DomainLayer.Product;
import DomainLayer.Store;
import DomainLayer.IToken;
import DomainLayer.Auction;
import InfrastructureLayer.*;
import ServiceLayer.BidService;
import ServiceLayer.AuctionService;
import ServiceLayer.PaymentService;
import ServiceLayer.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Acceptance tests for Bid payment system integration.
 * Tests verify that payment system calls are required and system state changes appropriately.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BidPaymentAcceptanceTest {

    @Mock private PaymentService paymentService;
    @Mock private ShippingService shippingService;
    @Mock private IToken tokenService;
    @Mock private StoreRepository storeRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private Store store;
    @Mock private Product product;
    @Mock private Order order;

    private BidService bidService;
    private AuctionService auctionService;
    private String bidId;
    private String auctionId;
    private String token = "valid-token";
    private String username = "testuser";
    private String storeId = "store-1";
    private String productId = "product-1";
    private String managerId = "storemanager";

    @BeforeEach
    void setUp() {
        bidService = new BidService(paymentService, shippingService, tokenService, 
                                   storeRepository, productRepository, orderRepository);
        auctionService = new AuctionService(paymentService, shippingService, tokenService,
                                           storeRepository, productRepository, orderRepository);
        
        // Setup basic mocks
        when(tokenService.extractUsername(token)).thenReturn(username);
        when(storeRepository.getById(storeId)).thenReturn(store);
        when(productRepository.getById(productId)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // Setup product mock properly
        when(product.getId()).thenReturn(productId);
        when(product.getQuantity()).thenReturn(5);
        
        // Setup store mock properly
        when(store.getId()).thenReturn(storeId);
        when(store.isOpenNow()).thenReturn(true);
        when(store.reserveProduct(anyString(), anyInt())).thenReturn(true);
        
        // Create a bid
        BidSale bid = bidService.start(storeId, productId, 100.0, 10.0, 60);
        bidId = bid.getId();
        
        // Place a winning bid
        bidService.place(bidId, username, 150.0);
        
        // Mark bid as awaiting payment
        bid.markAwaitingPayment();
        
        // Create an auction
        Auction auction = auctionService.create(storeId, productId, managerId, 100.0);
        auctionId = auction.getId();
    }

    @Test
    void successfulPayment_shouldCallPaymentSystemAndUpdateSystemState() throws Exception {
        // Arrange - Setup successful payment and shipping
        when(paymentService.processPayment(eq(token), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("payment-success");
        when(shippingService.processShipping(eq(token), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("shipping-success");

        // Act
        bidService.pay(bidId, token, "John Doe", "1234567890123456", "12/25", "123",
                      "CA", "Los Angeles", "123 Main St", "ID123", "90210");

        // Assert - Verify payment system was called
        verify(paymentService).processPayment(eq(token), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(shippingService).processShipping(eq(token), anyString(), anyString(), anyString(), anyString(), anyString());
        
        // Verify system state changes
        verify(store).reserveProduct(productId, 1);
        verify(store).sellProduct(productId, 1);
        verify(product).setQuantity(4); // 5 - 1
        verify(storeRepository, times(2)).update(store);
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
        
        // Verify bid was removed from active bids
        List<BidSale> remainingBids = bidService.open();
        assertFalse(remainingBids.stream().anyMatch(bid -> bid.getId().equals(bidId)));
    }

    @Test
    void auctionUserOffer_shouldAllowUserToMakeOfferAndManagerToAccept() {
        // Arrange - User submits initial offer
        double userOffer = 120.0;
        auctionService.offer(auctionId, username, userOffer);
        
        // Verify user's offer was accepted
        Auction auction = auctionService.list().stream()
                .filter(a -> a.getId().equals(auctionId))
                .findFirst()
                .orElse(null);
        
        assertNotNull(auction);
        assertEquals(userOffer, auction.getCurrentPrice());
        assertEquals(username, auction.getLastParty());
        assertTrue(auction.isWaitingConsent());
        
        // Act - Manager accepts the offer
        auctionService.accept(auctionId, managerId);
        
        // Assert - Verify offer was accepted and auction moved to payment stage
        auction = auctionService.list().stream()
                .filter(a -> a.getId().equals(auctionId))
                .findFirst()
                .orElse(null);
        
        assertNotNull(auction);
        assertFalse(auction.isWaitingConsent());
        assertTrue(auction.isAwaitingPayment());
        assertEquals(username, auction.getWinner());
        assertEquals(userOffer, auction.getAgreedPrice());
    }

    @Test
    void auctionUserOffer_withLowerPrice_shouldThrowException() {
        // Act & Assert - User tries to offer lower than current price
        double lowerOffer = 90.0; // Lower than start price of 100.0
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            auctionService.offer(auctionId, username, lowerOffer);
        });
        
        assertTrue(exception.getMessage().contains("price too low"));
        
        // Verify auction state remains unchanged
        Auction auction = auctionService.list().stream()
                .filter(a -> a.getId().equals(auctionId))
                .findFirst()
                .orElse(null);
        
        assertNotNull(auction);
        assertEquals(100.0, auction.getCurrentPrice()); // Original start price
        assertEquals(managerId, auction.getLastParty()); // Original manager
        assertFalse(auction.isWaitingConsent());
    }

    @Test
    void auctionUserOffer_whileWaitingConsent_shouldThrowException() {
        // Arrange - User submits initial offer
        double userOffer = 120.0;
        auctionService.offer(auctionId, username, userOffer);
        
        // Act & Assert - Another user tries to offer while waiting for consent
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            auctionService.offer(auctionId, "anotheruser", 130.0);
        });
        
        assertTrue(exception.getMessage().contains("pending consent"));
        
        // Verify auction state remains unchanged
        Auction auction = auctionService.list().stream()
                .filter(a -> a.getId().equals(auctionId))
                .findFirst()
                .orElse(null);
        
        assertNotNull(auction);
        assertEquals(userOffer, auction.getCurrentPrice());
        assertEquals(username, auction.getLastParty());
    }

    @Test
    void auctionUserOffer_guestUser_shouldThrowException() {
        // Act & Assert - Guest user tries to make an offer
        Exception exception = assertThrows(RuntimeException.class, () -> {
            auctionService.offer(auctionId, "Guest123", 120.0);
        });
        
        assertTrue(exception.getMessage().contains("must be logged-in"));
    }

    @Test
    void auctionManagerNewOffer_afterDecliningUserOffer_shouldAllowManagerToMakeNewOffer() {
        // Arrange - User submits initial offer
        double userOffer = 120.0;
        auctionService.offer(auctionId, username, userOffer);
        
        // Manager declines the offer
        auctionService.decline(auctionId, managerId);
        
        // After decline, auction is removed. Create a new auction for the manager to make a new offer.
        Auction newAuction = auctionService.create(storeId, productId, managerId, 100.0);
        String newAuctionId = newAuction.getId();
        double managerOffer = 110.0;
        auctionService.offer(newAuctionId, managerId, managerOffer);
        
        // Assert - Verify manager's offer was accepted
        Auction auction = auctionService.list().stream()
                .filter(a -> a.getId().equals(newAuctionId))
                .findFirst()
                .orElse(null);
        assertNotNull(auction);
        assertEquals(managerOffer, auction.getCurrentPrice());
        assertEquals(managerId, auction.getLastParty());
        assertFalse(auction.isWaitingConsent()); // Manager offers don't require consent
    }

    @Test
    void paymentSystemFailure_shouldThrowExceptionAndNotChangeSystemState() {
        // Arrange - Setup payment failure
        when(paymentService.processPayment(eq(token), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Payment system down"));
        when(shippingService.processShipping(eq(token), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("shipping-success");

        // Act & Assert - Payment should fail
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.pay(bidId, token, "John Doe", "1234567890123456", "12/25", "123",
                          "CA", "Los Angeles", "123 Main St", "ID123", "90210");
        });
        
        assertTrue(exception.getMessage().contains("Payment system down"));
        
        // Verify system state was not changed
        verify(store).reserveProduct(productId, 1);
        verify(store).unreserveProduct(productId, 1); // Should be unreserved on failure
        verify(storeRepository, times(2)).update(store); // Once for reserve, once for unreserve
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
        
        // Verify bid still exists
        List<BidSale> remainingBids = bidService.open();
        assertTrue(remainingBids.stream().anyMatch(bid -> bid.getId().equals(bidId)));
    }

    @Test
    void shippingSystemFailure_shouldThrowExceptionAndNotChangeSystemState() {
        // Arrange - Setup shipping failure
        when(paymentService.processPayment(eq(token), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("payment-success");
        when(shippingService.processShipping(eq(token), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Shipping system down"));

        // Act & Assert - Shipping should fail
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.pay(bidId, token, "John Doe", "1234567890123456", "12/25", "123",
                          "CA", "Los Angeles", "123 Main St", "ID123", "90210");
        });
        
        assertTrue(exception.getMessage().contains("Shipping system down"));
        
        // Verify system state was not changed
        verify(store).reserveProduct(productId, 1);
        verify(store).unreserveProduct(productId, 1); // Should be unreserved on failure
        verify(storeRepository, times(2)).update(store); // Once for reserve, once for unreserve
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
        
        // Verify bid still exists
        List<BidSale> remainingBids = bidService.open();
        assertTrue(remainingBids.stream().anyMatch(bid -> bid.getId().equals(bidId)));
    }

    @Test
    void productOutOfStock_shouldThrowExceptionAndNotChangeSystemState() {
        // Arrange - Setup product out of stock
        when(store.reserveProduct(productId, 1)).thenReturn(false);

        // Act & Assert - Should fail due to out of stock
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.pay(bidId, token, "John Doe", "1234567890123456", "12/25", "123",
                          "CA", "Los Angeles", "123 Main St", "ID123", "90210");
        });
        
        assertTrue(exception.getMessage().contains("out of stock"));
        
        // Verify no system state changes occurred
        verify(store).reserveProduct(productId, 1);
        verify(store, never()).sellProduct(anyString(), anyInt());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never()).processPayment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(shippingService, never()).processShipping(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        
        // Verify bid still exists
        List<BidSale> remainingBids = bidService.open();
        assertTrue(remainingBids.stream().anyMatch(bid -> bid.getId().equals(bidId)));
    }

    @Test
    void wrongUserAttemptingPayment_shouldThrowExceptionAndNotChangeSystemState() {
        // Arrange - Setup wrong user
        when(tokenService.extractUsername(token)).thenReturn("wronguser");

        // Act & Assert - Should fail due to wrong user
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.pay(bidId, token, "John Doe", "1234567890123456", "12/25", "123",
                          "CA", "Los Angeles", "123 Main St", "ID123", "90210");
        });
        
        assertTrue(exception.getMessage().contains("not your bid"));
        
        // Verify no system state changes occurred
        verify(store, never()).reserveProduct(anyString(), anyInt());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never()).processPayment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(shippingService, never()).processShipping(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        
        // Verify bid still exists
        List<BidSale> remainingBids = bidService.open();
        assertTrue(remainingBids.stream().anyMatch(bid -> bid.getId().equals(bidId)));
    }

    @Test
    void bidNotAwaitingPayment_shouldThrowExceptionAndNotChangeSystemState() {
        // Arrange - Create a new bid that's not awaiting payment
        BidSale newBid = bidService.start(storeId, productId, 200.0, 10.0, 60);
        String newBidId = newBid.getId();
        
        // Act & Assert - Should fail due to bid not awaiting payment
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.pay(newBidId, token, "John Doe", "1234567890123456", "12/25", "123",
                          "CA", "Los Angeles", "123 Main St", "ID123", "90210");
        });
        
        assertTrue(exception.getMessage().contains("not payable"));
        
        // Verify no system state changes occurred
        verify(store, never()).reserveProduct(anyString(), anyInt());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never()).processPayment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(shippingService, never()).processShipping(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        
        // Verify bid still exists
        List<BidSale> remainingBids = bidService.open();
        assertTrue(remainingBids.stream().anyMatch(bid -> bid.getId().equals(newBidId)));
    }

    @Test
    void nonExistentBid_shouldThrowExceptionAndNotChangeSystemState() {
        // Act & Assert - Should fail due to non-existent bid
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.pay("non-existent-bid", token, "John Doe", "1234567890123456", "12/25", "123",
                          "CA", "Los Angeles", "123 Main St", "ID123", "90210");
        });
        
        assertTrue(exception.getMessage().contains("bid not found"));
        
        // Verify no system state changes occurred
        verify(store, never()).reserveProduct(anyString(), anyInt());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never()).processPayment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(shippingService, never()).processShipping(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
} 