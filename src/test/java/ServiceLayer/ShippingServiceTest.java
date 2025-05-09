 package ServiceLayer;

import DomainLayer.*;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.Jobs.Job;
import DomainLayer.Roles.RegisteredUser;
import Mocks.MockPayment;
import com.fasterxml.jackson.databind.ObjectMapper;
import infrastructureLayer.ProxyShipping;
import Mocks.MockShipping;
import infrastructureLayer.StoreRepository;
import infrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
        store = new Store();
        Product product = new Product("1", store.getId(), "bgdfbf", "bdfgbfgds", 321, 3, 1.0, "1223r");
        store.addNewProduct(product.getId(), 3);
        tokenService = new TokenService();
        userRepository = new UserRepository();
        user = new RegisteredUser(new ArrayList<Job>(), "username");
        user.addProduct(store.getId(), product.getId(), 3);
        try {
            userRepository.addUser("username", "fsdfd", mapper.writeValueAsString(user));
        } catch (Exception e) {

        }
        token = tokenService.generateToken("username");
        IShipping mockShipping = new MockShipping();
        shippingService = new ShippingService(mockShipping, tokenService, userRepository);
    }
    @Test
    public void testProcessShipping_Successful() {
        boolean response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "Even Gvirol", "12");
        assertTrue(response);
    }

    @Test
    public void testProcessShipping_EmptyState_Failure() {
        boolean response = shippingService.processShipping(token, "", "Be'er Sheva", "Even Gvirol", "12");
        assertFalse(response);
    }

    @Test
    public void testProcessShipping_EmptyCity_Failure() {
        boolean response = shippingService.processShipping(token, "Israel", "", "Even Gvirol", "12");
        assertFalse(response);
    }

    @Test
    public void testProcessShipping_EmptyStreet_Failure() {
        boolean response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "", "12");
        assertFalse(response);
    }

    @Test
    public void testProcessShipping_InvalidHomeNumber_Failure() {
        boolean response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "Even Gvirol", "vjmikod");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_EmptyHomeNumber_Failure() {
        boolean response = shippingService.processShipping(token, "Israel", "Be'er Sheva", "Even Gvirol", "");
        assertFalse(response);
    }




 }
