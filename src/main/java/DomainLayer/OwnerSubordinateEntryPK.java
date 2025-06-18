package DomainLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable // This means it can be embedded as an ID
public class OwnerSubordinateEntryPK implements Serializable {

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "owner_id")
    private String ownerId;

    // Default constructor required for JPA
    public OwnerSubordinateEntryPK() {}

    public OwnerSubordinateEntryPK(String storeId, String ownerId) {
        this.storeId = storeId;
        this.ownerId = ownerId;
    }

    // IMPORTANT: Implement equals() and hashCode() for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnerSubordinateEntryPK that = (OwnerSubordinateEntryPK) o;
        return Objects.equals(storeId, that.storeId) &&
                Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, ownerId);
    }

    // Getters
    public String getStoreId() { return storeId; }
    public String getOwnerId() { return ownerId; }

    // Setters (optional, but good practice for JPA)
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
}
