package DomainLayer.Roles;

import jakarta.persistence.*;
import java.util.*;

import DomainLayer.ShoppingCart;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@PrimaryKeyJoinColumn(name = "username")
@Table(name = "registered_users")
public class RegisteredUser extends Guest {

    @ElementCollection
    @CollectionTable(name = "user_answers", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "question")
    @Column(name = "answer")
    private Map<String, String> answers = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "owned_stores", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "store_id")
    private List<String> ownedStores = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "managed_stores", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "store_id")
    private List<String> managedStores = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_purchased_products", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "product_id")
    private List<String> products = new ArrayList<>();

    @Column(name = "hashed_password",nullable = false)
    private String hashedPassword;

    public RegisteredUser(String username, String hashedPassword) {
        super(username);
        this.hashedPassword = hashedPassword;
    }

    public RegisteredUser(){
        super();
    }

    public RegisteredUser register(String u , String p){
        throw new UnsupportedOperationException("allready registered.");
    }
    //===============getters===============
    public String getHashedPassword() {
        return hashedPassword;
    }
    public List<String> getOwnedStores() {
        return ownedStores;
    }
    public List<String> getManagedStores() {
        return managedStores;
    }
    public void addOwnedStore(String storeId) {
        ownedStores.add(storeId);
    }
    public void addManagedStore(String storeId) {
        managedStores.add(storeId);
    }
    public void addProduct(String productId) {
        products.add(productId);
    }
    public void removeStore(String storeId) {
        this.ownedStores.remove(storeId);
        this.managedStores.remove(storeId);
    }

    public void acceptQueryResponse(String s) {
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }

    public void setOwnedStores(List<String> ownedStores) {
        this.ownedStores = ownedStores;
    }
    public void setManagedStores(List<String> managedStores) {
        this.managedStores = managedStores;
    }

        public List<String> getProducts() {     // ← new
                return products;
            }

    public boolean isOwnerOf(String storeId) {
        return ownedStores.contains(storeId);
    }

    public boolean isManagerOf(String storeId) {
        return managedStores.contains(storeId);
    }

    /**
     * Promotes the user to an Owner for a store.
     * This overrides any previous role (e.g., manager).
     */
    public void promoteToOwner(String storeId) {
        removeRoleFromStore(storeId);
        ownedStores.add(storeId);
    }

    /**
     * Assigns the user as a Manager for a store.
     * This overrides any previous role (e.g., owner).
     */
    public void assignAsManager(String storeId) {
        removeRoleFromStore(storeId);
        managedStores.add(storeId);
    }

    /**
     * Removes the user from all roles for a given store.
     */
    public void removeRoleFromStore(String storeId) {
        ownedStores.remove(storeId);
        managedStores.remove(storeId);
    }

}