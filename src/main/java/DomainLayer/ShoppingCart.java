package DomainLayer;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_cart")
public class ShoppingCart {

    @Id
    @JoinColumn(name = "username", nullable = false)
    private String username;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id")
    private List<ShoppingBag> shoppingBags = new ArrayList<ShoppingBag>();

    public ShoppingCart(String username) {
        this.username = username;
    }
    public ShoppingCart() {}
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
        // the chatGPT siad that there is a problem because were not using an iterator instead of the foreach loop
        //we didn't trust him
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
    public String getUserId() {
        return username;
    }
    public void sold (){
        for (ShoppingBag shoppingBag : shoppingBags) {
            shoppingBag.sold();
        }
        shoppingBags.clear();
    }
    @Override
    public String toString() {
        return "ShoppingCart{" +
                "usernameInShoppingCart='" + username.toString() + '\'' +
                ", shoppingBags=" + shoppingBags.stream().map(ShoppingBag::toString)  +
                '}';
    }
}