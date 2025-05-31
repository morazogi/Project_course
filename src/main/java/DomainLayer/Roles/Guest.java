package DomainLayer.Roles;

import java.util.UUID;

import DomainLayer.Product;
import DomainLayer.ShoppingBag;
import DomainLayer.ShoppingCart;
import DomainLayer.Store;
import io.micrometer.observation.Observation.Event;
import ServiceLayer.EventLogger;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id = UUID.randomUUID().toString();

    @Column(name = "username", nullable = false, unique = true)
    private String username = "Guest" + counter++;

    @Column(name = "cart_reserved")
    private Boolean cartReserved = false;

    @Transient
    protected ShoppingCart shoppingCart = new ShoppingCart(id);

    @Transient
    protected static int counter = 0;

    public Guest() {
    }

    public void addProduct(String storeId, String productId, Integer quantity) {
        boolean found = false;
        for (ShoppingBag shoppingBag : shoppingCart.getShoppingBags()) {
            if (shoppingBag.getStoreId().equals(storeId)) {
                shoppingBag.addProduct(productId , quantity);
                found = true;
                break;
            }
        }
        if (!found) {
            ShoppingBag shoppingBag = new ShoppingBag(storeId);
            shoppingBag.addProduct(productId , quantity);
            shoppingCart.getShoppingBags().add(shoppingBag);
        }
    }

    public void removeProduct(String storeId, String productId , Integer quantity) {
        for (ShoppingBag shoppingBag : shoppingCart.getShoppingBags()) {
            if (shoppingBag.getStoreId().equals(storeId)) {
                boolean found = shoppingBag.removeProduct(productId , quantity);
                if (found) {
                    if (shoppingBag.getProducts().isEmpty()) {
                        shoppingCart.getShoppingBags().remove(shoppingBag);
                        EventLogger.logEvent("removeProduct", "Shopping bag removed from cart");
                    }
                    EventLogger.logEvent("removeProduct", "Product removed from cart");
                    return;
                } else {
                    EventLogger.logEvent("removeProduct", "Product not found in cart");
                    throw new IllegalArgumentException("Product not found in cart");
                }
            }
        }
        EventLogger.logEvent("removeProduct", "Product not found in cart");
        throw new IllegalArgumentException("Product not found in cart");
    }

    public String getID() {
        return this.id;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setCartReserved(Boolean cartReserved) {
        this.cartReserved = cartReserved;
    }


    public Boolean getCartReserved() {
        return cartReserved;
    }

}
