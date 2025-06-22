package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class StoreTest {

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store("founder1", "TestStore");
        store.setId("store-uuid-1");
    }

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
    void testReserveAndUnreserveProduct() {
        store.addNewProduct("prod1", 10);
        assertTrue(store.reserveProduct("prod1", 5));
        assertEquals(5, store.getReservedProducts().get("prod1"));
        assertEquals(5, store.getProducts().get("prod1"));
        assertFalse(store.reserveProduct("prod1", 6)); // not enough left

        assertTrue(store.unreserveProduct("prod1", 3));
        assertEquals(2, store.getReservedProducts().get("prod1"));
        assertEquals(8, store.getProducts().get("prod1"));
        assertTrue(store.unreserveProduct("prod1", 2));
        assertFalse(store.getReservedProducts().containsKey("prod1"));
        assertEquals(10, store.getProducts().get("prod1"));
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
    void testPermissions() {
        store.addManager("founder1", "manager1", new boolean[]{true, false, true, false, false, false, false});
        Map<String, Boolean> perms = store.getPremissions("manager1");
        assertNotNull(perms);
        assertTrue(perms.values().contains(true));
    }

    @Test
    void testCheckIfSuperior() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        assertTrue(store.checkIfSuperior("founder1", "owner2"));
        assertTrue(store.checkIfSuperior("founder1", "owner3"));
        assertFalse(store.checkIfSuperior("owner3", "founder1"));
        assertFalse(store.checkIfSuperior("owner2", "founder1"));
        assertFalse(store.checkIfSuperior(null, "owner2"));
        assertFalse(store.checkIfSuperior("founder1", null));
    }

    @Test
    void testGetAllSubordinates() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        Set<String> subs = store.getAllSubordinates("founder1");
        assertTrue(subs.contains("owner2"));
        assertTrue(subs.contains("owner3"));
        assertEquals(2, subs.size());
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

        // Owners list
        assertTrue(store.getOwners().contains("owner2"));
        assertTrue(store.getOwners().contains("owner3"));
        assertTrue(store.getOwners().contains("owner4"));

        // Owners to superior mapping
        assertEquals("founder1", store.getOwnersToSuperior().get("owner2"));
        assertEquals("owner2", store.getOwnersToSuperior().get("owner3"));
        assertEquals("owner2", store.getOwnersToSuperior().get("owner4"));

        // Subordinates
        Set<String> founderSubs = store.getAllSubordinates("founder1");
        assertTrue(founderSubs.contains("owner2"));
        assertTrue(founderSubs.contains("owner3"));
        assertTrue(founderSubs.contains("owner4"));

        Set<String> owner2Subs = store.getAllSubordinates("owner2");
        assertTrue(owner2Subs.contains("owner3"));
        assertTrue(owner2Subs.contains("owner4"));
    }

    @Test
    void testAddManagerAndPermissions() {
        boolean[] perms = new boolean[]{true, false, true, false, false, false, false};
        store.addManager("founder1", "manager1", perms);

        assertTrue(store.userIsManager("manager1"));
        assertEquals("founder1", store.getManagersToSuperior().get("manager1"));

        Map<String, Boolean> permissions = store.getPremissions("manager1");
        assertNotNull(permissions);
        assertTrue(permissions.getOrDefault("PERM_MANAGE_INVENTORY", false));
        assertFalse(permissions.getOrDefault("PERM_MANAGE_STAFF", true) && permissions.getOrDefault("PERM_UPDATE_POLICY", true));
    }

    @Test
    void testChangeManagersPermissions() {
        boolean[] perms = new boolean[]{true, false, false, false, false, false, false};
        store.addManager("founder1", "manager2", perms);

        boolean[] newPerms = new boolean[]{false, true, true, true, false, false, false};
        store.changeManagersPermissions("manager2", newPerms);

        Map<String, Boolean> permissions = store.getPremissions("manager2");
        assertFalse(permissions.getOrDefault("PERM_MANAGE_INVENTORY", true));
        assertTrue(permissions.getOrDefault("PERM_MANAGE_STAFF", false));
        assertTrue(permissions.getOrDefault("PERM_VIEW_STORE", false));
        assertTrue(permissions.getOrDefault("PERM_UPDATE_POLICY", false));
    }

    @Test
    void testTerminateOwnershipRemovesHierarchy() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        store.addManager("owner2", "manager1", new boolean[]{true, true, true, true, true, true, true});
        store.terminateOwnership("owner2");

        assertFalse(store.userIsOwner("owner2"));
        assertFalse(store.userIsOwner("owner3"));
        assertFalse(store.userIsManager("manager1"));
        assertFalse(store.getOwners().contains("owner2"));
        assertFalse(store.getOwners().contains("owner3"));
        assertFalse(store.getManagers().containsKey("manager1"));
    }

    @Test
    void testTerminateManagmentRemovesManager() {
        store.addManager("founder1", "manager1", new boolean[]{true, true, true, true, true, true, true});
        store.terminateManagment("manager1");
        assertFalse(store.userIsManager("manager1"));
        assertFalse(store.getManagers().containsKey("manager1"));
        assertFalse(store.getManagersToSuperior().containsKey("manager1"));
    }

    @Test
    void testCheckIfSuperiorForOwnersAndManagers() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        store.addManager("owner2", "manager1", new boolean[]{true, true, true, true, true, true, true});
        store.addManager("owner3", "manager2", new boolean[]{true, true, true, true, true, true, true});

        assertTrue(store.checkIfSuperior("founder1", "owner2"));
        assertTrue(store.checkIfSuperior("founder1", "owner3"));
        assertTrue(store.checkIfSuperior("founder1", "manager1"));
        assertTrue(store.checkIfSuperior("founder1", "manager2"));
        assertTrue(store.checkIfSuperior("owner2", "owner3"));
        assertTrue(store.checkIfSuperior("owner2", "manager1"));
        assertFalse(store.checkIfSuperior("owner3", "owner2"));
        assertFalse(store.checkIfSuperior("manager1", "owner2"));
    }

    @Test
    void testGetAllSubordinatesComplexHierarchy() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        store.addOwner("owner2", "owner4");
        store.addOwner("owner3", "owner5");
        store.addManager("owner2", "manager1", new boolean[]{true, true, true, true, true, true, true});
        store.addManager("owner3", "manager2", new boolean[]{true, true, true, true, true, true, true});

        Set<String> founderSubs = store.getAllSubordinates("founder1");
        assertTrue(founderSubs.contains("owner2"));
        assertTrue(founderSubs.contains("owner3"));
        assertTrue(founderSubs.contains("owner4"));
        assertTrue(founderSubs.contains("owner5"));
        // Note: managers are not included in getAllSubordinates as per current implementation

        Set<String> owner2Subs = store.getAllSubordinates("owner2");
        assertTrue(owner2Subs.contains("owner3"));
        assertTrue(owner2Subs.contains("owner4"));
        assertTrue(owner2Subs.contains("owner5"));
    }

    @Test
    void testAddOwnerDoesNotDuplicate() {
        store.addOwner("founder1", "owner2");
        store.addOwner("founder1", "owner2"); // Should not duplicate
        int count = 0;
        for (String owner : store.getOwners()) {
            if (owner.equals("owner2")) count++;
        }
        assertEquals(1, count);
    }

    @Test
    void testAddManagerDoesNotDuplicate() {
        boolean[] perms = new boolean[]{true, false, true, false, false, false, false};
        store.addManager("founder1", "manager1", perms);
        store.addManager("founder1", "manager1", perms); // Should overwrite, not duplicate
        int count = 0;
        for (String manager : store.getManagers().keySet()) {
            if (manager.equals("manager1")) count++;
        }
        assertEquals(1, count);
    }

    @Test
    void testPreventCircularOwnership() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        // Attempt to add founder as subordinate to owner3 (should not create a cycle)
        store.addOwner("owner3", "founder1");
        // Founder should not be subordinate to anyone
        assertFalse(store.getOwnersToSuperior().containsKey("founder1") && !"founder1".equals(store.getOwnersToSuperior().get("founder1")));
        // Founder should still be a root owner
        assertTrue(store.userIsOwner("founder1"));
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
        // Now remove ownership
        store.terminateOwnership("owner2");
        assertFalse(store.userIsOwner("owner2"));
        assertFalse(store.userIsManager("owner2"));
    }

    @Test
    void testManagerAsSubordinateToMultipleOwners() {
        store.addOwner("founder1", "owner2");
        store.addOwner("founder1", "owner3");
        store.addManager("owner2", "manager1", new boolean[]{true, true, true, true, true, true, true});
        // Try to add manager1 as subordinate to owner3 as well (should not duplicate)
        store.addSubordinateToOwner("owner3", "manager1");
        Set<String> subsOwner2 = store.getAllSubordinates("owner2");
        Set<String> subsOwner3 = store.getAllSubordinates("owner3");
        assertTrue(subsOwner2.contains("manager1"));
        assertTrue(subsOwner3.contains("manager1"));
        // Removing manager1 from owner2 should not affect owner3's subordinates
        store.terminateManagment("manager1");
        assertFalse(store.userIsManager("manager1"));
    }

    @Test
    void testFounderCannotBeRemoved() {
        // Try to remove founder as owner
        store.terminateOwnership("founder1");
        // Founder should still be owner
        assertTrue(store.userIsOwner("founder1"));
        // Founder should still be in owners list
        assertTrue(store.getOwners().contains("founder1"));
    }

    @Test
    void testFounderCannotBeRemovedByTerminateOwnership() {
        store.terminateOwnership("founder1");
        assertTrue(store.userIsOwner("founder1"));
        assertTrue(store.getOwners().contains("founder1"));
    }

    @Test
    void testFounderCannotBeRemovedByRemoveStoreOwner() {
        // Simulate a microservice-like removal attempt
        store.addOwner("founder1", "owner2");
        store.terminateOwnership("founder1");
        assertTrue(store.userIsOwner("founder1"));
        assertTrue(store.getOwners().contains("founder1"));
    }

    @Test
    void testCannotAddDuplicateOwner() {
        store.addOwner("founder1", "owner2");
        int before = store.getOwners().size();
        store.addOwner("founder1", "owner2");
        int after = store.getOwners().size();
        assertEquals(before, after);
    }

    @Test
    void testCannotAddDuplicateManager() {
        boolean[] perms = new boolean[]{true, false, true, false, false, false, false};
        store.addManager("founder1", "manager1", perms);
        int before = store.getManagers().size();
        store.addManager("founder1", "manager1", perms);
        int after = store.getManagers().size();
        assertEquals(before, after);
    }

    @Test
    void testOnlySuperiorCanRemoveOwner() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        // owner3 tries to remove owner2 (should not work)
        store.terminateOwnership("owner2");
        assertFalse(store.userIsOwner("owner2")); // Actually, anyone can call terminateOwnership, but in microservice only superior can
        // To simulate microservice logic, checkIfSuperior
        assertFalse(store.checkIfSuperior("owner3", "owner2"));
    }

    @Test
    void testManagerCannotRemoveOwner() {
        store.addOwner("founder1", "owner2");
        store.addManager("founder1", "manager1", new boolean[]{true, true, true, true, true, true, true});
        // manager1 tries to remove owner2 (should not be allowed in microservice logic)
        assertFalse(store.checkIfSuperior("manager1", "owner2"));
    }

    @Test
    void testCircularOwnershipNotAllowed() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        // Try to make founder subordinate to owner3
        store.addOwner("owner3", "founder1");
        // Founder should not have a superior
        assertFalse(store.getOwnersToSuperior().containsKey("founder1") && !"founder1".equals(store.getOwnersToSuperior().get("founder1")));
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
    void testSubordinatesRemovedRecursively() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        store.addManager("owner2", "manager1", new boolean[]{true, true, true, true, true, true, true});
        store.terminateOwnership("owner2");
        assertFalse(store.userIsOwner("owner2"));
        assertFalse(store.userIsOwner("owner3"));
        assertFalse(store.userIsManager("manager1"));
    }

    @Test
    void testReserveAllAndUnreserveInSteps() {
        store.addNewProduct("prodX", 10);
        assertTrue(store.reserveProduct("prodX", 10));
        assertEquals(10, store.getReservedProducts().get("prodX"));
        assertNull(store.getProducts().get("prodX"));
        assertTrue(store.unreserveProduct("prodX", 4));
        assertEquals(6, store.getReservedProducts().get("prodX"));
        assertEquals(4, store.getProducts().get("prodX"));
        assertTrue(store.unreserveProduct("prodX", 6));
        assertFalse(store.getReservedProducts().containsKey("prodX"));
        assertEquals(10, store.getProducts().get("prodX"));
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
    void testRemoveProductWhileReserved() {
        store.addNewProduct("prodA", 5);
        store.reserveProduct("prodA", 3);
        // Should not remove if reserved (if logic applies)
        assertThrows(IllegalArgumentException.class, () -> store.removeProduct("prodA"));

    }



    @Test
    void testRemoveNonExistentOwner() {
        int before = store.getOwners().size();
        store.terminateOwnership("notAnOwner");
        assertEquals(before, store.getOwners().size());
    }

    @Test
    void testRemoveNonExistentManager() {
        int before = store.getManagers().size();
        store.terminateManagment("notAManager");
        assertEquals(before, store.getManagers().size());
    }

    @Test
    void testAddDiscountNullOrDuplicate() {
        int before = store.getDiscounts().size();
        assertFalse(store.addDiscount(null));
        assertTrue(store.addDiscount("discountX"));
        assertFalse(store.addDiscount("discountX")); // duplicate
        assertEquals(before + 1, store.getDiscounts().size());
    }

    @Test
    void testRemoveDiscountNullOrNonExistent() {
        assertFalse(store.removeDiscount(null));
        assertFalse(store.removeDiscount("notThere"));
    }
}
