package DomainLayer.Roles;

import java.util.*;
import DomainLayer.ShoppingCart;
import DomainLayer.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RegisteredUser extends User {

    ObjectMapper mapper = new ObjectMapper();
    private Map<String, String> answers = new HashMap<>();
    private String name;
    private LinkedList<String> ownedStores = new LinkedList<String>();
    private LinkedList<String> managedStores = new LinkedList<String>();



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

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
}