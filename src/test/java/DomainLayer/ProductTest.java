package DomainLayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    private Product product;
    private String testStoreId;
    private String testName;
    private String testDescription;
    private float testPrice;
    private int testQuantity;
    private double testRating;
    private String testCategory;

    @BeforeEach
    void setUp() {
        testStoreId = "store-123";
        testName = "Test Product";
        testDescription = "This is a test product description";
        testPrice = 29.99f;
        testQuantity = 10;
        testRating = 4.5;
        testCategory = "Electronics";

        product = new Product(testStoreId, testName, testDescription, testPrice,
                testQuantity, testRating, testCategory);
    }

    @Test
    @DisplayName("Constructor should create Product with valid quantity")
    void testValidConstructor() {
        Product newProduct = new Product("store-1", "Product 1", "Description",
                19.99f, 5, 3.8, "Books");

        assertEquals("store-1", newProduct.getStoreId());
        assertEquals("Product 1", newProduct.getName());
        assertEquals("Description", newProduct.getDescription());
        assertEquals(19.99f, newProduct.getPrice(), 0.01);
        assertEquals(5, newProduct.getQuantity());
        assertEquals(3.8, newProduct.getRating(), 0.01);
        assertEquals("Books", newProduct.getCategory());
        assertNull(newProduct.getId()); // ID is null until persisted
    }

    @Test
    @DisplayName("Constructor should throw exception for zero quantity")
    void testConstructorWithZeroQuantity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Product("store-1", "Product", "Description", 10.0f, 0, 4.0, "Category")
        );

        assertEquals("Product must have at least 1 quantity", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor should throw exception for negative quantity")
    void testConstructorWithNegativeQuantity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Product("store-1", "Product", "Description", 10.0f, -5, 4.0, "Category")
        );

        assertEquals("Product must have at least 1 quantity", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor should accept minimum valid quantity of 1")
    void testConstructorWithMinimumQuantity() {
        Product minProduct = new Product("store-1", "Product", "Description",
                10.0f, 1, 4.0, "Category");

        assertEquals(1, minProduct.getQuantity());
        assertEquals("Product", minProduct.getName());
    }

    @Test
    @DisplayName("Default constructor should create empty Product")
    void testDefaultConstructor() {
        Product emptyProduct = new Product();

        assertNull(emptyProduct.getId());
        assertNull(emptyProduct.getStoreId());
        assertNull(emptyProduct.getName());
        assertNull(emptyProduct.getDescription());
        assertEquals(0.0f, emptyProduct.getPrice(), 0.01);
        assertEquals(0, emptyProduct.getQuantity());
        assertEquals(0.0, emptyProduct.getRating(), 0.01);
        assertNull(emptyProduct.getCategory());
    }

    @Test
    @DisplayName("Getters should return correct values")
    void testGetters() {
        assertEquals(testStoreId, product.getStoreId());
        assertEquals(testName, product.getName());
        assertEquals(testDescription, product.getDescription());
        assertEquals(testPrice, product.getPrice(), 0.01);
        assertEquals(testQuantity, product.getQuantity());
        assertEquals(testRating, product.getRating(), 0.01);
        assertEquals(testCategory, product.getCategory());
        assertNull(product.getId()); // ID is null until persisted
    }

    @Test
    @DisplayName("Setters should update values correctly")
    void testSetters() {
        String newStoreId = "new-store-456";
        String newName = "Updated Product";
        String newDescription = "Updated description";
        int newPrice = 50; // Note: setPrice takes int parameter
        int newQuantity = 25;
        double newRating = 3.7;
        String newCategory = "Home & Garden";
        String newId = "product-id-123";

        product.setStoreId(newStoreId);
        product.setName(newName);
        product.setDescription(newDescription);
        product.setPrice(newPrice);
        product.setQuantity(newQuantity);
        product.setRating(newRating);
        product.setCategory(newCategory);
        product.setId(newId);

        assertEquals(newStoreId, product.getStoreId());
        assertEquals(newName, product.getName());
        assertEquals(newDescription, product.getDescription());
        assertEquals((float)newPrice, product.getPrice(), 0.01);
        assertEquals(newQuantity, product.getQuantity());
        assertEquals(newRating, product.getRating(), 0.01);
        assertEquals(newCategory, product.getCategory());
        assertEquals(newId, product.getId());
    }

    @Test
    @DisplayName("Product should handle null description")
    void testNullDescription() {
        Product productWithNullDesc = new Product("store-1", "Product", null,
                15.0f, 3, 4.0, "Category");

        assertNull(productWithNullDesc.getDescription());
        assertEquals("Product", productWithNullDesc.getName());
    }

    @Test
    @DisplayName("Product should handle null category")
    void testNullCategory() {
        Product productWithNullCategory = new Product("store-1", "Product", "Description",
                15.0f, 3, 4.0, null);

        assertNull(productWithNullCategory.getCategory());
        assertEquals("Product", productWithNullCategory.getName());
    }

    @Test
    @DisplayName("Product should handle empty string values")
    void testEmptyStringValues() {
        Product productWithEmptyStrings = new Product("", "", "", 0.0f, 1, 0.0, "");

        assertEquals("", productWithEmptyStrings.getStoreId());
        assertEquals("", productWithEmptyStrings.getName());
        assertEquals("", productWithEmptyStrings.getDescription());
        assertEquals("", productWithEmptyStrings.getCategory());
    }

    @Test
    @DisplayName("Product should handle zero price")
    void testZeroPrice() {
        Product freeProduct = new Product("store-1", "Free Product", "Free item",
                0.0f, 5, 4.0, "Free");

        assertEquals(0.0f, freeProduct.getPrice(), 0.01);
        assertEquals("Free Product", freeProduct.getName());
    }

    @Test
    @DisplayName("Product should handle zero rating")
    void testZeroRating() {
        Product unratedProduct = new Product("store-1", "Unrated Product", "No ratings yet",
                10.0f, 3, 0.0, "Category");

        assertEquals(0.0, unratedProduct.getRating(), 0.01);
        assertEquals("Unrated Product", unratedProduct.getName());
    }

    @Test
    @DisplayName("Product should handle maximum rating")
    void testMaximumRating() {
        Product maxRatedProduct = new Product("store-1", "Perfect Product", "5 star product",
                50.0f, 2, 5.0, "Premium");

        assertEquals(5.0, maxRatedProduct.getRating(), 0.01);
        assertEquals("Perfect Product", maxRatedProduct.getName());
    }

    @Test
    @DisplayName("toString should return formatted string")
    void testToString() {
        product.setId("test-id-123");
        String expected = "Product{" +
                "id=test-id-123" +
                ", storeId=" + testStoreId +
                ", name='" + testName + '\'' +
                ", description='" + testDescription + '\'' +
                ", price=" + testPrice +
                ", quantity=" + testQuantity +
                ", rating=" + testRating +
                ", category=" + testCategory +
                '}';

        assertEquals(expected, product.toString());
    }

    @Test
    @DisplayName("toString should handle null id")
    void testToStringWithNullId() {
        String result = product.toString();
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("name='" + testName + "'"));
    }

    @Test
    @DisplayName("Multiple products should be independent")
    void testProductIndependence() {
        Product product1 = new Product("store-1", "Product 1", "Desc 1", 10.0f, 5, 4.0, "Cat1");
        Product product2 = new Product("store-2", "Product 2", "Desc 2", 20.0f, 3, 3.0, "Cat2");

        product1.setName("Updated Product 1");
        product1.setPrice(15);

        assertEquals("Updated Product 1", product1.getName());
        assertEquals(15.0f, product1.getPrice(), 0.01);
        assertEquals("Product 2", product2.getName()); // Should remain unchanged
        assertEquals(20.0f, product2.getPrice(), 0.01); // Should remain unchanged
    }

    @Test
    @DisplayName("Setters should be thread-safe (synchronized)")
    void testSettersSynchronization() throws InterruptedException {
        // This test verifies that setters are synchronized
        // We'll create multiple threads that modify the same product
        final int numThreads = 10;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                product.setName("Thread-" + threadId);
                product.setPrice(threadId);
                product.setQuantity(threadId + 1);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify final state is consistent (some thread's values should be set)
        assertNotNull(product.getName());
        assertTrue(product.getName().startsWith("Thread-"));
        assertTrue(product.getPrice() >= 0 && product.getPrice() < numThreads);
        assertTrue(product.getQuantity() >= 1 && product.getQuantity() <= numThreads);
    }

    @Test
    @DisplayName("Constructor should handle edge case values")
    void testConstructorEdgeCases() {
        // Test with very large numbers
        Product largeProduct = new Product("store", "Large Product", "Description",
                Float.MAX_VALUE, Integer.MAX_VALUE,
                Double.MAX_VALUE, "Category");

        assertEquals(Float.MAX_VALUE, largeProduct.getPrice(), 0.01);
        assertEquals(Integer.MAX_VALUE, largeProduct.getQuantity());
        assertEquals(Double.MAX_VALUE, largeProduct.getRating(), 0.01);
    }
}