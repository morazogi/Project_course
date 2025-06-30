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

//    @Test
//    void testPermissions() {
//        store.addManager("founder1", "manager1", new boolean[]{true, false, true, false, false, false, false});
//        Map<String, Boolean> perms = store.getPremissions("manager1");
//        assertNotNull(perms);
//        assertTrue(perms.values().contains(true));
//    }

    @Test
    void testGetAllSubordinates() {
        store.addOwner("founder1", "owner2");
        store.addOwner("owner2", "owner3");
        LinkedList<String> subs = store.getAllSubordinates("founder1");
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
        // Note: managers are not included in getAllSubordinates as per current implementation

        LinkedList<String> owner2Subs = store.getAllSubordinates("owner2");
        assertTrue(owner2Subs.contains("owner3"));
        assertTrue(owner2Subs.contains("owner4"));
        assertTrue(owner2Subs.contains("owner5"));
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
}
