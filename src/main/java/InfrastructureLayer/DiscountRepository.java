package InfrastructureLayer;

import DomainLayer.Discount;
import DomainLayer.IDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiscountRepository implements IRepo<Discount> {
    @Autowired
    IDiscountRepository repo;

    public Discount save(Discount discount) {
        return repo.save(discount);
    }
    public Discount update(Discount discount) {
        return repo.saveAndFlush(discount);
    }
    public Discount getById(String id) {
        return repo.findById(id).orElse(null);
    }
    public List<Discount> getAll() {
        return repo.findAll();
    }
    public void deleteById(String discountID) {
        repo.deleteById(discountID);
    }
    public void delete(Discount discount){
        repo.delete(discount);
    }
    public boolean existsById(String id){
        return repo.existsById(id);
    }
}
