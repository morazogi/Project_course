package PresentorLayer;

import DomainLayer.DomainServices.AdminOperationsMicroservice;
import DomainLayer.Store;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.AdminService;
import ServiceLayer.TokenService;          // ← NEW
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminConsolePresenter {

    private final AdminOperationsMicroservice adminOps;
    private final StoreRepository             storeRepo;
    private final UserRepository              userRepo;
    private final TokenService                tokenSvc;
    private final AdminService                adminService;

    public AdminConsolePresenter(AdminOperationsMicroservice adminOps,
                                 StoreRepository             storeRepo,
                                 UserRepository              userRepo,
                                 TokenService                tokenSvc,
                                 AdminService adminService) {  // ← NEW
        this.adminOps  = adminOps;
        this.storeRepo = storeRepo;
        this.userRepo  = userRepo;
        this.tokenSvc  = tokenSvc;
        this.adminService = adminService;
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

    public String closeStore(String storeName) {
        List<Store> stores = storeRepo.getAll();
        for (Store store : stores) {
            if (store.getName().equals(storeName)) {
            if (adminService.adminCloseStore("1", store.getId())) {
                return "Closed store";
            }
                return "Failed to close store";
            }
        }
        return "Could not find store with that name";
    }
}
