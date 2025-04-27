package DomainLayer.Roles;
import DomainLayer.Product;
import DomainLayer.Store;
import DomainLayer.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisteredUser extends User {

    ObjectMapper mapper = new ObjectMapper();
    protected List<Object> myJobs = new ArrayList<>();
    protected List<String> ownedStores = new ArrayList<>();
    protected List<String> managedStores = new ArrayList<>();
    protected Map<String, List<String>> storePermissions = new HashMap<>();
    protected List<Map<String, Object>> purchaseHistory = new ArrayList<>();

    // Constructor for JSON deserialization
    public RegisteredUser(String json, boolean isJson) {
        try {
            RegisteredUser temp = mapper.readValue(json, RegisteredUser.class);
            this.id = temp.id;
            this.name = temp.name;
            this.shoppingCart = temp.shoppingCart;
            this.myToken = temp.myToken;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Constructor for creating a new RegisteredUser with a name
    public RegisteredUser(String name) {
        super(name);
    }

    public RegisteredUser() {
        super();
    }

    // Functions for registered users only

    // Override exitMarket to save shopping cart
    @Override
    public void exitMarket() {
        // For registered users, the shopping cart is saved
        // No need to do anything special here
    }

    public RegisteredUser register(String u, String p) {
        throw new UnsupportedOperationException("already registered.");
    }

    // Open a store
    public Store openStore(String storeName) {
        // Implementation to open a new store
        Store newStore = new Store(storeName);
        ownedStores.add(newStore.getId());
        return newStore;
    }

    // Write review for a purchased product
    public boolean writeReview(Product product, String review, int rating) {
        // Implementation to write a review for a purchased product
        // Check if the user has purchased the product
        return false; // Placeholder
    }

    // Rate a product
    public boolean rateProduct(Product product, int rating) {
        // Implementation to rate a product
        // Check if the user has purchased the product
        return false; // Placeholder
    }

    // Rate a store
    public boolean rateStore(Store store, int rating) {
        // Implementation to rate a store
        // Check if the user has purchased from the store
        return false; // Placeholder
    }

    // Send question/inquiry to a store
    public boolean sendInquiryToStore(Store store, String inquiry) {
        // Implementation to send an inquiry to a store
        return false; // Placeholder
    }

    // Submit complaint to system managers
    public boolean submitComplaint(String complaint) {
        // Implementation to submit a complaint to system managers
        return false; // Placeholder
    }

    // View purchase history
    public List<Map<String, Object>> viewPurchaseHistory() {
        // Implementation to view purchase history
        return purchaseHistory;
    }

    // View and update personal information
    public Map<String, Object> viewPersonalInfo() {
        // Implementation to view personal information
        Map<String, Object> info = new HashMap<>();
        info.put("id", id);
        info.put("name", name);
        return info;
    }

    // Update personal information
    public boolean updatePersonalInfo(Map<String, Object> newInfo) {
        // Implementation to update personal information
        if (newInfo.containsKey("name")) {
            this.name = (String) newInfo.get("name");
        }
        return true; // Placeholder
    }

    // Submit purchase offer (bid) for a product
    public boolean submitBid(Store store, Product product, double price) {
        // Implementation to submit a bid for a product
        return false; // Placeholder
    }

    // Purchase product in public auction
    public boolean purchaseInAuction(Store store, Product product, double price) {
        // Implementation to purchase a product in a public auction
        return false; // Placeholder
    }

    // Purchase product in raffle
    public boolean purchaseInRaffle(Store store, Product product, double amount) {
        // Implementation to purchase a product in a raffle
        return false; // Placeholder
    }

    // Store owner functions

    // Manage store inventory (as store owner)
    public boolean addProductToStore(Store store, Product product) {
        // Implementation to add a product to a store
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.addProduct(product)
            return true;
        }
        return false; // Placeholder
    }

    public boolean removeProductFromStore(Store store, Product product) {
        // Implementation to remove a product from a store
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.removeProduct(product)
            return true;
        }
        return false; // Placeholder
    }

    public boolean updateProductInStore(Store store, Product product, Map<String, Object> updates) {
        // Implementation to update a product in a store
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.updateProduct(product, updates)
            return true;
        }
        return false; // Placeholder
    }

    // Change purchase and discount policies (as store owner)
    public boolean changePurchasePolicy(Store store, Map<String, Object> policy) {
        // Implementation to change purchase policy
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.setPurchasePolicy(policy)
            return true;
        }
        return false; // Placeholder
    }

    public boolean changeDiscountPolicy(Store store, Map<String, Object> policy) {
        // Implementation to change discount policy
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.setDiscountPolicy(policy)
            return true;
        }
        return false; // Placeholder
    }

    // Appoint store owner (as store owner)
    public boolean appointStoreOwner(Store store, RegisteredUser user) {
        // Implementation to appoint a store owner
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            return user.receivedOwnershipRequest("You have been appointed as an owner of " + store.getName());
        }
        return false; // Placeholder
    }

    public boolean receivedOwnershipRequest(String request) {
        //some logic for how to show the user that he received an ownership request
        return returnOwnershipRequestAnswer();
    }

    public boolean returnOwnershipRequestAnswer() {
        return false;
    }

    // Remove store owner appointment (as store owner)
    public boolean removeStoreOwner(Store store, RegisteredUser user) {
        // Implementation to remove a store owner
        // Check if the user is the owner of the store and appointed the other user
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.removeOwner(user)
            return true;
        }
        return false; // Placeholder
    }

    // Resign from store ownership (as store owner)
    public boolean resignFromStoreOwnership(Store store) {
        // Implementation to resign from store ownership
        // Check if the user is the owner of the store but not the founder
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would check if the user is not the founder
            // and then call store.removeOwner(this)
            ownedStores.remove(store);
            return true;
        }
        return false; // Placeholder
    }

    // Appoint store manager (as store owner)
    public boolean appointStoreManager(Store store, RegisteredUser user, List<String> permissions) {
        // Implementation to appoint a store manager
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            return user.receivedManagingRequest("You have been appointed as a manager of " + store.getName());
        }
        return false; // Placeholder
    }

    public boolean receivedManagingRequest(String request) {
        //some logic for how to show the user that he received a managing request
        return returnManagingRequestAnswer();
    }

    private boolean returnManagingRequestAnswer() {
        return false;
    }

    // Change store manager permissions (as store owner)
    public boolean changeStoreManagerPermissions(Store store, RegisteredUser manager, List<String> permissions) {
        // Implementation to change store manager permissions
        // Check if the user is the owner of the store and appointed the manager
        if (ownedStores.contains(store)) {
            storePermissions.put(store.getId() + "_" + manager.getID(), permissions);
            return true;
        }
        return false; // Placeholder
    }

    // Remove store manager appointment (as store owner)
    public boolean removeStoreManager(Store store, RegisteredUser manager) {
        // Implementation to remove a store manager
        // Check if the user is the owner of the store and appointed the manager
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.removeManager(manager)
            return true;
        }
        return false; // Placeholder
    }

    // Close a store (as store founder)
    public boolean closeStore(Store store) {
        // Implementation to close a store
        // Check if the user is the founder of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would check if the user is the founder
            // and then call store.close()
            return true;
        }
        return false; // Placeholder
    }

    // Reopen a closed store (as store founder)
    public boolean reopenStore(Store store) {
        // Implementation to reopen a closed store
        // Check if the user is the founder of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would check if the user is the founder
            // and then call store.reopen()
            return true;
        }
        return false; // Placeholder
    }

    // Get information about store roles (as store owner)
    public Map<String, Object> getStoreRoles(Store store) {
        // Implementation to get information about store roles
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.getRoles()
            return new HashMap<>();
        }
        return new HashMap<>(); // Placeholder
    }

    // Receive and respond to user inquiries (as store owner)
    public void acceptQueryResponse(String s) {
        // Implementation for accepting query response
    }

    // View store purchase history (as store owner)
    public List<Map<String, Object>> viewStorePurchaseHistory(Store store) {
        // Implementation to view store purchase history
        // Check if the user is the owner of the store
        if (ownedStores.contains(store)) {
            // Placeholder: In a real implementation, this would call store.getPurchaseHistory()
            return new ArrayList<>();
        }
        return new ArrayList<>(); // Placeholder
    }

    // Store manager functions

    // Perform management actions based on permissions (as store manager)
    public boolean performManagementAction(Store store, String action, Object... params) {
        // Implementation to perform management actions based on permissions
        // Check if the user is a manager of the store and has the required permission
        if (managedStores.contains(store)) {
            List<String> permissions = storePermissions.get(store.getId() + "_" + this.getID());
            if (permissions != null && permissions.contains(action)) {
                // Perform the action
                return true;
            }
        }
        return false; // Placeholder
    }

    // System manager functions

    // Close a store (as system manager)
    public boolean closeStoreAsSystemManager(Store store) {
        // Implementation to close a store as system manager
        // Check if the user is a system manager
        return false; // Placeholder
    }

    // Remove user membership (as system manager)
    public boolean removeUserMembership(RegisteredUser user) {
        // Implementation to remove user membership
        // Check if the user is a system manager
        return false; // Placeholder
    }

    // Receive and respond to user complaints (as system manager)
    public boolean respondToComplaint(String complaintId, String response) {
        // Implementation to respond to user complaints
        // Check if the user is a system manager
        return false; // Placeholder
    }

    // View system-wide purchase history (as system manager)
    public List<Map<String, Object>> viewSystemPurchaseHistory() {
        // Implementation to view system-wide purchase history
        // Check if the user is a system manager
        return new ArrayList<>(); // Placeholder
    }

    // View system performance metrics (as system manager)
    public Map<String, Object> viewSystemPerformanceMetrics() {
        // Implementation to view system performance metrics
        // Check if the user is a system manager
        return new HashMap<>(); // Placeholder
    }

    // Job management
    public void addJob(Object job) {
        myJobs.add(job);
    }

    public void removeJob(Object job) {
        myJobs.remove(job);
    }
}
