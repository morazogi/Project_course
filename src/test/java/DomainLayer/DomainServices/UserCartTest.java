package DomainLayer.DomainServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import DomainLayer.*;
import DomainLayer.domainServices.UserCart;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.domainServices.DiscountPolicyMicroservice;

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
    private static final String USER  = "alice";
    private RegisteredUser baseUser;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws JsonProcessingException {
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
        baseUser.addProduct("store1", "prod1", 5);
        when(userRepository.getUser(USER)).thenReturn(mapper.writeValueAsString(baseUser));
        userCart.removeFromCart(TOKEN, "store1", "prod1", 3);
        verify(tokener).validateToken(TOKEN);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).update(eq(USER), jsonCaptor.capture());
        RegisteredUser updated = mapper.readValue(jsonCaptor.getValue(), RegisteredUser.class);
        assertEquals(2, updated.getShoppingCart().getShoppingBags().get(0).getProducts().get("prod1"));
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
        when(userRepository.getUser(USER)).thenReturn(mapper.writeValueAsString(baseUser));
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
    void purchaseCart_notInInventory_throwsIAE() throws Exception {
        baseUser.addProduct("store1", "prod1", 2);
        baseUser.setCartReserved(true);
        when(userRepository.getUser(USER)).thenReturn(mapper.writeValueAsString(baseUser));
        Store store = new Store("store1", "");
        store.addNewProduct("prod1", 1);
        when(storeRepository.getStore("store1")).thenReturn(mapper.writeValueAsString(store));
        when(productRepository.getProduct("prod1")).thenReturn(new Product("prod1", "name", "desc", "cat", 5, 10, 2.5, "store1"));
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userCart.purchaseCart(TOKEN, userCart.reserveCart(TOKEN))
        );
        assertEquals("Failed to reserve product: prod1", ex.getMessage());
    }

    // --- calculatePrice tests ---

    @Test
    void getCartPrice_emptyCart_returnsZero() throws Exception {
        // baseUser has empty cart by default
        when(userRepository.getUser(USER)).thenReturn(mapper.writeValueAsString(baseUser));
        double price = userCart.getCartPrice(USER);
        assertEquals(0.0, price, 1e-6);
    }

    @Test
    void getCartPrice_withDiscount_appliesDiscount() throws Exception {
        // add items
        baseUser.addProduct("store1", "prod1", 3);
        when(userRepository.getUser(USER)).thenReturn(mapper.writeValueAsString(baseUser));
        // stub DiscountPolicyMicroservice
        try (MockedConstruction<DiscountPolicyMicroservice> mc = mockConstruction(DiscountPolicyMicroservice.class,
                (mock, ctx) -> when(mock.calculatePrice(eq("store1"), any())).thenReturn(77.77f))) {
            double price = userCart.getCartPrice(USER);
            assertEquals(77.77, price, 0.01);
            assertEquals(1, mc.constructed().size());
        }
    }
}
