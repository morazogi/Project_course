 package ServiceLayer;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.GuestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import Mocks.MockShipping;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

 class ShippingServiceTest {

    private ShippingService shippingService;
    private RegisteredUser user;
    private Store store;
    private IToken tokenService;
    private String token;
    private UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        store = new Store("founderID" , "storeName");
        Product product = new Product(store.getId(), "bgdfbf", "bdfgbfgds", 321, 3, 1.0, "1223r");
        store.addNewProduct(product.getId(), 3);
        tokenService = new TokenService();
        userRepository = new UserRepository();
        user = new RegisteredUser("username", "mklpnkifgf");
        user.addProduct(store.getId(), product.getId(), 3);
        try {
            userRepository.save(user);
        } catch (Exception e) {

        }
        token = tokenService.generateToken("username");
        IShipping mockShipping = new MockShipping();
        GuestRepository guestRepository = new GuestRepository();
        shippingService = new ShippingService(mockShipping, tokenService, userRepository, guestRepository);
    }
    @Test
    public void testProcessShipping_Successful() {
        String response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "Even Gvirol", "bvc", "12");
        assertEquals(response, "Shipping successful");
    }

    @Test
    public void testProcessShipping_EmptyState_Failure() {
        String response = shippingService.processShipping(token, "", "Be'er Sheva", "Even Gvirol", "bvc", "12");
        assertNotEquals(response, "Shipping successful");
    }

    @Test
    public void testProcessShipping_EmptyCity_Failure() {
        String response = shippingService.processShipping(token, "Israel", "", "Even Gvirol", "bvc", "12");
        assertNotEquals(response, "Shipping successful");
    }

    @Test
    public void testProcessShipping_EmptyStreet_Failure() {
        String response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "", "bvc", "12");
        assertNotEquals(response, "Shipping successful");
    }

    @Test
    public void testProcessShipping_InvalidHomeNumber_Failure() {
        String response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "Even Gvirol", "bvc", "vjmikod");
        assertNotEquals(response, "Shipping successful");
    }

    @Test
    public void testProcessPayment_EmptyHomeNumber_Failure() {
        String response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "Even Gvirol", "bvc", "");
        assertNotEquals(response, "Shipping successful");
    }

 }
