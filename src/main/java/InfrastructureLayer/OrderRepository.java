package InfrastructureLayer;
import DomainLayer.IOrderRepository;
import DomainLayer.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
public class OrderRepository implements IRepo<Order> {
    @Autowired
    private IOrderRepository repo;

    public Order save(Order order) { return repo.save(order); }
    public Order update(Order order) {
        return repo.saveAndFlush(order);
    }
    public Order getById(String orderID) {
        return repo.findById(orderID).orElse(null);
    }
    public List<Order> getAll() {
        return repo.findAll();
    }
    public void deleteById(String orderID) {
        repo.deleteById(orderID);
    }
    public void delete(Order order){
        repo.delete(order);
    }
    public boolean existsById(String orderID){
        return repo.existsById(orderID);
    }
    public List<Order> findByStoreID(String StoreID) {
        return repo.findByStoreId(StoreID);
    }


    public List<Order> findByUserID(String userID) { return repo.findByUserId(userID); }

}
