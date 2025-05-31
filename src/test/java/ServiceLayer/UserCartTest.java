//
//
//package ServiceLayer;
//
//import DomainLayer.*;
//import DomainLayer.Roles.RegisteredUser;
//import DomainLayer.DomainServices.UserCart;
//import InfrastructureLayer.OrderRepository;
//import InfrastructureLayer.ProductRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//
//import java.lang.reflect.Field;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class UserCartRepositoryContentTest {
//
//    private static final String TOKEN      = "good-token";
//    private static final String USERNAME   = "river";
//    private static final String STORE_ID   = "store-1";
//    private static final String PRODUCT_ID = "prod-1";
//
//    /* mocks */
//    @Mock IToken           tokenSvc;
//    @Mock IUserRepository  userRepo;
//    @Mock IStoreRepository storeRepo;
//    @Captor ArgumentCaptor<String> jsonCaptor;     // capture JSON sent to updateStore
//
//    /* real repos */
//    private final ProductRepository productRepo = new ProductRepository();
//    private final OrderRepository   orderRepo   = new OrderRepository();
//
//    /* mapper spy */
//    private ObjectMapper mapperSpy;
//
//    /* domain objects */
//    private Store          store;
//    private Product        product10u;
//    private Product        product4u;
//    private RegisteredUser user;
//    private ShoppingCart   cart;
//    private ShoppingBag    bag;
//
//    private UserCart sut;
//
//    @BeforeEach
//    void setUp() throws Exception {
//
//        store = new Store("founder", "Test-Store");
//        store.setId(STORE_ID);
//        store.getProducts().put(PRODUCT_ID, 10);
//
//        product10u = new Product(PRODUCT_ID, STORE_ID, "Headphones", "BT", 10.0f, 10, 0.0, "audio");
//        product10u.setId(PRODUCT_ID);
//        product4u  = new Product(PRODUCT_ID, STORE_ID, "Headphones", "BT", 10.0f, 4 , 0.0, "audio");
//        product4u.setId(PRODUCT_ID);
//
//        productRepo.save(product10u);
//
//        user = mock(RegisteredUser.class);
//        cart = mock(ShoppingCart.class);
//        bag  = mock(ShoppingBag.class);
//
//        when(tokenSvc.extractUsername(TOKEN)).thenReturn(USERNAME);
//        doNothing().when(tokenSvc).validateToken(TOKEN);
//
//        when(user.getUsername()).thenReturn(USERNAME);
//        when(user.getShoppingCart()).thenReturn(cart);
//        lenient().when(user.getCartReserved()).thenReturn(false);
//
//        when(cart.getShoppingBags()).thenReturn(List.of(bag));
//        when(bag.getStoreId()).thenReturn(STORE_ID);
//        lenient().when(bag.getProducts()).thenReturn(Map.of(PRODUCT_ID, 2));
//
//        mapperSpy = spy(new ObjectMapper());
//        doReturn(store).when(mapperSpy).readValue(anyString(), eq(Store.class));
//        doReturn(user) .when(mapperSpy).readValue(anyString(), eq(RegisteredUser.class));
//
//        when(userRepo.getUser(USERNAME)).thenReturn("{}");
//        when(storeRepo.getStore(STORE_ID)).thenReturn("{}");
//        lenient().doNothing().when(storeRepo).updateStore(eq(STORE_ID), jsonCaptor.capture());
//
//        sut = new UserCart(tokenSvc, userRepo, storeRepo, productRepo, orderRepo);
//        Field f = UserCart.class.getDeclaredField("mapper");
//        f.setAccessible(true);
//        f.set(sut, mapperSpy);
//    }
//
//    @Test
//    void reserveCart_insufficientStock_keepsStateIntact() throws Exception {
//        when(bag.getProducts()).thenReturn(Map.of(PRODUCT_ID, 10));          // want 10
//        productRepo.deleteById(PRODUCT_ID);
//        productRepo.save(product4u);                                         // only 4 left
//
//        int qtyBefore      = store.getProducts().get(PRODUCT_ID);
//        int reservedBefore = store.getReservedProducts().size();
//
//        assertThrows(IllegalArgumentException.class, () -> sut.reserveCart(TOKEN));
//
//        assertEquals(qtyBefore,      store.getProducts().get(PRODUCT_ID));
//        assertEquals(reservedBefore, store.getReservedProducts().size());
//
//        verify(storeRepo, never()).updateStore(any(), any());
//        assertTrue(jsonCaptor.getAllValues().isEmpty());
//    }
//
//    @Test
//    void purchaseCart_happyFlow_updatesStateCorrectly() throws Exception {
//        when(bag.getProducts()).thenReturn(Map.of(PRODUCT_ID, 2));          // buy 2
//
//        /* reserve */
//        double price = sut.reserveCart(TOKEN);
//        assertEquals(20.0, price, 0.0001);
//
//        assertEquals(8, store.getProducts().get(PRODUCT_ID));               // 10 → 8
//        assertEquals(2, store.getReservedProducts().get(PRODUCT_ID));       // 0 → 2
//
//        assertFalse(jsonCaptor.getAllValues().isEmpty());
//        String reserveJson = jsonCaptor.getAllValues().get(0);
//        Store snapshotAfterReserve = new ObjectMapper().readValue(reserveJson, Store.class);
//        assertEquals(store.getProducts(),         snapshotAfterReserve.getProducts());
//        assertEquals(store.getReservedProducts(), snapshotAfterReserve.getReservedProducts());
//
//        when(user.getCartReserved()).thenReturn(true);
//        sut.purchaseCart(TOKEN, price);
//
//        assertFalse(store.getReservedProducts().containsKey(PRODUCT_ID));   // 2 → 0
//        assertEquals(1, jsonCaptor.getAllValues().size());
//
//        assertEquals(1, orderRepo.getOrderByStoreId(STORE_ID).size());
//        assertEquals(1, orderRepo.getOrderByUserId(USERNAME).size());
//
//        verify(storeRepo, atLeastOnce()).updateStore(eq(STORE_ID), anyString());
//        verify(userRepo,  atLeast(2)).update(eq(USERNAME), anyString());
//    }
//}
