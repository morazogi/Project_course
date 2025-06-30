package DomainLayer.DomainServices;

import DomainLayer.*;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.*;
import ServiceLayer.EventLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;

public class UserCart {

    private final IToken Tokener;
    private final StoreRepository   storeRepository;
    private final UserRepository    userRepository;
    private final GuestRepository   guestRepository;
    private final ProductRepository productRepository;
    private final OrderRepository   orderRepository;
    private final IToNotify          toNotify;

    /* ------------------------------------------------------------ */
    public UserCart(IToken Tokener,
                    UserRepository userRepository,
                    StoreRepository storeRepository,
                    ProductRepository productRepository,
                    OrderRepository orderRepository,
                    GuestRepository guestRepository,
                    IToNotify toNotify) {

        this.Tokener          = Tokener;
        this.userRepository   = userRepository;
        this.storeRepository  = storeRepository;
        this.productRepository= productRepository;
        this.orderRepository  = orderRepository;
        this.guestRepository  = guestRepository;
        this.toNotify         = toNotify;
    }

    /* ============================================================ */
    /*  INTERNAL HELPERS                                            */
    /* ============================================================ */

    private RegisteredUser getUserById(String userId) {
        try { return userRepository.getById(userId); }
        catch (Exception e) {
            EventLogger.logEvent(userId, "USER_NOT_FOUND");
            throw new IllegalArgumentException("user not found");
        }
    }

    /** Create-if-absent helper (NEW) */
    private Guest getGuestById(String guestId) {

        try {
        /* ---------------------------------------------------------
           1) Normal path: row exists, but we still have to heal it
           --------------------------------------------------------- */
            Guest g = guestRepository.getById(guestId);

            boolean dirty = false;

            /* heal null username */
            if (g.getUsername() == null) {
                g.setUsername(guestId);
                dirty = true;
            }

            /* heal missing cart */
            if (g.getShoppingCart() == null) {
                g.setShoppingCart(new ShoppingCart(guestId));
                dirty = true;
            }

            if (dirty) guestRepository.update(g);   // persist the fix
            return g;

        } catch (Exception ignored) {
        /* ---------------------------------------------------------
           2) Row not found: create a brand-new guest
           --------------------------------------------------------- */
            Guest g = new Guest(guestId);                // ctor sets cart
            g.setUsername(guestId);                      // safety
            if (g.getShoppingCart() == null)             // double-safety
                g.setShoppingCart(new ShoppingCart(guestId));

            return guestRepository.save(g);
        }
    }

    private int quantityInCart(ShoppingCart cart, String storeId, String productId) {
        for (ShoppingBag bag : cart.getShoppingBags())
            if (bag.getStoreId().equals(storeId))
                return bag.getProducts().getOrDefault(productId, 0);
        return 0;
    }

    private void validateAddParams(String token,
                                   String storeId,
                                   String productId,
                                   Integer quantity) {
        if (token == null)                      throw new IllegalArgumentException("Token cannot be null");
        if (storeId == null)                    throw new IllegalArgumentException("StoreId cannot be null");
        if (productId == null)                  throw new IllegalArgumentException("ProductId cannot be null");
        if (quantity == null || quantity <= 0)  throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    /* ============================================================ */
    /*  PUBLIC API                                                  */
    /* ============================================================ */

    /* ---------- REMOVE ------------------------------------------ */
    public void removeFromCart(String token, String storeId,
                               String productId, Integer quantity) throws JsonProcessingException {

        if (token == null || storeId == null || productId == null || quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Bad parameters");

        String username = Tokener.extractUsername(token);
        Tokener.validateToken(token);

        if (username.contains("Guest")) {
            Guest guest = getGuestById(username);            // ← will create if missing
            guest.removeProduct(storeId, productId, quantity);
            guestRepository.update(guest);
        } else {
            RegisteredUser user = getUserById(username);
            user.removeProduct(storeId, productId, quantity);
            userRepository.update(user);
        }
    }

    /* ---------- ADD --------------------------------------------- */
    /* ---------- ADD --------------------------------------------- */
    public void addToCart(String token, String storeId,
                          String productId, Integer quantity) throws JsonProcessingException {

        validateAddParams(token, storeId, productId, quantity);

        String username = Tokener.extractUsername(token);
        Tokener.validateToken(token);

        /* --- NEW: use store’s live quantity, not the Product row --- */
        Store store = storeRepository.getById(storeId);
        if (store == null) throw new IllegalArgumentException("Store not found");
        int available = store.getProductQuantity(productId);
        /* ----------------------------------------------------------- */

        if (available < 0) throw new IllegalArgumentException("Product not found");

        if (username.contains("Guest")) {
            Guest guest = getGuestById(username);
            int already = quantityInCart(guest.getShoppingCart(), storeId, productId);
            if (available < already + quantity)
                throw new IllegalArgumentException("Only " + (available - already) + " left in stock");
            guest.addProduct(storeId, productId, quantity);
            guestRepository.update(guest);
        } else {
            RegisteredUser user = getUserById(username);
            int already = quantityInCart(user.getShoppingCart(), storeId, productId);
            if (available < already + quantity)
                throw new IllegalArgumentException("Only " + (available - already) + " left in stock");
            user.addProduct(storeId, productId, quantity);
            userRepository.update(user);
        }
        EventLogger.logEvent(username, "ADD_TO_CART_SUCCESS");
    }

    /* ---------- RESERVE CART ------------------------------------ */
    public Double reserveCart(String token) throws JsonProcessingException {

        if (token == null)
            throw new IllegalArgumentException("Token cannot be null");

        String username = Tokener.extractUsername(token);
        Tokener.validateToken(token);

        /* fetch customer (creates guest on-the-fly) */
        Guest customer = username.contains("Guest")
                ? getGuestById(username)
                : getUserById(username);

        double totalPrice = 0;
        ShoppingCart cart = customer.getShoppingCart();
        Map<String,Integer> reserved = new HashMap<>();

        /* iterate over every bag and product in the cart */
        for (ShoppingBag bag : cart.getShoppingBags()) {

            String storeId = bag.getStoreId();
            Store  store   = storeRepository.getById(storeId);
            if (store == null)
                throw new IllegalArgumentException("Store not found");

            for (Map.Entry<String,Integer> entry : bag.getProducts().entrySet()) {

                String  pid = entry.getKey();
                int     qty = entry.getValue();

                Product product = productRepository.getById(pid);
                if (product == null)
                    throw new IllegalArgumentException("Product not found");

                /* -------- NEW: use shelf quantity (per-store) -------- */
                Integer shelfQty = store.getProductQuantity(pid);
                if (shelfQty == null || shelfQty < qty)
                    throw new IllegalArgumentException(
                            "Insufficient stock for product: " + pid);
                /* ----------------------------------------------------- */

                /* reserve in the store */
                if (!store.reserveProduct(pid, qty)) {
                    unreserveCart(reserved, username);           // roll back
                    throw new IllegalArgumentException(
                            "Failed to reserve product: " + pid);
                }

                /* bookkeeping */
                reserved.put(pid, qty);
                storeRepository.update(store);
                productRepository.save(product);                 // keep prices up-to-date
                totalPrice += product.getPrice() * qty;
            }
        }

        /* mark cart as reserved */
        customer.setCartReserved(true);
        if (customer instanceof RegisteredUser ru)
            userRepository.update(ru);
        else
            guestRepository.update(customer);

        return totalPrice;
    }


    /* ---------- UNRESERVE (helper) ------------------------------ */
    public void unreserveCart(Map<String,Integer> reserved, String username) throws JsonProcessingException {
        for (Map.Entry<String,Integer> e : reserved.entrySet()) {
            String pid = e.getKey(); int qty = e.getValue();
            Product product = productRepository.getById(pid);
            if (product == null) continue;
            Store store = storeRepository.getById(product.getStoreId());
            if (store != null) {
                store.unreserveProduct(pid, qty);
                storeRepository.update(store);
            }
        }
    }

    /* ---------- PURCHASE CART ----------------------------------- */
    public void purchaseCart(String token, double totalPrice) throws JsonProcessingException {

        if (token == null) throw new IllegalArgumentException("Token cannot be null");
        String username = Tokener.extractUsername(token);
        Tokener.validateToken(token);

        Guest customer = username.contains("Guest")
                ? getGuestById(username)
                : getUserById(username);

        if (!customer.getCartReserved())
            throw new IllegalArgumentException("Cart is not reserved");

        ShoppingCart cart = customer.getShoppingCart();

        for (ShoppingBag bag : cart.getShoppingBags()) {
            Store store = storeRepository.getById(bag.getStoreId());
            if (store == null) throw new IllegalArgumentException("Store not found");

            for (Map.Entry<String,Integer> e : bag.getProducts().entrySet()) {
                String pid = e.getKey(); int qty = e.getValue();
                store.sellProduct(pid, qty);
                storeRepository.update(store);
                if (toNotify != null) {
                    toNotify.sendNotificationToStoreOwners(token, store.getName(), productRepository.getById(pid).getName() + " quantity " + qty +  " sold in your store " + store.getName());
                }
                if (customer instanceof RegisteredUser ru) {
                                        if (!ru.getProducts().contains(pid))
                                                ru.addProduct(pid);
                                    }
                orderRepository.save(new Order(cart.toString(), store.getId(), username, new Date()));
            }
        }
        cart.sold();
        customer.setCartReserved(false);
        if (customer instanceof RegisteredUser ru) userRepository.update(ru);
        else                                       guestRepository.update(customer);
        if (customer instanceof RegisteredUser ru) userRepository.update(ru);
                else                                       guestRepository.update(customer);
    }
}
