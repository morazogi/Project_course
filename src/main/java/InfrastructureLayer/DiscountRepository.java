package InfrastructureLayer;

import DomainLayer.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;;

@Repository
public class DiscountRepository implements IDiscountRepository {

    private final Map<String, Discount> discounts = new ConcurrentHashMap<>();

    public Map<String, Discount> getAll() {
        return Collections.unmodifiableMap(discounts);
    }

    public boolean add(Discount discount) {
        if (discount == null || discount.Id == null) {
            return false;
        }
        return discounts.putIfAbsent(discount.Id, discount) == null;
    }

    public boolean update(Discount discount) {
        if (discount == null || discount.Id == null) {
            return false;
        }
        return discounts.replace(discount.Id, discount) != null;
    }

    public Discount remove(String discountId) {
        if (discountId == null) {
            return null;
        }
        return discounts.remove(discountId);
    }

    public Discount find(String discountId) {
        if (discountId == null) {
            return null;
        }
        return discounts.get(discountId);
    }
}
