package DomainLayer.Roles;

import java.util.UUID;

import DomainLayer.ShoppingBag;
import DomainLayer.ShoppingCart;
import DomainLayer.Store;
import ServiceLayer.EventLogger;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "guests")
public class Guest {

    @Id
    @Column(name = "username", nullable = false, unique = true)
    protected String username;

    @Column(name = "cart_reserved")
    protected Boolean cartReserved;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "shopping_cart_id")
    protected ShoppingCart shoppingCart;

    public Guest() {
        this.username = "Guest" + UUID.randomUUID().toString();
        this.cartReserved = false;
        this.shoppingCart = new ShoppingCart(username);
    }

    public Guest(String username) {
        this.username = username;
        this.cartReserved = false;
        this.shoppingCart = new ShoppingCart(username);
    }
    //================getters===================
    public String getUsername() { return username; }
    public ShoppingCart getShoppingCart() { return shoppingCart; }
    //================setters===================
    public void setUsername(String userName) { this.username = userName; }
    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
    //------------------------------------------
    public void addProduct(String storeId, String productId, Integer quantity) {
        if(quantity <= 0 ) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
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

    public void setCartReserved(Boolean cartReserved) {
        this.cartReserved = cartReserved;
    }

    public Boolean getCartReserved() {
        return cartReserved;
    }

}
