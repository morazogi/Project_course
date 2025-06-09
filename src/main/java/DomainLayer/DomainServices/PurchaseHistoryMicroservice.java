package DomainLayer.DomainServices;

import DomainLayer.IOrderRepository;
import DomainLayer.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import InfrastructureLayer.OrderRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PurchaseHistoryMicroservice {
    private OrderRepository orders;
    private ObjectMapper mapper = new ObjectMapper();

    public void PurchaseHistoryMicroservice(OrderRepository orderRepository) {
        this.orders = orderRepository;
    }

    public List<String> getStorePurchaseHistory(String ownerId, String storeId, Date startDate, Date endDate) throws JsonProcessingException {
        // Implementation would call domain layer
        List<Order> lst = this.orders.findByStoreID(storeId);
        List<String> returnList = new ArrayList<String>();
        for (Order order : lst) {
            if(order.getDate().before(endDate) && order.getDate().after(startDate))
                returnList.add(order.getId());
        }
        return returnList;
        // orderrepository get the orders from the store id and filter;
    }
}
