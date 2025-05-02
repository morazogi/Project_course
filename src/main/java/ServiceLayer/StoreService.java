package ServiceLayer;
import DomainLayer.IStoreRepository;
import DomainLayer.Store;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class StoreService{
    IStoreRepository StoreRepository;
    ProductService productService;
    private String id = "1";


    public StoreService(IStoreRepository StoreRepository, ProductService productService) {
        this.StoreRepository = StoreRepository;
        this.productService = productService;
    }

    public void addStore(Store store){
        store.setId(id);
        int numericId = Integer.parseInt(id);
        numericId++;
        id = String.valueOf(numericId);
        StoreRepository.addStore(store);
    }

    public Store createStore(){
        Store store = new Store();
        store.setId(id);
        int numericId = Integer.parseInt(id);
        numericId++;
        id = String.valueOf(numericId);
        StoreRepository.addStore(store);
        return store;
    }

    public void removeStore(Store store){
        StoreRepository.removeStore(store);
    }

    public void setRating(Store store, int rating){
        store.setRating(rating);
    }

    public Store getStoreById(String id) {StoreRepository.getStore(id);}

}