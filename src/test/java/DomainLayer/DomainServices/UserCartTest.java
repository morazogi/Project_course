package DomainLayer.DomainServices;

import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserCart}.  All collaborators are mocked; we use a real
 * {@link ShoppingCart} where needed to compute in-cart quantities.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserCartTest {

    /* ---------- mocked collaborators ---------- */
    @Mock IToken            tokener;
    @Mock UserRepository    userRepo;
    @Mock GuestRepository   guestRepo;
    @Mock StoreRepository   storeRepo;
    @Mock ProductRepository productRepo;
    @Mock OrderRepository   orderRepo;

    private UserCart cartSvc;

    /* shared fixtures */
    private final String storeId = "s1";
    private final String prodId  = "p1";

    @BeforeEach
    void setUp() {
        cartSvc = new UserCart(tokener, userRepo, storeRepo,
                productRepo, orderRepo, guestRepo);
    }

    /* ===============================================================
                           addToCart – registered
       =============================================================== */
    @Test
    void addToCart_registeredUser_success_updatesRepo() throws Exception {
        String token = "tok";
        when(tokener.extractUsername(token)).thenReturn("alice");
        doNothing().when(tokener).validateToken(token);

        /* product with ample stock */
        Product apple = new Product(storeId, "Apple", "", 1.0f, 10, 0d, "Food");
        apple.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(apple);

        /* user with empty ShoppingCart */
        RegisteredUser alice = mock(RegisteredUser.class, RETURNS_DEEP_STUBS);
        when(alice.getShoppingCart()).thenReturn(new ShoppingCart("alice"));
        when(userRepo.getById("alice")).thenReturn(alice);

        cartSvc.addToCart(token, storeId, prodId, 3);

        verify(alice).addProduct(storeId, prodId, 3);
        verify(userRepo).update(alice);
    }

    @Test
    void addToCart_insufficientStock_throws() {
        when(tokener.extractUsername("tok")).thenReturn("alice");
        doNothing().when(tokener).validateToken("tok");

        Product scarce = new Product(storeId, "Rare", "", 5f, 1, 0d, "Misc");
        scarce.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(scarce);

        RegisteredUser alice = mock(RegisteredUser.class, RETURNS_DEEP_STUBS);
        when(alice.getShoppingCart()).thenReturn(new ShoppingCart("alice"));
        when(userRepo.getById("alice")).thenReturn(alice);

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.addToCart("tok", storeId, prodId, 2));
        verify(userRepo, never()).update(any());
    }

    /* ===============================================================
                           removeFromCart – guest
       =============================================================== */
    @Test
    void removeFromCart_guest_success_callsUpdate() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("Guest-1");
        doNothing().when(tokener).validateToken("tok");

        Guest guest = mock(Guest.class);
        when(guest.getUsername()).thenReturn("Guest-1");
        when(guestRepo.getById("Guest-1")).thenReturn(guest);

        cartSvc.removeFromCart("tok", storeId, prodId, 1);

        verify(guest).removeProduct(storeId, prodId, 1);
        verify(guestRepo).update(guest);
    }

    @Test
    void removeFromCart_zeroQuantity_throws() {
        when(tokener.extractUsername("tok")).thenReturn("alice");

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.removeFromCart("tok", storeId, prodId, 0));
    }
}
