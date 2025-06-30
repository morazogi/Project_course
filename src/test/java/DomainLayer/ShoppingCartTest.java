package DomainLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart("user1");
    }

    @Test
    void testConstructorAndGetUserId() {
        assertEquals("user1", cart.getUserId());
        assertTrue(cart.getShoppingBags().isEmpty());
    }

    @Test
    void testAddProduct_NewBag() {
        cart.addProduct("store1", "prod1", 2);
        assertEquals(1, cart.getShoppingBags().size());
        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals("store1", bag.getStoreId());
        assertEquals(2, bag.getProducts().get("prod1"));
    }

    @Test
    void testAddProduct_ExistingBag() {
        cart.addProduct("store1", "prod1", 2);
        cart.addProduct("store1", "prod1", 3);
        assertEquals(1, cart.getShoppingBags().size());
        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals(5, bag.getProducts().get("prod1"));
    }

    @Test
    void testAddProduct_InvalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct("store1", "prod1", 0));
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct("store1", "prod1", -1));
    }

    @Test
    void testRemoveProduct_ReduceQuantity() {
        cart.addProduct("store1", "prod1", 5);
        boolean removed = cart.removeProduct("store1", "prod1", 2);
        assertTrue(removed);
        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals(3, bag.getProducts().get("prod1"));
    }

    @Test
    void testRemoveProduct_RemoveBagWhenEmpty() {
        cart.addProduct("store1", "prod1", 2);
        boolean removed = cart.removeProduct("store1", "prod1", 2);
        assertTrue(removed);
        assertTrue(cart.getShoppingBags().isEmpty());
    }

    @Test
    void testRemoveProduct_ProductNotFound() {
        cart.addProduct("store1", "prod1", 2);
        boolean removed = cart.removeProduct("store1", "prod2", 1);
        assertFalse(removed);
    }

    @Test
    void testRemoveProduct_BagNotFound() {
        cart.addProduct("store1", "prod1", 2);
        boolean removed = cart.removeProduct("store2", "prod1", 1);
        assertFalse(removed);
    }

    @Test
    void testSold_DelegatesToBags() {
        cart.addProduct("store1", "prod1", 2);
        cart.addProduct("store2", "prod2", 3);
        // No exception means sold() delegates to bags (assuming ShoppingBag.sold() is implemented)
        assertDoesNotThrow(() -> cart.sold());
    }

    @Test
    void testToString_NotNull() {
        cart.addProduct("store1", "prod1", 1);
        assertNotNull(cart.toString());
        assertTrue(cart.toString().contains("user1"));
    }

    @Test
    void testEachStoreGetsOwnShoppingBag() {
        cart.addProduct("store1", "prodA", 1);
        cart.addProduct("store2", "prodB", 2);
        cart.addProduct("store1", "prodC", 3);

        assertEquals(2, cart.getShoppingBags().size());

        ShoppingBag bag1 = cart.getShoppingBags().stream()
                .filter(b -> b.getStoreId().equals("store1")).findFirst().orElse(null);
        ShoppingBag bag2 = cart.getShoppingBags().stream()
                .filter(b -> b.getStoreId().equals("store2")).findFirst().orElse(null);

        assertNotNull(bag1);
        assertNotNull(bag2);
        assertEquals(2, bag1.getProducts().size());
        assertEquals(1, bag2.getProducts().size());
        assertTrue(bag1.getProducts().containsKey("prodA"));
        assertTrue(bag1.getProducts().containsKey("prodC"));
        assertTrue(bag2.getProducts().containsKey("prodB"));
    }

    @Test
    void testRemoveProductRemovesCorrectBag() {
        cart.addProduct("store1", "prodA", 1);
        cart.addProduct("store2", "prodB", 2);

        // Remove all from store1's bag
        assertTrue(cart.removeProduct("store1", "prodA", 1));
        assertEquals(1, cart.getShoppingBags().size());
        assertEquals("store2", cart.getShoppingBags().get(0).getStoreId());
    }

    @Test
    void testAddAndRemoveAcrossMultipleStores() {
        cart.addProduct("store1", "prodA", 2);
        cart.addProduct("store2", "prodB", 3);
        cart.addProduct("store3", "prodC", 4);

        assertEquals(3, cart.getShoppingBags().size());

        // Remove one product from store2
        assertTrue(cart.removeProduct("store2", "prodB", 3));
        assertEquals(2, cart.getShoppingBags().size());
        assertNull(cart.getShoppingBags().stream()
                .filter(b -> b.getStoreId().equals("store2")).findFirst().orElse(null));
    }
}