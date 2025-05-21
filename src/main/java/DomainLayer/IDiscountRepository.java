package DomainLayer;

import org.springframework.stereotype.Repository;

import java.util.Map;

public interface IDiscountRepository {

    Map<String, Discount> getAll();

    boolean add(Discount discount);

    boolean update(Discount discount);

    Discount remove(String discountId);

    Discount find(String discountId);
}
