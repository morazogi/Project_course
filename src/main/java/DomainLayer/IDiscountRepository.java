package DomainLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface IDiscountRepository extends JpaRepository<Discount, String> {
}