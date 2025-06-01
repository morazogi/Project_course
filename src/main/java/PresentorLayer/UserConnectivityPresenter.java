package PresentorLayer;

import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

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
                             String paymentMethod ,
                             String cardNumber,
                             String expirationDate,
                             String cvv,
                             String state,
                             String city,
                             String street,
                             String homeNumber) throws Exception {
        userService.purchaseCart(token, paymentMethod, cardNumber, expirationDate, cvv, state, city, street, homeNumber);
    }

    public void addStore(String token, String storeName) throws Exception {
        String storeId = registeredService.openStore(token, storeName);
        System.out.println(storeId);
        String username = tokenService.extractUsername(token);
        String jsonUser = userRepository.getUser(username);
        RegisteredUser user = null;
        try {
            user = mapper.readValue(jsonUser, RegisteredUser.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("dfsa");
        }
        System.out.println(ownerManagerService.appointStoreOwner(user.getID(), storeId, user.getID()));
        boolean[] arr = new boolean[7];
        arr[0] = true;
        arr[1] = true;
        arr[2] = true;
        arr[3] = true;
        arr[4] = true;
        arr[5] = true;
        arr[6] = true;
        System.out.println(ownerManagerService.appointStoreManager(user.getID(), storeId, user.getID(), arr));
        username = tokenService.extractUsername(token);
        jsonUser = userRepository.getUser(username);
        user = null;
        try {
            user = mapper.readValue(jsonUser, RegisteredUser.class);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        System.out.println(jsonUser);
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
        String jsonUser = userRepository.getUser(username);
        RegisteredUser user = null;
        try {
            user = mapper.readValue(jsonUser, RegisteredUser.class);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }

        List<String> managedStores = user.getManagedStores();
        for (String managedStore : managedStores) {
            String jsonStore = userService.getStoreById(token, managedStore);
            Store store = null;
            try {
                store = mapper.readValue(jsonStore, Store.class);
            } catch (Exception e) {
                return e.getMessage();
            }
            if (store.getName().equals(storeName)) {
                return ownerManagerService.addProduct(user.getID(), store.getId(), productName, description, price.floatValue(), quantity, category);
            }
        }
        return "Did not find store with that name";
    }

    public void signUp(String username, String password) throws Exception{
        userService.signUp(username, password);
    }

    public String login(String username, String password) throws Exception {
        return userService.login(username, password);
    }

    public LinkedList<String> getUserStoresName(String token) throws Exception {
        String username = tokenService.extractUsername(token);
        String jsonUser = userRepository.getUser(username);
        RegisteredUser user = null;
        try {
            user = mapper.readValue(jsonUser, RegisteredUser.class);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
        LinkedList<String> storeNames = new LinkedList<String>();
        List<String> managedStores = user.getManagedStores();
        for (String managedStore : managedStores) {
            String jsonStore = userService.getStoreById(token, managedStore);
            Store store = null;
            try {
                store = mapper.readValue(jsonStore, Store.class);
                storeNames.add(store.getName());
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }
        System.out.println(storeNames);
        return storeNames;
    }
}