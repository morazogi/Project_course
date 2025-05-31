package ServiceLayer;

import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import DomainLayer.User;
import InfrastructureLayer.DiscountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import InfrastructureLayer.ProductRepository;
import Mocks.MockPayment;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private User user;
    private Store store;
    private ProductRepository productRepository;
    private IToken tokenService;
    private String token;
    private UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private StoreRepository storeRepository;
    private DiscountRepository discountRepository;

    @BeforeEach
    void setUp() throws Exception {
        store = new Store("founderID" , "storeName");
        storeRepository = new StoreRepository();
        discountRepository = new DiscountRepository();
        productRepository = new ProductRepository();
        storeRepository.addStore(store.getId() , mapper.writeValueAsString(store));
        Product product = new Product("1", store.getId(), "bgdfbf", "bdfgbfgds", 321, 3, 1.0, "1223r");
        productRepository.save(product);
        store.addNewProduct(product.getId(), 3);
        storeRepository.updateStore(store.getId(), mapper.writeValueAsString(store));
        MockPayment mockPayment = new MockPayment();
        tokenService = new TokenService();
        userRepository = new UserRepository();
        paymentService = new PaymentService(userRepository, productRepository, mockPayment, tokenService, discountRepository, storeRepository);
        user = new RegisteredUser("username");
        token = tokenService.generateToken("username");
        user.addProduct(store.getId(), product.getId(), 3);
        try {
            userRepository.addUser("username", "fsdfd", mapper.writeValueAsString(user));
        } catch (Exception e) {

        }
    }

//    @Test
//    public void testProcessPayment_Successful() {
//        boolean response = paymentService.processPayment(token, "username", "5555555555554444", "10/26", "395");
//        assertTrue(response);
//    }

    @Test
    public void testProcessPayment_BadCreditCardNumber_Failure() {
        boolean response = paymentService.processPayment(token,"username","5355555555554444", "10/26", "395");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_EmptyCreditCardNumber_Failure() {
        boolean response = paymentService.processPayment(token,"username", "", "10/26", "395");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_BadlyWrittenExpirationDate_Failure() {
        boolean response = paymentService.processPayment(token,"username","5555555555554444", "10'26", "395");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_InvalidExpirationDate_Failure() {
        boolean response = paymentService.processPayment(token,"username","5555555555554444", "fewdki", "395");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_EmptyExpirationDate_Failure() {
        boolean response = paymentService.processPayment(token,"username", "5555555555554444", "", "395");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_InvalidBackNumber_Failure() {
        boolean response = paymentService.processPayment(token,"username","5555555555554444", "10/26", "kfjeowia0");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_ExpiredCreditCard_Failure() {
        boolean response = paymentService.processPayment(token,"username", "5555555555554444", "10/24", "395");
        assertFalse(response);
    }

    @Test
    public void testProcessPayment_EmptyBackNumber_Failure() {
        boolean response = paymentService.processPayment(token,"username", "5555555555554444", "10/26", "");
        assertFalse(response);
    }

}
