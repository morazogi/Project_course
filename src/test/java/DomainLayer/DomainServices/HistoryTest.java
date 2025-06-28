//package DomainLayer.DomainServices;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import java.util.Arrays;
//import java.util.List;
//
//import InfrastructureLayer.UserRepository;
//import InfrastructureLayer.OrderRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import DomainLayer.IToken;
//
//
//class HistoryTest {
//
//    @Mock private IToken tokener;
//    @Mock private OrderRepository orderRepository;
//    @Mock private UserRepository userRepository;
//
//    @InjectMocks private History historyService;
//    private AutoCloseable mocks;
//
//    private static final String TOKEN = "tok-1";
//    private static final String USER  = "alice";
//
//    @BeforeEach
//    void setUp() {
//        mocks = MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void getOrderHistory_nullToken_throws() {
//        IllegalArgumentException ex = assertThrows(
//            IllegalArgumentException.class,
//            () -> historyService.getOrderHistory(null)
//        );
//        assertEquals("Token cannot be null", ex.getMessage());
//    }
//
//    @Test
//    void getOrderHistory_invalidToken_throws() throws Exception {
//        // stub validateToken to do nothing, extractUsername to return null
//        doNothing().when(tokener).validateToken(TOKEN);
//        when(tokener.extractUsername(TOKEN)).thenReturn(null);
//
//        IllegalArgumentException ex = assertThrows(
//            IllegalArgumentException.class,
//            () -> historyService.getOrderHistory(TOKEN)
//        );
//        assertEquals("Invalid token", ex.getMessage());
//    }
//
//    @Test
//    void getOrderHistory_success_returnsList() throws Exception {
//        List<String> mockHistory = Arrays.asList("order1", "order2");
//
//        // stub token behavior
//        doNothing().when(tokener).validateToken(TOKEN);
//        when(tokener.extractUsername(TOKEN)).thenReturn(USER);
//
//        // stub repository
//        when(orderRepository.findByUserID(USER)).thenReturn(mockHistory);
//
//        // call under test
//        List<String> result = historyService.getOrderHistory(TOKEN);
//
//        // verify interactions
//        verify(tokener).validateToken(TOKEN);
//        verify(tokener).extractUsername(TOKEN);
//        verify(orderRepository).findByUserID(USER);
//
//        // assert result
//        assertSame(mockHistory, result);
//    }
//}
