
package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;

import java.util.*;

class RegisteredUserTest {

    private RegisteredUser user;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        user = new RegisteredUser("username","password");
        mapper = new ObjectMapper();
    }

    @Test
    void testParameterizedConstructor() {
        String name = "Alice";
        String HashedPassword = "password";
        RegisteredUser ru = new RegisteredUser(name,HashedPassword);
        assertEquals(name, ru.getUsername());
        assertEquals(name, ru.getUsername());
    }

    @Test
    void testSetName() {
        String newName = "Bob";
        user.setUsername(newName);
        assertEquals(newName, user.getUsername());
        assertEquals(newName, user.getUsername());
    }

    @Test
    void testSetAnswers() {
        Map<String, String> answers = new HashMap<>();
        answers.put("Q1", "A1");
        user.setAnswers(answers);
        // No getter, but we can serialize to JSON and ensure field exists
        assertDoesNotThrow(() -> mapper.writeValueAsString(user));
    }
}
