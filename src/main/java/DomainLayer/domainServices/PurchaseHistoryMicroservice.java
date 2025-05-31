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
    private IOrderRepository orders;
    private ObjectMapper mapper = new ObjectMapper();

    public void PurchaseHistoryMicroservice(IOrderRepository orderRepository) {
        this.orders = orderRepository;
    }

    public List<String> getStorePurchaseHistory(String ownerId, String storeId, Date startDate, Date endDate) throws JsonProcessingException {
        // Implementation would call domain layer
        List<String> lst = this.orders.getOrderByStoreId(storeId);
        List<String> returnList = new ArrayList<String>();
        for (String order : lst) {
            Order order1= mapper.readValue(order, Order.class);
            if(order1.getDate().before(endDate) && order1.getDate().after(startDate))
                returnList.add(order);
        }
        return returnList;

        // orderrepository get the orders from the store id and filter;
    }
}
