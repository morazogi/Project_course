package DomainLayer.DomainServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import DomainLayer.DomainServices.OpenStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import DomainLayer.IStoreRepository;
import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class OpenStoreTest {

    @Mock private IToken tokener;
    @Mock private IStoreRepository storeRepository;
    @Mock private IUserRepository userRepository;

    @InjectMocks private OpenStore openStoreService;
    private AutoCloseable mocks;
    private ObjectMapper mapper = new ObjectMapper();

    private static final String TOKEN    = "token-xyz";
    private static final String USERNAME = "alice";

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        // default: token is valid and extractUsername returns USERNAME
        doNothing().when(tokener).validateToken(TOKEN);
        when(tokener.extractUsername(TOKEN)).thenReturn(USERNAME);
    }

    @Test
    void openStore_nullToken_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> openStoreService.openStore(null , "storeName")
        );
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    void openStore_userNotExist_throws() {
        when(userRepository.getUser(USERNAME)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> openStoreService.openStore(TOKEN , "storeName")
        );
        assertEquals("User does not exist", ex.getMessage());
    }

    @Test
    void openStore_success_returnsNewStoreId_andAddsStore() throws Exception {
        // stub: user exists
        when(userRepository.getUser(USERNAME)).thenReturn("{\"dummy\":\"json\"}");

        // call
        String newStoreId = openStoreService.openStore(TOKEN , "storeName");

        // verify token validation and extraction
        verify(tokener).validateToken(TOKEN);
        verify(tokener).extractUsername(TOKEN);

        // capture addStore args
        ArgumentCaptor<String> idCap   = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCap = ArgumentCaptor.forClass(String.class);
        verify(storeRepository).addStore(idCap.capture(), jsonCap.capture());

        // the returned ID matches the one passed to addStore
        assertEquals(idCap.getValue(), newStoreId);

        // JSON should parse back to a Store with matching owner
        Store created = mapper.readValue(jsonCap.getValue(), Store.class);
        assertEquals(newStoreId, created.getId());
    }
}
