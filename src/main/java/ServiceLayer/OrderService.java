package ServiceLayer;
import DomainLayer.IOrderRepository;
import DomainLayer.Order;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class OrderService{
    IOrderRepository OrderRepository;
    private String id = "1";


    public OrderService(IOrderRepository OrderRepository) {

        this.OrderRepository = OrderRepository;
    }

    @Transactional
    public void addOrder(Order order){
        order.setId(id);
        int numericId = Integer.parseInt(id);
        numericId++;
        id = String.valueOf(numericId);
        OrderRepository.addOrder(order.toString(), order.getStoreId(), order.getUserId());
    }

    @Transactional
    public void removeOrder(Order order){
        OrderRepository.addOrder(order.toString(), order.getStoreId(), order.getUserId());
    }
}