package PresentorLayer;

import DomainLayer.Product;
import DomainLayer.Store;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.RegisteredService;
import java.util.List;
import java.util.stream.Collectors;

public class RatePurchasesPresenter {

    private final String token;
    private final RegisteredService regSvc;
    private final UserRepository userRepo;
    private final ProductRepository prodRepo;
    private final StoreRepository storeRepo;

    public record Item(String productId,String productName,
                       String storeId,String storeName){}

    public RatePurchasesPresenter(String token,
                                  RegisteredService regSvc,
                                  UserRepository userRepo,
                                  ProductRepository prodRepo,
                                  StoreRepository storeRepo) {
        this.token     = token;
        this.regSvc    = regSvc;
        this.userRepo  = userRepo;
        this.prodRepo  = prodRepo;
        this.storeRepo = storeRepo;
    }

    public List<Item> list() {
        String user = regSvc.getTokenService().extractUsername(token);
        RegisteredUser ru = userRepo.getById(user);
        return ru.getProducts().stream()
                .map(pid -> {
                    Product p = prodRepo.getById(pid);
                    Store   s = storeRepo.getById(p.getStoreId());
                    return new Item(pid,p.getName(),s.getId(),s.getName());
                })
                .collect(Collectors.toList());
    }

    public void rateProduct(String productId,int rate)throws Exception{
        regSvc.rateProduct(token,productId,rate);
    }

    public void rateStore(String storeId,int rate)throws Exception{
        regSvc.rateStore(token,storeId,rate);
    }
}
