package DomainLayer;

import ServiceLayer.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreTest {

    private Store store;
    private Product apple;
    private Product banana;
    private ProductService mockProductService;

    @BeforeEach
    void setUp() {
        mockProductService = Mockito.mock(ProductService.class);

        store = new Store();
        store.setId("store1");
        store.setRating(4);
        store.setProductService(mockProductService); // Inject mock

        apple = new Product("p1", "store1", "Apple", "Fresh apple", 200, 10, 4.5);
        banana = new Product("p2", "store1", "Banana", "Organic banana", 150, 15, 4.2);
    }

    @Test
    void testOpenAndCloseStore() {
        store.closeTheStore();
        assertFalse(store.isOpenNow());
        store.openTheStore();
        assertTrue(store.isOpenNow());
    }

    @Test
    void testRegisterUser() {
        User user = new DomainLayer.TestUser();
        assertTrue(store.registerUser(user));
    }

    @Test
    void testAddNewProduct_Success() {
        assertTrue(store.addNewProduct(apple, 5));
        assertFalse(store.addNewProduct(apple, 5)); // already exists
    }

    @Test
    void testAddNewProduct_InvalidQuantity() {
        assertFalse(store.addNewProduct(apple, 0));
        assertFalse(store.addNewProduct(apple, -1));
    }

    @Test
    void testIncreaseProduct_Success() {
        store.addNewProduct(apple, 5);
        when(mockProductService.increaseQuantity("p1", 3)).thenReturn(true);
        assertTrue(store.increaseProduct(apple, 3));
    }

    @Test
    void testIncreaseProduct_Invalid() {
        assertFalse(store.increaseProduct(apple, -1)); // invalid quantity
        assertFalse(store.increaseProduct(apple, 3));  // not added yet
    }

    @Test
    void testDecreaseProduct_Success() {
        store.addNewProduct(apple, 5);
        when(mockProductService.decreaseQuantity("p1", 2)).thenReturn(true);
        assertTrue(store.decreaseProduct(apple, 2));
    }

    @Test
    void testDecreaseProduct_Invalid() {
        assertFalse(store.decreaseProduct(apple, 2)); // not added yet
        store.addNewProduct(apple, 2);
        when(mockProductService.decreaseQuantity("p1", 3)).thenReturn(false);
        assertFalse(store.decreaseProduct(apple, 3)); // too many
    }

    @Test
    void testChangeProductQuantity() {
        store.addNewProduct(apple, 5);
        assertTrue(store.changeProductQuantity(apple, 10));
        assertTrue(store.changeProductQuantity(apple, 0)); // removed
        assertFalse(store.changeProductQuantity(apple, 3)); // already removed
    }

    @Test
    void testRemoveProduct() {
        assertFalse(store.removeProduct(apple));
        store.addNewProduct(apple, 5);
        assertTrue(store.removeProduct(apple));
    }

    @Test
    void testCalculateProduct() {
        store.addNewProduct(apple, 10);
        assertEquals(400, store.calculateProduct(apple, 2));
        assertEquals(-1, store.calculateProduct(apple, 0));
        assertEquals(-1, store.calculateProduct(banana, 1));
        assertEquals(-1, store.calculateProduct(apple, 20));
    }

    @Test
    void testSellProduct() {
        store.addNewProduct(apple, 10);
        when(mockProductService.decreaseQuantity("p1", 3)).thenReturn(true);
        assertEquals(600, store.sellProduct(apple, 3));
        when(mockProductService.decreaseQuantity("p1", 15)).thenReturn(false);
        assertEquals(-1, store.sellProduct(apple, 15));
    }

    @Test
    void testAvailableProduct() {
        store.addNewProduct(apple, 10);
        assertTrue(store.availableProduct(apple, 5));
        assertFalse(store.availableProduct(apple, 20));
        assertFalse(store.availableProduct(banana, 1));
    }

    @Test
    void testAddNewProductByIdFailsIfProductNotInStore() {
        assertFalse(store.addNewProduct("not-exist", 5));
    }

    @Test
    void testIncreaseDecreaseRemoveById() {
        store.addNewProduct(apple, 5);
        when(mockProductService.increaseQuantity("p1", 3)).thenReturn(true);
        when(mockProductService.decreaseQuantity("p1", 2)).thenReturn(true);
        assertTrue(store.increaseProduct("p1", 3));
        assertTrue(store.decreaseProduct("p1", 2));
        assertTrue(store.removeProduct("p1"));
        assertFalse(store.increaseProduct("p1", 1));
    }

    @Test
    void testChangeQuantityById() {
        store.addNewProduct(apple, 5);
        assertTrue(store.changeProductQuantity("p1", 2));
        assertTrue(store.changeProductQuantity("p1", 0));
        assertFalse(store.changeProductQuantity("p1", 3));
    }

    @Test
    void testStoreToStringNotNull() {
        store.addNewProduct(apple, 5);
        store.registerUser(new DomainLayer.TestUser());
        assertNotNull(store.toString());
    }
}
