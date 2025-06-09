package InfrastructureLayer;
import java.util.List;
import DomainLayer.INotificationRepository;

import DomainLayer.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import utils.Notifications;

@Repository
public class NotificationRepository implements IRepo<Notifications> {

    @Autowired
    INotificationRepository repo;

    public Notifications save(Notifications notifications) {
        return repo.save(notifications);
    }
    public Notifications update(Notifications notifications) {
        return repo.saveAndFlush(notifications);
    }
    public Notifications getById(String id) {
        return repo.getReferenceById(id);
    }
    public List<Notifications> getAll() {
        return repo.findAll();
    }
    public void deleteById(String notificationsID) {
        repo.deleteById(notificationsID);
    }
    public void delete(Notifications notifications){
        repo.delete(notifications);
    }
    public boolean existsById(String id){
        return repo.existsById(id);
    }

    public List<Notifications> findByUserID(String userID) { return repo.findByUserId(userID); }
    public List<Notifications> findByStoreID(String storeID) { return repo.findByStoreId(storeID); }

}