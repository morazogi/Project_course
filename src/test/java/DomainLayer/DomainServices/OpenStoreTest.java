//package DomainLayer.DomainServices;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import DomainLayer.Roles.RegisteredUser;
//import DomainLayer.Store;
//import InfrastructureLayer.StoreRepository;
//import InfrastructureLayer.UserRepository;
//import DomainLayer.IToken;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//class OpenStoreTest {
//
//    @Mock private IToken tokener;
//    @Mock private StoreRepository storeRepository;
//    @Mock private UserRepository userRepository;
//
//    @InjectMocks private OpenStore openStoreService;
//    private AutoCloseable mocks;
//
//    private static final String TOKEN    = "token-xyz";
//    private static final String USERNAME = "alice";
//
//    @BeforeEach
//    void setUp() {
//        mocks = MockitoAnnotations.openMocks(this);
//
//        doNothing().when(tokener).validateToken(TOKEN);
//        when(tokener.extractUsername(TOKEN)).thenReturn(USERNAME);
//    }
//
//    @Test
//    void openStore_nullToken_throws() {
//        IllegalArgumentException ex = assertThrows(
//                IllegalArgumentException.class,
//                () -> openStoreService.openStore(null , "storeName")
//        );
//        assertEquals("Invalid input", ex.getMessage());
//    }
//
//    @Test
//    void openStore_userNotExist_throws() {
//        when(userRepository.getById(USERNAME)).thenReturn(null);
//
//        IllegalArgumentException ex = assertThrows(
//                IllegalArgumentException.class,
//                () -> openStoreService.openStore(TOKEN , "storeName")
//        );
//        assertEquals("User does not exist", ex.getMessage());
//    }
//
//    @Test
//    void openStore_success_returnsNewStoreId_andSavesStore() throws Exception {
//        RegisteredUser dummyUser = mock(RegisteredUser.class);
//        when(userRepository.getById(USERNAME)).thenReturn(dummyUser);
//
//        Store mockStore = new Store(USERNAME, "storeName");
//        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);
//
//        // call
//        String newStoreId = openStoreService.openStore(TOKEN , "storeName");
//
//        // verify token validation and extraction
//        verify(tokener).validateToken(TOKEN);
//        verify(tokener).extractUsername(TOKEN);
//
//        // capture save args
//        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
//        verify(storeRepository).save(storeCaptor.capture());
//
//        // verify the store was created with correct parameters
//        Store savedStore = storeCaptor.getValue();
//        assertEquals(USERNAME, savedStore.getFounder());
//        assertEquals("storeName", savedStore.getName());
//
//        // verify the returned ID matches the saved store's ID
//        assertEquals(mockStore.getId(), newStoreId);
//    }
//
//    @Test
//    void openStore_invalidStoreName_throws() {
//        when(userRepository.getById(USERNAME)).thenReturn(mock(RegisteredUser.class));
//
//        IllegalArgumentException ex = assertThrows(
//                IllegalArgumentException.class,
//                () -> openStoreService.openStore(TOKEN , "")
//        );
//        assertEquals("Invalid store name", ex.getMessage());
//    }
//}
