package PresentorLayer;

import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.UserRepository;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;

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
        registeredService.openStore(token, storeName);
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
                return ownerManagerService.addProduct(user.getID(), store.getId(), productName, description, price, quantity, category);
            }
        }
        return "Did not find store with that name";
    }
}
