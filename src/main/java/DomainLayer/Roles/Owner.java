package DomainLayer.Roles;

import DomainLayer.Store;

import java.util.List;

public class Owner extends RegisteredUser {
    private Store store;
    private boolean isFounder;

    // Constructor for store founder
    public Owner(Store store) {
        super();
        this.store = store;
        this.isFounder = true;
        // Register with store
        store.addOwner(this);
    }

    // Constructor for other owners
    public Owner(String name, Store store, Owner superior) {
        super(name);
        this.store = store;
        this.isFounder = false;
        // Register with store
        store.addOwner(this, superior);
    }

    public Store getStore() {
        return store;
    }

    public boolean isFounder() {
        return isFounder;
    }

    public List<Owner> getSubordinateOwners() {
        return store.getSubordinateOwners(this.getID());
    }

    public Owner getSuperior() {
        return store.getSuperiorOwner(this.getID());
    }

    public boolean findSubordinate(Owner subordinateOwner) {
        return store.isSubordinateOwner(this.getID(), subordinateOwner.getID());
    }

    public boolean findSubordinate(Object subordinateJob) {
        if (subordinateJob instanceof Owner) {
            return findSubordinate((Owner) subordinateJob);
        }
        return false;
    }

    public String getOrderHistory() {
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
        sb.append("Store: ").append(store.getId()).append(". closed\n");
        sendErrorMessage(sb.toString());
    }

    protected void alertStoreReOpened() {
        StringBuilder sb = new StringBuilder();
        sb.append("Store: ").append(store.getId()).append(". reopened\n");
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
