package PresentorLayer;

public class Bid {
    private String productId;
    private String productName;
    private double currentPrice;

    public Bid(String productId, String productName, double currentPrice) {
        this.productId = productId;
        this.productName = productName;
        this.currentPrice = currentPrice;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getCurrentPrice() { return currentPrice; }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
}
