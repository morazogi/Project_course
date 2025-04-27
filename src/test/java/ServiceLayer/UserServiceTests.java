package ServiceLayer;

import DomainLayer.IJobRepository;
import DomainLayer.IProductRepository;
import DomainLayer.IStoreRepository;
import DomainLayer.IUserRepository;
import DomainLayer.Product;
import DomainLayer.Roles.RegisteredUser;
import infrastructureLayer.ProductRepository;
import infrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedUserServiceTest {

    private IUserRepository userRepo;
    private IStoreRepository storeRepo;
    private IProductRepository productRepo;
    private IJobRepository jobRepo;
    private TokenService tokenService;
    private UserService userService;
    private StoreService storeService;
    private ProductService productService;
    private ObjectMapper mapper = new ObjectMapper();

    private RegisteredUser testUser;
    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepo = new UserRepository();
        storeRepo = Mockito.mock(IStoreRepository.class);
        productRepo = new ProductRepository();
        jobRepo = Mockito.mock(IJobRepository.class);
        tokenService = new TokenService();
        productService = new ProductService(productRepo);
        storeService = new StoreService(storeRepo, productService);
        userService = new UserService(userRepo, tokenService, storeService, productService);

        testUser = userService.signUp("yaniv", "password");
        validToken = testUser.getToken();
    }

    @Test
    void signup_UserAlreadyExists() throws Exception {
        RegisteredUser u = userService.signUp("yaniv", "password");
        assertNull(u);
    }

    @Test
    void signup_UsernameIsNull() throws Exception {
        RegisteredUser u = userService.signUp(null, "password");
        assertNull(u);
    }

    @Test
    void signup_PasswordIsNull() throws Exception {
        RegisteredUser u = userService.signUp("yaniv", null);
        assertNull(u);
    }

    @Test
    void signup_UsernameIsEmpty() throws Exception {
        RegisteredUser u = userService.signUp("", "password");
        assertNull(u);
    }

    @Test
    void signup_PasswordIsEmpty() throws Exception {
        RegisteredUser u = userService.signUp("yaniv", "");
        assertNull(u);
    }

    @Test
    void login_Right_params() throws Exception {
        RegisteredUser u = userService.login("yaniv", "password");
        assertNotNull(u);
    }

    @Test
    void login_UserDoesNotExist() throws Exception {
        RegisteredUser u = userService.login("ghost", "password");
        assertNull(u);
    }

    @Test
    void login_IncorrectPassword() throws Exception {
        RegisteredUser u = userService.login("yaniv", "wrongpassword");
        assertNull(u);
    }

    @Test
    void login_UsernameIsNull() throws Exception {
        RegisteredUser u = userService.login(null, "password");
        assertNull(u);
    }

    @Test
    void login_PasswordIsNull() throws Exception {
        RegisteredUser u = userService.login("yaniv", null);
        assertNull(u);
    }

    @Test
    void login_UsernameIsEmpty() throws Exception {
        RegisteredUser u = userService.login("", "password");
        assertNull(u);
    }

    @Test
    void login_PasswordIsEmpty() throws Exception {
        RegisteredUser u = userService.login("yaniv", "");
        assertNull(u);
    }

    @Test
    void logoutRegistered_Right_params() throws Exception {
        userService.logoutRegistered(validToken, testUser);
        assertFalse(tokenService.validateToken(validToken));
    }

    @Test
    void logoutRegistered_UserNotLoggedIn() throws Exception {
        userService.logoutRegistered(validToken, testUser);
        assertThrows(Exception.class, () -> userService.logoutRegistered(validToken, testUser));
    }

    @Test
    void logoutRegistered_UserNotExist() throws Exception {
        userService.logoutRegistered(validToken, testUser);
        assertThrows(Exception.class, () -> userService.logoutRegistered(validToken, null));
    }

    @Test
    void findProductsByName_Right_params() throws Exception {
        Product product = new Product("product", "store", "product", "description", 100, 10, 5.0);
        productRepo.save(product);
        List<String> products = userService.searchItems("product", validToken);
        assertTrue(products.contains(mapper.writeValueAsString(product)));
    }

    @Test
    void findProductsByName_UserNotLoggedIn() throws Exception {
        assertThrows(Exception.class, () -> userService.searchItems("product", null));
    }

    @Test
    void findProductsByName_ProductNotExist() throws Exception {
        List<String> products = userService.searchItems("product", validToken);
        assertTrue(products.isEmpty());
    }

    @Test
    void findProductsByNameAndStore_Right_params() throws Exception {
        Product product = new Product("product", "store", "product", "description", 100, 10, 5.0);
        productRepo.save(product);
        List<String> products = userService.searchItemsInStore("product", "store", validToken);
        assertTrue(products.contains(mapper.writeValueAsString(product)));
    }

    @Test
    void findProductsByNameAndStore_UserNotLoggedIn() throws Exception {
        assertThrows(Exception.class, () -> userService.searchItemsInStore("product", "store", null));
    }

    @Test
    void findProductsByNameAndStore_ProductNotExist() throws Exception {
        List<String> products = userService.searchItemsInStore("product", "store", validToken);
        assertTrue(products.isEmpty());
    }

    @Test
    void findProductsByNameAndStore_StoreNotExist() throws Exception {
        Product product = new Product("product", "store", "product", "description", 100, 10, 5.0);
        productRepo.save(product);
        List<String> products = userService.searchItemsInStore("product", "nonexistentStore", validToken);
        assertTrue(products.isEmpty());
    }

    // Additional extended tests follow (already present in previous version)
    // ...
}
