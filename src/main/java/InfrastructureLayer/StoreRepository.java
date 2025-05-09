package InfrastructureLayer;
import DomainLayer.IStoreRepository;
import DomainLayer.Store;

import java.util.HashMap;
import java.util.Map;

public class StoreRepository implements IStoreRepository {
    private Map<Store, String> Stores = new HashMap<>();

    public void addStore(Store store) {
        for (Store existingStore : Stores.keySet()) {
            if (existingStore.getId().equals(store.getId())) {
                return;
            }
        }
        Stores.put(store, store.getId());
    }


    public void removeStore(Store store) {
        Store storeToRemove = null;
        for (Store existingStore : Stores.keySet()) {
            if (existingStore.getId().equals(store.getId())) {
                storeToRemove = existingStore;
                break;
            }
        }

        if (storeToRemove != null) {
            Stores.remove(storeToRemove);
        }
    }


    public Store getStore(String storeId) {
        for (Store store : Stores.keySet()) {
            if (store.getId().equals(storeId)) {
                return store;
            }
        }
        return null;
    }
    public void updateStore(String storeId, String storeJson) {
        for (Store store : Stores.keySet()) {
            if (store.getId().equals(storeId)) {
                Stores.put(store, storeJson);
                return;
            }
        }
    }
    public Map<Store, String> getStores() {
        return Stores;
    }
}
