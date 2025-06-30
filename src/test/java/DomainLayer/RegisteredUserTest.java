package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import DomainLayer.Roles.RegisteredUser;

import java.util.*;

class RegisteredUserTest {

    private RegisteredUser user;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        user = new RegisteredUser("username", "hashedPassword");
        mapper = new ObjectMapper();
    }

    @Test
    void testParameterizedConstructor() {
        String name = "Alice";
        String hashedPassword = "password";
        RegisteredUser ru = new RegisteredUser(name, hashedPassword);
        assertEquals(name, ru.getUsername());
        assertEquals(hashedPassword, ru.getHashedPassword());
        assertNotNull(ru.getOwnedStores());
        assertNotNull(ru.getManagedStores());
        assertTrue(ru.getOwnedStores().isEmpty());
        assertTrue(ru.getManagedStores().isEmpty());
    }

    @Test
    void testDefaultConstructor() {
        RegisteredUser ru = new RegisteredUser();
        assertNotNull(ru);
        // Username and password may be null or default, depending on Guest implementation
    }

    @Test
    void testSetAndGetUsername() {
        String newName = "Bob";
        user.setUsername(newName);
        assertEquals(newName, user.getUsername());
    }

    @Test
    void testSetAndGetAnswers() {
        Map<String, String> answers = new HashMap<>();
        answers.put("Q1", "A1");
        user.setAnswers(answers);
        // No getter, but we can serialize to JSON and ensure field exists
        assertDoesNotThrow(() -> mapper.writeValueAsString(user));
    }

    @Test
    void testSetAndGetOwnedStores() {
        List<String> stores = Arrays.asList("store1", "store2");
        user.setOwnedStores(stores);
        assertEquals(stores, user.getOwnedStores());
    }

    @Test
    void testSetAndGetManagedStores() {
        List<String> stores = Arrays.asList("store3", "store4");
        user.setManagedStores(stores);
        assertEquals(stores, user.getManagedStores());
    }

    @Test
    void testAddOwnedStore() {
        user.addOwnedStore("storeA");
        assertTrue(user.getOwnedStores().contains("storeA"));
        user.addOwnedStore("storeB");
        assertEquals(2, user.getOwnedStores().size());
    }

    @Test
    void testAddManagedStore() {
        user.addManagedStore("storeX");
        assertTrue(user.getManagedStores().contains("storeX"));
        user.addManagedStore("storeY");
        assertEquals(2, user.getManagedStores().size());
    }

    @Test
    void testRemoveStore() {
        user.addOwnedStore("store1");
        user.addManagedStore("store1");
        user.addOwnedStore("store2");
        user.removeStore("store1");
        assertFalse(user.getOwnedStores().contains("store1"));
        assertFalse(user.getManagedStores().contains("store1"));
        assertTrue(user.getOwnedStores().contains("store2"));
    }

    @Test
    void testGetHashedPassword() {
        assertEquals("hashedPassword", user.getHashedPassword());
    }


    @Test
    void testAcceptQueryResponseDoesNothing() {
        // Should not throw any exception
        assertDoesNotThrow(() -> user.acceptQueryResponse("response"));
    }

    @Test
    void testAddOwnedStoreAllowsDuplicates() {
        user.addOwnedStore("storeA");
        user.addOwnedStore("storeA");
        assertEquals(2, Collections.frequency(user.getOwnedStores(), "storeA"));
    }

    @Test
    void testAddManagedStoreAllowsDuplicates() {
        user.addManagedStore("storeX");
        user.addManagedStore("storeX");
        assertEquals(2, Collections.frequency(user.getManagedStores(), "storeX"));
    }

    @Test
    void testRemoveStoreNotPresentDoesNothing() {
        user.addOwnedStore("store1");
        user.removeStore("store2"); // store2 not present
        assertTrue(user.getOwnedStores().contains("store1"));
    }

    @Test
    void testSetOwnedStoresNull() {
        user.setOwnedStores(null);
        assertNull(user.getOwnedStores());
    }

    @Test
    void testSetManagedStoresNull() {
        user.setManagedStores(null);
        assertNull(user.getManagedStores());
    }

    @Test
    void testSetAnswersNull() {
        user.setAnswers(null);
        // No getter, but should not throw
        assertDoesNotThrow(() -> mapper.writeValueAsString(user));
    }

    @Test
    void testRegisterThrowsExceptionWithNulls() {
        assertThrows(UnsupportedOperationException.class, () -> user.register(null, null));
    }

    @Test
    void testGetOwnedStoresIsMutable() {
        user.addOwnedStore("store1");
        List<String> owned = user.getOwnedStores();
        owned.add("store2");
        assertTrue(user.getOwnedStores().contains("store2"));
    }

    @Test
    void testGetManagedStoresIsMutable() {
        user.addManagedStore("store1");
        List<String> managed = user.getManagedStores();
        managed.add("store2");
        assertTrue(user.getManagedStores().contains("store2"));
    }
}
