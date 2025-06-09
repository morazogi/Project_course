package DomainLayer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStoreID(String storeID);
    List<Order> findByUserId(String userID);
}