package InfrastructureLayer;
import DomainLayer.IProductRepository;
import DomainLayer.Product;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductRepository implements IProductRepository {
    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, Product> productsByName = new HashMap<>();
    private final Map<String, Product> productsByCategory = new HashMap<>();

    public void save(Product product) {
        products.put(product.getId(), product);
        productsByName.put(product.getName(), product);
        productsByCategory.put(product.getCategory(), product);
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public Optional<Product> findByName(String name) {
        return Optional.ofNullable(productsByName.get(name));
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public void deleteById(String id) {
        Product product = products.remove(id);
        if (product != null) {
            productsByName.remove(product.getName());
        }
    }

    public Product getProduct(String id) {
        return products.get(id);
    }
}
