package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingBagTest {

    private ShoppingBag bag;

    @BeforeEach
    void setUp() {
        bag = new ShoppingBag("store1");
    }

    @Test
    void testAddProduct_NewProduct() {
        bag.addProduct("prod1", 2);
        assertEquals(2, bag.getProducts().get("prod1"));
    }

    @Test
    void testAddProduct_ExistingProduct() {
        bag.addProduct("prod1", 2);
        bag.addProduct("prod1", 3);
        assertEquals(5, bag.getProducts().get("prod1"));
    }

    @Test
    void testRemoveProduct_ReduceQuantity() {
        bag.addProduct("prod1", 5);
        assertTrue(bag.removeProduct("prod1", 2));
        assertEquals(3, bag.getProducts().get("prod1"));
    }

    @Test
    void testRemoveProduct_RemoveCompletely() {
        bag.addProduct("prod1", 2);
        assertTrue(bag.removeProduct("prod1", 2));
        assertFalse(bag.getProducts().containsKey("prod1"));
    }

    @Test
    void testRemoveProduct_QuantityTooHighThrows() {
        bag.addProduct("prod1", 2);
        assertThrows(IllegalArgumentException.class, () -> bag.removeProduct("prod1", 3));
    }

    @Test
    void testRemoveProduct_ProductNotFound() {
        bag.addProduct("prod1", 2);
        assertFalse(bag.removeProduct("prod2", 1));
    }

    @Test
    void testSoldSetsAllQuantitiesToZero() {
        bag.addProduct("prod1", 2);
        bag.addProduct("prod2", 3);
        bag.sold();
        for (Integer qty : bag.getProducts().values()) {
            assertEquals(0, qty);
        }
    }

    @Test
    void testToStringContainsStoreId() {
        bag.addProduct("prod1", 1);
        assertTrue(bag.toString().contains("store1"));
    }

    @Test
    void testAddMultipleProducts() {
        bag.addProduct("prod1", 2);
        bag.addProduct("prod2", 3);
        assertEquals(2, bag.getProducts().size());
        assertEquals(2, bag.getProducts().get("prod1"));
        assertEquals(3, bag.getProducts().get("prod2"));
    }

    @Test
    void testRemoveAllProductsEmptiesBag() {
        bag.addProduct("prod1", 1);
        bag.addProduct("prod2", 1);
        assertTrue(bag.removeProduct("prod1", 1));
        assertTrue(bag.removeProduct("prod2", 1));
        assertTrue(bag.getProducts().isEmpty());
    }

    @Test
    void testAddProductWithZeroOrNegativeQuantity() {
        assertDoesNotThrow(() -> bag.addProduct("prod1", 0)); // Current implementation allows this
        assertDoesNotThrow(() -> bag.addProduct("prod2", -1)); // Current implementation allows this
        // If you want to restrict, add validation in ShoppingBag.addProduct
    }

    @Test
    void testSoldOnEmptyBagDoesNotThrow() {
        assertDoesNotThrow(() -> bag.sold());
        assertTrue(bag.getProducts().isEmpty());
    }
}
