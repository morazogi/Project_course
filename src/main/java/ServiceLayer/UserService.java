package ServiceLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.*;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final IToken tokenService;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ShippingService shippingService;
    private final PaymentService paymentService;
    private final DiscountPolicyMicroservice discountPolicy;
    private final UserConnectivity userConnectivity;
    private final UserCart userCart;
    private final Search search;
    private final GuestRepository guestRepository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserService(IToken tokenService,
                       StoreRepository storeRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       ShippingService shippingService,
                       PaymentService paymentService,
                       GuestRepository guestRepository,
                       DiscountRepository discountRepository,
                       NotificationRepository notificationRepository,
                       NotificationWebSocketHandler notificationWebSocketHandler
                       ) {
        this.productRepository = productRepository;
        this.storeRepository   = storeRepository;
        this.guestRepository   = guestRepository;
        this.userRepository    = userRepository;
        this.tokenService      = tokenService;
        this.shippingService   = shippingService;
        this.paymentService    = paymentService;
        this.userConnectivity  = new UserConnectivity(tokenService, userRepository, guestRepository);
        this.userCart          = new UserCart(tokenService, userRepository, storeRepository,
                productRepository, orderRepository, guestRepository, new ToNotify(notificationRepository, tokenService, notificationWebSocketHandler,userRepository, storeRepository));
        this.search            = new Search(productRepository, storeRepository);
        this.discountPolicy    = new DiscountPolicyMicroservice(storeRepository, userRepository,
                productRepository, discountRepository);
    }

    /* ───────────────────────── login / signup ───────────────────────── */

    @Transactional
    public String login(String username, String password) throws JsonProcessingException {
        try {
            EventLogger.logEvent(username, "LOGIN");
            return userConnectivity.login(username, password);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null &&
                    e.getMessage().toLowerCase().contains("suspended"))
                throw new RuntimeException("suspended");
            EventLogger.logEvent(username, "LOGIN_FAILED");
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public void signUp(String username, String password) throws Exception {
        try {
            userConnectivity.signUp(username, password);
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(username, "SIGNUP_FAILED");
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "User already exists" : e.getMessage();
            throw new RuntimeException(msg);
        }
    }

    /* ───────────────────────── cart-clean helpers ───────────────────── */

    /** Remove any items whose product-ID no longer exists. */
    private boolean cleanMissingProducts(String token) {
        boolean changed = false;
        for (ShoppingBag bag : getShoppingCart(token)) {
            String storeId = bag.getStoreId();
            for (Map.Entry<String,Integer> e : bag.getProducts().entrySet()) {
                String productId = e.getKey();
                int    qty       = e.getValue();
                try { productRepository.getById(productId); }
                catch (Exception ex) {
                    try { userCart.removeFromCart(token, storeId, productId, qty); }
                    catch (Exception ignored) {}
                    changed = true;
                }
            }
        }
        return changed;
    }

    /** True ⇢ after clean-up the cart is empty. */
    private boolean cartIsEmpty(String token) {
        for (ShoppingBag bag : getShoppingCart(token))
            if (!bag.getProducts().isEmpty()) return false;
        return true;
    }

    /* ───────────────────────── cart operations ──────────────────────── */

    @Transactional
    public void removeFromCart(String token, String storeId, String productId, Integer quantity) {
        try {
            userCart.removeFromCart(token, storeId, productId, quantity);
            EventLogger.logEvent(tokenService.extractUsername(token), "REMOVE_FROM_CART");
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token),
                    "REMOVE_FROM_CART_FAILED " + e.getMessage());
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "Failed to remove product from cart" : e.getMessage();
            throw new RuntimeException(msg);
        }
    }

    @Transactional
    public String addToCart(String token, String storeId, String productId, Integer quantity) {
        try {
            userCart.addToCart(token, storeId, productId, quantity);
            EventLogger.logEvent(tokenService.extractUsername(token), "ADD_TO_CART");
            return "Product added to cart";
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(tokenService.extractUsername(token),
                    "ADD_TO_CART_FAILED " + e.getMessage());
            return e.getMessage();
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token),
                    "ADD_TO_CART_FAILED " + e.getMessage());
            return "Failed to add product to cart";
        }
    }

    @Transactional
    public Double reserveCart(String token) {
        try {
            return userCart.reserveCart(token);
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "RESERVE_CART_FAILED");
            throw new RuntimeException("Failed to reserve cart");
        }
    }

    @Transactional
    public void purchaseCart(String token,
                             String name,
                             String cardNumber,
                             String expirationDate,
                             String cvv,
                             String state,
                             String city,
                             String address,
                             String id,
                             String zip) {

        String paymentTransactionId  = null;
        String shippingTransactionId = null;

        try {
            /* ───────────── cart-sanity checks ───────────── */
            if (cleanMissingProducts(token))
                throw new RuntimeException("cart changed");

            if (cartIsEmpty(token))
                throw new RuntimeException("cart is empty");

            /* ──────── NEW: block closed stores here ──────── */
            for (ShoppingBag bag : getShoppingCart(token)) {
                Store store = storeRepository.getById(bag.getStoreId());
                if (store == null)
                    throw new RuntimeException("Store not found");
                if (!store.isOpenNow())
                    throw new RuntimeException(
                            "Cannot purchase – store '" + store.getName() + "' is closed");
            }
            /* ─────────────────────────────────────────────── */

            /* reserve inventory & get final price */
            Double price = reserveCart(token);

            /* shipping + payment */
            shippingTransactionId = shippingService.processShipping(
                    token, state, city, address, name, zip);
            paymentTransactionId  = paymentService.processPayment(
                    token, name, cardNumber, expirationDate, cvv, id);

            /* commit cart */
            userCart.purchaseCart(token, price);

        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token),
                    "PURCHASE_CART_FAILED " + e.getMessage());

            if (shippingTransactionId != null)
                shippingService.cancelShipping(token, shippingTransactionId);
            if (paymentTransactionId != null)
                paymentService.cancelPayment(token, paymentTransactionId);

            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "Failed to purchase cart" : e.getMessage();
            throw new RuntimeException(msg);
        }
    }


    /* ─────────────────────― search / miscellaneous ―─────────────────── */

    @Transactional
    public List<String> findProduct(String token, String name, String category) {
        try { tokenService.validateToken(token); }
        catch (Exception ex) {
            throw new PermissionException(
                    "No permission: you must be logged-in to search products.");
        }
        return search.findProduct(name == null ? "" : name, category);
    }

    @Transactional
    public List<Product> getAllProducts(String token) {
        try {
            tokenService.validateToken(token);
            return productRepository.findAll();
        } catch (Exception e) {
            System.out.println("ERROR getting all products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public List<String> getStoreByName(String token, String name) {
        try {
            tokenService.validateToken(token);

            List<String> rawIds = search.getStoreByName(name);
            List<String> openIds = new ArrayList<>();

            /* ★ NEW – keep only stores that are still open */
            for (String id : rawIds) {
                try {
                    Store s = storeRepository.getById(id);
                    if (s != null && s.isOpenNow()) {
                        openIds.add(id);
                    }
                } catch (Exception ignored) {
                    // store row vanished ⇒ just skip it
                }
            }
            return openIds;

        } catch (Exception e) {
            System.out.println("ERROR finding store by Name:" + e.getMessage());
            return Collections.emptyList();
        }
    }


    @Transactional
    public List<Store> searchStoreByName(String token, String storeName) {
        try {
            List<Store> found = search.searchStoreByName(storeName);

            /* ★ NEW – customers only see stores that are currently open */
            List<Store> visible = new ArrayList<>();
            for (Store s : found) {
                if (s.isOpenNow()) {
                    visible.add(s);
                }
            }
            return visible;

        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "SEARCH_STORE_FAILED");
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "Failed to search store" : e.getMessage();
            throw new RuntimeException(msg);
        }
    }


    @Transactional
    public String getStoreById(String token, String storeId) {
        try {
            return search.getStoreById(storeId);
        } catch (Exception e) {
            String username = "unknown";
            try { username = tokenService.extractUsername(token); }
            catch (Exception ignored) {}
            EventLogger.logEvent(username,
                    "SEARCH_STORE_FAILED: " + e.getClass().getName() +
                            " - " + e.getMessage());
            throw new RuntimeException("Failed to search store. Cause: " +
                    e.getMessage(), e);
        }
    }

    public Optional<Product> getProductById(String id) {
        try {
            return productRepository.findById(id);
        } catch (Exception e) {
            System.out.println("ERROR finding product by ID:" + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Product> getProductsInStore(String storeid) {
        try {
            return search.getProductsByStore(storeid);
        } catch (Exception e) {
            System.out.println("ERROR finding product by ID:" + e.getMessage());
        }
        return null;
    }

    @Transactional
    public double calculateCartPrice(String token) {
        if (token == null)
            throw new IllegalArgumentException("Token cannot be null");

        String username = tokenService.extractUsername(token);
        Guest user;
        boolean isRegistered = !username.contains("Guest");
        try {
            user = isRegistered
                    ? (RegisteredUser) userRepository.getById(username)
                    : guestRepository.getById(username);
        } catch (Exception e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "User not found" : e.getMessage();
            throw new RuntimeException(msg);
        }

        ShoppingCart cart = user.getShoppingCart();
        double total = 0;
        for (ShoppingBag bag : cart.getShoppingBags()) {
            total += discountPolicy.calculatePrice(bag.getStoreId(),
                    bag.getProducts());
        }
        return total;
    }

    /**
     * Return every product in the user’s cart with its quantity.
     * Key = product name, Value = total quantity across all stores.
     */
    @Transactional
    public Map<String,Integer> getCartProducts(String token) {
        if (token == null)
            throw new IllegalArgumentException("Token cannot be null");

        String username = tokenService.extractUsername(token);
        Guest user = username.contains("Guest")
                ? guestRepository.getById(username)
                : userRepository.getById(username);

        Map<String,Integer> result = new LinkedHashMap<>();
        for (ShoppingBag bag : user.getShoppingCart().getShoppingBags()) {
            for (Map.Entry<String,Integer> e : bag.getProducts().entrySet()) {
                String productId = e.getKey();
                int qty          = e.getValue();
                String name      = productRepository.getById(productId).getName();
                result.merge(name, qty, Integer::sum);
            }
        }
        return result;
    }

    @Transactional
    public List<ShoppingBag> getShoppingCart(String token) {
        if (token == null)
            throw new IllegalArgumentException("Token cannot be null");

        String username = tokenService.extractUsername(token);
        Guest user = username.contains("Guest")
                ? guestRepository.getById(username)
                : userRepository.getById(username);

        List<ShoppingBag> shoppingBags = new ArrayList<>();
        for (ShoppingBag bag : user.getShoppingCart().getShoppingBags()) {
            ShoppingBag copy = new ShoppingBag(bag.getStoreId());
            for (Map.Entry<String,Integer> e : bag.getProducts().entrySet()) {
                copy.addProduct(e.getKey(), e.getValue());
            }
            shoppingBags.add(copy);
        }
        return shoppingBags;
    }
}
