package PresentorLayer;

public class Offer {
    private String buyerName;
    private String productName;
    private double offerAmount;

    public Offer(String buyerName, String productName, double offerAmount) {
        this.buyerName = buyerName;
        this.productName = productName;
        this.offerAmount = offerAmount;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getProductName() {
        return productName;
    }

    public double getOfferAmount() {
        return offerAmount;
    }

    @Override
    public String toString() {
        return buyerName + " offered $" + offerAmount + " on " + productName;
    }
}
