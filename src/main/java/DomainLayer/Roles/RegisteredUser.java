package DomainLayer.Roles;

import jakarta.persistence.*;
import java.util.*;

import DomainLayer.ShoppingCart;
import DomainLayer.User;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "registered_users")
public class RegisteredUser extends User {

    @Transient
    private ObjectMapper mapper = new ObjectMapper();

    @ElementCollection
    @CollectionTable(name = "user_answers", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "question")
    @Column(name = "answer")
    private Map<String, String> answers = new HashMap<>();

    @Column(name = "name", nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "owned_stores", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "store_id")
    private LinkedList<String> ownedStores = new LinkedList<>();

    @ElementCollection
    @CollectionTable(name = "managed_stores", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "store_id")
    private LinkedList<String> managedStores = new LinkedList<>();

    public RegisteredUser(String username) {
        super();
        this.name = username;
    }
    public RegisteredUser(){
        super();
    }

    public String getUsername() {
        return this.name;
    }


    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }


    public RegisteredUser register(String u , String p){
        throw new UnsupportedOperationException("allready registered.");
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public void addOwnedStore(String storeId) {
        ownedStores.add(storeId);
    }
    public void addManagedStore(String storeId) {
        managedStores.add(storeId);
    }
    public LinkedList<String> getOwnedStores() {
        return ownedStores;
    }
    public LinkedList<String> getManagedStores() {
        return managedStores;
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
}