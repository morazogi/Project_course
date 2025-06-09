package InfrastructureLayer;
import DomainLayer.IUserRepository;
import DomainLayer.Roles.RegisteredUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
        return repo.getReferenceById(id);
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
        return repo.findByNameContaining(name);
    }

}