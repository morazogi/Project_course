package ServiceLayer;

import DomainLayer.DomainServices.DiscountPolicyMicroservice;
import DomainLayer.DomainServices.Search;
import DomainLayer.DomainServices.UserCart;
import DomainLayer.DomainServices.UserConnectivity;
import DomainLayer.IToken;
import DomainLayer.PermissionException;
import DomainLayer.Product;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingBag;
import DomainLayer.ShoppingCart;
import InfrastructureLayer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Focuses on UserService façade behaviour: delegation, permission checks and
 * error propagation.  We replace internally-constructed collaborators with
 * mocks using Mockito’s construction mocking.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    /* -------- repositories & external services passed through ctor -------- */
    @Mock StoreRepository     storeRepo;
    @Mock UserRepository      userRepo;
    @Mock ProductRepository   productRepo;
    @Mock OrderRepository     orderRepo;
    @Mock DiscountRepository  discountRepo;
    @Mock GuestRepository     guestRepo;
    @Mock ShippingService     shippingSvc;
    @Mock PaymentService      paymentSvc;
    @Mock IToken              tokenSvc;

    /* ===============================================================
                              addToCart flow
       =============================================================== */
    @Test
    void addToCart_success_returnsFriendlyMessage() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        try (MockedConstruction<UserConnectivity> _uc =
                     mockConstruction(UserConnectivity.class);
             MockedConstruction<UserCart> cartMock =
                     mockConstruction(UserCart.class,
                             (mock, ctx) -> doNothing().when(mock)
                                     .addToCart("tok", "store-1", "p1", 2));
             MockedConstruction<Search> _search =
                     mockConstruction(Search.class);
             MockedConstruction<DiscountPolicyMicroservice> _disc =
                     mockConstruction(DiscountPolicyMicroservice.class))
        {
            UserService svc = new UserService(tokenSvc, storeRepo, userRepo,
                    productRepo, orderRepo,
                    shippingSvc, paymentSvc,
                    guestRepo, discountRepo);

            String msg = svc.addToCart("tok", "store-1", "p1", 2);

            assertEquals("Product added to cart", msg);
            verify(cartMock.constructed().get(0))
                    .addToCart("tok", "store-1", "p1", 2);
        }
    }

    @Test
    void addToCart_stockError_propagatesExactMessage() throws Exception {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        try (MockedConstruction<UserConnectivity> _uc =
                     mockConstruction(UserConnectivity.class);
             MockedConstruction<UserCart> cartMock =
                     mockConstruction(UserCart.class,
                             (mock, ctx) -> doThrow(new IllegalArgumentException("Only 1 left in stock"))
                                     .when(mock).addToCart(any(), any(), any(), anyInt()));
             MockedConstruction<Search> _search =
                     mockConstruction(Search.class);
             MockedConstruction<DiscountPolicyMicroservice> _disc =
                     mockConstruction(DiscountPolicyMicroservice.class))
        {
            UserService svc = new UserService(tokenSvc, storeRepo, userRepo,
                    productRepo, orderRepo,
                    shippingSvc, paymentSvc,
                    guestRepo, discountRepo);

            String msg = svc.addToCart("tok", "s", "p", 5);

            assertEquals("Only 1 left in stock", msg);  // exact text bubbled up
        }
    }

    /* ===============================================================
                       findProduct – permission check
       =============================================================== */
    @Test
    void findProduct_withoutValidToken_throwsPermissionException() throws Exception {
        doThrow(new RuntimeException("bad token"))
                .when(tokenSvc).validateToken("badTok");

        try (MockedConstruction<UserConnectivity> _uc =
                     mockConstruction(UserConnectivity.class);
             MockedConstruction<UserCart> _cart =
                     mockConstruction(UserCart.class);
             MockedConstruction<Search> _search =
                     mockConstruction(Search.class))
        {
            UserService svc = new UserService(tokenSvc, storeRepo, userRepo,
                    productRepo, orderRepo,
                    shippingSvc, paymentSvc,
                    guestRepo, discountRepo);

            assertThrows(PermissionException.class,
                    () -> svc.findProduct("badTok", "a", null));
        }
    }

    /* ===============================================================
                         getCartProducts aggregation
       =============================================================== */
    @Test
    void getCartProducts_mergesSameNames() {
        when(tokenSvc.extractUsername("tok")).thenReturn("alice");

        /* ---------- cart with two bags ---------- */
        ShoppingBag bag1 = new ShoppingBag("store-1");
        bag1.addProduct("p1", 2);                     // 2 × apple
        ShoppingBag bag2 = new ShoppingBag("store-2");
        bag2.addProduct("p1", 1);                     // 1 × apple in another store
        ShoppingCart cart = new ShoppingCart();
        cart.getShoppingBags().addAll(List.of(bag1, bag2));

        RegisteredUser alice = mock(RegisteredUser.class);
        when(alice.getShoppingCart()).thenReturn(cart);
        when(userRepo.getById("alice")).thenReturn(alice);

        Product apple = new Product("store-1", "Apple", "", 1f, 100, 0, "Food");
        apple.setId("p1");
        when(productRepo.getById("p1")).thenReturn(apple);

        try (MockedConstruction<UserConnectivity> _uc =
                     mockConstruction(UserConnectivity.class);
             MockedConstruction<UserCart> _cart =
                     mockConstruction(UserCart.class);
             MockedConstruction<Search> _search =
                     mockConstruction(Search.class);
             MockedConstruction<DiscountPolicyMicroservice> _d =
                     mockConstruction(DiscountPolicyMicroservice.class))
        {
            UserService svc = new UserService(tokenSvc, storeRepo, userRepo,
                    productRepo, orderRepo,
                    shippingSvc, paymentSvc,
                    guestRepo, discountRepo);

            Map<String,Integer> result = svc.getCartProducts("tok");

            assertEquals(Map.of("Apple", 3), result);   // 2 + 1 merged
        }
    }
}
