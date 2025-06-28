package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ShoppingCart} on Java 17 (no List#getFirst()).
 */
class ShoppingCartTest {

    private ShoppingCart cart;

    private final String store = "store-1";
    private final String prod  = "apple";

    @BeforeEach
    void init() {
        cart = new ShoppingCart("alice");
    }

    /* ------------------------------------------------------------
                        addProduct happy-path
       ------------------------------------------------------------ */
    @Test
    void addProduct_firstTime_createsNewBag() {
        cart.addProduct(store, prod, 2);

        List<ShoppingBag> bags = cart.getShoppingBags();
        assertEquals(1, bags.size());

        ShoppingBag bag = bags.get(0);
        assertEquals(store,            bag.getStoreId());
        assertEquals(Map.of(prod, 2),  bag.getProducts());
    }

    @Test
    void addProduct_again_sameStore_mergesQuantity() {
        cart.addProduct(store, prod, 2);   // 2 apples
        cart.addProduct(store, prod, 3);   // +3 apples

        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals(5, bag.getProducts().get(prod));  // 2 + 3
    }

    @Test
    void addProduct_invalidQuantity_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> cart.addProduct(store, prod, 0));
    }

    /* ------------------------------------------------------------
                        removeProduct behaviour
       ------------------------------------------------------------ */
    @Test
    void removeProduct_reducesQuantity_andKeepsBag() {
        cart.addProduct(store, prod, 4);

        boolean ok = cart.removeProduct(store, prod, 1); // leave 3

        assertTrue(ok);
        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals(3, bag.getProducts().get(prod));
        assertEquals(1, cart.getShoppingBags().size());  // bag still present
    }

    @Test
    void removeProduct_removesBagWhenEmpty() {
        cart.addProduct(store, prod, 2);

        cart.removeProduct(store, prod, 2);              // quantity → 0

        assertTrue(cart.getShoppingBags().isEmpty());
    }

    @Test
    void removeProduct_nonExisting_returnsFalse() {
        cart.addProduct(store, prod, 2);

        assertFalse(cart.removeProduct("unknownStore", "x", 1));
        assertEquals(1, cart.getShoppingBags().size());  // unchanged
    }

    /* ------------------------------------------------------------
                               sold()
       ------------------------------------------------------------ */
    @Test
    void sold_clearsAllBags() {
        cart.addProduct(store,  "p1", 1);
        cart.addProduct("s2",   "p2", 2);

        cart.sold();

        assertTrue(cart.getShoppingBags().isEmpty(),
                "Cart should be empty after sold()");
    }
}
