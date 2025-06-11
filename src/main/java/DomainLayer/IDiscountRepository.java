package DomainLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.*;


import java.util.List;

import java.util.List;

@Repository
public interface IDiscountRepository extends JpaRepository<Discount, String> {
}