package DomainLayer.DomainServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import DomainLayer.IToken;
import DomainLayer.Store;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;

class OpenStoreTest {

   @Mock private IToken tokener;
   @Mock private StoreRepository storeRepository;
   @Mock private UserRepository userRepository;

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
       when(userRepository.getById(USERNAME)).thenReturn(null);

       NullPointerException ex = assertThrows(
           NullPointerException.class,
           () -> openStoreService.openStore(TOKEN , "storeName")
       );
   }

   @Test
   void openStore_success_returnsNewStoreId_andAddsStore() throws Exception {
       // Create mocks for user and cart
       RegisteredUser mockUser = mock(RegisteredUser.class);
       ShoppingCart mockCart = mock(ShoppingCart.class);

       // Set up the cart to return the user ID
       when(mockCart.getUserId()).thenReturn(USERNAME);
       when(mockUser.getShoppingCart()).thenReturn(mockCart);

       // Stub: user exists
       when(userRepository.getById(USERNAME)).thenReturn(mockUser);

       // Mock storeRepository.save to set the ID
       doAnswer(invocation -> {
           Store store = invocation.getArgument(0);
           store.setId("generated-id");
           return store;
       }).when(storeRepository).save(any(Store.class));

       // call
       String newStoreId = openStoreService.openStore(TOKEN, "storeName");

       // verify token validation and extraction
       verify(tokener).validateToken(TOKEN);
       verify(tokener).extractUsername(TOKEN);

       // verify storeRepository.save was called and capture the store
       ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
       verify(storeRepository).save(storeCaptor.capture());
       Store created = storeCaptor.getValue();

       // the returned ID matches the one set in save
       assertEquals("generated-id", newStoreId);

       // Check the store's founder and name
       assertEquals(USERNAME, created.getFounder());
       assertEquals("storeName", created.getName());
   }
}
