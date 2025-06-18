package DomainLayer;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// This will be a standalone entity that represents one owner's subordinates in a store
@Entity
@Table(name = "store_owner_subordinates") // New table for this
public class OwnerSubordinateEntry implements Serializable {

    // Composite Primary Key for this entity
    @EmbeddedId // This indicates a composite primary key
    private OwnerSubordinateEntryPK id; // Referencing the PK class below

    // This field will be managed by the composite ID, so no @Column directly on ownerId or storeId here.
    // If you need direct access, use the getters from 'id'.

    // The list of subordinates for THIS specific owner.
    // This will be its own join table linked to this OwnerSubordinateEntry entity.
    @ElementCollection
    @CollectionTable(
            name = "subordinate_ids_list", // A separate table for the list of IDs
            joinColumns = {
                    @JoinColumn(name = "store_id", referencedColumnName = "store_id"), // Part of the compound PK
                    @JoinColumn(name = "owner_id", referencedColumnName = "owner_id")   // Part of the compound PK
            }
    )
    @OrderColumn(name = "subordinate_index") // Optional: maintains order
    @Column(name = "subordinate_id") // Column for the actual subordinate ID string
    private List<String> subordinates = new ArrayList<>();

    // Default constructor for JPA
    public OwnerSubordinateEntry() {}

    // Constructor for creating new entries
    public OwnerSubordinateEntry(String storeId, String ownerId, List<String> subordinates) {
        this.id = new OwnerSubordinateEntryPK(storeId, ownerId);
        this.subordinates = subordinates != null ? new ArrayList<>(subordinates) : new ArrayList<>();
    }

    // Getters and Setters
    public OwnerSubordinateEntryPK getId() {
        return id;
    }

    public void setId(OwnerSubordinateEntryPK id) {
        this.id = id;
    }

    public List<String> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(List<String> subordinates) {
        this.subordinates = subordinates != null ? new ArrayList<>(subordinates) : new ArrayList<>();
    }

    public void addSubordinate(String newSubordinate) {
        if (newSubordinate != null && !this.subordinates.contains(newSubordinate)) {
            this.subordinates.add(newSubordinate);
        }
    }

    public void removeSubordinate(String subordinateId) {
        this.subordinates.remove(subordinateId);
    }

    // You might want to add convenience methods to get storeId and ownerId from the composite ID
    public String getStoreId() { return this.id != null ? this.id.getStoreId() : null; }
    public String getOwnerId() { return this.id != null ? this.id.getOwnerId() : null; }
}
