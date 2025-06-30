/* ────────────────────────────────────────────────────────────────
   src/test/java/ServiceLayer/UserServiceTest.java
   ──────────────────────────────────────────────────────────────── */
package ServiceLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.*;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    /* ctor-level dependencies */
    @Mock StoreRepository        storeRepo;
    @Mock UserRepository         userRepo;
    @Mock ProductRepository      productRepo;
    @Mock OrderRepository        orderRepo;
    @Mock DiscountRepository     discountRepo;
    @Mock GuestRepository        guestRepo;
    @Mock ShippingService        shippingSvc;
    @Mock PaymentService         paymentSvc;
    @Mock NotificationRepository notificationRepo;
    @Mock NotificationWebSocketHandler wsHandler;
    @Mock IToken                 tokenSvc;

    /* inner components swapped out */
    @Mock UserConnectivity           connMock;
    @Mock UserCart                   cartMock;
    @Mock Search                     searchMock;
    @Mock DiscountPolicyMicroservice discMock;

    private UserService svc;

    @BeforeEach
    void setUp() {
        svc = new UserService(tokenSvc, storeRepo, userRepo,
                productRepo, orderRepo, shippingSvc,
                paymentSvc, guestRepo, discountRepo,
                notificationRepo, wsHandler);

        ReflectionTestUtils.setField(svc, "userConnectivity", connMock);
        ReflectionTestUtils.setField(svc, "userCart",         cartMock);
        ReflectionTestUtils.setField(svc, "search",           searchMock);
        ReflectionTestUtils.setField(svc, "discountPolicy",   discMock);
    }

    /* ───────────────────────── add-to-cart ───────────────────── */

    @Test
    void addToCart_success_returnsFriendlyMessage() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");
        doNothing().when(cartMock).addToCart("tok","s","p",2);

        assertEquals("Product added to cart",
                svc.addToCart("tok","s","p",2));
        verify(cartMock).addToCart("tok","s","p",2);
    }

    @Test
    void addToCart_stockError_bubblesExactMsg() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");
        doThrow(new IllegalArgumentException("Only 1 left in stock"))
                .when(cartMock).addToCart(any(),any(),any(),anyInt());

        String msg = svc.addToCart("tok","s","p",5);
        assertEquals("Only 1 left in stock", msg);
    }

    /* ───────────────────────── login / signup ────────────────── */

    @Test
    void login_success_delegatesToConnectivity() throws Exception {
        when(connMock.login("bob","pw")).thenReturn("tok-123");
        assertEquals("tok-123", svc.login("bob","pw"));
        verify(connMock).login("bob","pw");
    }

    @Test
    void login_suspendedUser_translatesToSuspendedRuntime() throws Exception {
        when(connMock.login(any(),any()))
                .thenThrow(new IllegalArgumentException("User suspended"));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.login("bob","pw"));
        assertEquals("suspended", ex.getMessage());
    }

    @Test
    void login_badCreds_runtimeInvalidUserPwd() throws Exception {
        when(connMock.login(any(),any()))
                .thenThrow(new IllegalArgumentException("other"));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.login("bob","pw"));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void signUp_success_passthrough() throws Exception {
        svc.signUp("new","pw");
        verify(connMock).signUp("new","pw");
    }

    @Test
    void signUp_duplicateUser_translatesMessage() throws Exception {
        doThrow(new IllegalArgumentException())
                .when(connMock).signUp(any(),any());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.signUp("dup","pw"));
        assertEquals("User already exists", ex.getMessage());
    }

    /* ───────────────────── findProduct perms ─────────────────── */

    @Test
    void findProduct_withoutValidToken_throwsPermissionException() {
        doThrow(new RuntimeException("bad"))
                .when(tokenSvc).validateToken("badTok");
        assertThrows(PermissionException.class,
                () -> svc.findProduct("badTok","apple",null));
    }

    @Test
    void findProduct_success_passesThrough() throws Exception {
        doNothing().when(tokenSvc).validateToken("tok");
        when(searchMock.findProduct("apple","Fruit"))
                .thenReturn(List.of("p1","p2"));

        List<String> res = svc.findProduct("tok","apple","Fruit");
        assertEquals(List.of("p1","p2"), res);
    }

    /* ─────────────── getCartProducts merge logic ─────────────── */

    @Test
    void getCartProducts_mergesSameNames() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        ShoppingBag bag1 = new ShoppingBag("s1"); bag1.addProduct("p1",2);
        ShoppingBag bag2 = new ShoppingBag("s2"); bag2.addProduct("p1",1);
        ShoppingCart cart = new ShoppingCart();
        cart.getShoppingBags().addAll(List.of(bag1,bag2));

        RegisteredUser alice = mock(RegisteredUser.class);
        when(alice.getShoppingCart()).thenReturn(cart);
        when(userRepo.getById("alice")).thenReturn(alice);

        Product apple = new Product("s1","Apple","",1f,100,0,"Food");
        apple.setId("p1");
        when(productRepo.getById("p1")).thenReturn(apple);

        Map<String,Integer> result = svc.getCartProducts("tok");
        assertEquals(Map.of("Apple",3), result);
    }

    /* ───────── calculateCartPrice via DiscountPolicy ─────────── */

    @Test
    void calculateCartPrice_sumsBagsViaPolicy() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        ShoppingBag bag = new ShoppingBag("store");
        bag.addProduct("p1",2);
        ShoppingCart cart = new ShoppingCart();
        cart.getShoppingBags().add(bag);

        RegisteredUser alice = mock(RegisteredUser.class);
        when(alice.getShoppingCart()).thenReturn(cart);
        when(userRepo.getById("alice")).thenReturn(alice);

        doReturn(42.0f).when(discMock)
                .calculatePrice(eq("store"), any());

        assertEquals(42.0, svc.calculateCartPrice("tok"));
    }

    /* ────────────────────────── NEW tests ────────────────────── */

    /* ---------- removeFromCart additional branches ---------- */

    @Test
    void removeFromCart_success_delegatesToCart() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");
        doNothing().when(cartMock).removeFromCart("tok","s","p",1);

        svc.removeFromCart("tok","s","p",1);
        verify(cartMock).removeFromCart("tok","s","p",1);
    }

    @Test
    void removeFromCart_failure_propagatesMessage() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");
        doThrow(new IllegalArgumentException("store not found"))
                .when(cartMock).removeFromCart(any(),any(),any(),anyInt());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.removeFromCart("tok","s","p",1));
        assertEquals("store not found", ex.getMessage());
    }

    @Test
    void removeFromCart_failure_blankMessage_usesDefault() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");
        doThrow(new IllegalArgumentException())
                .when(cartMock).removeFromCart(any(),any(),any(),anyInt());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.removeFromCart("tok","s","p",1));
        assertEquals("Failed to remove product from cart", ex.getMessage());
    }

    /* ---------------- reserveCart branches ------------------- */

    @Test
    void reserveCart_success_passthrough() throws Exception {
        when(cartMock.reserveCart("tok")).thenReturn(99.5);
        assertEquals(99.5, svc.reserveCart("tok"));
    }

    @Test
    void reserveCart_failure_throwsRuntime() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");
        doThrow(new RuntimeException("boom"))
                .when(cartMock).reserveCart("tok");
        assertThrows(RuntimeException.class, () -> svc.reserveCart("tok"));
    }

    /* ---------------- getStoreByName / getAllProducts -------- */

    @Test
    void getStoreByName_filtersClosedStores() throws Exception {
        doNothing().when(tokenSvc).validateToken("tok");
        when(searchMock.getStoreByName("my")).thenReturn(List.of("s"));

        Store closed = mock(Store.class);
        when(closed.isOpenNow()).thenReturn(false);
        when(storeRepo.getById("s")).thenReturn(closed);

        assertTrue(svc.getStoreByName("tok","my").isEmpty());
    }

    @Test
    void getAllProducts_success() throws Exception {
        doNothing().when(tokenSvc).validateToken("tok");
        when(productRepo.findAll()).thenReturn(List.of(new Product()));
        assertEquals(1, svc.getAllProducts("tok").size());
    }

    /* ---------------- calculateCartPrice edge cases --------- */

    @Test
    void calculateCartPrice_singleBagUsesPolicy() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        ShoppingBag bag = new ShoppingBag("s"); bag.addProduct("p",1);
        ShoppingCart cart = new ShoppingCart();
        cart.getShoppingBags().add(bag);

        RegisteredUser user = mock(RegisteredUser.class);
        when(user.getShoppingCart()).thenReturn(cart);
        when(userRepo.getById("alice")).thenReturn(user);

        doReturn(5.0f).when(discMock).calculatePrice(eq("s"), any());
        assertEquals(5.0, svc.calculateCartPrice("tok"), 0.001);
    }

    @Test
    void calculateCartPrice_nullToken_illegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> svc.calculateCartPrice(null));
    }

    /* ---------------- token-null guards --------------------- */

    @Test
    void getCartProducts_nullToken_illegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> svc.getCartProducts(null));
    }

    @Test
    void getShoppingCart_nullToken_illegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> svc.getShoppingCart(null));
    }

    /* ---------------- searchStoreByName filtering ----------- */

    @Test
    void searchStoreByName_filtersOutClosedStores() throws Exception {
        Store open  = mock(Store.class);
        Store closed = mock(Store.class);
        when(open .isOpenNow()).thenReturn(true);
        when(closed.isOpenNow()).thenReturn(false);

        when(searchMock.searchStoreByName("mega"))
                .thenReturn(List.of(open, closed));

        List<Store> visible = svc.searchStoreByName("tok","mega");
        assertEquals(1, visible.size());
        assertTrue(visible.contains(open));
    }

    /* ---------------- getStoreById paths -------------------- */

    @Test
    void getStoreById_success_passesThrough() throws Exception {
        when(searchMock.getStoreById("s")).thenReturn("json");
        assertEquals("json", svc.getStoreById("tok","s"));
    }

    @Test
    void getStoreById_failure_wrapsMessage() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");
        when(searchMock.getStoreById("s"))
                .thenThrow(new IllegalArgumentException("fail reason"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.getStoreById("tok","s"));
        assertTrue(ex.getMessage().contains("fail reason"));
    }

    /* ---------------- getProductById ------------------------ */

    @Test
    void getProductById_success_returnsOptional() {
        Product p = new Product();
        when(productRepo.findById("x")).thenReturn(Optional.of(p));
        assertTrue(svc.getProductById("x").isPresent());
    }

    @Test
    void getProductById_repoThrows_returnsEmpty() {
        when(productRepo.findById("x"))
                .thenThrow(new RuntimeException("db down"));
        assertTrue(svc.getProductById("x").isEmpty());
    }

    /* ---------------- getProductsInStore -------------------- */

    @Test
    void getProductsInStore_success_passthrough() throws Exception {
        List<Product> list = List.of(new Product());
        when(searchMock.getProductsByStore("s")).thenReturn(list);
        assertEquals(list, svc.getProductsInStore("s"));
    }

    @Test
    void getProductsInStore_failure_returnsNull() throws Exception {
        when(searchMock.getProductsByStore("s"))
                .thenThrow(new RuntimeException("boom"));
        assertNull(svc.getProductsInStore("s"));
    }

    /* ---------------- getShoppingCart deep-copy ------------- */

    @Test
    void getShoppingCart_returnsDeepCopy() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        ShoppingBag originalBag = new ShoppingBag("s");
        originalBag.addProduct("p",1);
        ShoppingCart cart = new ShoppingCart();
        cart.getShoppingBags().add(originalBag);

        RegisteredUser alice = mock(RegisteredUser.class);
        when(alice.getShoppingCart()).thenReturn(cart);
        when(userRepo.getById("alice")).thenReturn(alice);

        List<ShoppingBag> copy = svc.getShoppingCart("tok");

        assertEquals(1, copy.size());
        assertNotSame(originalBag, copy.get(0));         // deep copy
        assertEquals(originalBag.getProducts(), copy.get(0).getProducts());
    }

    /* ---------------- purchaseCart early-exit branches ------ */

    @Test
    void purchaseCart_cartEmpty_throwsCartEmpty() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        // Spy so we can stub getShoppingCart
        UserService spySvc = Mockito.spy(svc);
        ReflectionTestUtils.setField(spySvc, "userCart", cartMock);

        doReturn(Collections.emptyList()).when(spySvc).getShoppingCart("tok");
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> spySvc.purchaseCart("tok",
                        "n","111","12/25","123","st","ct","addr","1","00000"));
        assertEquals("cart is empty", ex.getMessage());
    }

    @Test
    void purchaseCart_missingProduct_throwsCartChanged() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        ShoppingBag bag = new ShoppingBag("s");
        bag.addProduct("missing",1);

        UserService spySvc = Mockito.spy(svc);
        ReflectionTestUtils.setField(spySvc, "userCart", cartMock);

        doReturn(List.of(bag)).when(spySvc).getShoppingCart("tok");
        when(productRepo.getById("missing"))
                .thenThrow(new RuntimeException("no row"));
        doNothing().when(cartMock).removeFromCart(any(),any(),any(),anyInt());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> spySvc.purchaseCart("tok",
                        "n","111","12/25","123","st","ct","addr","1","00000"));
        assertEquals("cart changed", ex.getMessage());
    }

    @Test
    void purchaseCart_storeClosed_throwsFriendlyMsg() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        ShoppingBag bag = new ShoppingBag("s");
        bag.addProduct("p",1);

        UserService spySvc = Mockito.spy(svc);
        ReflectionTestUtils.setField(spySvc, "userCart", cartMock);

        doReturn(List.of(bag)).when(spySvc).getShoppingCart("tok");

        // product exists → cleanMissingProducts = false
        when(productRepo.getById("p")).thenReturn(new Product());

        Store closed = mock(Store.class);
        when(closed.isOpenNow()).thenReturn(false);
        when(closed.getName()).thenReturn("ClosedStore");
        when(storeRepo.getById("s")).thenReturn(closed);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> spySvc.purchaseCart("tok",
                        "n","111","12/25","123","st","ct","addr","1","00000"));
        assertTrue(ex.getMessage().contains("ClosedStore"));
    }
}
