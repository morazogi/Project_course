package ServiceLayer;

import DomainLayer.IToken;
import DomainLayer.DomainServices.Search;
import DomainLayer.DomainServices.UserCart;
import DomainLayer.DomainServices.UserConnectivity;
import DomainLayer.IStoreRepository;
import DomainLayer.IUserRepository;
import DomainLayer.Product;
import DomainLayer.IProductRepository;
import DomainLayer.IOrderRepository;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;
import DomainLayer.ShoppingBag;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Optional;

import jakarta.transaction.Transactional;
import utils.ProductKeyModule;

import DomainLayer.Store;
import DomainLayer.User;
import DomainLayer.DomainServices.UserCart;
import DomainLayer.DomainServices.UserConnectivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final IToken tokenService;
    private final IProductRepository productRepository;
    private final IStoreRepository storeRepository;
    private final ShippingService shippingService;
    private final PaymentService paymentService;
    private final UserConnectivity userConnectivity;
    private final UserCart userCart;
    private final Search search;

    public UserService(IToken tokenService, 
                       IStoreRepository storeRepository,
                       IUserRepository userRepository,
                       IProductRepository productRepository,
                       IOrderRepository orderRepository,
                       ShippingService shippingService,
                       PaymentService paymentService){
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.tokenService = tokenService;
        this.shippingService = shippingService;
        this.paymentService = paymentService;
        this.userConnectivity = new UserConnectivity(tokenService, userRepository);
        this.userCart = new UserCart(tokenService, userRepository, storeRepository, productRepository, orderRepository);    
        this.search = new Search(productRepository, storeRepository);   
    }

    @Transactional
    public String login(String username, String password) throws JsonProcessingException {
        try {
            EventLogger.logEvent(username , "LOGIN");
            return userConnectivity.login(username, password);
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(username, "LOGIN_FAILED");
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public void signUp(String username, String password)  throws Exception {
        try {
            userConnectivity.signUp(username, password);
        } catch (IllegalArgumentException e) {
            EventLogger.logEvent(username, "SIGNUP_FAILED");
            throw new RuntimeException("User already exists");
        }
    }

    @Transactional
    public void removeFromCart(String token, String storeId, String productId, Integer quantity) {
        try{
            userCart.removeFromCart(token, storeId, productId, quantity);
            EventLogger.logEvent(tokenService.extractUsername(token), "REMOVE_FROM_CART");
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "REMOVE_FROM_CART_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to remove product from cart");
        }
    }

    @Transactional
    public String addToCart(String token, String storeId, String productId, Integer quantity) {
        try{
            userCart.addToCart(token, storeId, productId, quantity);
            EventLogger.logEvent(tokenService.extractUsername(token), "ADD_TO_CART");
            return "Product added to cart";
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "ADD_TO_CART_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to add product to cart");
        }
    }

    @Transactional
    public Double reserveCart(String token) {
        try{
            return userCart.reserveCart(token);
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "RESERVE_CART_FAILED");
            throw new RuntimeException("Failed to purchase cart");
        }
    }

    @Transactional
    public void purchaseCart(String token ,
                             String paymentMethod ,
                             String cardNumber,
                             String expirationDate,
                             String cvv,
                             String state,
                             String city,
                             String street,
                             String homeNumber) {
        try{
            Double price = reserveCart(token);
            shippingService.processShipping(token, state, city, street, homeNumber);
            paymentService.processPayment(token, paymentMethod, cardNumber, expirationDate, cvv);
            userCart.purchaseCart(token , price);
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "PURCHASE_CART_FAILED " + e.getMessage());
            throw new RuntimeException("Failed to purchase cart");
        }
    }

    @Transactional
    public List<String> findProduct(String token, String name , String category){
        try {
            tokenService.validateToken(token);
            return search.findProduct(name, category);
        } catch (Exception e) {
            System.out.println("ERROR finding product by Name:" + e.getMessage());
            return Collections.emptyList();
        }
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
    public List<String> getStoreByName(String token , String name) {
        try {
            tokenService.validateToken(token);
            return search.getStoreByName(name);
        } catch (Exception e) {
            System.out.println("ERROR finding store by Name:" + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Transactional
    public String searchStoreByName(String token, String storeName) {
        try {
            return search.searchStoreByName(storeName);
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "SEARCH_STORE_FAILED");
            throw new RuntimeException("Failed to search store");
        }
    }

    @Transactional
    public String getStoreById(String token, String storeId) {
        try {
            return search.getStoreById(storeId);
        } catch (Exception e) {
            EventLogger.logEvent(tokenService.extractUsername(token), "SEARCH_STORE_FAILED");
            throw new RuntimeException("Failed to search store");
        }
    }

}