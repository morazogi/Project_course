package InfrastructureLayer;
import DomainLayer.IStoreRepository;
import DomainLayer.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StoreRepository implements IRepo<Store>{

    @Autowired
    IStoreRepository repo;

    public Store save(Store store) {
        return repo.save(store);
    }
    public Store update(Store store) {
        return repo.saveAndFlush(store);
    }
    public Store getById(String id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }
    public List<Store> getAll() {
        return repo.findAll();
    }
    public void deleteById(String storeID) {
        repo.deleteById(storeID);
    }
    public void delete(Store store){
        repo.delete(store);
    }
    public boolean existsById(String id){
        return repo.existsById(id);
    }


    //---------------specific class functions:-----------------
    public Store getStoreByName(String name) {
        return repo.findByNameContaining(name);
    }

}