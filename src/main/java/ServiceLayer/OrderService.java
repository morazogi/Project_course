package ServiceLayer;
import DomainLayer.IOrderRepository;
import DomainLayer.Order;
import InfrastructureLayer.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class OrderService{
    OrderRepository orderRepository;
    private String id = "1";


    public OrderService(OrderRepository OrderRepository) {

        this.orderRepository = OrderRepository;
    }

    @Transactional
    public void addOrder(Order order){
        order.setId(id);
        int numericId = Integer.parseInt(id);
        numericId++;
        id = String.valueOf(numericId);
        orderRepository.save(order);
    }

    @Transactional
    public void removeOrder(Order order){
        orderRepository.save(order);
    }
}