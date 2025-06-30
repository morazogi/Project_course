package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Roles.Guest;

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

    @Test
    void testAddProduct_InvalidQuantity_ThrowsException() {
        String storeId = "store1";
        String productId = "prodA";
        assertThrows(IllegalArgumentException.class, () -> guest.addProduct(storeId, productId, 0));
        assertThrows(IllegalArgumentException.class, () -> guest.addProduct(storeId, productId, -5));
    }

    @Test
    void testRemoveProduct_RemovesOnlySpecifiedQuantity() {
        String storeId = "store1";
        String productId = "prodA";
        guest.addProduct(storeId, productId, 5);
        guest.removeProduct(storeId, productId, 2);
        ShoppingBag bag = guest.getShoppingCart().getShoppingBags().get(0);
        assertEquals(3, bag.getProducts().get(productId));
    }

    @Test
    void testRemoveProduct_RemovesBagIfLastProductRemoved() {
        String storeId = "store1";
        String productId = "prodA";
        guest.addProduct(storeId, productId, 1);
        guest.removeProduct(storeId, productId, 1);
        assertTrue(guest.getShoppingCart().getShoppingBags().isEmpty());
    }

    @Test
    void testRemoveProduct_WrongStore_ThrowsException() {
        guest.addProduct("store1", "prodA", 1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            guest.removeProduct("store2", "prodA", 1);
        });
        assertEquals("Product not found in cart", exception.getMessage());
    }

    @Test
    void testSetAndGetShoppingCart() {
        ShoppingCart newCart = new ShoppingCart("testUser");
        guest.setShoppingCart(newCart);
        assertEquals(newCart, guest.getShoppingCart());
    }

    @Test
    void testSetAndGetUsername() {
        guest.setUsername("newGuestName");
        assertEquals("newGuestName", guest.getUsername());
    }

    @Test
    void testSetCartReservedAndGetCartReserved() {
        guest.setCartReserved(true);
        assertTrue(guest.getCartReserved());
        guest.setCartReserved(false);
        assertFalse(guest.getCartReserved());
    }

    @Test
    void testRemoveProduct_RemovesBagAndLogsEvent() {
        // This test ensures that removing the last product from a bag removes the bag.
        String storeId = "store1";
        String productId = "prodA";
        guest.addProduct(storeId, productId, 1);
        guest.removeProduct(storeId, productId, 1);
        assertTrue(guest.getShoppingCart().getShoppingBags().isEmpty());
    }

    @Test
    void testRemoveProduct_ProductNotInBag_ThrowsException() {
        String storeId = "store1";
        String productId = "prodA";
        guest.addProduct(storeId, productId, 1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            guest.removeProduct(storeId, "prodB", 1);
        });
        assertEquals("Product not found in cart", exception.getMessage());
    }

    @Test
    void testAddProduct_DuplicateProductInSameBag() {
        String storeId = "store1";
        String productId = "prodA";
        guest.addProduct(storeId, productId, 2);
        guest.addProduct(storeId, productId, 3);
        ShoppingBag bag = guest.getShoppingCart().getShoppingBags().get(0);
        assertEquals(5, bag.getProducts().get(productId));
    }

    @Test
    void testAddProduct_DifferentStoresCreatesMultipleBags() {
        guest.addProduct("store1", "prodA", 1);
        guest.addProduct("store2", "prodB", 2);
        assertEquals(2, guest.getShoppingCart().getShoppingBags().size());
    }


    @Test
    void testSetShoppingCartToNull() {
        guest.setShoppingCart(null);
        assertNull(guest.getShoppingCart());
    }

    @Test
    void testSetUsernameToNull() {
        guest.setUsername(null);
        assertNull(guest.getUsername());
    }

    @Test
    void testCartBagsListIsMutable() {
        guest.addProduct("store1", "prodA", 1);
        ShoppingCart cart = guest.getShoppingCart();
        cart.getShoppingBags().clear();
        assertTrue(cart.getShoppingBags().isEmpty());
    }
}
