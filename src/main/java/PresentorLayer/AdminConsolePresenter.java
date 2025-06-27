package PresentorLayer;

import DomainLayer.DomainServices.AdminOperationsMicroservice;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.TokenService;          // ← NEW
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminConsolePresenter {

    private final AdminOperationsMicroservice adminOps;
    private final StoreRepository             storeRepo;
    private final UserRepository              userRepo;
    private final TokenService                tokenSvc;      // ← NEW

    public AdminConsolePresenter(AdminOperationsMicroservice adminOps,
                                 StoreRepository             storeRepo,
                                 UserRepository              userRepo,
                                 TokenService                tokenSvc) {  // ← NEW
        this.adminOps  = adminOps;
        this.storeRepo = storeRepo;
        this.userRepo  = userRepo;
        this.tokenSvc  = tokenSvc;
    }

    /* user-management only — store actions dropped */

    public boolean suspend(String userId) {
        boolean ok = adminOps.suspendMember("1", userId);
        if (ok) tokenSvc.suspendUser(userId);
        return ok;
    }
    public boolean unSuspend(String userId) {
        boolean ok = adminOps.unSuspendMember("1", userId);
        if (ok) tokenSvc.unsuspendUser(userId);
        return ok;
    }

    public List<String> allUsers() {
        return userRepo.getAll().stream()
                .map(u -> u.getUsername())
                .filter(u -> !u.equals("1"))
                .toList();
    }
}
