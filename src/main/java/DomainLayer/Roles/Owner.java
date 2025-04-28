package DomainLayer.Roles;

import DomainLayer.Store;
import DomainLayer.IStoreRepository;

import java.util.List;

public class Owner extends RegisteredUser {
    private String storeId;
    private boolean isFounder;
    private static IStoreRepository storeRepository;

    // Static method to set the store repository
    public static void setStoreRepository(IStoreRepository repository) {
        storeRepository = repository;
    }

    // Constructor for store founder
    public Owner(Store store) {
        super();
        this.storeId = store.getId();
        this.isFounder = true;
        // Register with store
        store.addOwner(this);
    }

    // Constructor for store founder with storeId
    public Owner(String storeId) {
        super();
        this.storeId = storeId;
        this.isFounder = true;
        // Register with store
        Store store = getStore();
        if (store != null) {
            store.addOwner(this);
        }
    }

    // Constructor for other owners
    public Owner(String name, Store store, Owner superior) {
        super(name);
        this.storeId = store.getId();
        this.isFounder = false;
        // Register with store
        store.addOwner(this, superior);
    }

    // Constructor for other owners with storeId
    public Owner(String name, String storeId, Owner superior) {
        super(name);
        this.storeId = storeId;
        this.isFounder = false;
        // Register with store
        Store store = getStore();
        if (store != null) {
            store.addOwner(this, superior);
        }
    }

    public Store getStore() {
        if (storeRepository == null) {
            return null;
        }
        return storeRepository.getStoreById(storeId);
    }

    public String getStoreId() {
        return storeId;
    }

    public boolean isFounder() {
        return isFounder;
    }

    public List<Owner> getSubordinateOwners() {
        Store store = getStore();
        if (store == null) {
            return List.of();
        }
        return store.getSubordinateOwners(this.getID());
    }

    public Owner getSuperior() {
        Store store = getStore();
        if (store == null) {
            return null;
        }
        return store.getSuperiorOwner(this.getID());
    }

    public boolean findSubordinate(Owner subordinateOwner) {
        Store store = getStore();
        if (store == null) {
            return false;
        }
        return store.isSubordinateOwner(this.getID(), subordinateOwner.getID());
    }

    public boolean findSubordinate(Object subordinateJob) {
        if (subordinateJob instanceof Owner) {
            return findSubordinate((Owner) subordinateJob);
        }
        return false;
    }

    public String getOrderHistory() {
        Store store = getStore();
        if (store == null) {
            return "";
        }
        return store.getOrderHistory();
    }

    public String answerCustomersQuery(String query) {
        return "answerCustomersQuery in class ownership not implemented yet";
    }

    public void closeStore() {
        List<Owner> subordinates = getSubordinateOwners();
        for (Owner owner : subordinates) {
            owner.alertStoreClosed();
            owner.closeStore();
        }
        this.getFired();
    }

    public void reOpenStore() {
        List<Owner> subordinates = getSubordinateOwners();
        for (Owner owner : subordinates) {
            owner.alertStoreReOpened();
            owner.reOpenStore();
        }
        this.reHire();
    }

    protected void alertStoreClosed() {
        StringBuilder sb = new StringBuilder();
        sb.append("Store: ").append(storeId).append(". closed\n");
        sendErrorMessage(sb.toString());
    }

    protected void alertStoreReOpened() {
        StringBuilder sb = new StringBuilder();
        sb.append("Store: ").append(storeId).append(". reopened\n");
        sendErrorMessage(sb.toString());
    }

    protected void getFired() {
        // Implementation for getting fired
    }

    protected void reHire() {
        // Implementation for rehiring
    }

    protected void sendErrorMessage(String message) {
        // Implementation for sending error message
        System.out.println("Error: " + message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ownership: \n");
        sb.append("User ID: ").append(this.getID()).append("\n");
        sb.append("Name: ").append(this.getName()).append("\n");
        if (isFounder) {
            sb.append("Founder: Yes\n");
        } else {
            Owner superior = getSuperior();
            if (superior != null) {
                sb.append("Superior ID: ").append(superior.getID()).append("\n");
            }
        }
        sb.append("Subordinate owners: \n");
        int i = 1;
        List<Owner> subordinates = getSubordinateOwners();
        for (Owner owner : subordinates) {
            sb.append(i).append(": ").append(owner.getID()).append(" - ").append(owner.getName()).append("\n");
            i++;
        }
        return sb.toString();
    }
}
