//
//package ServiceLayer;
//
//import DomainLayer.*;
//import DomainLayer.DomainServices.UserCart;
//import DomainLayer.DomainServices.UserConnectivity;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Exhaustive unit tests for {@link UserService}:
// *   – happy, sad, and bad paths for every public method.
// *
// * Requires mockito-inline (or Mockito ≥ 5) for mockConstruction().
// */
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceAllPathsTest {
//
//    /* ---- ctor dependencies ---- */
//    @Mock IToken             tokenSrv;
//    @Mock IStoreRepository   storeRepo;
//    @Mock IUserRepository    userRepo;
//    @Mock IProductRepository prodRepo;
//    @Mock IOrderRepository   orderRepo;
//    @Mock ShippingService    shipSrv;
//    @Mock PaymentService     paySrv;
//
//    /* ---- construction mocks ---- */
//    MockedConstruction<UserConnectivity> mcConn;
//    MockedConstruction<UserCart>         mcCart;
//
//    @AfterEach
//    void tearDown() {
//        if (mcConn != null) mcConn.close();
//        if (mcCart != null) mcCart.close();
//    }
//
//    // -------------------------------------------------------------------------
//    // 1. login
//    // -------------------------------------------------------------------------
//    @Test
//    void login_happy_returnsJwt() throws Exception {
//        mcConn = mockConstruction(UserConnectivity.class,
//            (mock, ctx) -> when(mock.login("alice", "pw")).thenReturn("jwt"));
//
//        assertEquals("jwt", newSvc().login("alice", "pw"));
//    }
//
//    @Test
//    void login_sad_illegalArgumentWrapped() throws Exception {
//        mcConn = mockConstruction(UserConnectivity.class,
//            (mock, ctx) -> when(mock.login(any(), any()))
//                               .thenThrow(new IllegalArgumentException()));
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().login("bob", "bad"));
//    }
//
//    // -------------------------------------------------------------------------
//    // 2. signUp
//    // -------------------------------------------------------------------------
//    @Test
//    void signUp_happy() throws Exception {
//        mcConn = mockConstruction(UserConnectivity.class);
//
//        assertDoesNotThrow(() -> newSvc().signUp("neo", "matrix"));
//        verify(mcConn.constructed().get(0)).signUp("neo", "matrix");
//    }
//
//    @Test
//    void signUp_bad_userExists() throws Exception {
//        mcConn = mockConstruction(UserConnectivity.class,
//            (mock, ctx) -> doThrow(new IllegalArgumentException())
//                               .when(mock).signUp(any(), any()));
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().signUp("dup", "pw"));
//    }
//
//    // -------------------------------------------------------------------------
//    // 3. addToCart
//    // -------------------------------------------------------------------------
//    @Test
//    void addToCart_happy() throws Exception {
//        initCart();
//        when(tokenSrv.extractUsername("jwt")).thenReturn("yaniv");
//
//        String msg = newSvc().addToCart("jwt", "s1", "p1", 1);
//
//        assertEquals("Product added to cart", msg);
//        verify(cart()).addToCart("jwt", "s1", "p1", 1);
//    }
//
//    @Test
//    void addToCart_bad_failureInCart() throws Exception {
//        initCart(c -> {
//            try {
//                doThrow(new RuntimeException("fail"))
//                                 .when(c).addToCart(any(), any(), any(), anyInt());
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//        when(tokenSrv.extractUsername("jwt")).thenReturn("yaniv");
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().addToCart("jwt", "s1", "p1", 2));
//    }
//
//    // -------------------------------------------------------------------------
//    // 4. removeFromCart
//    // -------------------------------------------------------------------------
//    @Test
//    void removeFromCart_happy() throws Exception {
//        initCart();
//        when(tokenSrv.extractUsername("jwt")).thenReturn("yaniv");
//
//        assertDoesNotThrow(
//            () -> newSvc().removeFromCart("jwt", "s1", "p1", 1));
//        verify(cart()).removeFromCart("jwt", "s1", "p1", 1);
//    }
//
//    @Test
//    void removeFromCart_sad_cartThrows() throws Exception {
//        initCart(c -> {
//            try {
//                doThrow(new RuntimeException())
//                                 .when(c).removeFromCart(any(), any(), any(), anyInt());
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//        when(tokenSrv.extractUsername("jwt")).thenReturn("yaniv");
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().removeFromCart("jwt", "s", "p", 3));
//    }
//
//    // -------------------------------------------------------------------------
//    // 5. reserveCart
//    // -------------------------------------------------------------------------
//    @Test
//    void reserveCart_happy_returnsPrice() throws Exception {
//        initCart(c -> {
//            try {
//                when(c.reserveCart("jwt")).thenReturn(42.0);
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//
//        assertEquals(42.0, newSvc().reserveCart("jwt"));
//    }
//
//    @Test
//    void reserveCart_bad_wrapException() throws Exception {
//        initCart(c -> {
//            try {
//                when(c.reserveCart("jwt"))
//                                 .thenThrow(new RuntimeException("boom"));
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().reserveCart("jwt"));
//    }
//
//    // -------------------------------------------------------------------------
//    // 6. purchaseCart
//    // -------------------------------------------------------------------------
//    @Test
//    void purchaseCart_happy_fullFlow() throws Exception {
//        initCart(c -> {
//            try {
//                when(c.reserveCart("jwt")).thenReturn(100.0);
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//
//        newSvc().purchaseCart(
//            "jwt", "credit", "4111", "06/30", "123",
//            "IL", "Beer Sheva", "Ben-Gurion", "15");
//
//        UserCart cart = cart();
//        verify(cart).reserveCart("jwt");
//        verify(shipSrv).processShipping("jwt", "IL", "Beer Sheva",
//                                        "Ben-Gurion", "15");
//        verify(paySrv).processPayment("jwt", "credit", "4111", "06/30", "123");
//        verify(cart).purchaseCart("jwt", 100.0);
//    }
//
//    @Test
//    void purchaseCart_sad_shippingFails() throws Exception {
//        initCart(c -> {
//            try {
//                when(c.reserveCart("jwt")).thenReturn(50.0);
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//        when(shipSrv.processShipping(any(), any(), any(), any(), any()))
//            .thenThrow(new RuntimeException("ship down"));
//        when(tokenSrv.extractUsername("jwt")).thenReturn("yaniv");
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().purchaseCart("jwt", "credit", "4", "06/30", "1",
//                                        "IL", "City", "Street", "10"));
//
//        verify(paySrv, never()).processPayment(any(), any(), any(), any(), any());
//    }
//
//    @Test
//    void purchaseCart_sad_paymentFails() throws Exception {
//        initCart(c -> {
//            try {
//                when(c.reserveCart("jwt")).thenReturn(50.0);
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//        when(paySrv.processPayment(any(), any(), any(), any(), any()))
//            .thenThrow(new RuntimeException("ship down"));
//        when(tokenSrv.extractUsername("jwt")).thenReturn("yaniv");
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().purchaseCart("jwt", "credit", "4", "06/30", "1",
//                                        "IL", "City", "Street", "10"));
//
//    }
//
//    @Test
//    void purchaseCart_bad_reserveFailsImmediately() throws Exception {
//        initCart(c -> {
//            try {
//                when(c.reserveCart("jwt"))
//                                 .thenThrow(new RuntimeException("reserve fail"));
//            } catch (JsonProcessingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        });
//        when(tokenSrv.extractUsername("jwt")).thenReturn("yaniv");
//
//        assertThrows(RuntimeException.class,
//            () -> newSvc().purchaseCart("jwt", "credit", "4", "06/30", "1",
//                                        "IL", "City", "Street", "10"));
//
//        verify(shipSrv, never()).processShipping(any(), any(), any(), any(), any());
//        verify(paySrv,  never()).processPayment(any(), any(), any(), any(), any());
//    }
//
//    // -------------------------------------------------------------------------
//    // helpers
//    // -------------------------------------------------------------------------
//    private UserService newSvc() {
//        return new UserService(tokenSrv, storeRepo, userRepo,
//                               prodRepo, orderRepo, shipSrv, paySrv);
//    }
//
//    private void initCart() throws Exception { initCart(c -> {}); }
//
//    private void initCart(java.util.function.Consumer<UserCart> cfg)
//            throws Exception {
//        mcCart = mockConstruction(UserCart.class,
//                   (mock, ctx) -> cfg.accept(mock));
//    }
//
//    private UserCart cart() { return mcCart.constructed().get(0); }
//}
