package DomainLayer.domainServices;

import DomainLayer.*;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import infrastructureLayer.StoreRepository;
import infrastructureLayer.UserRepository;
import utils.ProductKeyModule;

import java.util.List;
import java.util.Map;

public class ShippingConnectivity {

    private final ObjectMapper mapper = new ObjectMapper();
    private final IShipping proxyShipping;
    private final IUserRepository userRepository;

    public ShippingConnectivity(IShipping proxyShipping, IUserRepository userRepository) {
        this.proxyShipping = proxyShipping;
        this.userRepository = userRepository;
        this.mapper.registerModule(new ProductKeyModule());
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void processShipping(String username, String state, String city, String street, String homeNumber) throws Exception {
        String jsonUser = userRepository.getUser(username);
        User user = mapper.readValue(jsonUser, User.class);
        List<ShoppingBag> shoppingBags = user.getShoppingCart().getShoppingBags();
        for (ShoppingBag shoppingBag : shoppingBags) {
            proxyShipping.processShipping(user.getID(), state, city, street, shoppingBag.getProducts(), homeNumber);
        }
    }
}
