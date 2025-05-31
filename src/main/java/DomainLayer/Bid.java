package DomainLayer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    @Column(name = "min_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "increment", nullable = false, precision = 15, scale = 2)
    private BigDecimal increment;

    @Column(name = "last_bid", precision = 15, scale = 2)
    private BigDecimal lastBid;

    @Column(name = "highest_bidder")
    private String highestBidder;

    public Bid() {}

    public Bid(LocalDateTime bidTime, BigDecimal minPrice, BigDecimal increment) {
        if (bidTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Bid time must be in the future");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum price must be positive");
        }
        if (increment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Increment must be positive");
        }
        this.bidTime = bidTime;
        this.minPrice = minPrice;
        this.increment = increment;
        this.lastBid = minPrice;
        this.highestBidder = null;
    }

    public void placeBid(BigDecimal amount, String bidder) {
        if (LocalDateTime.now().isAfter(bidTime)) {
            throw new IllegalStateException("Bidding time has expired");
        }
        if (amount.compareTo(lastBid) <= 0) {
            throw new IllegalArgumentException("Bid must be higher than the current highest bid");
        }
        BigDecimal difference = amount.subtract(lastBid);
        if (difference.remainder(increment).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Bid must be in increments of " + increment);
        }
        lastBid = amount;
        highestBidder = bidder;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public BigDecimal getLastBid() {
        return lastBid;
    }

    public String getHighestBidder() {
        return highestBidder;
    }
}
