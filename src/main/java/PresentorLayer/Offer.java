package PresentorLayer;

/**
 * A single customer offer shown to the auction-manager UI.
 * Carries readable names so the UI can just call toString().
 */
public class Offer {

    private final String buyer;        // who made the offer
    private final String storeName;    // human-readable store name
    private final String productName;  // human-readable product name
    private final double price;        // $ offered

    public Offer(String buyer,
                 String storeName,
                 String productName,
                 double price) {
        this.buyer        = buyer;
        this.storeName    = storeName;
        this.productName  = productName;
        this.price        = price;
    }

    /* getters in case you need them later */
    public String  getBuyer()       { return buyer; }
    public String  getStoreName()   { return storeName; }
    public String  getProductName() { return productName; }
    public double  getPrice()       { return price; }

    /** Pretty print for the Vaadin list */
    @Override
    public String toString() {
        return buyer + " offered $" + price
                + " on \"" + productName + "\""
                + " in store \"" + storeName + "\"";
    }
}
