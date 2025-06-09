package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.ShoppingCart;
import DomainLayer.ShoppingBag;

import java.util.List;

class ShoppingCartTest {

    private ShoppingCart cart;
    private final String userId = "user123";
    private final String storeId = "storeA";
    private final String productId = "prod1";

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart("username");
    }

    @Test
    void testConstructorSetsUserIdAndEmptyBags() {
        assertEquals(userId, cart.getUserId(), "UserId should be set by constructor");
        assertNotNull(cart.getShoppingBags(), "ShoppingBags list should not be null");
        assertTrue(cart.getShoppingBags().isEmpty(), "ShoppingBags should start empty");
    }

    @Test
    void testAddProduct_NewStore_CreatesBagWithProduct() {
        cart.addProduct(storeId, productId, 2);
        List<ShoppingBag> bags = cart.getShoppingBags();
        assertEquals(1, bags.size(), "Should create one shopping bag");
        ShoppingBag bag = bags.get(0);
        assertEquals(storeId, bag.getStoreId(), "Bag should have correct storeId");
        assertTrue(bag.getProducts().containsKey(productId), "Bag should contain added product");
        assertEquals(2, bag.getProducts().get(productId).intValue(), "Product quantity should match");
    }

    @Test
    void testAddProduct_ExistingBag_IncrementsQuantity() {
        cart.addProduct(storeId, productId, 1);
        cart.addProduct(storeId, productId, 3);
        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals(4, bag.getProducts().get(productId).intValue(), "Quantity should accumulate on repeated adds");
    }

    @Test
    void testAddProduct_NonPositiveQuantity_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cart.addProduct(storeId, productId, 0));
        assertEquals("Quantity must be greater than 0", ex.getMessage());
    }

    @Test
    void testRemoveProduct_ExistingProduct_ReturnsTrueAndAdjustsQuantity() {
        cart.addProduct(storeId, productId, 5);
        boolean removed = cart.removeProduct(storeId, productId, 3);
        assertTrue(removed, "removeProduct should return true when product found");
        ShoppingBag bag = cart.getShoppingBags().get(0);
        assertEquals(2, bag.getProducts().get(productId).intValue(), "Quantity should decrease by removed amount");
    }

    @Test
    void testRemoveProduct_RemovesBagWhenEmpty() {
        cart.addProduct(storeId, productId, 2);
        boolean removed = cart.removeProduct(storeId, productId, 2);
        assertTrue(removed);
        assertTrue(cart.getShoppingBags().isEmpty(), "Bag should be removed when no products remain");
    }

    @Test
    void testRemoveProduct_Nonexistent_ReturnsFalseWithoutException() {
        boolean removed = cart.removeProduct(storeId, productId, 1);
        assertFalse(removed, "removeProduct should return false when product not found");
    }

    @Test
    void testSold_DoesNotThrow() {
        cart.addProduct(storeId, productId, 1);
        assertDoesNotThrow(() -> cart.sold(), "sold() should execute without exception");
    }
}
