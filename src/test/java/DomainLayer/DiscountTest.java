/*Test Coverage Includes:
1. Constructor Testing

Tests proper initialization of all discount parameters
Validates enum conversions from float values

2. Discount Level Testing

Product Level: Tests discounts applied to specific products
Category Level: Tests discounts applied to product categories
Store Level: Tests store-wide discounts

3. Conditional Logic Testing

MIN_PRICE: Tests minimum price requirements
MIN_QUANTITY: Tests minimum quantity requirements for specific products
MAX_QUANTITY: Tests maximum quantity limits
No Condition: Tests unconditional discounts

4. Numerical Composition Testing

UNDEFINED: Basic subtraction discount
MULTIPLICATION: Multiplicative discount application
MAXIMUM: Tests finding and applying the highest discount among nested discounts

5. Logic Composition Testing

XOR: Tests exclusive OR logic (exactly one condition must be true)
AND: Tests that all conditions must be true
OR: Tests that at least one condition must be true

6. Edge Cases

Tests already used discounts (should not reapply)
Tests empty discounted strings (applies to all products in scope)
Tests getter/setter methods
Direct testing of the checkConditional method

7. Setup
The test uses a realistic setup with:

3 different products (Laptop, Mouse, Book) from different categories
Proper quantity mapping
Initial price mapping for discount calculations

Key Features:

Comprehensive Coverage: Tests all major paths through your discount logic
Realistic Data: Uses actual product data that mirrors real-world scenarios
Edge Case Testing: Covers boundary conditions and error states
Clear Test Names: Each test has a descriptive name explaining what it's testing
Proper Assertions: Uses appropriate delta values for float comparisons

*/

package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class DiscountTest {

    private Product product1;
    private Product product2;
    private Product product3;
    private Map<Product, Integer> productsQuantity;
    private Map<Product, Float> productDiscounts;
    private String storeId;

    @BeforeEach
    void setUp() {
        storeId = "store123";

        // Create test products
        product1 = new Product(storeId, "Laptop", "Gaming laptop", 1000.0f, 5, 4.5, "Electronics");
        product2 = new Product(storeId, "Mouse", "Gaming mouse", 50.0f, 10, 4.0, "Electronics");
        product3 = new Product(storeId, "Book", "Programming book", 30.0f, 15, 4.2, "Books");

        // Initialize quantity map
        productsQuantity = new HashMap<>();
        productsQuantity.put(product1, 2);
        productsQuantity.put(product2, 3);
        productsQuantity.put(product3, 1);

        // Initialize discount map with original prices
        productDiscounts = new HashMap<>();
        productDiscounts.put(product1, product1.getPrice());
        productDiscounts.put(product2, product2.getPrice());
        productDiscounts.put(product3, product3.getPrice());
    }

    @Test
    @DisplayName("Test discount constructor with valid parameters")
    void testDiscountConstructor() {
        List<String> nestedDiscounts = Arrays.asList("discount1", "discount2");

        Discount discount = new Discount(
                storeId,
                1.0f,  // Product level
                2.0f,  // AND logic
                1.0f,  // Maximum numerical
                nestedDiscounts,
                0.15f, // 15% discount
                "Laptop",
                1.0f,  // Min price condition
                500.0f, // Minimum $500
                ""
        );

        assertEquals(storeId, discount.getStoreId());
        assertEquals(Discount.Level.PRODUCT, discount.getLevel());
        assertEquals(Discount.LogicComposition.AND, discount.getLogicComposition());
        assertEquals(Discount.NumericalComposition.MAXIMUM, discount.getNumericalComposition());
        assertEquals(0.15f, discount.getPercentDiscount());
        assertEquals("Laptop", discount.getDiscounted());
        assertEquals(Discount.ConditionalType.MIN_PRICE, discount.getConditional());
        assertEquals(500.0f, discount.getLimiter());
        assertEquals(nestedDiscounts, discount.getDiscounts());
    }

    @Test
    @DisplayName("Test product level discount with no condition")
    void testProductLevelDiscountNoCondition() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 0.0f, -1.0f, ""
        );

        Map<Product, Float> result = discount.applyDiscount(
                2000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Laptop should have 10% discount applied
        assertEquals(900.0f, result.get(product1), 0.01f);
        // Other products should remain unchanged
        assertEquals(50.0f, result.get(product2), 0.01f);
        assertEquals(30.0f, result.get(product3), 0.01f);
    }

    @Test
    @DisplayName("Test category level discount")
    void testCategoryLevelDiscount() {
        Discount discount = new Discount(
                storeId, 2.0f, 0.0f, 0.0f, null, 0.2f, "Electronics", 0.0f, -1.0f, ""
        );

        Map<Product, Float> result = discount.applyDiscount(
                2000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Electronics products should have 20% discount
        assertEquals(800.0f, result.get(product1), 0.01f); // 1000 - 200
        assertEquals(40.0f, result.get(product2), 0.01f);  // 50 - 10
        // Books category should remain unchanged
        assertEquals(30.0f, result.get(product3), 0.01f);
    }

    @Test
    @DisplayName("Test store level discount")
    void testStoreLevelDiscount() {
        Discount discount = new Discount(
                storeId, 3.0f, 0.0f, 0.0f, null, 0.05f, "", 0.0f, -1.0f, ""
        );

        Map<Product, Float> result = discount.applyDiscount(
                2000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // All products should have 5% discount
        assertEquals(950.0f, result.get(product1), 0.01f);
        assertEquals(47.5f, result.get(product2), 0.01f);
        assertEquals(28.5f, result.get(product3), 0.01f);
    }

    @Test
    @DisplayName("Test minimum price condition - condition met")
    void testMinPriceConditionMet() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 500.0f, ""
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Condition met (1000 >= 500), discount should be applied
        assertEquals(900.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test minimum price condition - condition not met")
    void testMinPriceConditionNotMet() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 2000.0f, ""
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Condition not met (1000 < 2000), no discount should be applied
        assertEquals(1000.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test minimum quantity condition - condition met")
    void testMinQuantityConditionMet() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.15f, "Mouse", 2.0f, 2.0f, "Mouse"
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Mouse quantity is 3, condition met (3 >= 2)
        assertEquals(42.5f, result.get(product2), 0.01f); // 50 - 7.5
    }

    @Test
    @DisplayName("Test minimum quantity condition - condition not met")
    void testMinQuantityConditionNotMet() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.15f, "Mouse", 2.0f, 5.0f, "Mouse"
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Mouse quantity is 3, condition not met (3 < 5)
        assertEquals(50.0f, result.get(product2), 0.01f);
    }

    @Test
    @DisplayName("Test maximum quantity condition - condition met")
    void testMaxQuantityConditionMet() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Book", 3.0f, 2.0f, "Book"
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Book quantity is 1, condition met (1 <= 2)
        assertEquals(27.0f, result.get(product3), 0.01f); // 30 - 3
    }

    @Test
    @DisplayName("Test maximum quantity condition - condition not met")
    void testMaxQuantityConditionNotMet() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Mouse", 3.0f, 2.0f, "Mouse"
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Mouse quantity is 3, condition not met (3 > 2)
        assertEquals(50.0f, result.get(product2), 0.01f);
    }

    @Test
    @DisplayName("Test multiplication numerical composition")
    void testMultiplicationNumericalComposition() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 2.0f, null, 0.2f, "Laptop", 0.0f, -1.0f, ""
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Multiplication: 1000 * (1 - 0.2) = 800
        assertEquals(800.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test maximum numerical composition with nested discounts")
    void testMaximumNumericalComposition() {
        // Create nested discounts
        List<Discount> nestedDiscounts = Arrays.asList(
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 0.0f, -1.0f, ""),
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.25f, "Laptop", 0.0f, -1.0f, "")
        );

        Discount mainDiscount = new Discount(
                storeId, 1.0f, 0.0f, 1.0f, null, 0.15f, "Laptop", 0.0f, -1.0f, ""
        );

        Map<Product, Float> result = mainDiscount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, nestedDiscounts
        );

        // Should apply maximum discount: 0.25 (25%)
        assertEquals(750.0f, result.get(product1), 0.01f); // 1000 - 250
        assertTrue(mainDiscount.isAlreadyUsed());
    }

    @Test
    @DisplayName("Test XOR logic composition - exactly one condition true")
    void testXorLogicCompositionOneTrue() {
        List<Discount> nestedDiscounts = Arrays.asList(
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 2000.0f, "") // condition false
        );

        Discount mainDiscount = new Discount(
                storeId, 1.0f, 1.0f, 0.0f, null, 0.15f, "Laptop", 1.0f, 500.0f, "" // condition true
        );

        Map<Product, Float> result = mainDiscount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, nestedDiscounts
        );

        // XOR: exactly one condition is true, discount should apply
        assertEquals(850.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test XOR logic composition - both conditions true")
    void testXorLogicCompositionBothTrue() {
        List<Discount> nestedDiscounts = Arrays.asList(
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 500.0f, "") // condition true
        );

        Discount mainDiscount = new Discount(
                storeId, 1.0f, 1.0f, 0.0f, null, 0.15f, "Laptop", 1.0f, 500.0f, "" // condition true
        );

        Map<Product, Float> result = mainDiscount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, nestedDiscounts
        );

        // XOR: both conditions true, discount should NOT apply
        assertEquals(1000.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test AND logic composition - all conditions true")
    void testAndLogicCompositionAllTrue() {
        List<Discount> nestedDiscounts = Arrays.asList(
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 500.0f, "") // condition true
        );

        Discount mainDiscount = new Discount(
                storeId, 1.0f, 2.0f, 0.0f, null, 0.15f, "Laptop", 1.0f, 500.0f, "" // condition true
        );

        Map<Product, Float> result = mainDiscount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, nestedDiscounts
        );

        // AND: all conditions true, discount should apply
        assertEquals(850.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test AND logic composition - one condition false")
    void testAndLogicCompositionOneFalse() {
        List<Discount> nestedDiscounts = Arrays.asList(
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 2000.0f, "") // condition false
        );

        Discount mainDiscount = new Discount(
                storeId, 1.0f, 2.0f, 0.0f, null, 0.15f, "Laptop", 1.0f, 500.0f, "" // condition true
        );

        Map<Product, Float> result = mainDiscount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, nestedDiscounts
        );

        // AND: one condition false, discount should NOT apply
        assertEquals(1000.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test OR logic composition - one condition true")
    void testOrLogicCompositionOneTrue() {
        List<Discount> nestedDiscounts = Arrays.asList(
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 2000.0f, "") // condition false
        );

        Discount mainDiscount = new Discount(
                storeId, 1.0f, 3.0f, 0.0f, null, 0.15f, "Laptop", 1.0f, 500.0f, "" // condition true
        );

        Map<Product, Float> result = mainDiscount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, nestedDiscounts
        );

        // OR: at least one condition true, discount should apply
        assertEquals(850.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test OR logic composition - all conditions false")
    void testOrLogicCompositionAllFalse() {
        List<Discount> nestedDiscounts = Arrays.asList(
                new Discount(storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 2000.0f, "") // condition false
        );

        Discount mainDiscount = new Discount(
                storeId, 1.0f, 3.0f, 0.0f, null, 0.15f, "Laptop", 1.0f, 2000.0f, "" // condition false
        );

        Map<Product, Float> result = mainDiscount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, nestedDiscounts
        );

        // OR: all conditions false, discount should NOT apply
        assertEquals(1000.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test already used discount")
    void testAlreadyUsedDiscount() {
        Discount discount = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 0.0f, -1.0f, ""
        );

        discount.setAlreadyUsed(true);

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // Already used discount should not be applied
        assertEquals(1000.0f, result.get(product1), 0.01f);
    }

    @Test
    @DisplayName("Test empty discounted string applies to all products in level")
    void testEmptyDiscountedString() {
        Discount discount = new Discount(
                storeId, 2.0f, 0.0f, 0.0f, null, 0.1f, "", 0.0f, -1.0f, "" // Empty discounted string
        );

        Map<Product, Float> result = discount.applyDiscount(
                1000.0f, productsQuantity, productDiscounts, new ArrayList<>()
        );

        // All products should get discount since discounted is empty
        assertEquals(900.0f, result.get(product1), 0.01f);
        assertEquals(45.0f, result.get(product2), 0.01f);
        assertEquals(27.0f, result.get(product3), 0.01f);
    }

    @Test
    @DisplayName("Test getter and setter methods")
    void testGettersAndSetters() {
        Discount discount = new Discount();

        discount.setStoreId("newStore");
        assertEquals("newStore", discount.getStoreId());

        discount.setLevel(Discount.Level.CATEGORY);
        assertEquals(Discount.Level.CATEGORY, discount.getLevel());

        discount.setLogicComposition(Discount.LogicComposition.OR);
        assertEquals(Discount.LogicComposition.OR, discount.getLogicComposition());

        discount.setNumericalComposition(Discount.NumericalComposition.MULTIPLICATION);
        assertEquals(Discount.NumericalComposition.MULTIPLICATION, discount.getNumericalComposition());

        discount.setPercentDiscount(0.25f);
        assertEquals(0.25f, discount.getPercentDiscount());

        discount.setDiscounted("TestProduct");
        assertEquals("TestProduct", discount.getDiscounted());

        discount.setConditional(Discount.ConditionalType.MIN_PRICE);
        assertEquals(Discount.ConditionalType.MIN_PRICE, discount.getConditional());

        discount.setLimiter(100.0f);
        assertEquals(100.0f, discount.getLimiter());

        discount.setConditionalDiscounted("ConditionalProduct");
        assertEquals("ConditionalProduct", discount.getConditionalDiscounted());

        List<String> discounts = Arrays.asList("d1", "d2");
        discount.setDiscounts(discounts);
        assertEquals(discounts, discount.getDiscounts());

        discount.setAlreadyUsed(true);
        assertTrue(discount.isAlreadyUsed());
    }

    @Test
    @DisplayName("Test checkConditional method directly")
    void testCheckConditionalMethod() {
        // Test UNDEFINED condition
        Discount discount1 = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 0.0f, -1.0f, ""
        );
        assertTrue(discount1.checkConditinal(1000.0f, productsQuantity));

        // Test MIN_PRICE condition
        Discount discount2 = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 1.0f, 500.0f, ""
        );
        assertTrue(discount2.checkConditinal(1000.0f, productsQuantity));
        assertFalse(discount2.checkConditinal(400.0f, productsQuantity));

        // Test MIN_QUANTITY condition
        Discount discount3 = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 2.0f, 1.0f, "Mouse"
        );
        assertTrue(discount3.checkConditinal(1000.0f, productsQuantity)); // Mouse quantity is 3

        // Test MAX_QUANTITY condition
        Discount discount4 = new Discount(
                storeId, 1.0f, 0.0f, 0.0f, null, 0.1f, "Laptop", 3.0f, 5.0f, "Mouse"
        );
        assertTrue(discount4.checkConditinal(1000.0f, productsQuantity)); // Mouse quantity is 3 <= 5
    }
}