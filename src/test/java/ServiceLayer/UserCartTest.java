package ServiceLayer;

import DomainLayer.DomainServices.UserCart;
import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingBag;
import DomainLayer.ShoppingCart;
import InfrastructureLayer.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies add/remove flows and stock enforcement inside {@link UserCart}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserCartTest {

    /* ------------- collaborators injected through ctor ------------- */
    @Mock IToken            tokener;
    @Mock UserRepository    userRepo;
    @Mock GuestRepository   guestRepo;
    @Mock StoreRepository   storeRepo;
    @Mock ProductRepository productRepo;
    @Mock OrderRepository   orderRepo;

    private UserCart cartService;

    /* ---------- shared fixtures ---------- */
    private Product apple;
    private final String storeId = "store-1";
    private final String productId = "p-apple";

    @BeforeEach
    void init() {
        cartService = new UserCart(tokener, userRepo, storeRepo,
                productRepo, orderRepo, guestRepo);

        /* product in catalogue with 10 in stock */
        apple = new Product(storeId, "Apple", "", 1.5f, 10, 0, "Food");
        apple.setId(productId);
        when(productRepo.getById(productId)).thenReturn(apple);
    }

    /* ================================================================
                            addToCart – registered user
       ================================================================ */
    @Test
    void addToCart_registeredUser_success_updatesRepo() throws Exception {
        // token resolves to registered username
        when(tokener.extractUsername("tok")).thenReturn("alice");
        doNothing().when(tokener).validateToken("tok");

        // user with empty cart
        RegisteredUser alice = mock(RegisteredUser.class, RETURNS_DEEP_STUBS);
        ShoppingCart   empty = mock(ShoppingCart.class);
        when(empty.getShoppingBags()).thenReturn(new ArrayList<>()); // no bags yet
        when(alice.getShoppingCart()).thenReturn(empty);
        when(alice.getUsername()).thenReturn("alice");
        doNothing().when(alice).addProduct(storeId, productId, 3);
        when(userRepo.getById("alice")).thenReturn(alice);

        cartService.addToCart("tok", storeId, productId, 3);

        verify(alice).addProduct(storeId, productId, 3);
        verify(userRepo).update(alice);
    }

    @Test
    void addToCart_insufficientStock_throwsIllegalArgument() {
        when(tokener.extractUsername("tok")).thenReturn("alice");
        doNothing().when(tokener).validateToken("tok");

        apple.setQuantity(2); // only 2 left, we’ll ask for 5

        RegisteredUser alice = mock(RegisteredUser.class, RETURNS_DEEP_STUBS);
        ShoppingCart   empty = mock(ShoppingCart.class);
        when(empty.getShoppingBags()).thenReturn(new ArrayList<>());
        when(alice.getShoppingCart()).thenReturn(empty);
        when(userRepo.getById("alice")).thenReturn(alice);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCart("tok", storeId, productId, 5));

        assertTrue(ex.getMessage().startsWith("Only"));
        verify(userRepo, never()).update(any());
    }

    /* ================================================================
                           removeFromCart – guest
       ================================================================ */
    @Test
    void removeFromCart_guest_success_callsUpdate() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("Guest-42");
        doNothing().when(tokener).validateToken("tok");

        Guest guest = mock(Guest.class);
        when(guest.getUsername()).thenReturn("Guest-42");
        doNothing().when(guest).removeProduct(storeId, productId, 1);
        when(guestRepo.getById("Guest-42")).thenReturn(guest);

        cartService.removeFromCart("tok", storeId, productId, 1);

        verify(guest).removeProduct(storeId, productId, 1);
        verify(guestRepo).update(guest);
    }

    /* ================================================================
                       removeFromCart – bad quantity
       ================================================================ */
    @Test
    void removeFromCart_zeroQuantity_throwsIllegalArgument() {
        when(tokener.extractUsername("tok")).thenReturn("alice");
        assertThrows(IllegalArgumentException.class,
                () -> cartService.removeFromCart("tok", storeId, productId, 0));
    }
}
