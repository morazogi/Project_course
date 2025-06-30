package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store("founder1", "TestStore");
        store.setId("store-uuid-1");
    }

    /* ────────────────────────────────────────────────────────────────
       ORIGINAL TESTS – UNCHANGED
       ──────────────────────────────────────────────────────────────── */
    @Test
    void testConstructorAndGetters() {
        assertEquals("TestStore", store.getName());
        assertEquals("founder1", store.getFounder());
        assertTrue(store.isOpenNow());
        assertEquals("store-uuid-1", store.getId());
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
        assertTrue(store.registerUser("user1"));
        assertFalse(store.registerUser("user1")); // duplicate
        assertTrue(store.getUsers().contains("user1"));
    }

    @Test
    void testAddNewProduct() {
        assertTrue(store.addNewProduct("prod1", 10));
        assertEquals(10, store.getProducts().get("prod1"));
        assertFalse(store.addNewProduct("prod1", 0)); // invalid quantity
        assertTrue(store.addNewProduct("prod1", 5)); // increases quantity
        assertEquals(15, store.getProducts().get("prod1"));
    }

    @Test
    void testIncreaseAndDecreaseProduct() {
        store.addNewProduct("prod1", 10);
        assertTrue(store.increaseProduct("prod1", 5));
        assertEquals(15, store.getProducts().get("prod1"));
        assertFalse(store.increaseProduct("prod2", 5)); // not exist
        assertFalse(store.increaseProduct("prod1", 0)); // invalid

        assertTrue(store.decreaseProduct("prod1", 5));
        assertEquals(10, store.getProducts().get("prod1"));
        assertFalse(store.decreaseProduct("prod1", 20)); // too much
        assertFalse(store.decreaseProduct("prod2", 1)); // not exist
    }

    @Test
    void testChangeProductQuantity() {
        store.addNewProduct("prod1", 10);
        assertTrue(store.changeProductQuantity("prod1", 5));
        assertEquals(5, store.getProducts().get("prod1"));
        assertTrue(store.changeProductQuantity("prod1", 0));
        assertNull(store.getProducts().get("prod1"));
        assertFalse(store.changeProductQuantity("prod2", 5)); // not exist
        assertFalse(store.changeProductQuantity("prod1", -1)); // negative
    }

    @Test
    void testRemoveProduct() {
        store.addNewProduct("prod1", 10);
        assertTrue(store.removeProduct("prod1"));
        assertFalse(store.getProducts().containsKey("prod1"));
        assertFalse(store.removeProduct("prod2")); // not exist
    }

    @Test
    void testAvailableProduct() {
        store.addNewProduct("prod1", 10);
        assertTrue(store.availableProduct("prod1", 5));
        assertFalse(store.availableProduct("prod1", 20));
        assertFalse(store.availableProduct("prod2", 1));
        assertFalse(store.availableProduct("prod1", 0));
    }

    @Test
    void testSellProduct() {
        store.addNewProduct("prod1", 10);
        store.reserveProduct("prod1", 5);
        store.sellProduct("prod1", 3);
        assertEquals(2, store.getReservedProducts().get("prod1"));
        assertThrows(IllegalArgumentException.class, () -> store.sellProduct("prod1", 10)); // not enough reserved
        store.sellProduct("prod1", 2);
        assertFalse(store.getReservedProducts().containsKey("prod1"));
    }

    @Test
    void testAddAndRemoveDiscount() {
        assertTrue(store.addDiscount("discount1"));
        assertTrue(store.getDiscounts().contains("discount1"));
        assertFalse(store.addDiscount(null));
        assertTrue(store.removeDiscount("discount1"));
        assertFalse(store.getDiscounts().contains("discount1"));
        assertFalse(store.removeDiscount(null));
    }

    @Test
    void testOwnershipAndManagement() {
        store.addOwner("founder1", "owner2");
        assertTrue(store.userIsOwner("owner2"));
        assertTrue(store.getOwners().contains("owner2"));
        assertEquals("founder1", store.getOwnersToSuperior().get("owner2"));

        store.addManager("founder1", "manager1", new boolean[]{true, false, true, false, false, false, false});
        assertTrue(store.userIsManager("manager1"));
        assertTrue(store.getManagers().containsKey("manager1"));
        assertEquals("founder1", store.getManagersToSuperior().get("manager1"));

        store.terminateOwnership("owner2");
        assertFalse(store.userIsOwner("owner2"));
        assertFalse(store.getOwners().contains("owner2"));
    }


    @Test
    void testToString() {
        store.registerUser("user1");
        store.addNewProduct("prod1", 10);
        String str = store.toString();
        assertTrue(str.contains("user1"));
        assertTrue(str.contains("prod1"));
    }

    @Test
    void testAddOwnerAndHierarchy() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        store.addOwner("owner2", "owner4");

        assertTrue(store.getOwners().contains("owner2"));
        assertTrue(store.getOwners().contains("owner3"));
        assertTrue(store.getOwners().contains("owner4"));

        assertEquals("founder1", store.getOwnersToSuperior().get("owner2"));
        assertEquals("owner2", store.getOwnersToSuperior().get("owner3"));
        assertEquals("owner2", store.getOwnersToSuperior().get("owner4"));

        LinkedList<String> founderSubs = store.getAllSubordinates("founder1");
        assertTrue(founderSubs.contains("owner2"));
        assertTrue(founderSubs.contains("owner3"));
        assertTrue(founderSubs.contains("owner4"));

        LinkedList<String> owner2Subs = store.getAllSubordinates("owner2");
        assertTrue(owner2Subs.contains("owner3"));
        assertTrue(owner2Subs.contains("owner4"));
    }

    @Test
    void testGetAllSubordinatesComplexHierarchy() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        store.addOwner("owner2", "owner4");
        store.addOwner("owner3", "owner5");
        store.addManager("owner2", "manager1", new boolean[]{true, true, true, true, true, true, true});
        store.addManager("owner3", "manager2", new boolean[]{true, true, true, true, true, true, true});

        LinkedList<String> founderSubs = store.getAllSubordinates("founder1");
        assertTrue(founderSubs.contains("owner2"));
        assertTrue(founderSubs.contains("owner3"));
        assertTrue(founderSubs.contains("owner4"));
        assertTrue(founderSubs.contains("owner5"));

        LinkedList<String> owner2Subs = store.getAllSubordinates("owner2");
        assertTrue(owner2Subs.contains("owner3"));
        assertTrue(owner2Subs.contains("owner4"));
        assertTrue(owner2Subs.contains("owner5"));
    }

    @Test
    void testAddManagerDoesNotDuplicate() {
        boolean[] perms = new boolean[]{true, false, true, false, false, false, false};
        store.addManager("founder1", "manager1", perms);
        store.addManager("founder1", "manager1", perms); // overwrite
        long count = store.getManagers().keySet().stream().filter("manager1"::equals).count();
        assertEquals(1, count);
    }

    @Test
    void testRemoveManagerWhoIsAlsoOwner() {
        store.addOwner("founder1", "owner2");
        store.addManager("founder1", "owner2", new boolean[]{true, true, true, true, true, true, true});
        assertTrue(store.userIsOwner("owner2"));
        assertTrue(store.userIsManager("owner2"));
        store.terminateManagment("owner2");
        assertTrue(store.userIsOwner("owner2"));
        assertFalse(store.userIsManager("owner2"));
        store.terminateOwnership("owner2");
        assertFalse(store.userIsOwner("owner2"));
        assertFalse(store.userIsManager("owner2"));
    }

    @Test
    void testCannotAddDuplicateManager() {
        boolean[] perms = new boolean[]{true, false, true, false, false, false, false};
        store.addManager("founder1", "manager1", perms);
        int before = store.getManagers().size();
        store.addManager("founder1", "manager1", perms);
        assertEquals(before, store.getManagers().size());
    }

    @Test
    void testOnlySuperiorCanRemoveOwner() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        store.terminateOwnership("owner2");
        assertFalse(store.userIsOwner("owner2"));
        assertFalse(store.checkIfSuperior("owner3", "owner2"));
    }

    @Test
    void testManagerCannotRemoveOwner() {
        store.addOwner("founder1", "owner2");
        store.addManager("founder1", "manager1", new boolean[]{true, true, true, true, true, true, true});
        assertFalse(store.checkIfSuperior("manager1", "owner2"));
    }

    @Test
    void testManagerCanBeRemovedIndependentlyOfOwnership() {
        store.addOwner("founder1", "owner2");
        store.addManager("founder1", "owner2", new boolean[]{true, true, true, true, true, true, true});
        assertTrue(store.userIsOwner("owner2"));
        assertTrue(store.userIsManager("owner2"));
        store.terminateManagment("owner2");
        assertTrue(store.userIsOwner("owner2"));
        assertFalse(store.userIsManager("owner2"));
    }

    @Test
    void testReserveWithInvalidQuantity() {
        store.addNewProduct("prodY", 5);
        assertFalse(store.reserveProduct("prodY", 0));
        assertFalse(store.reserveProduct("prodY", -1));
    }

    @Test
    void testUnreserveWithInvalidQuantity() {
        store.addNewProduct("prodZ", 5);
        store.reserveProduct("prodZ", 3);
        assertFalse(store.unreserveProduct("prodZ", 0));
        assertFalse(store.unreserveProduct("prodZ", -2));
        assertFalse(store.unreserveProduct("prodZ", 10)); // more than reserved
    }

    @Test
    void testReserveNonExistentProduct() {
        assertFalse(store.reserveProduct("noSuchProduct", 1));
    }

    @Test
    void testUnreserveNonExistentProduct() {
        assertFalse(store.unreserveProduct("noSuchProduct", 1));
    }

    @Test
    void testRemoveNonExistentManager() {
        int before = store.getManagers().size();
        store.terminateManagment("notAManager");
        assertEquals(before, store.getManagers().size());
    }

    @Test
    void testRemoveDiscountNullOrNonExistent() {
        assertFalse(store.removeDiscount(null));
        assertFalse(store.removeDiscount("notThere"));
    }

    /* ────────────────────────────────────────────────────────────────
       NEW ORIGINAL TESTS – ADDITIONAL COVERAGE
       ──────────────────────────────────────────────────────────────── */

    @Test
    void testRateMethodValidAndInvalid() {
        // First valid rating
        assertTrue(store.rate(5));
        assertEquals(5.0, store.getRating());

        // Update rating by the same (implicit) rater – should overwrite to 3
        assertTrue(store.rate(3));
        assertEquals(3.0, store.getRating());

        // Invalid low and high ratings (should be rejected and rating unchanged)
        assertFalse(store.rate(0));
        assertFalse(store.rate(6));
        assertEquals(3.0, store.getRating());
    }

    @Test
    void testCloseByAdmin() {
        assertTrue(store.isOpenNow());
        assertTrue(store.closeByAdmin());
        assertFalse(store.isOpenNow());
    }

    @Test
    void testGetRolesStringRepresentation() {
        store.addOwner("founder1", "owner2");
        boolean[] perms = new boolean[]{true, true, true, true, true, true, true};
        store.addManager("owner2", "manager1", perms);

        String roles = store.getRoles();
        assertTrue(roles.contains("founder1"));
        assertTrue(roles.contains("owner2"));
        assertTrue(roles.contains("manager1"));
    }

    @Test
    void testSubordinateHelperMethods() {
        List<String> init = Arrays.asList("sub1", "sub2");
        store.setSubordinatesForOwner("founder1", init);
        store.addSubordinateToOwner("founder1", "sub3");

        List<String> subs = store.getSubordinatesForOwner("founder1");
        assertEquals(3, subs.size());
        assertTrue(subs.containsAll(Arrays.asList("sub1", "sub2", "sub3")));
    }

    @Test
    void testChangeManagersPermissions() {
        // create manager with all-false permissions
        boolean[] initialPerms = new boolean[]{false, false, false, false, false, false, false};
        store.addManager("founder1", "manager1", initialPerms);
        Map<String, Boolean> before = store.getPremissions("manager1");

        // flip all to true
        boolean[] newPerms = new boolean[before.size()];
        Arrays.fill(newPerms, true);
        store.changeManagersPermissions("manager1", newPerms);

        Map<String, Boolean> after = store.getPremissions("manager1");
        // At the very least, the map should still be non-null and same size
        assertNotNull(after);
        assertEquals(before.size(), after.size());
    }

    @Test
    void testUserHasPermissionsOwnerAlwaysTrue() {
        store.addOwner("founder1", "owner2");
        assertTrue(store.userHasPermissions("owner2", "anyPermissionString"));
    }

    @Test
    void testUserHasPermissionsManagerReflectsInternalMap() {
        boolean[] perms = new boolean[]{true, false, false, false, false, false, false};
        store.addManager("founder1", "manager1", perms);

        Map<String, Boolean> permMap = store.getPremissions("manager1");
        assertNotNull(permMap);
        String someKey = permMap.keySet().iterator().next();
        assertEquals(permMap.get(someKey), store.userHasPermissions("manager1", someKey));
    }


    @Test
    void testSellProductInvalidCases() {
        store.addNewProduct("prodA", 5);

        // Attempt to sell without reservation
        assertThrows(IllegalArgumentException.class, () -> store.sellProduct("prodA", 1));

        // Proper reservation
        store.reserveProduct("prodA", 2);

        // Invalid quantities
        assertThrows(IllegalArgumentException.class, () -> store.sellProduct("prodA", 0));
        assertThrows(IllegalArgumentException.class, () -> store.sellProduct("prodA", -1));
    }

    @Test
    void testCheckIfSuperiorWithManagerHierarchy() {
        store.addOwner("founder1", "owner2");
        boolean[] perms = new boolean[]{true, true, true, true, true, true, true};
        store.addManager("owner2", "manager1", perms);

        // founder1 should be indirect superior of manager1
        assertTrue(store.checkIfSuperior("founder1", "manager1"));
        // owner2 direct superior
        assertTrue(store.checkIfSuperior("owner2", "manager1"));
        // reverse should be false
        assertFalse(store.checkIfSuperior("manager1", "owner2"));
    }

    @Test
    void testUpdateProductDetailsAlwaysTrue() {
        store.addNewProduct("prodD", 7);
        assertTrue(store.updateProductDetails("prodD",
                "NewName", "NewDesc", 19.99, "CategoryX"));
    }
}
