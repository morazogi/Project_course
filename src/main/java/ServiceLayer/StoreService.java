package ServiceLayer;
import DomainLayer.IStoreRepository;
import DomainLayer.Store;


import java.util.*;

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

    public Store createStore(String founderID){
        Store store = new Store(founderID);
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

    public void closeStore(Store store) {
        store.closeTheStore();
    }
}