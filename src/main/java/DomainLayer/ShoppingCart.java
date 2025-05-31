package DomainLayer;
import java.util.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private List<ShoppingBag> shoppingBags = new ArrayList<>();

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
}