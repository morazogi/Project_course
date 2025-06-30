/* ────────────────────────────────────────────────────────────────
   src/test/java/ServiceLayer/UserCartTest.java
   ──────────────────────────────────────────────────────────────── */
package ServiceLayer;

import DomainLayer.DomainServices.IToNotify;
import DomainLayer.DomainServices.UserCart;
import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.ShoppingBag;
import DomainLayer.ShoppingCart;
import DomainLayer.Store;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserCartTest {

    /* ────────────────────────── common mocks ──────────────────── */
    @Mock IToken            tokener;
    @Mock UserRepository    userRepo;
    @Mock GuestRepository   guestRepo;
    @Mock StoreRepository   storeRepo;
    @Mock ProductRepository productRepo;
    @Mock OrderRepository   orderRepo;

    /* shared stub store (most tests re-use) */
    private Store stubStore;

    /* instance under test (without notifier) */
    private UserCart cartSvc;

    private final String storeId = "s1";
    private final String prodId  = "p1";

    @BeforeEach
    void setUp() {
        cartSvc = new UserCart(tokener, userRepo, storeRepo,
                productRepo, orderRepo, guestRepo, null);

        stubStore = mock(Store.class);
        when(stubStore.getId()).thenReturn(storeId);
        when(stubStore.getProductQuantity(anyString())).thenReturn(10);
        when(stubStore.reserveProduct(anyString(), anyInt())).thenReturn(true);
        when(stubStore.unreserveProduct(anyString(), anyInt())).thenReturn(true);
        doNothing().when(stubStore).sellProduct(anyString(), anyInt());
        when(storeRepo.getById(storeId)).thenReturn(stubStore);
    }

    /* ═══════════════════  ORIGINAL (existing) TESTS  ════════════ */

    @Test
    void addToCart_registeredUser_success_updatesRepo() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("alice");
        doNothing().when(tokener).validateToken("tok");

        Product apple = new Product(storeId,"Apple","",1f,10,0d,"F");
        apple.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(apple);

        RegisteredUser alice = mock(RegisteredUser.class, RETURNS_DEEP_STUBS);
        when(alice.getShoppingCart()).thenReturn(new ShoppingCart("alice"));
        when(userRepo.getById("alice")).thenReturn(alice);

        cartSvc.addToCart("tok", storeId, prodId, 3);

        verify(alice).addProduct(storeId, prodId, 3);
        verify(userRepo).update(alice);
    }

    @Test
    void addToCart_insufficientStock_throws() {
        when(tokener.extractUsername("tok")).thenReturn("alice");
        doNothing().when(tokener).validateToken("tok");

        Product scarce = new Product(storeId,"Rare","",5f,1,0d,"M");
        scarce.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(scarce);
        when(stubStore.getProductQuantity(prodId)).thenReturn(1);

        RegisteredUser alice = mock(RegisteredUser.class, RETURNS_DEEP_STUBS);
        when(alice.getShoppingCart()).thenReturn(new ShoppingCart("alice"));
        when(userRepo.getById("alice")).thenReturn(alice);

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.addToCart("tok", storeId, prodId, 2));
        verify(userRepo, never()).update(any());
    }

    @Test
    void removeFromCart_guest_success_callsUpdate() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("Guest-1");
        doNothing().when(tokener).validateToken("tok");

        Guest guest = mock(Guest.class);
        when(guest.getUsername()).thenReturn("Guest-1");
        when(guest.getShoppingCart()).thenReturn(new ShoppingCart("Guest-1"));
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

    /* tiny edge from the earlier second file */
    @Test
    void addToCart_unknownStore_throws() {
        when(tokener.extractUsername("tok")).thenReturn("alice");
        doNothing().when(tokener).validateToken("tok");
        when(storeRepo.getById("bad")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.addToCart("tok", "bad", prodId, 1));
    }

    /* ════════════════════  NEW ORIGINAL TESTS  ══════════════════ */

    /* 1 ▸ guest row missing → created & later updated */
    @Test
    void addToCart_guestMissing_createsAndUpdatesGuest() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("Guest-42");
        doNothing().when(tokener).validateToken("tok");

        when(guestRepo.getById("Guest-42"))
                .thenThrow(new RuntimeException("not found"));
        when(guestRepo.save(any(Guest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Product prod = new Product(storeId, "Pen", "", 2f, 20, 0d, "S");
        prod.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(prod);

        cartSvc.addToCart("tok", storeId, prodId, 1);

        verify(guestRepo).save(any(Guest.class));
        verify(guestRepo, atLeastOnce()).update(any(Guest.class)); // product-save path
    }

    /* 2 ▸ corrupt guest row (null username) is healed & then updated again */
    @Test
    void addToCart_corruptGuestHealed_updatesTwice() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("Guest-bad");
        doNothing().when(tokener).validateToken("tok");

        Guest corrupt = mock(Guest.class);
        when(corrupt.getUsername()).thenReturn(null);              // forces healing
        when(corrupt.getShoppingCart()).thenReturn(new ShoppingCart("Guest-bad"));
        when(guestRepo.getById("Guest-bad")).thenReturn(corrupt);

        Product prod = new Product(storeId,"Eraser","",1f,10,0d,"S");
        prod.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(prod);

        cartSvc.addToCart("tok", storeId, prodId, 1);

        verify(guestRepo, times(2)).update(corrupt);               // heal + add-product
    }

    /* 3 ▸ store returns –1 → “product not found” branch */
    @Test
    void addToCart_productNotFoundInStore_throws() {
        when(tokener.extractUsername("tok")).thenReturn("alice");
        when(stubStore.getProductQuantity(prodId)).thenReturn(-1);

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.addToCart("tok", storeId, prodId, 1));
    }

    /* 4 ▸ already-in-cart + new qty > available → overflow error */
    @Test
    void addToCart_quantityExceedsAvailable_throws() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("Guest-full");
        doNothing().when(tokener).validateToken("tok");
        when(stubStore.getProductQuantity(prodId)).thenReturn(3);

        Map<String,Integer> items = new HashMap<>();
        items.put(prodId, 2);                                      // already 2 in cart
        ShoppingBag bag = mock(ShoppingBag.class);
        when(bag.getStoreId()).thenReturn(storeId);
        when(bag.getProducts()).thenReturn(items);

        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getShoppingBags()).thenReturn(List.of(bag));

        Guest guest = mock(Guest.class);
        when(guest.getUsername()).thenReturn("Guest-full");        // avoid healing
        when(guest.getShoppingCart()).thenReturn(cart);
        when(guestRepo.getById("Guest-full")).thenReturn(guest);

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.addToCart("tok", storeId, prodId, 2)); // would need 4

        verify(guestRepo, never()).update(guest);                   // only heal avoids
    }

    /* 5 ▸ full happy-path reserveCart (guest) */
    @Test
    void reserveCart_guest_success_returnsTotalPrice() throws Exception {
        when(tokener.extractUsername("tok")).thenReturn("Guest-10");
        doNothing().when(tokener).validateToken("tok");

        Product prod = new Product(storeId,"Cookie","",4f,50,0d,"F");
        prod.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(prod);
        when(stubStore.getProductQuantity(prodId)).thenReturn(5);

        ShoppingBag bag = mock(ShoppingBag.class);
        when(bag.getStoreId()).thenReturn(storeId);
        when(bag.getProducts()).thenReturn(Map.of(prodId,2));

        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getShoppingBags()).thenReturn(List.of(bag));

        Guest guest = mock(Guest.class);
        when(guest.getUsername()).thenReturn("Guest-10");          // avoid healing
        when(guest.getShoppingCart()).thenReturn(cart);
        when(guest.getCartReserved()).thenReturn(false);
        when(guestRepo.getById("Guest-10")).thenReturn(guest);

        double total = cartSvc.reserveCart("tok");

        assertEquals(8.0, total);
        verify(stubStore).reserveProduct(prodId, 2);
        verify(guestRepo, atLeastOnce()).update(guest);
    }

    /* 6 ▸ product missing during reserve → throws before reserving */
    @Test
    void reserveCart_productMissing_throws() {
        when(tokener.extractUsername("tok")).thenReturn("Guest-x");
        doNothing().when(tokener).validateToken("tok");

        ShoppingBag bag = mock(ShoppingBag.class);
        when(bag.getStoreId()).thenReturn(storeId);
        when(bag.getProducts()).thenReturn(Map.of(prodId,1));

        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getShoppingBags()).thenReturn(List.of(bag));

        Guest guest = mock(Guest.class);
        when(guest.getUsername()).thenReturn("Guest-x");
        when(guest.getShoppingCart()).thenReturn(cart);
        when(guestRepo.getById("Guest-x")).thenReturn(guest);

        when(productRepo.getById(prodId)).thenReturn(null);         // missing

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.reserveCart("tok"));
        verify(stubStore, never()).reserveProduct(any(), anyInt());
    }

    /* 7 ▸ explicit unreserveCart releases stock */
    @Test
    void unreserveCart_releasesReservedStock() throws Exception {
        Map<String,Integer> reserved = Map.of(prodId, 3);

        Product prod = new Product(storeId,"Ball","",1f,10,0d,"T");
        prod.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(prod);

        cartSvc.unreserveCart(reserved, "someone");

        verify(stubStore).unreserveProduct(prodId, 3);
        verify(storeRepo).update(stubStore);
    }

    /* 8 ▸ purchaseCart invoked while cart NOT reserved → error */
    @Test
    void purchaseCart_cartNotReserved_throws() {
        when(tokener.extractUsername("tok")).thenReturn("Guest-noRes");
        doNothing().when(tokener).validateToken("tok");

        Guest guest = mock(Guest.class);
        when(guest.getCartReserved()).thenReturn(false);
        when(guestRepo.getById("Guest-noRes")).thenReturn(guest);

        assertThrows(IllegalArgumentException.class,
                () -> cartSvc.purchaseCart("tok", 0d));
    }

    /* 9 ▸ full happy-path purchaseCart with notification & order save */
    @Test
    void purchaseCart_success_processesSaleAndSendsNotification() throws Exception {
        IToNotify notifier = mock(IToNotify.class);
        UserCart svc = new UserCart(tokener, userRepo, storeRepo,
                productRepo, orderRepo, guestRepo, notifier);

        when(tokener.extractUsername("tok")).thenReturn("Guest-buy");
        doNothing().when(tokener).validateToken("tok");

        Product prod = new Product(storeId,"Game","",10f,100,0d,"G");
        prod.setId(prodId);
        when(productRepo.getById(prodId)).thenReturn(prod);
        when(stubStore.getName()).thenReturn("MegaStore");

        ShoppingBag bag = mock(ShoppingBag.class);
        when(bag.getStoreId()).thenReturn(storeId);
        when(bag.getProducts()).thenReturn(Map.of(prodId,1));

        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getShoppingBags()).thenReturn(List.of(bag));

        Guest guest = mock(Guest.class);
        when(guest.getUsername()).thenReturn("Guest-buy");
        when(guest.getShoppingCart()).thenReturn(cart);
        when(guest.getCartReserved()).thenReturn(true);
        when(guestRepo.getById("Guest-buy")).thenReturn(guest);

        svc.purchaseCart("tok", 10d);

        verify(stubStore).sellProduct(prodId, 1);
        verify(orderRepo).save(any());
        verify(notifier).sendNotificationToStoreOwners(
                eq("tok"), eq("MegaStore"), contains("sold in your store"));
        verify(guestRepo, atLeastOnce()).update(guest);
    }
}
