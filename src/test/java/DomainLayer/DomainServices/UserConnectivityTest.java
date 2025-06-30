package DomainLayer.DomainServices;

import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.GuestRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link UserConnectivity}: login / signup / logout logic.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserConnectivityTest {

    @Mock IToken         tokener;
    @Mock UserRepository userRepo;
    @Mock GuestRepository guestRepo;

    private UserConnectivity conn;

    /* -------------------------------------------------------------
                           login success
       ------------------------------------------------------------- */
    @Test
    void login_happyPath_returnsToken() {
        String username = "alice";
        String password = "secret";
        String hashed   = BCrypt.hashpw(password, BCrypt.gensalt());

        RegisteredUser alice = mock(RegisteredUser.class);
        when(alice.getHashedPassword()).thenReturn(hashed);
        when(userRepo.getById(username)).thenReturn(alice);
        when(tokener.generateToken(username)).thenReturn("tkn");

        conn = new UserConnectivity(tokener, userRepo, guestRepo);

        assertEquals("tkn", conn.login(username, password));
    }

    @Test
    void login_wrongPassword_throws() {
        String username = "bob";
        String hashed   = BCrypt.hashpw("good", BCrypt.gensalt());

        RegisteredUser bob = mock(RegisteredUser.class);
        when(bob.getHashedPassword()).thenReturn(hashed);
        when(userRepo.getById(username)).thenReturn(bob);

        conn = new UserConnectivity(tokener, userRepo, guestRepo);

        assertThrows(IllegalArgumentException.class,
                () -> conn.login(username, "bad"));
    }

    /* -------------------------------------------------------------
                             sign-up
       ------------------------------------------------------------- */
    @Test
    void signUp_newUser_savesAndReturnsUsername() throws Exception {
        when(userRepo.existsById("newbie")).thenReturn(false);

        conn = new UserConnectivity(tokener, userRepo, guestRepo);

        String returned = conn.signUp("newbie", "pwd123");
        assertEquals("newbie", returned);

        ArgumentCaptor<RegisteredUser> cap =
                ArgumentCaptor.forClass(RegisteredUser.class);
        verify(userRepo).save(cap.capture());
        assertEquals("newbie", cap.getValue().getUsername());
    }

    @Test
    void signUp_duplicate_throws() {
        when(userRepo.existsById("dup")).thenReturn(true);

        conn = new UserConnectivity(tokener, userRepo, guestRepo);

        assertThrows(IllegalArgumentException.class,
                () -> conn.signUp("dup", "x"));
    }

    /* -------------------------------------------------------------
                               logout
       ------------------------------------------------------------- */
    @Test
    void logout_success_invokesInvalidate() {
        doNothing().when(tokener).invalidateToken("t");

        conn = new UserConnectivity(tokener, userRepo, guestRepo);

        conn.logout("alice", "t");
        verify(tokener).invalidateToken("t");
    }

    @Test
    void logout_guest_throws() {
        conn = new UserConnectivity(tokener, userRepo, guestRepo);

        assertThrows(IllegalArgumentException.class,
                () -> conn.logout("Guest", "t"));
    }
}
