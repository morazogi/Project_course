package DomainLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface IProductRepository extends JpaRepository<Product, String> {
    @Override
    Optional<Product> findById(String id);
    Optional<Product> findByName(String name);
    List<Product> findByCategory(String category);
    List<Product> findByStoreId(String storeId);
}
