package InfrastructureLayer;
import DomainLayer.IStoreRepository;
import DomainLayer.Product;
import DomainLayer.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StoreRepository implements IStoreRepository {
    private Map<String, String> stores = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void addStore(String storeId , String storeJson) {
        stores.put(storeId, storeJson);
    }


    public void removeStore(String storeId) {
        stores.remove(storeId);
    }


    public String getStore(String storeId) {
        return stores.get(storeId);
    }



    public void updateStore(String storeId, String storeJson) {
        if(stores.containsKey(storeId)) {
            stores.put(storeId, storeJson);
        } else {
            throw new IllegalArgumentException("Store does not exist");
        }
    }

    public Map<String, String> getStores() {
        return stores;
    }

    public List<String> findAll() {return stores.values().stream().toList();}

    public String getStoreByName(String storeName) {
        try {
            for (String storeId : stores.keySet()) {
                if (mapper.readValue(stores.get(storeId), Store.class).getName().equals(storeName)) {
                    return stores.get(storeId);
                }
            }
        } catch (Exception e) {
            return "This should not happen";
        }
        return null;
    }

}