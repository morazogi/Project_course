package DomainLayer.DomainServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import DomainLayer.IProductRepository;
import DomainLayer.IStoreRepository;
import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Product;
import DomainLayer.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class RateTest {

    @Mock private IToken tokener;
    @Mock private IStoreRepository storeRepository;
    @Mock private IUserRepository userRepository;
    @Mock private IProductRepository productRepository;

    @InjectMocks private Rate rateService;
    private AutoCloseable mocks;
    private ObjectMapper mapper = new ObjectMapper();

    private static final String TOKEN = "token";
    private static final String USER = "alice";
    private static final String STORE_ID = "store1";
    private static final String PRODUCT_ID = "prod1";

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(tokener.extractUsername(TOKEN)).thenReturn(USER);
        doNothing().when(tokener).validateToken(TOKEN);
    }

    // --- rateStore() tests ---

    @Test
    void rateStore_nullToken_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateStore(null, STORE_ID, 3));
    }

    @Test
    void rateStore_nullStoreId_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateStore(TOKEN, null, 3));
    }

    @Test
    void rateStore_invalidRate_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateStore(TOKEN, STORE_ID, 0));
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateStore(TOKEN, STORE_ID, 6));
    }

    @Test
    void rateStore_storeNotExist_throws() throws Exception {
        // stub repository to return null JSON
        when(storeRepository.getStore(STORE_ID)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateStore(TOKEN, STORE_ID, 4));
    }

    @Test
    void rateStore_userNotExist_throws() throws Exception {
        // prepare a valid store JSON
        Store store = new Store(STORE_ID , "");
        String storeJson = mapper.writeValueAsString(store);
        when(storeRepository.getStore(STORE_ID)).thenReturn(storeJson);

        // user repo returns null
        when(userRepository.getUser(USER)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateStore(TOKEN, STORE_ID, 4));
    }

    @Test
    void rateStore_rateTrue_updatesAndReturnsTrue() throws Exception {
        // prepare a valid store JSON
        Store store = mock(Store.class);
        String storeJson = mapper.writeValueAsString(store);
        when(storeRepository.getStore(STORE_ID)).thenReturn(storeJson);

        when(userRepository.getUser(USER)).thenReturn("someUserJson");
        when(store.rate(5)).thenReturn(true);

        boolean result = rateService.rateStore(TOKEN, STORE_ID, 5);

        verify(tokener).validateToken(TOKEN);
        verify(storeRepository).updateStore(eq(STORE_ID), anyString());
        assertTrue(result);
    }

    @Test
    void rateStore_rateFalse_returnsFalse() throws Exception {
        // prepare a valid store JSON
        Store store = mock(Store.class);
        String storeJson = mapper.writeValueAsString(store);
        when(storeRepository.getStore(STORE_ID)).thenReturn(storeJson);
        when(userRepository.getUser(USER)).thenReturn("someUserJson");

        //rate with -4 should throw an exception
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateStore(TOKEN, STORE_ID, -4));

    }

    // --- rateProduct() tests ---

    @Test
    void rateProduct_nullArgs_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateProduct(null, PRODUCT_ID, 4.0));
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateProduct(TOKEN, null, 4.0));
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateProduct(TOKEN, PRODUCT_ID, 0.5));
        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateProduct(TOKEN, PRODUCT_ID, 6.0));
    }

    @Test
    void rateProduct_productNotExist_throws() {
        when(productRepository.getProduct(PRODUCT_ID)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateProduct(TOKEN, PRODUCT_ID, 4.0));
    }

    @Test
    void rateProduct_userNotExist_throws() {
        // prepare a valid product
        Product product = mock(Product.class);
        when(productRepository.getProduct(PRODUCT_ID)).thenReturn(product);
        // user repo returns null
        when(userRepository.getUser(USER)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> rateService.rateProduct(TOKEN, PRODUCT_ID, 4.0));
    }

    @Test
    void rateProduct_addRatingTrue_savesAndReturnsTrue() {
        // prepare a valid product
        Product product = mock(Product.class);
        when(productRepository.getProduct(PRODUCT_ID)).thenReturn(product);
        when(userRepository.getUser(USER)).thenReturn("someUserJson");
        when(product.addRating(USER, 5.0)).thenReturn(true);

        boolean result = rateService.rateProduct(TOKEN, PRODUCT_ID, 5.0);

        verify(tokener).validateToken(TOKEN);
        verify(productRepository).save(product);
        assertTrue(result);
    }

    @Test
    void rateProduct_addRatingFalse_returnsFalse() {
        // prepare a valid product
        Product product = mock(Product.class);
        when(productRepository.getProduct(PRODUCT_ID)).thenReturn(product);
        when(userRepository.getUser(USER)).thenReturn("someUserJson");
        when(product.addRating(USER, 3.5)).thenReturn(false);

        boolean result = rateService.rateProduct(TOKEN, PRODUCT_ID, 3.5);

        verify(productRepository, never()).save(product);
        assertFalse(result);
    }
}
