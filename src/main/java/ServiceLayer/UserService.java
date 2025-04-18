package ServiceLayer;

import DomainLayer.IUserRepository;
import DomainLayer.Product;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.Jobs.Job;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Optional;
import utils.ProductKeyModule;

import DomainLayer.Store;
import org.mindrot.jbcrypt.BCrypt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserService {

    private final TokenService tokenService;
    private final IUserRepository userRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final JobService jobService;
    private final StoreService storeService;
    private final ProductService productService;

    public UserService(IUserRepository repository, TokenService tokenService, StoreService storeService, JobService jobService, ProductService productService) {
        this.productService = productService;
        this.storeService = storeService;
        this.userRepo = repository;
        this.tokenService = tokenService;
        this.jobService = jobService;
        this.mapper.registerModule(new ProductKeyModule());
    }


    public RegisteredUser login(String username, String password) throws JsonProcessingException {
        if (!userRepo.isUserExist(username)) {
            return null;
        }
        if(username == null || password == null){
            return null;
        }
        if(username.isEmpty() || password.isEmpty()){
            return null;
        }

        String hashedPassword = userRepo.getUserPass(username);
        if (BCrypt.checkpw(password, hashedPassword)) {
            String token = tokenService.generateToken(username);
            String userJson = userRepo.getUser(username);
            RegisteredUser user = deserializeUser(userJson);
            user.setToken(token);
            return user;
        }

        return null;
    }

    public RegisteredUser signUp(String username, String password)  throws JsonProcessingException {
        if (userRepo.isUserExist(username)) {
            return null;
        }
        if(username == null || password == null){
            return null;
        }
        if(username.isEmpty() || password.isEmpty()){
            return null;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String token = tokenService.generateToken(username);
        RegisteredUser user = new RegisteredUser(new LinkedList<>() , username);
        user.setToken(token);
        userRepo.addUser(username, hashedPassword , mapper.writeValueAsString(user));
        return user;
    }

    public Guest logoutRegistered(String token, RegisteredUser user) throws Exception {
        if(!tokenService.validateToken(token)){
            throw new Exception("user not logged in");
        }
        String id = String.valueOf(user.getName());
        userRepo.update(id, mapper.writeValueAsString(user));
        tokenService.invalidateToken(token);
        return new Guest();
    }

    public String purchaseCart(int userId, String token, ShoppingCart cart) {
        if (!tokenService.validateToken(token)) {
            return "Invalid or expired token";
        }

        double totalPrice = cart.calculatePurchaseCart();

        if (totalPrice <= 0) {
            return "Cart is empty";
        }

        return "Purchase successful. Total paid: $" + totalPrice;
    }

    protected RegisteredUser deserializeUser(String json) throws JsonProcessingException {
        return mapper.readValue(json, RegisteredUser.class);
    }

    public List<String> searchItems(String name , String token) throws Exception {
        if (!tokenService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        if (name == null || name.isEmpty()) {
            return Collections.emptyList();
        }
    
        if (name.equals("all")) {
            return productService.getAllProducts().stream()
                    .map(product -> {
                        try {
                            return mapper.writeValueAsString(product);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to serialize product to JSON", e);
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            return productService.getProductByName(name).stream()
                    .map(product -> {
                        try {
                            return mapper.writeValueAsString(product);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to serialize product to JSON", e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public List<String> searchItemsInStore(String name , String storeId , String token) throws Exception {
        if (!tokenService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        if (name == null || name.isEmpty()) {
            return Collections.emptyList();
        }
        return searchItems(name , token).stream()
                .filter(item -> {
                    try {
                        Product product = mapper.readValue(item, Product.class);
                        return product.getStoreId().equals(storeId);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize product from JSON", e);
                    }
                })
                .collect(Collectors.toList());
    }

    public String getHistory(int userId, String token) {
        if (!tokenService.validateToken(token)) {
            return "Invalid or expired token";
        }
        // Dummy history retrieval
        return "History retrieved for user ID: " + userId;
    }

    public String bid(String itemName, int price, String token) {
        if (!tokenService.validateToken(token)) {
            return "Invalid or expired token";
        }
        // Dummy bidding logic
        return "Bid of $" + price + " placed on item '" + itemName + "'.";
    }

    public String purchaseViaBid(String itemName, int price, String token) {
        if (!tokenService.validateToken(token)) {
            return "Invalid or expired token";
        }
        // Dummy purchase via bid logic
        return "Purchased item '" + itemName + "' via bid for $" + price + ".";
    }
    public String createStore(RegisteredUser user, String token) {
        if (!tokenService.validateToken(token)) {
            return "Invalid or expired token";
        }
        this.jobService.createStore(user);
        return null;
    }
    public void sendAppointNewOwnerRequest(String token ,Store store, RegisteredUser oldOwner, RegisteredUser newOwner){
        boolean accepted=false;
        if(!this.jobService.UserIsOwnerOfStore(store.getId(), newOwner.getID())){
            StringBuilder requestText = new StringBuilder();
            requestText.append("hi, ").append(newOwner.getName()).append(".\n").append("I would like for you to be an owner in my store: ").append(store.getId());
            accepted = newOwner.receivedOwnershipRequest(requestText.toString());
        }
        //insert listener for the users answer
        if (accepted){
            jobService.addNewOwnerToStore(store,oldOwner,newOwner);
        }
    }
    public void sendAppointNewManagerRequest(Store store, RegisteredUser oldOwner, RegisteredUser newManager, boolean[] permissions){
        boolean accepted=false;
        if(!this.jobService.UserIsManagerOfStore(store.getId(), newManager.getID())){
            StringBuilder requestText = new StringBuilder();
            requestText.append("hi, ").append(newManager.getName()).append(".\n").append("I would like for you to be a manager at my store: ").append(store.getId());
            accepted = newManager.receivedManagingRequest(requestText.toString());
        }
        //insert listener for the users answer
        if (accepted){
            jobService.addNewManagerToStore(store,oldOwner,newManager,permissions);
        }
    }

    public void fireFromMyStore(Store store, RegisteredUser superior, RegisteredUser subordinate) {
        jobService.fireFromMyStore(store,superior,subordinate);
    }
    public void changeManagerPermissions(Store store, RegisteredUser owner, RegisteredUser Manager,boolean[] permissions){
        jobService.changeManagerPermissions(store, owner, Manager, permissions);
    }
    public void closeStore(Store store, RegisteredUser founder){
        jobService.closeStore(store,founder);
    }
    public void openStore(Store store, RegisteredUser founder){
        jobService.openStore(store,founder);
    }
    public String getInfoJobsInStore(Store store, RegisteredUser owner){
        return jobService.getInfoJobsInStore(store,owner);
    }
    public String getInfoOrdersInStore(Store store, RegisteredUser owner){
        return jobService.getInfoOrdersInStore(store,owner);
    }
    public void respondToBuyer(Store store, RegisteredUser owner, RegisteredUser customer,String query){
        customer.acceptQueryResponse(jobService.respondToBuyer(store,owner,query));
    }

    // public String addToCart(String token,RegisteredUser u , Product product, int quantity) {
    //     if (!tokenService.validateToken(token)) {
    //         throw new RuntimeException("Invalid or expired token");
    //     }
    //     if (product == null) {
    //         return "Product not found";
    //     }
    //     if (quantity <= 0) {
    //         return "Invalid quantity";
    //     }
    //     if (u == null) {
    //         return "User not found";
    //     }
    //     u.getShoppingCart().addProduct(product, quantity);
    //     return "Product added to cart";
    // }

    public String viewShoppingCart(String token, RegisteredUser u) {
        if (!tokenService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        if (u == null) {
            return "User not found";
        }
        return u.getShoppingCart().toString();
    }

    // public String removeFromCart(String token, RegisteredUser u, Product product) {
    //     if (!tokenService.validateToken(token)) {
    //         throw new RuntimeException("Invalid or expired token");
    //     }
    //     if (product == null) {
    //         return "Product not found";
    //     }
    //     if (u == null) {
    //         return "User not found";
    //     }
    //     u.getShoppingCart().removeProduct(product);
    //     return "Product removed from cart";
    // }
    
}
