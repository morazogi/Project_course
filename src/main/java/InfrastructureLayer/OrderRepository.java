package InfrastructureLayer;
import DomainLayer.IOrderRepository;
import DomainLayer.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;


public class OrderRepository implements IOrderRepository {
    private Map<Order, String> Orders = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void addOrder(Order order) {
        for (Order existingOrder : Orders.keySet()) {
            if (existingOrder.getId().equals(order.getId())) {
                return;
            }
        }
        Orders.put(order, order.getId());
    }


    public void removeOrder(Order order) {
        Order orderToRemove = null;
        for (Order existingOrder : Orders.keySet()) {
            if (existingOrder.getId().equals(order.getId())) {
                orderToRemove = existingOrder;
                break;
            }
        }

        if (orderToRemove != null) {
            Orders.remove(orderToRemove);
        }
    }

    public List<String> getOrderHistory(String userId) throws Exception {
        List<String> orderHistory = new ArrayList<>();
        for (Map.Entry<Order, String> entry : Orders.entrySet()) {
            if (entry.getValue().equals(userId)) {
                orderHistory.add(mapper.writeValueAsString(entry.getKey()));
            }
        }
        return orderHistory;
    }

}
