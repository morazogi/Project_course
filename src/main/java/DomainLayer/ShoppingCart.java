package DomainLayer;
import java.util.*;

public class ShoppingCart {
    private String userId;
    private List<ShoppingBag> shoppingBags;

    public ShoppingCart(String userId) {
        this.userId = userId;
        this.shoppingBags = new ArrayList<ShoppingBag>();
    }

    public ShoppingCart() {
        this.userId = "";
        this.shoppingBags = new ArrayList<ShoppingBag>();
    }

    public void addProduct(String storeId, String productId , Integer quantity) {
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        boolean found = false;
        for (ShoppingBag shoppingBag : shoppingBags) {
            if (shoppingBag.getStoreId().equals(storeId)) {
                shoppingBag.addProduct(productId, quantity);
                found = true;
            }
        }

        if (!found) {
            ShoppingBag newShoppingBag = new ShoppingBag(storeId);
            newShoppingBag.addProduct(productId, quantity);
            shoppingBags.add(newShoppingBag);
        }
    }

    public boolean removeProduct(String storeId, String productId , Integer quantity) {
        boolean found = false;
        for (ShoppingBag shoppingBag : shoppingBags) {
            if (shoppingBag.getStoreId().equals(storeId)) {
                found = shoppingBag.removeProduct(productId , quantity);
                if (shoppingBag.getProducts().isEmpty()) {
                    shoppingBags.remove(shoppingBag);
                }
                return found;
            }
        }
        return found;
    }

    public List<ShoppingBag> getShoppingBags() {return shoppingBags;}

    public String getUserId() { return userId; }



    public void sold (){
        for (ShoppingBag shoppingBag : shoppingBags) {
            shoppingBag.sold();
        }
    }

    public void clear() {
        shoppingBags.clear();
    }
}