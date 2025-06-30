package DomainLayer;

import java.time.LocalDateTime;
import java.util.UUID;

public class BidSale {

    private final String id = UUID.randomUUID().toString();
    private final String storeId;
    private final String productId;
    private final double minIncrease;
    private final LocalDateTime endAt;

    private double  currentPrice;
    private String  currentBidder;

    /* payment */
    private boolean awaitingPayment = false;
    private String  winner;
    private double  priceToPay;

    public BidSale(String storeId,
                   String productId,
                   double startPrice,
                   double minIncrease,
                   LocalDateTime endAt) {
        this.storeId      = storeId;
        this.productId    = productId;
        this.minIncrease  = minIncrease;
        this.endAt        = endAt;
        this.currentPrice = startPrice;
        this.currentBidder= null;
    }

    /* getters */
    public String getId()            { return id; }
    public String getStoreId()       { return storeId; }
    public String getProductId()     { return productId; }
    public double getCurrentPrice()  { return currentPrice; }
    public String getCurrentBidder() { return currentBidder; }
    public LocalDateTime getEndAt()  { return endAt; }
    public boolean isAwaitingPayment(){ return awaitingPayment; }
    public String  getWinner()       { return winner; }

    /* active bid */
    public void bid(String bidder,double amount,LocalDateTime now) {
        if (now.isAfter(endAt)) throw new IllegalStateException("ended");
        if (amount < currentPrice + minIncrease)
            throw new IllegalArgumentException("too low");
        currentPrice  = amount;
        currentBidder = bidder;
    }

    /* mark finished → winner pays later */
    public void markAwaitingPayment() {
        if (currentBidder == null) return;           // nobody bid
        awaitingPayment = true;
        winner          = currentBidder;
        priceToPay      = currentPrice;
    }

    public double getPriceToPay() { return priceToPay; }
}
