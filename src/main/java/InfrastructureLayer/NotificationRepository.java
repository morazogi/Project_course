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
        System.out.println("Calling repo.findAll()...");
        List<Notifications> list = repo.findAll();
        System.out.println("Got " + list.size() + " notifications");
        return list;
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

    public List<Notifications> findByUserID(String userId) { return repo.findByUserId(userId); }
    public List<Notifications> findByStoreID(String storeId) { return repo.findByStoreId(storeId); }

}