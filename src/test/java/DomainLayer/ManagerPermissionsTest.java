/*Test Coverage Includes:
1. Constructor Testing

Default Constructor: Tests that all permissions are initialized to false
Parameterized Constructor: Tests with full permissions, partial permissions, and no permissions
Validates that the ID is properly set from constructor parameters

2. Permission Array Handling

Valid Arrays: Tests setPermissionsFromArray with proper 7-element arrays
Invalid Arrays: Tests handling of null arrays and arrays that are too short
Array Order Mapping: Verifies the correct mapping of array indices to specific permissions

3. Individual Permission Management

Get/Set Individual Permissions: Tests getting and setting specific permissions
Non-existent Permissions: Tests behavior when requesting unknown permissions
Custom Permissions: Tests adding custom permissions beyond the default 7

4. Permission Map Operations

Map Getter/Setter: Tests direct manipulation of the permissions map
Map Initialization: Verifies proper initialization of all 7 default permissions
Map Size Validation: Ensures the map contains exactly the expected permissions

5. ID Management

ID Getter/Setter: Tests the composite primary key functionality
Manager/Store ID Access: Tests convenience methods for accessing ID components
Null ID Handling: Tests behavior when ID is null

6. Object Equality and Hashing

Equals Method: Tests equality based on ID comparison
Null ID Equals: Tests equality when IDs are null
HashCode Method: Tests consistent hash code generation
Different Object Types: Tests equality with non-ManagerPermissions objects

7. Permission Constants

Constant Definitions: Verifies all permission constants are properly defined
Constant Values: Tests that constants have expected string values

8. Edge Cases and Error Handling

Null Parameter Handling: Tests behavior with null inputs
Array Length Validation: Tests proper handling of invalid array lengths
Initialization Consistency: Ensures consistent initialization across constructors

Key Features:

Comprehensive Coverage: Tests all public methods and edge cases
Realistic Scenarios: Uses actual manager and store IDs that mirror real usage
Error Condition Testing: Validates proper handling of invalid inputs
Permission Mapping Validation: Ensures array indices map to correct permissions
Constant Verification: Validates all permission constants are properly defined*/
package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class ManagerPermissionsTest {

    private ManagerPermissions managerPermissions;
    private String managerId;
    private String storeId;
    private boolean[] fullPermissions;
    private boolean[] partialPermissions;
    private boolean[] noPermissions;

    @BeforeEach
    void setUp() {
        managerId = "manager123";
        storeId = "store456";

        // Array with all permissions true
        fullPermissions = new boolean[]{true, true, true, true, true, true, true};

        // Array with some permissions true
        partialPermissions = new boolean[]{true, false, true, false, true, false, true};

        // Array with all permissions false
        noPermissions = new boolean[]{false, false, false, false, false, false, false};
    }

    @Test
    @DisplayName("Test default constructor initializes with default permissions")
    void testDefaultConstructor() {
        managerPermissions = new ManagerPermissions();

        // All permissions should be false by default
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));

        // Permissions map should contain all 7 permissions
        assertEquals(7, managerPermissions.getPermissions().size());
    }

    @Test
    @DisplayName("Test parameterized constructor with full permissions")
    void testParameterizedConstructorFullPermissions() {
        managerPermissions = new ManagerPermissions(fullPermissions, managerId, storeId);

        // All permissions should be true
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));

        // Check ID is set correctly
        assertEquals(managerId, managerPermissions.getManagerId());
        assertEquals(storeId, managerPermissions.getStoreId());
    }

    @Test
    @DisplayName("Test parameterized constructor with partial permissions")
    void testParameterizedConstructorPartialPermissions() {
        managerPermissions = new ManagerPermissions(partialPermissions, managerId, storeId);

        // Check specific permissions match the array
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));  // index 0: true
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));     // index 1: false
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_VIEW_STORE));        // index 2: true
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));    // index 3: false
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));       // index 4: true
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));   // index 5: false
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));    // index 6: true
    }

    @Test
    @DisplayName("Test parameterized constructor with no permissions")
    void testParameterizedConstructorNoPermissions() {
        managerPermissions = new ManagerPermissions(noPermissions, managerId, storeId);

        // All permissions should be false
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));
    }

    @Test
    @DisplayName("Test setPermissionsFromArray with valid array")
    void testSetPermissionsFromArrayValid() {
        managerPermissions = new ManagerPermissions();
        managerPermissions.setPermissionsFromAarray(partialPermissions);

        // Verify permissions match the array
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));
    }

    @Test
    @DisplayName("Test setPermissionsFromArray with null array")
    void testSetPermissionsFromArrayNull() {
        managerPermissions = new ManagerPermissions();

        // Store original permissions
        Map<String, Boolean> originalPermissions = new HashMap<>(managerPermissions.getPermissions());

        // Try to set with null array
        managerPermissions.setPermissionsFromAarray(null);

        // Permissions should remain unchanged
        assertEquals(originalPermissions, managerPermissions.getPermissions());
    }

    @Test
    @DisplayName("Test setPermissionsFromArray with short array")
    void testSetPermissionsFromArrayTooShort() {
        managerPermissions = new ManagerPermissions();

        // Store original permissions
        Map<String, Boolean> originalPermissions = new HashMap<>(managerPermissions.getPermissions());

        // Try to set with array that's too short
        boolean[] shortArray = new boolean[]{true, false, true}; // Only 3 elements
        managerPermissions.setPermissionsFromAarray(shortArray);

        // Permissions should remain unchanged
        assertEquals(originalPermissions, managerPermissions.getPermissions());
    }

    @Test
    @DisplayName("Test individual permission getter and setter")
    void testIndividualPermissionGetterSetter() {
        managerPermissions = new ManagerPermissions();

        // Initially should be false
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));

        // Set to true
        managerPermissions.setPermission(ManagerPermissions.PERM_MANAGE_INVENTORY, true);
        assertTrue(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));

        // Set back to false
        managerPermissions.setPermission(ManagerPermissions.PERM_MANAGE_INVENTORY, false);
        assertFalse(managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
    }

    @Test
    @DisplayName("Test getPermission with non-existent permission")
    void testGetPermissionNonExistent() {
        managerPermissions = new ManagerPermissions();

        // Should return false for non-existent permission
        assertFalse(managerPermissions.getPermission("NON_EXISTENT_PERMISSION"));
    }

    @Test
    @DisplayName("Test setPermission with custom permission")
    void testSetPermissionCustom() {
        managerPermissions = new ManagerPermissions();

        String customPermission = "CUSTOM_PERMISSION";
        managerPermissions.setPermission(customPermission, true);

        assertTrue(managerPermissions.getPermission(customPermission));
        assertEquals(8, managerPermissions.getPermissions().size()); // 7 default + 1 custom
    }

    @Test
    @DisplayName("Test permissions map getter and setter")
    void testPermissionsMapGetterSetter() {
        managerPermissions = new ManagerPermissions();

        Map<String, Boolean> customPermissions = new HashMap<>();
        customPermissions.put(ManagerPermissions.PERM_MANAGE_INVENTORY, true);
        customPermissions.put(ManagerPermissions.PERM_VIEW_STORE, true);
        customPermissions.put("CUSTOM_PERM", true);

        managerPermissions.setPermissions(customPermissions);

        Map<String, Boolean> retrievedPermissions = managerPermissions.getPermissions();
        assertEquals(customPermissions, retrievedPermissions);
        assertTrue(retrievedPermissions.get(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertTrue(retrievedPermissions.get(ManagerPermissions.PERM_VIEW_STORE));
        assertTrue(retrievedPermissions.get("CUSTOM_PERM"));
    }

    @Test
    @DisplayName("Test ID getter and setter")
    void testIdGetterSetter() {
        managerPermissions = new ManagerPermissions();

        // Initially ID should be null
        assertNull(managerPermissions.getId());
        assertNull(managerPermissions.getManagerId());
        assertNull(managerPermissions.getStoreId());

        // Create and set ID
        ManagerPermissionsPK id = new ManagerPermissionsPK(managerId, storeId);
        managerPermissions.setId(id);

        assertEquals(id, managerPermissions.getId());
        assertEquals(managerId, managerPermissions.getManagerId());
        assertEquals(storeId, managerPermissions.getStoreId());
    }

    @Test
    @DisplayName("Test getManagerId and getStoreId with null ID")
    void testGetManagerIdStoreIdWithNullId() {
        managerPermissions = new ManagerPermissions();
        managerPermissions.setId(null);

        assertNull(managerPermissions.getManagerId());
        assertNull(managerPermissions.getStoreId());
    }

    @Test
    @DisplayName("Test equals method")
    void testEquals() {
        ManagerPermissionsPK id1 = new ManagerPermissionsPK(managerId, storeId);
        ManagerPermissionsPK id2 = new ManagerPermissionsPK(managerId, storeId);
        ManagerPermissionsPK id3 = new ManagerPermissionsPK("differentManager", storeId);

        ManagerPermissions mp1 = new ManagerPermissions();
        mp1.setId(id1);

        ManagerPermissions mp2 = new ManagerPermissions();
        mp2.setId(id2);

        ManagerPermissions mp3 = new ManagerPermissions();
        mp3.setId(id3);

        // Same object
        assertEquals(mp1, mp1);

        // Different objects with same ID should be equal (assuming ManagerPermissionsPK implements equals correctly)
        assertEquals(mp1, mp2);

        // Different IDs should not be equal
        assertNotEquals(mp1, mp3);

        // Null comparison
        assertNotEquals(mp1, null);

        // Different class comparison
        assertNotEquals(mp1, "string");
    }

    @Test
    @DisplayName("Test equals with null IDs")
    void testEqualsWithNullIds() {
        ManagerPermissions mp1 = new ManagerPermissions();
        ManagerPermissions mp2 = new ManagerPermissions();

        // Both have null IDs - should be equal
        assertEquals(mp1, mp2);

        // One has ID, other has null - should not be equal
        mp1.setId(new ManagerPermissionsPK(managerId, storeId));
        assertNotEquals(mp1, mp2);
    }

    @Test
    @DisplayName("Test hashCode method")
    void testHashCode() {
        ManagerPermissionsPK id = new ManagerPermissionsPK(managerId, storeId);

        ManagerPermissions mp1 = new ManagerPermissions();
        mp1.setId(id);

        ManagerPermissions mp2 = new ManagerPermissions();
        mp2.setId(id);

        // Objects with same ID should have same hash code
        assertEquals(mp1.hashCode(), mp2.hashCode());

        // Test with null ID
        ManagerPermissions mp3 = new ManagerPermissions();
        int hashWithNullId = mp3.hashCode();
        assertNotNull(hashWithNullId); // Should not throw exception
    }

    @Test
    @DisplayName("Test all permission constants are defined")
    void testPermissionConstants() {
        assertNotNull(ManagerPermissions.PERM_MANAGE_INVENTORY);
        assertNotNull(ManagerPermissions.PERM_MANAGE_STAFF);
        assertNotNull(ManagerPermissions.PERM_VIEW_STORE);
        assertNotNull(ManagerPermissions.PERM_UPDATE_POLICY);
        assertNotNull(ManagerPermissions.PERM_ADD_PRODUCT);
        assertNotNull(ManagerPermissions.PERM_REMOVE_PRODUCT);
        assertNotNull(ManagerPermissions.PERM_UPDATE_PRODUCT);

        // Verify they have expected values
        assertEquals("PERM_MANAGE_INVENTORY", ManagerPermissions.PERM_MANAGE_INVENTORY);
        assertEquals("PERM_MANAGE_STAFF", ManagerPermissions.PERM_MANAGE_STAFF);
        assertEquals("PERM_VIEW_STORE", ManagerPermissions.PERM_VIEW_STORE);
        assertEquals("PERM_UPDATE_POLICY", ManagerPermissions.PERM_UPDATE_POLICY);
        assertEquals("PERM_ADD_PRODUCT", ManagerPermissions.PERM_ADD_PRODUCT);
        assertEquals("PERM_REMOVE_PRODUCT", ManagerPermissions.PERM_REMOVE_PRODUCT);
        assertEquals("PERM_UPDATE_PRODUCT", ManagerPermissions.PERM_UPDATE_PRODUCT);
    }

    @Test
    @DisplayName("Test permission array order mapping")
    void testPermissionArrayOrderMapping() {
        managerPermissions = new ManagerPermissions();

        // Create array where each position has different value to test mapping
        boolean[] testArray = new boolean[]{true, false, true, false, true, false, true};
        managerPermissions.setPermissionsFromAarray(testArray);

        // Verify the mapping order is correct
        assertEquals(testArray[0], managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertEquals(testArray[1], managerPermissions.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertEquals(testArray[2], managerPermissions.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertEquals(testArray[3], managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
        assertEquals(testArray[4], managerPermissions.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));
        assertEquals(testArray[5], managerPermissions.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertEquals(testArray[6], managerPermissions.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));
    }

    @Test
    @DisplayName("Test permissions map is properly initialized")
    void testPermissionsMapInitialization() {
        managerPermissions = new ManagerPermissions();

        Map<String, Boolean> permissions = managerPermissions.getPermissions();

        // Should contain exactly 7 permissions
        assertEquals(7, permissions.size());

        // Should contain all expected permissions
        assertTrue(permissions.containsKey(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertTrue(permissions.containsKey(ManagerPermissions.PERM_MANAGE_STAFF));
        assertTrue(permissions.containsKey(ManagerPermissions.PERM_VIEW_STORE));
        assertTrue(permissions.containsKey(ManagerPermissions.PERM_UPDATE_POLICY));
        assertTrue(permissions.containsKey(ManagerPermissions.PERM_ADD_PRODUCT));
        assertTrue(permissions.containsKey(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertTrue(permissions.containsKey(ManagerPermissions.PERM_UPDATE_PRODUCT));

        // All should be false initially
        permissions.values().forEach(value -> assertFalse(value));
    }
}