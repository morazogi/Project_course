package DomainLayer.Roles;
import DomainLayer.Roles.Jobs.Job;
import DomainLayer.Store;
import ServiceLayer.JobService;

import java.util.List;



public class SystemManager extends RegisteredUser {
    private String SystemManagerID;
    private JobService jobService;


    public SystemManager(String json) {
        super(json);
    }

    public SystemManager(List<Job> jobs, String name) {
        super(jobs, name);
    }
    public void closeStore(Store store){
        jobService.closeStore(store,this);
    }
}
