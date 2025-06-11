package DomainLayer.Roles;

import jakarta.persistence.*;
import java.util.*;

import DomainLayer.ShoppingCart;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "registered_users")
public class RegisteredUser extends Guest {

    @ElementCollection
    @CollectionTable(name = "user_answers", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "question")
    @Column(name = "answer")
    private Map<String, String> answers = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "owned_stores", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "store_id")
    private LinkedList<String> ownedStores = new LinkedList<>();

    @ElementCollection
    @CollectionTable(name = "managed_stores", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "store_id")
    private LinkedList<String> managedStores = new LinkedList<>();

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
    public LinkedList<String> getOwnedStores() {
        return ownedStores;
    }
    public LinkedList<String> getManagedStores() {
        return managedStores;
    }
    public void addOwnedStore(String storeId) {
        ownedStores.add(storeId);
    }
    public void addManagedStore(String storeId) {
        managedStores.add(storeId);
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

    public void setOwnedStores(LinkedList<String> ownedStores) {
        this.ownedStores = ownedStores;
    }
    public void setManagedStores(LinkedList<String> managedStores) {
        this.managedStores = managedStores;
    }

}