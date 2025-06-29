package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-level tests for {@link Auction}.
 * Updated for the new rules:
 *   • Buyer may counter with a lower (but positive) price
 *   • Only the party that spoke last is blocked while waitingConsent
 *   • Illegal price ≤ 0 still throws
 */
class AuctionTest {

    private Auction auction;
    private final String store   = "s1";
    private final String product = "p1";
    private final String manager = "mgr";
    private final String buyer   = "bob";

    @BeforeEach
    void init() {
        auction = new Auction(store, product, manager, 100.0);
    }

    /* -------------------------------------------------------------
                        initial state
       ------------------------------------------------------------- */
    @Test
    void ctor_setsInitialFields() {
        assertEquals(store,   auction.getStoreId());
        assertEquals(product, auction.getProductId());
        assertEquals(manager, auction.getLastParty());
        assertEquals(100.0,   auction.getCurrentPrice());
        assertFalse(auction.isWaitingConsent());
        assertFalse(auction.isAwaitingPayment());
    }

    /* -------------------------------------------------------------
                    offer / accept happy-path
       ------------------------------------------------------------- */
    @Test
    void offer_thenManagerAccept_flowWorks() {
        auction.offer(buyer, 90.0);                       // buyer proposes lower
        assertTrue(auction.isWaitingConsent());
        assertEquals(buyer,  auction.getLastParty());
        assertEquals(90.0,   auction.getCurrentPrice());

        auction.accept(manager);                          // manager accepts
        assertFalse(auction.isWaitingConsent());
        assertEquals(buyer, auction.getLastParty());      // unchanged
    }

    /* -------------------------------------------------------------
                        offer validations
       ------------------------------------------------------------- */
    @Test
    void offer_negativePrice_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> auction.offer(buyer, 0.0));         // non-positive price
    }

    @Test
    void offer_samePartyWhileWaitingConsent_throwsIllegalState() {
        auction.offer(buyer, 90.0);                       // first offer
        assertThrows(IllegalStateException.class,
                () -> auction.offer(buyer, 95.0));        // same party again
    }

    /* -------------------------------------------------------------
                       accept validations
       ------------------------------------------------------------- */
    @Test
    void accept_withoutPendingOffer_throwsIllegalState() {
        assertThrows(IllegalStateException.class,
                () -> auction.accept(manager));
    }

    @Test
    void accept_bySameParty_throwsIllegalArgument() {
        auction.offer(buyer, 90.0);
        assertThrows(IllegalArgumentException.class,
                () -> auction.accept(buyer));             // same as lastParty
    }

    /* -------------------------------------------------------------
              markAwaitingPayment sets winner & price
       ------------------------------------------------------------- */
    @Test
    void markAwaitingPayment_setsWinnerAndPrice() {
        auction.markAwaitingPayment(buyer, 150.0);

        assertTrue(auction.isAwaitingPayment());
        assertEquals(buyer,  auction.getWinner());
        assertEquals(150.0,  auction.getAgreedPrice());
    }
}
