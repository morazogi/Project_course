package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    private Store store;
    private ShoppingCart shoppingCart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId("123"); // ✔️ sets the ID manually
        store.setRating(5); // optional, if you need a rating

        shoppingCart = new ShoppingCart("user1");

        apple = new Product("1", "123", "Apple", "Fresh red apple", 250, 10, 4.7);
        banana = new Product("2", "123", "Banana", "Organic banana", 175, 20, 4.5);

    }

    @Test
    void testAddNewProductToNewStore() {
        shoppingCart.addProduct(store, apple);

        assertEquals(1, shoppingCart.getShoppingBags().size());
        assertEquals(1, shoppingCart.getShoppingBags().get(0).getProducts().get(apple));
    }

    @Test
    void testAddSameProductTwiceIncrementsQuantity() {
        shoppingCart.addProduct(store, apple);
        shoppingCart.addProduct(store, apple);

        assertEquals(2, shoppingCart.getShoppingBags().get(0).getProducts().get(apple));
    }

    @Test
    void testAddTwoDifferentProductsToSameStore() {
        shoppingCart.addProduct(store, apple);
        shoppingCart.addProduct(store, banana);

        assertEquals(2, shoppingCart.getShoppingBags().get(0).getProducts().size());
    }

    @Test
    void testRemoveProductDecreasesQuantityOrRemovesProduct() {
        shoppingCart.addProduct(store, apple);
        shoppingCart.addProduct(store, apple);

        shoppingCart.removeProduct(store, apple);

        assertEquals(1, shoppingCart.getShoppingBags().get(0).getProducts().get(apple));
    }

    @Test
    void testRemoveLastProductRemovesBag() {
        shoppingCart.addProduct(store, apple);
        shoppingCart.removeProduct(store, apple);

        assertTrue(shoppingCart.getShoppingBags().isEmpty());
    }

    @Test
    void testRemoveNonExistingProductReturnsFalse() {
        assertFalse(shoppingCart.removeProduct(store, banana));
    }

    @Test
    void testCalculatePurchaseTotal_Success() {
        store.increaseProduct(apple, 10);
        store.increaseProduct(banana, 10);
        shoppingCart.addProduct(store, apple);
        shoppingCart.addProduct(store, banana);

        double total = shoppingCart.calculatePurchaseCart();
        assertEquals(4.25, total);
        assertTrue(shoppingCart.getShoppingBags().isEmpty());
    }

    @Test
    void testCalculatePurchaseTotal_ProductUnavailable_Failure() {
        store.increaseProduct(apple, 0); // Not enough stock
        shoppingCart.addProduct(store, apple);

        double total = shoppingCart.calculatePurchaseCart();
        assertEquals(-1, total);
    }

    @Test
    void testCalculatePurchaseTotal_StoreClosed_Failure() {
        store.close();
        shoppingCart.addProduct(store, apple);

        double total = shoppingCart.calculatePurchaseCart();
        assertEquals(-1, total);
    }

    @Test
    void testEmptyCartReturnsZeroTotal() {
        assertEquals(0, shoppingCart.calculatePurchaseCart());
    }

    @Test
    void testProductQuantityDoesNotExceedInventoryLimit() {
        store.increaseProduct(apple, 1);
        shoppingCart.addProduct(store, apple);
        shoppingCart.addProduct(store, apple); // should not add the second time

        assertEquals(1, shoppingCart.getShoppingBags().get(0).getProducts().get(apple));
    }

}
