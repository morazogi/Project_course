package DomainLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import utils.Notifications;

import java.util.List;

public interface INotificationRepository extends JpaRepository<Notifications,String> {

    List<Notifications> findByUserId(String userID);
    List<Notifications> findByStoreId(String storeID);
}
