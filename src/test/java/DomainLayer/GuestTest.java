package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Roles.Guest;
import DomainLayer.ShoppingCart;
import DomainLayer.ShoppingBag;

class GuestTest {

    private Guest guest;

    @BeforeEach
    void setUp() {
        guest = new Guest();
    }

    @Test
    void testInitialShoppingCartEmpty() {
        ShoppingCart cart = guest.getShoppingCart();
        assertNotNull(cart, "ShoppingCart should not be null");
        assertTrue(cart.getShoppingBags().isEmpty(), "Cart should start empty");
    }

    @Test
    void testAddProduct_NewStore_CreatesNewBag() {
        String storeId = "store1";
        String productId = "prodA";

        guest.addProduct(storeId, productId, 2);

        ShoppingCart cart = guest.getShoppingCart();
        assertEquals(1, cart.getShoppingBags().size(), "One bag should be created");
        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals(storeId, bag.getStoreId());
        assertTrue(bag.getProducts().containsKey(productId));
        assertEquals(2, bag.getProducts().get(productId).intValue());
    }

    @Test
    void testAddProduct_ExistingStore_IncrementsQuantity() {
        String storeId = "store1";
        String productId = "prodA";

        guest.addProduct(storeId, productId, 1);
        guest.addProduct(storeId, productId, 3);

        ShoppingBag bag = guest.getShoppingCart().getShoppingBags().get(0);
        assertEquals(4, bag.getProducts().get(productId).intValue(), "Quantity should accumulate");
    }

    @Test
    void testRemoveProduct_DecrementsQuantity() {
        String storeId = "store1";
        String productId = "prodA";

        guest.addProduct(storeId, productId, 5);
        guest.removeProduct(storeId, productId, 2);

        ShoppingBag bag = guest.getShoppingCart().getShoppingBags().get(0);
        assertEquals(3, bag.getProducts().get(productId).intValue(), "Quantity should decrease");
    }

    @Test
    void testRemoveProduct_RemovesBagWhenEmpty() {
        String storeId = "store1";
        String productId = "prodA";

        guest.addProduct(storeId, productId, 2);
        guest.removeProduct(storeId, productId, 2);

        assertTrue(guest.getShoppingCart().getShoppingBags().isEmpty(), "Bag should be removed when empty");
    }

    @Test
    void testRemoveProduct_ProductNotFound_ThrowsException() {
        String storeId = "store1";
        String productId = "prodB";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            guest.removeProduct(storeId, productId, 1);
        });
        assertEquals("Product not found in cart", exception.getMessage());
    }

    @Test
    void testCartReservedFlag() {
        assertFalse(guest.getCartReserved(), "Default cartReserved should be false");
        guest.setCartReserved(true);
        assertTrue(guest.getCartReserved(), "cartReserved should be true after setting");
    }

    @Test
    void testGetID_UniquePerInstance() {
        Guest other = new Guest();
        assertNotEquals(guest.getUsername(), other.getUsername(), "Each Guest should have a unique ID"); // changed from id to user name just now 11.06
    }
}
