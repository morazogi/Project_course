package InfrastructureLayer;

import DomainLayer.IJobRepository;
import DomainLayer.Roles.Jobs.Job;

import java.util.HashMap;
import java.util.LinkedList;

public class JobRepository implements IJobRepository {
    /**user to jobs list connection*/
    HashMap<String, LinkedList<Job>> userToJobRep = new HashMap<>();
    /**store-to-jobs list connection*/
    HashMap<String,LinkedList<Job>> storeToJobRep = new HashMap<>();

    public LinkedList<Job> getJobsByUser(String userID) {
        return userToJobRep.get(userID);
    }
    public LinkedList<Job> getJobsByStore(String storeID) {
        return storeToJobRep.get(storeID);
    }
    public boolean userHasJob(String RegisteredUserID, Job job){
        return userToJobRep.get(RegisteredUserID).contains(job);
    }
    public boolean storeHaveJob(String storeID, Job job){
        return storeToJobRep.get(storeID).contains(job);
    }
    public void removeJob(String RegisteredUserID, String storeID, Job job){
        userToJobRep.get(RegisteredUserID).remove(job);
        storeToJobRep.get(storeID).remove(job);
    }
    public void addJob(String RegisteredUserID, String storeID, Job job){
        userToJobRep.get(RegisteredUserID).push(job);
        storeToJobRep.get(storeID).push(job);

    }
}
