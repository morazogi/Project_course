package InfrastructureLayer;
import DomainLayer.IUserRepository; // Assuming this is your Spring Data JPA repository interface
import DomainLayer.Roles.RegisteredUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional; // Import Optional

@Component
public class UserRepository implements IRepo<RegisteredUser> {

    @Autowired
    IUserRepository repo;

    public RegisteredUser save(RegisteredUser RegisteredUser) {
        return repo.save(RegisteredUser);
    }
    public RegisteredUser update(RegisteredUser RegisteredUser) {
        return repo.saveAndFlush(RegisteredUser);
    }

    public RegisteredUser getById(String id) {
        Optional<RegisteredUser> userOptional = repo.findById(id);
        return userOptional.orElseThrow(() -> new RuntimeException("RegisteredUser with ID " + id + " not found."));
    }

    public List<RegisteredUser> getAll() {
        return repo.findAll();
    }
    public void deleteById(String userID) {
        repo.deleteById(userID);
    }
    public void delete(RegisteredUser RegisteredUser){
        repo.delete(RegisteredUser);
    }
    public boolean existsById(String id){
        return repo.existsById(id);
    }
    public RegisteredUser getByName(String name) {
        return repo.findByUsernameContaining(name);
    }
}
