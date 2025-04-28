package infrastructureLayer;
import DomainLayer.IStoreRepository;
import DomainLayer.Product;
import DomainLayer.Store;

import java.util.HashMap;
import java.util.Map;

public class StoreRepository implements IStoreRepository {
    // Changed to Map<String, Store> for more efficient lookups by ID
    private Map<String, Store> stores = new HashMap<>();

    public void addStore(Store store) {
        // Simplified check since we're using store ID as the key
        if (!stores.containsKey(store.getId())) {
            stores.put(store.getId(), store);
        }
    }

    public void removeStore(Store store) {
        // Simplified removal since we're using store ID as the key
        stores.remove(store.getId());
    }

    @Override
    public Store getStoreById(String storeId) {
        return stores.get(storeId);
    }
}
