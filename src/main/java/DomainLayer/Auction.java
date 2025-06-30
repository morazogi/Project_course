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

    public Auction(String storeId, String productId,
                   String managerId,              // NEW
                   double startPrice) {

        this.storeId   = storeId;
        this.productId = productId;
        this.managerId = managerId;               // NEW
        this.currentPrice   = startPrice;
        this.lastParty      = managerId;
        this.waitingConsent = false;
    }

    /* getters */
    public String  getId()             { return id; }
    public String  getStoreId()        { return storeId; }
    public String  getProductId()      { return productId; }
    public String getManagerId() { return managerId; }
    public double  getCurrentPrice()   { return currentPrice; }
    public String  getLastParty()      { return lastParty; }
    public boolean isWaitingConsent()  { return waitingConsent; }
    public boolean isAwaitingPayment() { return awaitingPayment; }
    public String  getWinner()         { return winner; }
    public double  getAgreedPrice()    { return agreedPrice; }

    /* offer cycle */
// DomainLayer/Auction.java  – unchanged file except for this method
    public void offer(String partyId, double price) {

    /* block a repeated offer from the SAME side while consent is pending,
       but let the opposite side counter-offer */
        if (waitingConsent && partyId.equals(lastParty))
            throw new IllegalStateException("pending consent");

        if (price <= 0)
            throw new IllegalArgumentException("Price must be positive");

        lastParty      = partyId;   // who spoke last
        currentPrice   = price;
        waitingConsent = true;      // now the OTHER side must react
    }

    public void accept(String party) {

        if (!waitingConsent)
            throw new IllegalStateException("nothing to accept");

        /* it is NOT your turn if you were the last to speak */
        if (party.equals(lastParty))
            throw new IllegalArgumentException(
                    "You already sent an offer – wait for the other side to respond");

        waitingConsent = false;
    }

    /* ─── NEW helper used by the service layer ─── */
    public boolean isManagersTurn(String managerId) {
        return !waitingConsent || lastParty.equals(managerId);
    }

    /* called from AuctionService when manager accepts */
    public void markAwaitingPayment(String buyer,double price) {
        awaitingPayment = true;
        winner          = buyer;
        agreedPrice     = price;
    }

    /* ─── add inside class, after accept() ─────────────────────────── */
    /**
     * @return true  ⇒ auction must be closed
     *         false ⇒ auction stays open
     */
    /* replace the previous decline(...) */
    public void decline(String party) {

        if (!waitingConsent)
            throw new IllegalStateException("nothing to decline");
        if (party.equals(lastParty))
            throw new IllegalArgumentException(
                    "Wait for the counter-offer before declining");

        waitingConsent = false;   // conversation reset – auction stays open
    }



}
