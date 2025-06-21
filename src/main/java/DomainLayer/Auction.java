package DomainLayer;

import java.util.UUID;

public class Auction {
    private final String id = UUID.randomUUID().toString();
    private final String storeId;
    private final String productId;
    private final String managerId;
    private double currentPrice;
    private String lastParty;
    private boolean waitingConsent;

    /* payment stage */
    private boolean awaitingPayment = false;
    private String  winner;
    private double  agreedPrice;

    public Auction(String storeId, String productId, String managerId, double startPrice) {
        this.storeId     = storeId;
        this.productId   = productId;
        this.managerId   = managerId;
        this.currentPrice = startPrice;
        this.lastParty    = managerId;
        this.waitingConsent = false;
    }

    /* getters */
    public String  getId()             { return id; }
    public String  getStoreId()        { return storeId; }
    public String  getProductId()      { return productId; }
    public double  getCurrentPrice()   { return currentPrice; }
    public String  getLastParty()      { return lastParty; }
    public boolean isWaitingConsent()  { return waitingConsent; }
    public boolean isAwaitingPayment() { return awaitingPayment; }
    public String  getWinner()         { return winner; }
    public double  getAgreedPrice()    { return agreedPrice; }

    /* offer cycle */
    public void offer(String party,double price) {
        if (waitingConsent)                throw new IllegalStateException("pending consent");
        if (price <= currentPrice)         throw new IllegalArgumentException("price too low");
        currentPrice  = price;
        lastParty     = party;
        waitingConsent = true;
    }
    public void accept(String party) {
        if (!waitingConsent)               throw new IllegalStateException("nothing to accept");
        if (party.equals(lastParty))       throw new IllegalArgumentException("same party");
        waitingConsent = false;
    }

    /* called from AuctionService when manager accepts */
    public void markAwaitingPayment(String buyer,double price) {
        awaitingPayment = true;
        winner          = buyer;
        agreedPrice     = price;
    }
}
