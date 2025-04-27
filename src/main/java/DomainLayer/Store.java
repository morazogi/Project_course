package DomainLayer;
import DomainLayer.Roles.Manager;
import DomainLayer.Roles.Owner;
import ServiceLayer.PaymentService;
import ServiceLayer.ProductService;

import java.util.*;

public class Store {
    private String id;
    private PurchasePolicy purchasePolicy;
    private List<User> users = new ArrayList<>();
    private Map<Product, Integer> products = new HashMap<>();
    private List<Owner> owners = new ArrayList<>();
    private List<Manager> managers = new ArrayList<>();
    // Maps to track relationships between owners and managers
    private String founderID;
    private Map<String, String> ownerToSuperiorOwner = new HashMap<>(); // Maps owner ID to superior owner ID
    private Map<String, List<String>> ownerToSubordinateOwners = new HashMap<>(); // Maps owner ID to list of subordinate owner IDs
    private Map<String, String> managerToSuperiorOwner = new HashMap<>(); // Maps manager ID to superior owner ID
    private ProductService productService;
    private PaymentService paymentService;
    private boolean openNow;
    private int rating;

    public Store(String founderID) {
        this.id = "-1"; //currently doesnt have id as it gets one only when its added to the store repository
        openNow = true;
        this.founderID = founderID;
    }
    public boolean isOpenNow() {
        return openNow;
    }
    public void openTheStore() {
        openNow = true;
    }
    public void closeTheStore() {
        openNow = false;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;

    }
    public int getRating(){
        return rating;
    }
    public void setRating(int rating){
        this.rating = rating;
    }
    public String getName() {
        return id;
    }
    public Boolean registerUser(User user) {
        if(users.contains(user)) {
            return false;
        }
        users.add(user);
        return true;
    }
    public boolean increaseProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (!products.containsKey(product)) {
            return false;
        }

        int currentQuantity = products.get(product);
        products.put(product, Integer.valueOf(currentQuantity + quantity));
        return true;
    }
    public boolean decreaseProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (!products.containsKey(product)) {
            return false;
        }

        int currentQuantity = products.get(product);
        if (quantity > currentQuantity) {
            return false;
        }

        int updatedQuantity = currentQuantity - quantity;

        products.put(product, Integer.valueOf(updatedQuantity));

        productService.decreaseQuantity(product.getId(), quantity);
        return true;
    }
    public boolean changeProductQuantity(Product product, int newQuantity) {
        if (newQuantity < 0) {
            return false;
        }

        if (!products.containsKey(product)) {
            return false;
        }

        if (newQuantity == 0) {
            products.remove(product);
        } else {
            products.put(product, Integer.valueOf(newQuantity));
        }

        return true;
    }
    public boolean removeProduct(Product product) {
        if (!products.containsKey(product)) {
            return false;
        }

        products.remove(product);
        return true;
    }
    public boolean addNewProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (!products.containsKey(product) || products.get(product) == 0) {
            products.put(product, Integer.valueOf(quantity));
            return true;
        }

        return false; // Product already exists with quantity > 0
    }
    private Product findProductById(String productId) {
        for (Product product : products.keySet()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }
    public boolean increaseProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        int currentQuantity = products.get(product);
        products.put(product, Integer.valueOf(currentQuantity + quantity));
        return true;
    }
    public boolean decreaseProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        int currentQuantity = products.get(product);
        if (quantity > currentQuantity) {
            return false;
        }

        int updatedQuantity = currentQuantity - quantity;
        products.put(product, Integer.valueOf(updatedQuantity));

        productService.decreaseQuantity(product.getId(), quantity);
        return true;
    }
    public boolean changeProductQuantity(String productId, int newQuantity) {
        if (newQuantity < 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        if (newQuantity == 0) {
            products.remove(product);
        } else {
            products.put(product, Integer.valueOf(newQuantity));
        }

        return true;
    }
    public boolean removeProduct(String productId) {
        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        products.remove(product);
        return true;
    }
    public boolean addNewProduct(String productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Product product = findProductById(productId);
        if (product == null) {
            return false;
        }

        if (!products.containsKey(product) || products.get(product) == 0) {
            products.put(product, Integer.valueOf(quantity));
            return true;
        }

        return false;
    }
    public int calculateProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return -1;
        }
        if (!products.containsKey(product)) {
            return -1;
        }
        if (quantity > products.get(product)) {
            return -1;
        }

        return product.getPrice() * quantity;            //got to decide how price works
    }
    public int sellProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return -1;
        }
        if (!products.containsKey(product)) {
            return -1;
        }
        if (quantity > products.get(product)) {
            return -1;
        }

        int updatedQuantity = products.get(product) - quantity;
        if (updatedQuantity == 0) {
            products.remove(product);
        } else {
            products.put(product, Integer.valueOf(updatedQuantity));
        }
        productService.decreaseQuantity(product.getId(), quantity);       //Changed according productService implementation
        return product.getPrice() * quantity;            //got to decide how price works
    }
    public boolean availableProduct(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        return products.containsKey(product) && products.get(product) >= quantity;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nUsers:\n");
        for (User user : users) {
            sb.append(user.toString()).append("\n");
        }

        sb.append("\nAll Products in Store:\n");
        for (Product product : products.keySet()) {
            sb.append(product.toString()).append("\n");
        }

        return sb.toString();
    }
    public String getOrderHistory() {
        //returns an order history
        return "";
    }
    // Methods for managing owners
    public void addOwner(Owner owner) {
        if (!owners.contains(owner)) {
            owners.add(owner);
            registerUser(owner);

            // Initialize relationship data structures for this owner
            if (!ownerToSubordinateOwners.containsKey(owner.getID())) {
                ownerToSubordinateOwners.put(owner.getID(), new ArrayList<>());
            }
        }
    }
    // Add an owner with a superior owner
    public void addOwner(Owner owner, Owner superior) {
        if (!owners.contains(owner)) {
            owners.add(owner);
            registerUser(owner);

            // Initialize relationship data structures for this owner
            if (!ownerToSubordinateOwners.containsKey(owner.getID())) {
                ownerToSubordinateOwners.put(owner.getID(), new ArrayList<>());
            }

            // Set superior owner
            ownerToSuperiorOwner.put(owner.getID(), superior.getID());

            // Add this owner to the superior's subordinates
            List<String> subordinates = ownerToSubordinateOwners.get(superior.getID());
            if (subordinates == null) {
                subordinates = new ArrayList<>();
                ownerToSubordinateOwners.put(superior.getID(), subordinates);
            }
            subordinates.add(owner.getID());
        }
    }
    public void removeOwner(Owner owner) {
        owners.remove(owner);

        // Remove from relationship data structures
        String ownerId = owner.getID();

        // Remove from superior's subordinates list
        String superiorId = ownerToSuperiorOwner.get(ownerId);
        if (superiorId != null) {
            List<String> subordinates = ownerToSubordinateOwners.get(superiorId);
            if (subordinates != null) {
                subordinates.remove(ownerId);
            }
        }

        // Remove all subordinates
        List<String> subordinateIds = ownerToSubordinateOwners.get(ownerId);
        if (subordinateIds != null) {
            for (String subordinateId : new ArrayList<>(subordinateIds)) {
                Owner subordinate = findOwnerById(subordinateId);
                if (subordinate != null) {
                    removeOwner(subordinate);
                }
            }
        }

        // Remove from maps
        ownerToSuperiorOwner.remove(ownerId);
        ownerToSubordinateOwners.remove(ownerId);
    }
    public List<Owner> getOwners() {
        return owners;
    }
    public boolean isOwner(Owner owner) {
        return owners.contains(owner);
    }
    public boolean isOwner(String userId) {
        for (Owner owner : owners) {
            if (owner.getID().equals(userId)) {
                return true;
            }
        }
        return false;
    }
    // New methods for owner relationships
    public boolean isFounder(String ownerId) {
        return ownerId==founderID;
    }
    public String getSuperiorOwnerId(String ownerId) {
        return ownerToSuperiorOwner.get(ownerId);
    }
    public Owner getSuperiorOwner(String ownerId) {
        String superiorId = getSuperiorOwnerId(ownerId);
        if (superiorId != null) {
            return findOwnerById(superiorId);
        }
        return null;
    }
    public List<String> getSubordinateOwnerIds(String ownerId) {
        List<String> subordinates = ownerToSubordinateOwners.get(ownerId);
        return subordinates != null ? new ArrayList<>(subordinates) : new ArrayList<>();
    }
    public List<Owner> getSubordinateOwners(String ownerId) {
        List<String> subordinateIds = getSubordinateOwnerIds(ownerId);
        List<Owner> result = new ArrayList<>();
        for (String id : subordinateIds) {
            Owner owner = findOwnerById(id);
            if (owner != null) {
                result.add(owner);
            }
        }
        return result;
    }
    public boolean isSubordinateOwner(String ownerId, String potentialSubordinateId) {
        List<String> directSubordinates = getSubordinateOwnerIds(ownerId);
        if (directSubordinates.contains(potentialSubordinateId)) {
            return true;
        }

        // Check recursively
        for (String subordinateId : directSubordinates) {
            if (isSubordinateOwner(subordinateId, potentialSubordinateId)) {
                return true;
            }
        }

        return false;
    }
    private Owner findOwnerById(String ownerId) {
        for (Owner owner : owners) {
            if (owner.getID().equals(ownerId)) {
                return owner;
            }
        }
        return null;
    }
    // Methods for managing managers
    public void addManager(Manager manager) {
        if (!managers.contains(manager)) {
            managers.add(manager);
            registerUser(manager);

            // If the manager has a superior owner, record the relationship
            Owner superior = manager.getSuperior();
            if (superior != null) {
                managerToSuperiorOwner.put(manager.getID(), superior.getID());
            }
        }
    }
    // Add a manager with a superior owner
    public void addManager(Manager manager, Owner superior) {
        if (!managers.contains(manager)) {
            managers.add(manager);
            registerUser(manager);

            // Record the relationship
            managerToSuperiorOwner.put(manager.getID(), superior.getID());
        }
    }
    public void removeManager(Manager manager) {
        managers.remove(manager);

        // Remove from relationship data structures
        managerToSuperiorOwner.remove(manager.getID());
    }
    public List<Manager> getManagers() {
        return managers;
    }
    public boolean isManager(Manager manager) {
        return managers.contains(manager);
    }
    public boolean isManager(String userId) {
        for (Manager manager : managers) {
            if (manager.getID().equals(userId)) {
                return true;
            }
        }
        return false;
    }
    // New methods for manager relationships
    public String getManagerSuperiorOwnerId(String managerId) {
        return managerToSuperiorOwner.get(managerId);
    }
    public Owner getManagerSuperiorOwner(String managerId) {
        String superiorId = getManagerSuperiorOwnerId(managerId);
        if (superiorId != null) {
            return findOwnerById(superiorId);
        }
        return null;
    }
    public List<Manager> getManagersAppointedBy(String ownerId) {
        List<Manager> result = new ArrayList<>();
        for (Manager manager : managers) {
            String superiorId = managerToSuperiorOwner.get(manager.getID());
            if (superiorId != null && superiorId.equals(ownerId)) {
                result.add(manager);
            }
        }
        return result;
    }
    private Manager findManagerById(String managerId) {
        for (Manager manager : managers) {
            if (manager.getID().equals(managerId)) {
                return manager;
            }
        }
        return null;
    }
}
