package ServiceLayer;

import DomainLayer.Order;
import InfrastructureLayer.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository repo;
    private OrderService  service;

    @BeforeEach
    void setUp() {
        service = new OrderService(repo);
    }

    @Test
    void addOrder_setsSequentialIds() {
        Order o1 = new Order("info1", "store", "user", new Date());
        Order o2 = new Order("info2", "store", "user", new Date());
        Order o3 = new Order("info3", "store", "user", new Date());

        service.addOrder(o1);
        service.addOrder(o2);
        service.addOrder(o3);

        ArgumentCaptor<Order> cap = ArgumentCaptor.forClass(Order.class);
        verify(repo, times(3)).save(cap.capture());

        assertEquals("1", cap.getAllValues().get(0).getId());
        assertEquals("2", cap.getAllValues().get(1).getId());
        assertEquals("3", cap.getAllValues().get(2).getId());
    }

    @Test
    void removeOrder_callsSave() {
        Order o = new Order("info", "store", "user", new Date());
        service.removeOrder(o);
        verify(repo).save(o);
    }
}
