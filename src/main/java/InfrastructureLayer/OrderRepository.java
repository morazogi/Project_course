package infrastructureLayer;
import DomainLayer.IOrderRepository;
import DomainLayer.Order;
import ServiceLayer.EventLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository implements IOrderRepository {
    private Map<String, List<String>> Orders = new HashMap<>();
    private static OrderRepository instance;

    public OrderRepository() {}

    // Public method to provide access to the singleton instance
    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    @Override
    public void addOrder(String order, String storeId, String userId) {
        List<String> orderByStore = Orders.get(storeId);
        if(orderByStore == null) {
            List<String> lst = new ArrayList<>();
            lst.add(order);
            Orders.put(storeId, lst);
        }
        else {
            List<String> lst = orderByStore;
            lst.add(order);
            Orders.put(storeId, lst);
        }

        List<String> orderByUser = Orders.get(userId);
        if(orderByUser == null) {
            List<String> lst = new ArrayList<>();
            lst.add(order);
            Orders.put(userId, lst);
        }
        else {
            List<String> lst = orderByUser;
            lst.add(order);
            Orders.put(userId, lst);
        }
    }

    @Override
    public void removeOrder(String order, String storeId, String userId) {
        List<String> lst = this.Orders.get(storeId);
        boolean remove = lst.remove(order);
        this.Orders.put(storeId, lst);

        List<String> lst1 = this.Orders.get(userId);
        boolean remove1 = lst.remove(order);
        this.Orders.put(userId, lst1);

        EventLogger.logEvent("remove order in order repository",
                "removed order from the OrderRepository adding the 2 booleans " +remove+ " "+ remove1);
    }

    @Override
    public List<String> getOrderByStoreId(String storeId) {
        List<String> lst = this.Orders.get(storeId);
        List<String> clone = new ArrayList<>();
        clone.addAll(lst);
        return clone;
    }

    @Override
    public List<String> getOrderByUserId(String userId) {
        List<String> lst = this.Orders.get(userId);
        List<String> clone = new ArrayList<>();
        clone.addAll(lst);
        return clone;
    }

    @Override
    public List<String> getOrderHistory(String userId) {
        List<String> lst = this.Orders.get(userId);
        List<String> clone = new ArrayList<>();
        clone.addAll(lst);
        return clone;
    }
}