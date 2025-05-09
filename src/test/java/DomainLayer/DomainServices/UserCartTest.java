package DomainLayer.DomainServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import DomainLayer.*;
import DomainLayer.DomainServices.UserCart;
import DomainLayer.Roles.RegisteredUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

class UserCartTest {

    @Mock private IToken tokener;
    @Mock private IUserRepository userRepository;
    @Mock private IStoreRepository storeRepository;
    @Mock private IProductRepository productRepository;
    @Mock private IOrderRepository orderRepository;
    @Mock private IPayment paymentSystem;
    @Mock private IShipping shippingSystem;

    @InjectMocks private UserCart userCart;
    private ObjectMapper mapper = new ObjectMapper();

    private static final String TOKEN = "token123";
    private static final String USER = "alice";
    private RegisteredUser baseUser;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // Initialize Mockito annotations
        mocks = MockitoAnnotations.openMocks(this);

        // Base user with empty cart
        baseUser = new RegisteredUser("username");
        baseUser.setName(USER);
        String userJson = mapper.writeValueAsString(baseUser);

        when(tokener.extractUsername(TOKEN)).thenReturn(USER);
        doNothing().when(tokener).validateToken(TOKEN);
        when(userRepository.getUser(USER)).thenReturn(userJson);
    }

    // --- addToCart tests ---

    @Test
    void addToCart_nullToken_throwsIAE() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.addToCart(null, "store1", "prod1", 1)
        );
        assertEquals("Token cannot be null", ex.getMessage());
    }

    @Test
    void addToCart_invalidQuantity_throwsIAE() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.addToCart(TOKEN, "store1", "prod1", 0)
        );
        assertEquals("Quantity must be greater than 0", ex.getMessage());
    }

    @Test
    void addToCart_success_updatesUserRepository() throws Exception {
        userCart.addToCart(TOKEN, "store1", "prod1", 2);

        verify(tokener).validateToken(TOKEN);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).update(eq(USER), jsonCaptor.capture());

        RegisteredUser updated = mapper.readValue(jsonCaptor.getValue(), RegisteredUser.class);
        assertEquals(1, updated.getShoppingCart().getShoppingBags().size());
        ShoppingBag bag = updated.getShoppingCart().getShoppingBags().get(0);
        assertEquals("store1", bag.getStoreId());
        assertEquals(2, bag.getProducts().get("prod1"));
    }

    // --- removeFromCart tests ---

    @Test
    void removeFromCart_nullStore_throwsIAE() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.removeFromCart(TOKEN, null, "prod1", 1)
        );
        assertEquals("StoreId cannot be null", ex.getMessage());
    }

    @Test
    void removeFromCart_success_decrementsQuantity() throws Exception {
        // prepare a cart with quantity 5
        baseUser.addProduct("store1", "prod1", 5);
        when(userRepository.getUser(USER))
            .thenReturn(mapper.writeValueAsString(baseUser));

        userCart.removeFromCart(TOKEN, "store1", "prod1", 3);

        verify(tokener).validateToken(TOKEN);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).update(eq(USER), jsonCaptor.capture());

        RegisteredUser updated = mapper.readValue(jsonCaptor.getValue(), RegisteredUser.class);
        ShoppingBag bag = updated.getShoppingCart().getShoppingBags().get(0);
        assertEquals(2, bag.getProducts().get("prod1"));
    }

    // --- reserveCart tests ---

    @Test
    void reserveCart_nullToken_throwsIAE() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.reserveCart(null)
        );
        assertEquals("Token cannot be null", ex.getMessage());
    }

    @Test
    void reserveCart_storeNotFound_throwsIAE() throws Exception {
        baseUser.addProduct("storeX", "p1", 1);
        when(userRepository.getUser(USER))
            .thenReturn(mapper.writeValueAsString(baseUser));
        when(storeRepository.getStore("storeX")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.reserveCart(TOKEN)
        );
        assertEquals("Store not found", ex.getMessage());
    }

    // --- purchaseCart tests ---

    @Test
    void purchaseCart_notReserved_throwsIAE() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.purchaseCart(TOKEN, 10.0)
        );
        assertEquals("Cart is not reserved", ex.getMessage());
    }

    @Test
    void purchaseCart_happyPath_processesAll() throws Exception {
        // user with reserved cart and one item
        baseUser.addProduct("store1", "p1", 2);
        baseUser.setCartReserved(true);
        when(userRepository.getUser(USER))
            .thenReturn(mapper.writeValueAsString(baseUser));

        Store store = new Store("store1"  , "");

        // stub product
        Product product = new Product("p1", "name", "desc", "cat", 5, 10, 2.5, "store1");
        when(productRepository.getProduct("p1")).thenReturn(product);
        
        store.addNewProduct("p1", 5);
        store.reserveProduct("p1", 2);
        when(storeRepository.getStore("store1")).thenReturn(mapper.writeValueAsString(store));

        // execute
        userCart.purchaseCart(TOKEN, userCart.reserveCart(TOKEN));

        verify(orderRepository).addOrder(any(Order.class));

        // final state persisted
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository, atLeastOnce()).update(eq(USER), jsonCaptor.capture());
        RegisteredUser post = mapper.readValue(jsonCaptor.getValue(), RegisteredUser.class);
        assertFalse(post.getCartReserved());
        assertTrue(post.getShoppingCart().getShoppingBags().isEmpty());
    }

    @Test
    void purchaseCart_notInInventory_throwsIAE() throws Exception {
        baseUser.addProduct("store1", "p1", 2);
        baseUser.setCartReserved(true);
        Store store = new Store("store1" , "");
        store.addNewProduct("p1", 1);
        when(storeRepository.getStore("store1")).thenReturn(mapper.writeValueAsString(store));
        when(userRepository.getUser(USER))
            .thenReturn(mapper.writeValueAsString(baseUser));
        when(productRepository.getProduct("p1")).thenReturn(new Product("p1", "name", "desc", "cat", 5, 10, 2.5, "store1"));

        // stub product
        Product product = new Product("p1", "name", "desc", "cat", 5, 10, 2.5, "store1");
        when(productRepository.getProduct("p1")).thenReturn(product);
        
        when(storeRepository.getStore("store1")).thenReturn(mapper.writeValueAsString(store));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.purchaseCart(TOKEN, userCart.reserveCart(TOKEN))
        );
        assertEquals("Failed to reserve product: " + "p1" , ex.getMessage());
    }
}
