package DomainLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IOrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStoreId(String storeId);
    List<Order> findByUserId(String userId);
}