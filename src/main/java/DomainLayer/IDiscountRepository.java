package DomainLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDiscountRepository extends JpaRepository<Discount, String> {
    void saveDiscount(String storeId, String discount);
    List<String> findAllDiscountsOfAStore(String storeId);
    Discount getReferenceById(String s);
}