package DomainLayer;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    // todo should there be a one to many annotation here?
    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private float price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "rating")
    private double rating;

    @Column(name = "category")
    private String category;

    // Constructors
    public Product(String storeId, String name, String description, float price , int quantity, double rating, String category) {
        if(quantity > 0) {
            // id is not needed because we are working with UUID
            this.storeId = storeId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
            this.rating = rating;
            this.category = category;
        } else {
            throw new IllegalArgumentException("Product must have at least 1 quantity");
        }
    }

    public Product() {
        // Needed for JPA / Jackson
    }

    // Getters
    public String getId() { return id; }
    public String getStoreId() { return storeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getRating() { return rating; }
    public String getCategory() { return category; }

    // Setters
    public synchronized void setId(String id) { this.id = id; }
    public synchronized void setStoreId(String storeId) { this.storeId = storeId; }
    public synchronized void setName(String name) { this.name = name; }
    public synchronized void setDescription(String description) { this.description = description; }
    public synchronized void setPrice(int price) { this.price = price; }
    public synchronized void setQuantity(int quantity) { this.quantity = quantity; }
    public synchronized void setRating(double rating) { this.rating = rating; }
    public synchronized void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", storeId=" + storeId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", rating=" + rating +
                ", category=" + category +
                '}';
    }

    public boolean addRating(String username, double rate) {
        return false;
    }
}
