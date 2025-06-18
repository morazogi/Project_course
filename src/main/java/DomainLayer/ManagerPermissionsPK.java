package DomainLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable // Mark this as embeddable, for use as an @EmbeddedId
public class ManagerPermissionsPK implements Serializable {

    @Column(name = "manager_id") // Explicit column name
    private String managerId;

    @Column(name = "store_id") // Explicit column name
    private String storeId;

    // Default constructor required by JPA
    public ManagerPermissionsPK() {}

    // Constructor for easy initialization
    public ManagerPermissionsPK(String managerId, String storeId) {
        this.managerId = managerId;
        this.storeId = storeId;
    }

    // IMPORTANT: Implement equals() and hashCode() for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerPermissionsPK that = (ManagerPermissionsPK) o;
        return Objects.equals(managerId, that.managerId) &&
                Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(managerId, storeId);
    }

    // Getters (and optionally setters)
    public String getManagerId() { return managerId; }
    public String getStoreId() { return storeId; }
    // You can add setters if needed, but often not for PK parts
}
