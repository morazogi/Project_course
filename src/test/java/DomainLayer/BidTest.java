//package DomainLayer;
//
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BidTest {
//
//
//    @Test
//    void constructor_rejectsPastTime() {
//        LocalDateTime past = LocalDateTime.now().minusSeconds(1);
//        assertThrows(IllegalArgumentException.class,
//                () -> new BidSale(past, new BigDecimal("100"), new BigDecimal("10")));
//    }
//
//    @Test
//    void constructor_rejectsNonPositiveMinPrice() {
//        LocalDateTime future = LocalDateTime.now().plusDays(1);
//        assertThrows(IllegalArgumentException.class,
//                () -> new BidSale(future, BigDecimal.ZERO, new BigDecimal("10")));
//    }
//
//    @Test
//    void constructor_rejectsNonPositiveIncrement() {
//        LocalDateTime future = LocalDateTime.now().plusDays(1);
//        assertThrows(IllegalArgumentException.class,
//                () -> new BidSale(future, new BigDecimal("100"), BigDecimal.ZERO));
//    }
//
//
//    @Test
//    void placeBid_firstLegalBidIsAccepted() {
//        LocalDateTime future = LocalDateTime.now().plusDays(1);
//        BidSale bid = new BidSale(future,
//                new BigDecimal("100"),   // min price
//                new BigDecimal("10"));   // increment
//
//        bid.placeBid(new BigDecimal("110"), "alice");
//
//        assertEquals(new BigDecimal("110"), bid.getLastBid());
//        assertEquals("alice",               bid.getHighestBidder());
//    }
//
//
//    @Test
//    void placeBid_rejectsAmountBelowOrEqualCurrent() {
//        LocalDateTime future = LocalDateTime.now().plusDays(1);
//        BidSale bid = new BidSale(future, new BigDecimal("100"), new BigDecimal("10"));
//        bid.placeBid(new BigDecimal("110"), "alice");      // current highest = 110
//
//        assertThrows(IllegalArgumentException.class,
//                () -> bid.placeBid(new BigDecimal("110"), "bob"));   // equal
//        assertThrows(IllegalArgumentException.class,
//                () -> bid.placeBid(new BigDecimal("105"), "bob"));   // lower
//    }
//
//    @Test
//    void placeBid_rejectsWrongIncrement() {
//        LocalDateTime future = LocalDateTime.now().plusDays(1);
//        BidSale bid = new BidSale(future, new BigDecimal("100"), new BigDecimal("10"));
//
//        // 109 is > 100 but not a multiple of increment 10
//        assertThrows(IllegalArgumentException.class,
//                () -> bid.placeBid(new BigDecimal("109"), "alice"));
//    }
//}