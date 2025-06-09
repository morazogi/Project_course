package InfrastructureLayer;

import DomainLayer.IProductRepository;
import DomainLayer.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import java.util.List;

@Component
public class ProductRepository implements IRepo<Product> {
    @Autowired
    IProductRepository repo;

    public Product save(Product product) {
        return repo.save(product);
    }
    public Product update(Product product) {
        return repo.save(product);
    }
    public Product getById(String productID) {
        return repo.getReferenceById(productID);
    }
    public List<Product> getAll() {
        return repo.findAll();
    }
    public void delete(Product product) {
        repo.delete(product);
    }
    public void deleteById(String productID) {
        repo.deleteById(productID);
    }
    public boolean existsById(String id){
        return repo.existsById(id);
    }
    public Product getProductByName(String productName) {return repo.findByNameContaining(productName); }
}
