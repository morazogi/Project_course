package DomainLayer;
import DomainLayer.Roles.RegisteredUser;
import ServiceLayer.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class User {
    protected String id = UUID.randomUUID().toString();
    protected ShoppingCart shoppingCart;
    protected String myToken;
    protected String name;

    public User() {
        this.shoppingCart = new ShoppingCart(id);
        this.name = "";
    }

    public User(String name) {
        this.shoppingCart = new ShoppingCart(id);
        this.name = name;
    }

    // Functions for all users (registered and unregistered)

    // Enter the market - already implemented as constructor

    // Exit the market
    public void exitMarket() {
        // Implementation depends on whether the user is a guest or registered user
        // For registered users, the shopping cart is saved
        // For guests, the shopping cart is lost
    }

    // Get information about stores
    public List<Store> getStoreInfo() {
        // Implementation to get information about stores in the market
        return new ArrayList<>(); // Placeholder
    }

    // Get information about products in a store
    public List<Product> getProductInfo(Store store) {
        // Implementation to get information about products in a specific store
        return new ArrayList<>(); // Placeholder
    }

    // Search for products by name, category, or keywords
    public List<Product> searchProducts(String query, String searchType) {
        // searchType can be "name", "category", or "keyword"
        // Implementation to search for products
        return new ArrayList<>(); // Placeholder
    }

    // Filter search results
    public List<Product> filterSearchResults(List<Product> searchResults, Map<String, Object> filters) {
        // filters can include price range, product rating, category, store rating, etc.
        // Implementation to filter search results
        return new ArrayList<>(); // Placeholder
    }

    // Add product to shopping cart
    public void addProduct(Store store, Product product){    //Store helps shopping cart to know to what shopping bag
        shoppingCart.addProduct(store , product);
    }

    // Remove product from shopping cart
    public void removeProduct(Store store, Product product){    //Store helps shopping cart to know to what shopping bag
        shoppingCart.removeProduct(store, product);
    }

    // View shopping cart contents
    public String viewShoppingCart() {
        // Implementation to view shopping cart contents
        return shoppingCart.toString();
    }

    // Purchase shopping cart
    public boolean purchaseShoppingCart() {
        // Implementation to purchase shopping cart
        if (shoppingCart.availablePurchaseCart()) {
            double price = shoppingCart.calculatePurchaseCart();
            if (price > 0) {
                shoppingCart.sold();
                return true;
            }
        }
        return false;
    }

    public String getToken() {
        return myToken;
    }

    public void setToken(String token) {
        myToken = token;
    }

    public String getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void logout() {
        myToken = null;
    }
}
