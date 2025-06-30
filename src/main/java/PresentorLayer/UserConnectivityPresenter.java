package PresentorLayer;

import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingBag;
import DomainLayer.Store;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.EventLogger;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.notification.Notification;
import jakarta.transaction.Transactional;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserConnectivityPresenter {

    private final UserService userService;
    private final RegisteredService registeredService;
    private final OwnerManagerService ownerManagerService;
    private final IToken tokenService;
    private final UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public UserConnectivityPresenter(UserService userService, RegisteredService registeredService, OwnerManagerService ownerManagerService, IToken tokenService, UserRepository userRepository) {
        this.userService = userService;
        this.registeredService = registeredService;
        this.ownerManagerService = ownerManagerService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public void purchaseCart(String token ,
                             String name ,
                             String cardNumber,
                             String expirationDate,
                             String cvv,
                             String state,
                             String city,
                             String address,
                             String zip,
                             String id) throws Exception {
        userService.purchaseCart(token, name, cardNumber, expirationDate, cvv, state, city, address, id, zip);
    }


    public List<ShoppingBag> getShoppingBags(String token) {
        return userService.getShoppingCart(token);
    }

    public void addStore(String token, String storeName) throws Exception {
        String storeId = registeredService.openStore(token, storeName);
        System.out.println(storeId);
        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("dfsa");
        }
        System.out.println(ownerManagerService.appointStoreOwner(user.getUsername(), storeId, user.getUsername()));
        boolean[] arr = new boolean[9];
        arr[0] = true;
        arr[1] = true;
        arr[2] = true;
        arr[3] = true;
        arr[4] = true;
        arr[5] = true;
        arr[6] = true;
        arr[7] = true;
        arr[8] = true;
        System.out.println(ownerManagerService.appointStoreManager(user.getUsername(), storeId, user.getUsername(), arr));
        // username = tokenService.extractUsername(token);
        user = null;
        try {
       //     user = userRepository.getById(username);
        } catch (Exception e) {
        //    throw new Exception(e.getMessage());
        }

        //System.out.println(mapper.writeValueAsString(user));
    }


    public void removeFromCart(String token,
                               String storeId,
                               String productId,
                               int quantity) {

        userService.removeFromCart(token, storeId, productId, quantity);
    }

    public String getInformationAboutProduct(String token, String storeName, String productName) {
        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
        try {
            List<Store> managedStores = getUserStoresName(token);
            List<Product> products = userService.getAllProducts(token);
            for (Store store: managedStores) {
            if (store.getName().equals(storeName)) {
                for (Product product: products) {
                    if (product.getStoreId().equals(store.getId()) & product.getName().equals(productName)) {
                        return "Product name: " + product.getName() + "\nProduct price: " + product.getPrice() + "\nProduct quantity: " + product.getQuantity() + "\nProduct category: "  + product.getCategory() + "\nProduct description: " + product.getDescription();
                    }
                }
                return "could not find this product in the store";
            }
        }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "user can't edit this store";
    }

    public String addNewProductToStore(String token, String storeName, String productName, String description, String stringPrice, String stringQuantity, String category) {
        Double price = 0.0;
        Integer quantity = 0;
        try {
            price = Double.valueOf(stringPrice);
        } catch (Exception e) {
            return "Invalid price";
        }
        try {
            quantity = Integer.valueOf(stringQuantity);
        } catch (Exception e) {
            return "Invalid quantity";
        }
        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }

        try {
            List<Store> managedStores = getUserStoresName(token);
            List<Product> products = userService.getAllProducts(token);
            for (Store store: managedStores) {
            try {

            } catch (Exception e) {
                return e.getMessage();
            }
            if (store.getName().equals(storeName)) {
                return ownerManagerService.addProduct(user.getUsername(), store.getId(), productName, description, price.floatValue(), quantity, category);
            }
        }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "user can't edit this store";
    }

    public String removeProductFromStore(String token, String storeName, String productName) {
        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }

        try {
            List<Store> managedStores = getUserStoresName(token);
            List<Product> products = userService.getAllProducts(token);
            for (Store store: managedStores) {
                try {
                        for (Product product : products) {
                            if (product.getStoreId().equals(store.getId()) & product.getName().equals(productName)) {
                                return ownerManagerService.removeProduct(user.getUsername(), store.getId(), product.getId());
                            }

                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
                if (store.getName().equals(storeName)) {
                    for (Product product : products) {
                        if (product.getStoreId().equals(store.getId()) & product.getName().equals(productName)) {
                            return ownerManagerService.removeProduct(user.getUsername(), store.getId(), product.getId());
                        }
                    }
                    return "could not find this product in store";
                }
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "user can't edit this store";
    }

    public String updateProduct(String token, String storeName, String productName, String description, String price, String newProductName, String category) {
        Double doublePrice = 0.0;
        try {
            doublePrice = Double.valueOf(price);
        } catch (Exception e) {
            return "Invalid price";
        }
        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }

        try {
            List<Store> managedStores = getUserStoresName(token);
            List<Product> products = userService.getAllProducts(token);
            for (Store store: managedStores) {
            try {
                    for (Product product: products) {
                        if (product.getStoreId().equals(store.getId()) & product.getName().equals(productName)) {
                           return ownerManagerService.updateProductDetails(user.getShoppingCart().getUserId(), store.getId(), product.getId(), newProductName, description, doublePrice, category);
                        }



                } }
            catch (Exception e) {
                return e.getMessage();
            }
            if (store.getName().equals(storeName)) {
                for (Product product: products) {
                    if (product.getStoreId().equals(store.getId()) & product.getName().equals(productName)) {
                        return ownerManagerService.updateProductDetails(user.getShoppingCart().getUserId(), store.getId(), product.getId(), newProductName, description, doublePrice, category);
                    }
                }
                return "could not find this product in the store";
            }
        }
        } catch (Exception e) {
            return e.getMessage();
        }

        return "user can't edit this store";
    }

    public void signUp(String username, String password) throws Exception{
        userService.signUp(username, password);
    }

    public String login(String username, String password) throws Exception {
        return userService.login(username, password);
    }

    public LinkedList<Store> getUserStoresName(String token) throws Exception {
        String username;
        try {
            username = tokenService.extractUsername(token);
        } catch (Exception e) {
            throw new Exception(e.getMessage() + "1");
        }

        RegisteredUser user;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            Notification.show(e.getMessage());
            throw e;
        }

        LinkedList<Store> storeNames = new LinkedList<>();
        List<String> managedStores   = user.getManagedStores();

        // DEBUG lines kept
        System.out.println("DEBUG: User '" + username + "' has managedStores: " + managedStores);

        for (String managedStore : managedStores) {
            System.out.println("DEBUG: Attempting to retrieve details for storeId: " + managedStore);

            String jsonStore = userService.getStoreById(token, managedStore);
            Store  store     = mapper.readValue(jsonStore, Store.class);

            /*  ★ NO filter here → managers always see their stores  */
            storeNames.add(store);
        }


        List<String> owneedStores   = user.getOwnedStores();

        // DEBUG lines kept
        System.out.println("DEBUG: User '" + username + "' has managedStores: " + managedStores);

        for (String managedStore : owneedStores) {
            System.out.println("DEBUG: Attempting to retrieve details for storeId: " + managedStore);

            String jsonStore = userService.getStoreById(token, managedStore);
            Store  store     = mapper.readValue(jsonStore, Store.class);

            /*  ★ NO filter here → managers always see their stores  */
            Boolean hasa = false;
            for (Store storename : storeNames) {
            if (user.getManagedStores().contains(storename.getId())) {
            hasa = true;
            }
            }
            if (!hasa) {
            storeNames.add(store);
            }
        }

        System.out.println(storeNames);
        return storeNames;
    }



    public String addDiscount(String token,
                              String storeNameOrId,
                              float discountLevel,
                              float logicComposition,
                              float numericalComposition,
                              float percentDiscount,
                              String discountedItem,
                              float discountCondition,
                              float discountLimiter,
                              float conditional,
                              String conditionalDiscounted,
                              List<String> discountsId) {

        /* ---------- who’s calling? ---------- */
        String username = tokenService.extractUsername(token);
        RegisteredUser user;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            return e.getMessage();
        }

        /* ---------- resolve name → id ---------- */
        Store   store   = getStore(token, storeNameOrId);   // try as id first
        String  storeId = storeNameOrId;

        if (store == null) {                                // maybe it was a name
            try {
                for (Store s : getUserStoresName(token)) {
                    if (s.getName().equals(storeNameOrId)) {
                        store   = s;
                        storeId = s.getId();
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (store == null) return "Store not found";

        /* ---------- permission check ---------- */
        boolean allowed;
        try {
            allowed = ownerManagerService.isFounderOrOwner(username, storeId);
        } catch (Exception e) {
            allowed = false;
        }
        if (!allowed) {
            Map<String, Boolean> perms =
                    ownerManagerService.getManagerPermissions(username, storeId, username);
            allowed = perms != null && Boolean.TRUE.equals(perms.get("PERM_UPDATE_POLICY"));
        }
        if (!allowed) return "User is not allowed to add discount";

        /* ---------- sanitise inputs ---------- */
        if (conditional != 1 && conditional != 2 && conditional != 3) {
            conditional = -1;                    // sentinel for “no condition”
        }

        /* ---------- service call ---------- */
        boolean ok = ownerManagerService.defineDiscountPolicy(
                username,
                storeId,
                "",                    // parent-id (we’re creating a fresh one)
                "",                    // own-id   (service generates)
                discountLevel,
                logicComposition,
                numericalComposition,
                discountsId,           // <<<<< nested children
                percentDiscount,
                discountedItem,
                discountCondition,
                discountLimiter,
                conditionalDiscounted
        );

        if (ok) {
            EventLogger.logEvent(username, "Discount successfully added");
            return "Discount successfully added";
        }
        return "Did not manage to add discount";
    }

    /* Convenience overload kept for old callers (simple voucher) */
    public String addDiscount(String token,
                              String storeNameOrId,
                              float discountLevel,
                              float logicComposition,
                              float numericalComposition,
                              float percentDiscount,
                              String discountedItem,
                              float discountCondition,
                              float discountLimiter,
                              float conditional,
                              String conditionalDiscounted) {

        return addDiscount(token, storeNameOrId, discountLevel, logicComposition,
                numericalComposition, percentDiscount, discountedItem,
                discountCondition, discountLimiter, conditional,
                conditionalDiscounted, List.of());
    }





    public double calculateCartPrice(String token) {
        return userService.calculateCartPrice(token);
    }

    public Map<String,Integer> getCartProducts(String token) {
        return userService.getCartProducts(token);
    }

    public Store getStore(String token, String storeId) {
        try {
            return mapper.readValue(userService.getStoreById(token, storeId), Store.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getUsername(String token) {
        return tokenService.extractUsername(token);
    }

    public String openStore(String token, String storeId) {
        String username = "";
        try {
            username = tokenService.extractUsername(token);
        } catch (Exception e) {
            return e.getMessage();
        }
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
        return ownerManagerService.reopenStore(user.getShoppingCart().getUserId(), storeId);
    }

    public String closeStore(String token, String storeId) {
        String username = "";
        try {
            username = tokenService.extractUsername(token);
        } catch (Exception e) {
            return e.getMessage();
        }
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
        return ownerManagerService.closeStore(user.getShoppingCart().getUserId(), storeId);
    }

    public String getUserId(String token) {
        if (tokenService.getToken("Guest").equals(token)) {
            System.out.println(true);
            return "";
        }
        return userRepository.getById(tokenService.extractUsername(token)).getShoppingCart().getUserId();
    }

}