package DomainLayer.DomainServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import DomainLayer.DomainServices.UserConnectivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Roles.RegisteredUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;

class UserConnectivityTest {

    @Mock private IToken tokener;
    @Mock private IUserRepository userRepository;

    @InjectMocks private UserConnectivity connectivity;
    private AutoCloseable mocks;
    private ObjectMapper mapper = new ObjectMapper();

    private static final String USER = "alice";
    private static final String PASS = "secret";
    private static final String HASH = org.mindrot.jbcrypt.BCrypt.hashpw(PASS, org.mindrot.jbcrypt.BCrypt.gensalt());
    private static final String TOKEN = "tok-123";

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    // --- login() tests ---

    @Test
    void login_nullUsername_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.login(null, "p")
        );
        assertEquals("Username and password cannot be null", ex.getMessage());
    }

    @Test
    void login_nullPassword_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.login("u", null)
        );
        assertEquals("Username and password cannot be null", ex.getMessage());
    }

    @Test
    void login_emptyUsername_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.login("", "p")
        );
        assertEquals("Username and password cannot be empty", ex.getMessage());
    }

    @Test
    void login_emptyPassword_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.login("u", "")
        );
        assertEquals("Username and password cannot be empty", ex.getMessage());
    }

    @Test
    void login_userNotExist_throws() {
        when(userRepository.getUserPass(USER)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.login(USER, PASS)
        );
        assertEquals("User does not exist", ex.getMessage());
    }

    @Test
    void login_wrongPassword_throws() {
        when(userRepository.getUserPass(USER)).thenReturn(HASH);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.login(USER, "badpass")
        );
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void login_success_returnsToken() {
        when(userRepository.getUserPass(USER)).thenReturn(HASH);
        when(tokener.generateToken(USER)).thenReturn(TOKEN);

        String result = connectivity.login(USER, PASS);

        verify(tokener).generateToken(USER);
        assertEquals(TOKEN, result);
    }

    // --- signUp() tests ---

    @Test
    void signUp_nullUsername_throws() {
        assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.signUp(null, PASS)
        );
    }

    @Test
    void signUp_nullPassword_throws() {
        assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.signUp(USER, null)
        );
    }

    @Test
    void signUp_emptyUsername_throws() {
        assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.signUp("", PASS)
        );
    }

    @Test
    void signUp_emptyPassword_throws() {
        assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.signUp(USER, "")
        );
    }

    @Test
    void signUp_userExists_throws() {
        when(userRepository.isUserExist(USER)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.signUp(USER, PASS)
        );
        assertEquals("User already exists", ex.getMessage());
    }

    @Test
    void signUp_success_addsUserAndReturnsId() throws JsonProcessingException {
        when(userRepository.isUserExist(USER)).thenReturn(false);

        ArgumentCaptor<String> userCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> passCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCap = ArgumentCaptor.forClass(String.class);

        String returnedId = connectivity.signUp(USER, PASS);

        verify(userRepository).addUser(
            userCap.capture(),
            passCap.capture(),
            jsonCap.capture()
        );

        assertEquals(USER, userCap.getValue());
        assertNotEquals(PASS, passCap.getValue());              // should be hashed
        String json = jsonCap.getValue();
        RegisteredUser created = mapper.readValue(json, RegisteredUser.class);
        assertEquals(USER, created.getName());
        assertEquals(created.getID(), returnedId);
    }

    // --- logout() tests ---

    @Test
    void logout_guest_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.logout("Guest", TOKEN)
        );
        assertEquals("Guest cannot logout", ex.getMessage());
    }

    @Test
    void logout_nullUsername_throws() {
        assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.logout(null, TOKEN)
        );
    }

    @Test
    void logout_emptyUsername_throws() {
        assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.logout("", TOKEN)
        );
    }

    @Test
    void logout_nullToken_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.logout(USER, null)
        );
        assertEquals("Token cannot be null", ex.getMessage());
    }

    @Test
    void logout_emptyToken_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> connectivity.logout(USER, "")
        );
        assertEquals("Token cannot be empty", ex.getMessage());
    }

    @Test
    void logout_success_invalidatesToken() {
        connectivity.logout(USER, TOKEN);
        verify(tokener).invalidateToken(TOKEN);
    }
}
