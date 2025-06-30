package ServiceLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private TokenService service;

    @BeforeEach
    void setUp() {
        service = new TokenService();          // no Spring context needed
    }

    /* ─────────────────── happy flow ─────────────────── */

    @Test
    void generate_validate_extract_roundTrip() {
        String token = service.generateToken("alice");

        // username comes back unchanged
        assertEquals("alice", service.extractUsername(token));

        // expiration is in the future (≈ 1 h)
        Date exp = service.extractExpiration(token);
        assertTrue(exp.after(new Date()));

        // token is active and validates cleanly
        assertDoesNotThrow(() -> service.validateToken(token));
        assertEquals(token, service.getToken("alice"));
    }

    /* ─────────────────── invalidate path ─────────────────── */

    @Test
    void invalidateToken_blocksSubsequentUse() {
        String token = service.generateToken("bob");

        service.invalidateToken(token);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validateToken(token));
        assertEquals("user not logged in", ex.getMessage());
    }

    /* ─────────────────── inactive token path ─────────────────── */

    @Test
    void validateToken_whenNotInActiveMap_throws() {
        String token = service.generateToken("carol");

        // remove from activeTokens via reflection to simulate external loss
        Map<?,?> active = (Map<?, ?>) ReflectionTestUtils.getField(service, "activeTokens");
        active.remove(token);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validateToken(token));
        assertEquals("Token is not active", ex.getMessage());
    }

    /* ─────────────────── suspension paths ─────────────────── */

    @Test
    void suspendUser_killsSessions_and_blocksNewTokens() {
        String token = service.generateToken("dave");

        service.suspendUser("dave");

        // existing token now forbidden
        assertThrows(IllegalArgumentException.class, () -> service.validateToken(token));

        // user sits on the suspended list
        assertTrue(service.showSuspended().contains("dave"));

        // cannot generate another token while suspended
        assertThrows(IllegalArgumentException.class, () -> service.generateToken("dave"));
    }

    @Test
    void unsuspendUser_allowsLoginAgain() {
        service.suspendUser("erin");
        service.unsuspendUser("erin");

        // removed from suspended list
        assertFalse(service.showSuspended().contains("erin"));

        // can generate / validate normally again
        String token = assertDoesNotThrow(() -> service.generateToken("erin"));
        assertDoesNotThrow(() -> service.validateToken(token));
    }

    /* ─────────────────── input-validation guards ─────────────────── */

    @Test
    void validateToken_nullOrEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.validateToken(null));
        assertThrows(IllegalArgumentException.class, () -> service.validateToken(""));
    }

    @Test
    void suspendUser_nullOrEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.suspendUser(null));
        assertThrows(IllegalArgumentException.class, () -> service.suspendUser(""));
    }

    @Test
    void invalidateToken_nullOrEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.invalidateToken(null));
        assertThrows(IllegalArgumentException.class, () -> service.invalidateToken(""));
    }
}
