package DomainLayer.DomainServices;

import DomainLayer.*;
import DomainLayer.IPayment;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.*;
import ServiceLayer.EventLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.ProductKeyModule;
import InfrastructureLayer.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentConnectivity {
    private final ObjectMapper mapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final IPayment proxyPayment;
    private final StoreRepository storeRepository;
    private final DiscountRepository discountRepository;
    private final GuestRepository guestRepository;


    public PaymentConnectivity(IPayment proxyPayment, UserRepository userRepository, ProductRepository productRepository, StoreRepository storeRepository, DiscountRepository discountRepository, GuestRepository guestRepository) {
        this.proxyPayment = proxyPayment;
        this.userRepository = userRepository;
        this.guestRepository = guestRepository;
        this.storeRepository = storeRepository;
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
        this.mapper.registerModule(new ProductKeyModule());
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public String processPayment(String username, String creditCardNumber, String expirationDate, String backNumber, String Id, String name) throws Exception {
        try {
            boolean isRegisterdUser = (!username.contains("Guest"));
            Guest user;
            if(isRegisterdUser) {
                try {
                    user = (RegisteredUser) userRepository.getById(username);
                }
                catch (Exception e) {
                    EventLogger.logEvent(username, "PROCESS_PAYMENT - USER_NOT_FOUND:"+e.toString());
                    throw new IllegalArgumentException("User not found");
                }
            }
            else {
                try {
                    user = guestRepository.getById(username);
                }
                catch (Exception e) {
                    EventLogger.logEvent(username, "PROCESS_PAYMENT - USER_NOT_FOUND:"+e.toString());
                    throw new IllegalArgumentException("User not found");
                }
            }
            List<ShoppingBag> shoppingBags = user.getShoppingCart().getShoppingBags();
            for (ShoppingBag shoppingBag : shoppingBags) {
                DiscountPolicyMicroservice discountPolicy = new DiscountPolicyMicroservice(storeRepository, userRepository, productRepository, discountRepository);
                Map<Product, Integer> products = new HashMap<Product, Integer>();
                double payment = 0;
                for (String product : shoppingBag.getProducts().keySet()) {
                    Product p = productRepository.getById(product);
                    products.put(p, shoppingBag.getProducts().get(product));
                }
                Product firstProduct = products.keySet().iterator().next();
                Map<String, Integer> productsString = new HashMap<>();
                for (Map.Entry<Product, Integer> entry : products.entrySet()) {
                    String storeId = entry.getKey().getId();  // Extract key
                    productsString.put(storeId, entry.getValue()); // Preserve value
                }
                payment = discountPolicy.calculatePrice(firstProduct.getStoreId(), productsString);
                return proxyPayment.processPayment(payment, creditCardNumber, expirationDate, backNumber, Id, name);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return "";
    }

    public String cancelPayment(String username, String id) {
        try {
            boolean isRegisterdUser = (!username.contains("Guest"));
            Guest user;
            if(isRegisterdUser) {
                try {
                    user = (RegisteredUser) userRepository.getById(username);
                }
                catch (Exception e) {
                    EventLogger.logEvent(username, "CANCEL_PAYMENT - USER_NOT_FOUND:"+e.toString());
                    throw new IllegalArgumentException("User not found");
                }
            }
            else {
                try {
                    user = guestRepository.getById(username);
                }
                catch (Exception e) {
                    EventLogger.logEvent(username, "CANCEL_PAYMENT - USER_NOT_FOUND:"+e.toString());
                    throw new IllegalArgumentException("User not found");
                }
            }
                return proxyPayment.cancelPayment(id);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
