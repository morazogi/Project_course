//package DomainLayer.DomainServices;
//
//import DomainLayer.IToken;
//import DomainLayer.Order;
//import InfrastructureLayer.OrderRepository;
//import InfrastructureLayer.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class HistoryTest {
//
//    private IToken mockToken;
//    private OrderRepository mockOrderRepository;
//    private UserRepository mockUserRepository;
//    private History history;
//
//    @BeforeEach
//    void setUp() {
//        mockToken = mock(IToken.class);
//        mockOrderRepository = mock(OrderRepository.class);
//        mockUserRepository = mock(UserRepository.class);
//        history = new History(mockToken, mockOrderRepository, mockUserRepository);
//    }
//
//    @Test
//    void testGetOrderHistory_success() throws Exception {
//        String token = "validToken";
//        String username = "user1";
//
//        Order order1 = mock(Order.class);
//        Order order2 = mock(Order.class);
//
//        when(mockToken.extractUsername(token)).thenReturn(username);
//        doNothing().when(mockToken).validateToken(token);
//        when(mockOrderRepository.findByUserID(username)).thenReturn(Arrays.asList(order1, order2));
//        when(order1.toString()).thenReturn("Order1");
//        when(order2.toString()).thenReturn("Order2");
//
//        List<String> result = history.getOrderHistory(token);
//
//        assertEquals(2, result.size());
//        assertTrue(result.contains("Order1"));
//        assertTrue(result.contains("Order2"));
//
//        verify(mockToken).validateToken(token);
//        verify(mockToken).extractUsername(token);
//        verify(mockOrderRepository).findByUserID(username);
//    }
//
//    @Test
//    void testGetOrderHistory_nullToken() {
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            history.getOrderHistory(null);
//        });
//        assertEquals("Token cannot be null", exception.getMessage());
//    }
//
//    @Test
//    void testGetOrderHistory_invalidToken() throws Exception {
//        String token = "invalidToken";
//
//        doNothing().when(mockToken).validateToken(token);
//        when(mockToken.extractUsername(token)).thenReturn(null);
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            history.getOrderHistory(token);
//        });
//        assertEquals("Invalid token", exception.getMessage());
//    }
//}
