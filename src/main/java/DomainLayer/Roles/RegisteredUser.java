package DomainLayer.Roles;

import java.util.*;
import DomainLayer.Roles.Jobs.Job;
import DomainLayer.Roles.Jobs.Managing;
import DomainLayer.Roles.Jobs.Ownership;
import DomainLayer.Store;
import DomainLayer.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class RegisteredUser extends User {

    ObjectMapper mapper = new ObjectMapper();

    private List<Job> jobs;

    private String name;

    public RegisteredUser(String json) {
        try {
            RegisteredUser temp = mapper.readValue(json , RegisteredUser.class);
            this.jobs = temp.jobs;
            this.id = temp.id;
            this.name = temp.name;
            this.shoppingCart = temp.shoppingCart;
            this.userService = temp.userService;
            this.myToken = temp.myToken;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public RegisteredUser() {
        // needed for Jackson
    }


    public void logout()  {
        try{
            userService.logoutRegistered(this.myToken, mapper.writeValueAsString(this));
            myToken=null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public RegisteredUser register(String u , String p){
        throw new UnsupportedOperationException("allready registered.");
    }


    public int getID() {
        return this.id;
    }
    public void addJob(Job job) {
        jobs.add(job);
    }
    public List<Job> getJobs() {
        return jobs;
    }
    public void createStore(String storeName){
        userService.createStore(storeName, this.id, this.myToken);
        this.jobs.add(new Ownership(storeName,this.id));
    }

    public boolean receivedOwnershipRequest(String request) {
        return false;
    }

    public String getName() {
        return this.name;
    }

    public void becomeNewOwnerRequest(String messageFromTheOwner, Job jobOffer, Ownership owner) {
        //print the string received
        boolean jobOfferAnswer = userService.becomeNewOwnerRequest(messageFromTheOwner);
        if(jobOfferAnswer){
            this.jobs.add(jobOffer);
            owner.jobOfferAccepted(jobOffer);
        }else{
            owner.jobOfferDeclined(jobOffer);
        }
    }


    public void setToken(String token) {
        myToken = token;
    public void becomeNewManagerRequest(String messageFromTheOwner, Managing jobOffer, Ownership owner) {
        //print the string received
        boolean jobOfferAnswer = userService.becomeNewManagerRequest(messageFromTheOwner);
        if(jobOfferAnswer){
            this.jobs.add(jobOffer);
            owner.jobOfferAccepted(jobOffer);
        }else{
            owner.jobOfferDeclined(jobOffer);
        }
    }
}
