package DomainLayer;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.*;

@Entity
@Table(name = "shopping_bag")
public class ShoppingBag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")    // NEW
    private String id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @ElementCollection
    @CollectionTable(                                   // NEW
            name        = "shopping_bag_products",      // NEW  dedicated table
            joinColumns = @JoinColumn(name = "shopping_bag_id")  // NEW  FK → bag PK
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<String, Integer> products = new HashMap<>();


    public ShoppingBag(String storeId) {
        this.storeId = storeId;
        this.products = new HashMap<String, Integer>();
    }
    public ShoppingBag() {
        this.storeId = null;
        this.products = new HashMap<String, Integer>();
    }
    public String getStoreId() {return storeId;}
    public Map<String, Integer> getProducts() { return products; }


    public void addProduct(String productId , Integer quantity) {
        if (products.containsKey(productId)) {
            products.put(productId, Integer.valueOf(products.get(productId) + quantity));
        } else {
            products.put(productId, quantity);
        }
    }
    public boolean removeProduct(String productId , Integer quantity) {
        boolean found = false;
        for (String product : products.keySet()) {
            if (productId.equals(product)) {
                if (products.get(product) < quantity) {
                    throw new IllegalArgumentException("Quantity is greater than available");
                }
                if(products.get(product) == quantity) {
                    products.remove(product);
                }
                else{
                    products.put(product, Integer.valueOf(products.get(product) - quantity));
                }
                found = true;
            }
        }
        return found;
    }
    public void sold() {
        for (String product : products.keySet()) {
            products.put(product, 0);
        }
    }
    @Override
    public String toString() {
        return "\n ShoppingBag{" +
                "id='" + id + '\'' +
                ", storeId='" + storeId.toString() + '\'' +
                ", products=" + products.keySet().stream().toString() +
                '}';
    }
}