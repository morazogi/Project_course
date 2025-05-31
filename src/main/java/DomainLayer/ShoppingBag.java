package DomainLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ServiceLayer.EventLogger;
import io.micrometer.observation.Observation.Event;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "shopping_bags")
public class ShoppingBag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @ElementCollection
    @CollectionTable(name = "shopping_bag_products", joinColumns = @JoinColumn(name = "bag_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<String, Integer> products = new HashMap<>();

    public ShoppingBag( String storeId) {
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

}