package DomainLayer.DomainServices;
import DomainLayer.Roles.Guest;
import InfrastructureLayer.OrderRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import ServiceLayer.EventLogger;
import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import InfrastructureLayer.GuestRepository;
import DomainLayer.Product;
import DomainLayer.ShoppingBag;
import DomainLayer.ShoppingCart;
import DomainLayer.Store;
import DomainLayer.Order;
import DomainLayer.Roles.RegisteredUser;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import org.apache.catalina.User;

public class UserCart {
    private final IToken Tokener;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final GuestRepository guestRepository;

    public UserCart(IToken Tokener,
                    UserRepository userRepository,
                    StoreRepository storeRepository,
                    ProductRepository productRepository,
                    OrderRepository orderRepository,
                    GuestRepository guestRepository) {
        this.orderRepository = orderRepository;
        this.Tokener = Tokener;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.guestRepository = guestRepository;
    }

    private RegisteredUser getUserById(String userId) {
        if (userRepository == null) {
            throw new EntityNotFoundException("UserRepository is null in UserCart class");
        }
        try {
            return userRepository.getById(userId);
        } catch (EntityNotFoundException e) {
            EventLogger.logEvent(userId, "get user by id in UserCart - USER_NOT_FOUND:"+e.toString());
            throw new IllegalArgumentException("user not found");
        }
    }

    private Guest getGuestById(String guestId) {
        if (guestRepository == null) {
            throw new EntityNotFoundException("GuestRepository is null in UserCart class");
        }
        try {
            return guestRepository.getById(guestId);
        }
        catch (Exception e) {
            EventLogger.logEvent(guestId, "get guest by id in UserCart - USER_NOT_FOUND:"+e.toString());
            throw new IllegalArgumentException("User not found");
        }
    }

    public void removeFromCart(String token , String storeId , String productId , Integer quantity) throws JsonProcessingException {
        if (token == null){
            EventLogger.logEvent(Tokener.extractUsername(token), "REMOVE_FROM_CART_FAILED - NULL");
            throw new IllegalArgumentException("Token cannot be null");
        }
        if (storeId == null) {
            EventLogger.logEvent(Tokener.extractUsername(token), "REMOVE_FROM_CART_FAILED - NULL");
            throw new IllegalArgumentException("StoreId cannot be null");
        }
        if (productId == null) {
            EventLogger.logEvent(Tokener.extractUsername(token), "REMOVE_FROM_CART_FAILED - NULL");
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        if (quantity == null) {
            EventLogger.logEvent(Tokener.extractUsername(token), "REMOVE_FROM_CART_FAILED - NULL");
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity <= 0) {
            EventLogger.logEvent(Tokener.extractUsername(token), "REMOVE_FROM_CART_FAILED - INVALID_QUANTITY");
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        //checks if the username belongs to a guest or to a registered user and approaches the appropriate repository
        String username = Tokener.extractUsername(token);
        if (!username.contains("Guest")) {
            try {
                RegisteredUser user = userRepository.getById(username);
                Tokener.validateToken(token);
                user.removeProduct(storeId, productId, quantity);
                userRepository.update(user);
                EventLogger.logEvent(user.getUsername(), "REMOVE_FROM_CART_SUCCESS");

            }
            catch (EntityNotFoundException e) {
                EventLogger.logEvent(Tokener.extractUsername(token), "REMOVE_FROM_CART_FAILED - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        } else {
            try {
                Guest user = guestRepository.getById(username);
                Tokener.validateToken(token);
                user.removeProduct(storeId, productId, quantity);
                guestRepository.update(user);
                EventLogger.logEvent(user.getUsername(), "REMOVE_FROM_CART_SUCCESS");
            }
            catch (Exception e) {
                EventLogger.logEvent(Tokener.extractUsername(token), "REMOVE_FROM_CART_FAILED - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        }
    }

    public void addToCart(String token, String storeId, String productId, Integer quantity) throws JsonProcessingException {
        validateAddParams(token, storeId, productId, quantity);          // existing validation

        String username = Tokener.extractUsername(token);
        Tokener.validateToken(token);

        Product product = productRepository.getById(productId);
        if (product == null) throw new IllegalArgumentException("Product not found");

        int available = product.getQuantity();

        if (username.contains("Guest")) {
            Guest guest = guestRepository.getById(username);
            int already = quantityInCart(guest.getShoppingCart(), storeId, productId);
            if (available < already + quantity)
                throw new IllegalArgumentException("Only " + (available - already) + " left in stock");
            guest.addProduct(storeId, productId, quantity);
            guestRepository.update(guest);
        } else {
            RegisteredUser user = userRepository.getById(username);
            int already = quantityInCart(user.getShoppingCart(), storeId, productId);
            if (available < already + quantity)
                throw new IllegalArgumentException("Only " + (available - already) + " left in stock");
            user.addProduct(storeId, productId, quantity);
            userRepository.update(user);
        }

        EventLogger.logEvent(username, "ADD_TO_CART_SUCCESS");
    }

    private int quantityInCart(ShoppingCart cart, String storeId, String productId) {
        for (ShoppingBag bag : cart.getShoppingBags())
            if (bag.getStoreId().equals(storeId))
                return bag.getProducts().getOrDefault(productId, 0);
        return 0;
    }

    public Double reserveCart(String token) throws JsonProcessingException {
        if (token == null) {
            EventLogger.logEvent(Tokener.extractUsername(token), "PURCHASE_CART_FAILED - NULL");
            throw new IllegalArgumentException("Token cannot be null");
        }
        String username = Tokener.extractUsername(token);
        Guest user;
        boolean isRegisteredUser = !username.contains("Guest");
        if(isRegisteredUser){
            try {
                user = (RegisteredUser) userRepository.getById(username);
            }
            catch (Exception e) {
                EventLogger.logEvent(username, "RESERVE_CART_FAILED - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        }
        else {
            try {
                user = userRepository.getById(username);
            }
            catch (Exception e) {
                EventLogger.logEvent(username, "RESERVE_CART_FAILED - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        }
        Tokener.validateToken(token);
        double totalPrice = 0;
        ShoppingCart cart = user.getShoppingCart();
        Map<String, Integer> reservedProducts = new HashMap<>();
        for (ShoppingBag bag : cart.getShoppingBags()) {
            String storeId = bag.getStoreId();
            Store store;
            try {
                store = storeRepository.getById(storeId);
            } catch (Exception e) {
                EventLogger.logEvent(user.getUsername(), "RESERVE_CART_FAILED - STORE_NOT_FOUND");
                throw new IllegalArgumentException("Store not found");
            }
            for (Map.Entry<String, Integer> entry : bag.getProducts().entrySet()) {
                String productId = entry.getKey();
                Integer quantity = entry.getValue();
                Product product = productRepository.getById(productId);
                if (product == null) {
                    EventLogger.logEvent(user.getUsername(), "PURCHASE_CART_FAILED - PRODUCT_NOT_FOUND");
                    throw new IllegalArgumentException("Product not found");
                }
                if (product.getQuantity() < quantity) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + productId);
                }
                if(store.reserveProduct(productId, quantity)){
                    EventLogger.logEvent(user.getUsername(), "RESERVE_PRODUCT_SUCCESS");
                    reservedProducts.put(productId, quantity);
                }else{
                    unreserveCart(reservedProducts, user.getUsername());
                    EventLogger.logEvent(user.getUsername(), "PURCHASE_CART_FAILED - RESERVE_FAILED");
                    throw new IllegalArgumentException("Failed to reserve product: " + productId);
                }
                storeRepository.update(store);
                productRepository.save(product);
                totalPrice += product.getPrice() * quantity;
            }
        }
        user.setCartReserved(true);
        if(isRegisteredUser) userRepository.update((RegisteredUser) user);
        else guestRepository.update(user);
        return totalPrice;
    }

    public void unreserveCart(Map<String, Integer> reservedProducts ,String username) throws JsonProcessingException {
        for(Map.Entry<String, Integer> entry : reservedProducts.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            Product product = productRepository.getById(productId);
            if (product == null) {
                EventLogger.logEvent(username, "UNRESERVE_CART_FAILED - PRODUCT_NOT_FOUND");
                throw new IllegalArgumentException("Product not found");
            }
            if (product.getQuantity() < quantity) {
                EventLogger.logEvent(username, "UNRESERVE_CART_FAILED - INSUFFICIENT_STOCK");
                throw new IllegalArgumentException("Insufficient stock for product: " + productId);
            }
            Store store = storeRepository.getById(product.getStoreId());
            if (store == null) {
                EventLogger.logEvent(username, "UNRESERVE_CART_FAILED - STORE_NOT_FOUND");
                throw new IllegalArgumentException("Store not found");
            }
            store.unreserveProduct(productId, quantity);
            storeRepository.update(store);
            productRepository.save(product);
        }
    }

    public void purchaseCart(String token , double totalPrice) throws JsonProcessingException {
        Guest user;
        if (token == null) {
            EventLogger.logEvent(Tokener.extractUsername(token), "PURCHASE_CART_FAILED - NULL");
            throw new IllegalArgumentException("Token cannot be null");
        }
        String username = Tokener.extractUsername(token);
        boolean isRegisteredUser = !username.contains("Guest");
        if (isRegisteredUser) {
            try {
                user = (RegisteredUser) userRepository.getById(username);
            }
            catch (Exception e) {
                EventLogger.logEvent(username, "PURCHASE_CART_FAILED - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        }
        else {
            try {
                user = guestRepository.getById(username);
            }
            catch (Exception e) {
                EventLogger.logEvent(username, "PURCHASE_CART_FAILED - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        }

        if (!user.getCartReserved()) {
            EventLogger.logEvent(user.getUsername(), "PURCHASE_CART_FAILED - CART_NOT_RESERVED");
            throw new IllegalArgumentException("Cart is not reserved");
        }
        ShoppingCart cart = user.getShoppingCart();
        // tell the store the products are sold
        for (ShoppingBag bag : cart.getShoppingBags()) {
            String storeId = bag.getStoreId();
            Store store = storeRepository.getById(storeId);
            if (store == null) {
                EventLogger.logEvent(user.getUsername(), "PURCHASE_CART_FAILED - STORE_NOT_FOUND");
                throw new IllegalArgumentException("Store not found");
            }
            for (Map.Entry<String, Integer> entry : bag.getProducts().entrySet()) {
                String productId = entry.getKey();
                Integer quantity = entry.getValue();
                Product product = productRepository.getById(productId);
                if (product == null) {
                    EventLogger.logEvent(user.getUsername(), "PURCHASE_CART_FAILED - PRODUCT_NOT_FOUND");
                    throw new IllegalArgumentException("Product not found");
                }
                store.sellProduct(productId, quantity);
                storeRepository.update(store);
                Order order = new Order(user.getShoppingCart().toString(), storeId, username, new Date());
                orderRepository.save(order);
            }
        }
        // create an order
//        orderRepository.addOrder(new Order(mapper.writeValueAsString(cart), username , totalPrice));
        user.setCartReserved(false);
//        user.getShoppingCart().clearBags();
        if(isRegisteredUser) userRepository.update((RegisteredUser) user);
        else guestRepository.update(user);
    }


    private void validateAddParams(String token,
                                   String storeId,
                                   String productId,
                                   Integer quantity) {
        if (token == null)          throw new IllegalArgumentException("Token cannot be null");
        if (storeId == null)        throw new IllegalArgumentException("StoreId cannot be null");
        if (productId == null)      throw new IllegalArgumentException("ProductId cannot be null");
        if (quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than 0");
    }
}  