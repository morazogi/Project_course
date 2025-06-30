package DomainLayer.DomainServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Date;

import InfrastructureLayer.UserRepository;
import InfrastructureLayer.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import DomainLayer.IToken;
import DomainLayer.Order;


class HistoryTest {

   @Mock private IToken tokener;
   @Mock private OrderRepository orderRepository;
   @Mock private UserRepository userRepository;

   @InjectMocks private History historyService;
   private AutoCloseable mocks;

   private static final String TOKEN = "tok-1";
   private static final String USER  = "alice";

   @BeforeEach
   void setUp() {
       mocks = MockitoAnnotations.openMocks(this);
   }

   @Test
   void getOrderHistory_nullToken_throws() {
       IllegalArgumentException ex = assertThrows(
           IllegalArgumentException.class,
           () -> historyService.getOrderHistory(null)
       );
       assertEquals("Token cannot be null", ex.getMessage());
   }

   @Test
   void getOrderHistory_invalidToken_throws() throws Exception {
       // stub validateToken to do nothing, extractUsername to return null
       doNothing().when(tokener).validateToken(TOKEN);
       when(tokener.extractUsername(TOKEN)).thenReturn(null);

       IllegalArgumentException ex = assertThrows(
           IllegalArgumentException.class,
           () -> historyService.getOrderHistory(TOKEN)
       );
       assertEquals("Invalid token", ex.getMessage());
   }

   @Test
   void getOrderHistory_success_returnsList() throws Exception {
       // Create real Order objects with custom toString behavior
       Order mockOrder1 = new TestOrder("order1");
       Order mockOrder2 = new TestOrder("order2");
       List<Order> mockOrders = Arrays.asList(mockOrder1, mockOrder2);

       // stub token behavior
       doNothing().when(tokener).validateToken(TOKEN);
       when(tokener.extractUsername(TOKEN)).thenReturn(USER);

       // stub repository to return List<Order>
       when(orderRepository.findByUserID(USER)).thenReturn(mockOrders);

       // call under test
       List<String> result = historyService.getOrderHistory(TOKEN);

       // verify interactions
       verify(tokener).validateToken(TOKEN);
       verify(tokener).extractUsername(TOKEN);
       verify(orderRepository).findByUserID(USER);

       // assert result - should contain the string representations of the orders
       assertEquals(2, result.size());
       assertEquals("order1", result.get(0));
       assertEquals("order2", result.get(1));
   }

   // Test helper class that extends Order with custom toString
   private static class TestOrder extends Order {
       private final String stringRepresentation;

       public TestOrder(String stringRepresentation) {
           super("test info", "test store", "test user", new Date());
           this.stringRepresentation = stringRepresentation;
       }

       @Override
       public String toString() {
           return stringRepresentation;
       }
   }
}
