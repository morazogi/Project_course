package DomainLayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

public class OrderTest {

    private Order order;
    private String testInfo;
    private String testStoreId;
    private String testUserId;
    private Date testDate;

    @BeforeEach
    void setUp() {
        testInfo = "Test order information";
        testStoreId = "store-123";
        testUserId = "user-456";
        testDate = new Date();
        order = new Order(testInfo, testStoreId, testUserId, testDate);
    }

    @Test
    @DisplayName("Constructor should create Order with correct values")
    void testConstructor() {
        Order newOrder = new Order("Order info", "store-1", "user-1", new Date());

        assertEquals("Order info", newOrder.getInfo());
        assertEquals("store-1", newOrder.getStoreId());
        assertEquals("user-1", newOrder.getUserId());
        assertNotNull(newOrder.getDate());
    }

    @Test
    @DisplayName("Default constructor should create Order with null info")
    void testDefaultConstructor() {
        Order emptyOrder = new Order();

        assertNull(emptyOrder.getInfo());
        assertNull(emptyOrder.getStoreId());
        assertNull(emptyOrder.getUserId());
        assertNull(emptyOrder.getDate());
        assertNull(emptyOrder.getId());
    }

    @Test
    @DisplayName("Getters should return correct values")
    void testGetters() {
        assertEquals(testInfo, order.getInfo());
        assertEquals(testStoreId, order.getStoreId());
        assertEquals(testUserId, order.getUserId());
        assertEquals(testDate, order.getDate());
        assertNull(order.getId()); // ID is null until persisted
    }

    @Test
    @DisplayName("Setters should update values correctly")
    void testSetters() {
        String newStoreId = "new-store-789";
        String newUserId = "new-user-101";
        String newId = "order-id-123";
        Date newDate = new Date(System.currentTimeMillis() + 10000);

        order.setStoreId(newStoreId);
        order.setUserId(newUserId);
        order.setId(newId);
        order.setDate(newDate);

        assertEquals(newStoreId, order.getStoreId());
        assertEquals(newUserId, order.getUserId());
        assertEquals(newId, order.getId());
        assertEquals(newDate, order.getDate());
    }

    @Test
    @DisplayName("Info field should be immutable after construction")
    void testInfoImmutability() {
        // Since info is final, it cannot be changed after construction
        assertEquals(testInfo, order.getInfo());

        // Create another order to verify info is set correctly
        String differentInfo = "Different order info";
        Order anotherOrder = new Order(differentInfo, testStoreId, testUserId, testDate);
        assertEquals(differentInfo, anotherOrder.getInfo());
    }

    @Test
    @DisplayName("Order should handle null date")
    void testNullDate() {
        Order orderWithNullDate = new Order("Info", "store", "user", null);

        assertNull(orderWithNullDate.getDate());
        assertEquals("Info", orderWithNullDate.getInfo());
        assertEquals("store", orderWithNullDate.getStoreId());
        assertEquals("user", orderWithNullDate.getUserId());
    }

    @Test
    @DisplayName("Order should handle empty string values")
    void testEmptyStringValues() {
        Order orderWithEmptyStrings = new Order("", "", "", new Date());

        assertEquals("", orderWithEmptyStrings.getInfo());
        assertEquals("", orderWithEmptyStrings.getStoreId());
        assertEquals("", orderWithEmptyStrings.getUserId());
    }

    @Test
    @DisplayName("Date should be mutable through setter")
    void testDateMutability() {
        Date originalDate = order.getDate();
        Date newDate = new Date(System.currentTimeMillis() + 5000);

        order.setDate(newDate);

        assertNotEquals(originalDate, order.getDate());
        assertEquals(newDate, order.getDate());
    }

    @Test
    @DisplayName("Multiple orders should be independent")
    void testOrderIndependence() {
        Order order1 = new Order("Order 1", "store-1", "user-1", new Date());
        Order order2 = new Order("Order 2", "store-2", "user-2", new Date());

        order1.setStoreId("updated-store-1");

        assertEquals("updated-store-1", order1.getStoreId());
        assertEquals("store-2", order2.getStoreId()); // Should remain unchanged
    }

    @Test
    @DisplayName("toString should return empty string")
    void testToString() {
        assertEquals("", order.toString());
    }

    @Test
    @DisplayName("Order with same values should have same content")
    void testOrderEquality() {
        Date sameDate = new Date(testDate.getTime());
        Order order1 = new Order(testInfo, testStoreId, testUserId, testDate);
        Order order2 = new Order(testInfo, testStoreId, testUserId, sameDate);

        // Note: Since Order doesn't override equals(), this tests object reference equality
        // If you want content equality, you'd need to override equals() and hashCode()
        assertNotSame(order1, order2);

        // But content should be the same
        assertEquals(order1.getInfo(), order2.getInfo());
        assertEquals(order1.getStoreId(), order2.getStoreId());
        assertEquals(order1.getUserId(), order2.getUserId());
        assertEquals(order1.getDate(), order2.getDate());
    }

    @Test
    @DisplayName("Constructor should allow null and empty values for info, storeId, userId")
    void testConstructorWithNullAndEmptyValues() {
        // Should not throw
        assertDoesNotThrow(() -> new Order(null, null, null, null));
        assertDoesNotThrow(() -> new Order("", "", "", null));
    }

    @Test
    @DisplayName("Order ID should be null until persisted")
    void testIdIsNullUntilPersisted() {
        Order o = new Order("info", "store", "user", new Date());
        assertNull(o.getId());
    }

    @Test
    @DisplayName("Order equals/hashCode contract (if implemented in future)")
    void testEqualsAndHashCodeContract() {
        // This test is a placeholder for when equals/hashCode are implemented.
        // For now, just check reference equality.
        Order o1 = new Order("info", "store", "user", new Date());
        Order o2 = new Order("info", "store", "user", new Date(o1.getDate().getTime()));
        assertNotEquals(o1, o2); // Should be false unless equals is overridden
    }
}