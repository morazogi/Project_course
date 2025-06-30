package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BidSale}.
 */
class BidTest {

    private BidSale bid;
    private final LocalDateTime endsAt = LocalDateTime.now().plusHours(2);

    @BeforeEach
    void init() {
        bid = new BidSale(
                "store-1",
                "product-1",
                100.0,     // start price
                10.0,      // minimum increase
                endsAt);
    }

    /* -------------------------------------------------------------
                          initial state
       ------------------------------------------------------------- */
    @Test
    void initialState_matchesCtorArgs() {
        assertEquals("store-1",  bid.getStoreId());
        assertEquals("product-1",bid.getProductId());
        assertEquals(100.0,      bid.getCurrentPrice());
        assertNull(bid.getCurrentBidder());
        assertFalse(bid.isAwaitingPayment());
    }

    /* -------------------------------------------------------------
                           successful bid
       ------------------------------------------------------------- */
    @Test
    void bid_validAmount_updatesPriceAndBidder() {
        bid.bid("alice", 110.0,
                LocalDateTime.now().plusMinutes(1));

        assertEquals(110.0, bid.getCurrentPrice());
        assertEquals("alice", bid.getCurrentBidder());
    }

    /* -------------------------------------------------------------
                       too-low bid validation
       ------------------------------------------------------------- */
    @Test
    void bid_tooLow_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> bid.bid("bob", 105.0, LocalDateTime.now()));
    }

    /* -------------------------------------------------------------
                        bidding after end time
       ------------------------------------------------------------- */
    @Test
    void bid_afterEnd_throwsIllegalState() {
        LocalDateTime afterEnd = endsAt.plusSeconds(1);
        assertThrows(IllegalStateException.class,
                () -> bid.bid("late", 120.0, afterEnd));
    }

    /* -------------------------------------------------------------
                        winner & payment flow
       ------------------------------------------------------------- */
    @Test
    void markAwaitingPayment_setsWinnerAndPrice() {
        bid.bid("alice", 130.0, LocalDateTime.now());
        bid.markAwaitingPayment();

        assertTrue(bid.isAwaitingPayment());
        assertEquals("alice", bid.getWinner());
        assertEquals(130.0,   bid.getPriceToPay());
    }

    @Test
    void markAwaitingPayment_withoutAnyBid_doesNothing() {
        bid.markAwaitingPayment();

        assertFalse(bid.isAwaitingPayment());
        assertNull(bid.getWinner());
    }
}
