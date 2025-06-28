package DomainLayer;

import DomainLayer.Roles.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<RegisteredUser, String> {

    RegisteredUser findByUsernameContaining(String name);
}