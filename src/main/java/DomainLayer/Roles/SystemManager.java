package DomainLayer.Roles;

import DomainLayer.Store;
import DomainLayer.User;

public class SystemManager extends User {
    private String systemManagerID;
    public SystemManager(String name) {
        super(name);
        this.systemManagerID = this.getID(); // Using the user ID as the system manager ID
    }

    public String getSystemManagerID() {
        return systemManagerID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SystemManager: \n");
        sb.append("User ID: ").append(this.getID()).append("\n");
        sb.append("Name: ").append(this.getName()).append("\n");
        sb.append("System Manager ID: ").append(this.systemManagerID).append("\n");
        return sb.toString();
    }
}
