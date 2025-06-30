package DomainLayer;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_owner_subordinates")
public class OwnerSubordinateEntry implements Serializable {

    @EmbeddedId
    private OwnerSubordinateEntryPK id;

    // Bidirectional ManyToOne to Store
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("storeId")
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ElementCollection
    @CollectionTable(
            name = "subordinate_ids_list",
            joinColumns = {
                    @JoinColumn(name = "store_id", referencedColumnName = "store_id"),
                    @JoinColumn(name = "owner_id", referencedColumnName = "owner_id")
            }
    )
    @OrderColumn(name = "subordinate_index")
    @Column(name = "subordinate_id")
    private List<String> subordinates = new ArrayList<>();

    public OwnerSubordinateEntry() {}

    public OwnerSubordinateEntry(String storeId, String ownerId, Store store, List<String> subordinates) {
        this.id = new OwnerSubordinateEntryPK(storeId, ownerId);
        this.store = store;
        this.subordinates = subordinates != null ? new ArrayList<>(subordinates) : new ArrayList<>();
    }

    // Getters / Setters

    public OwnerSubordinateEntryPK getId() {
        return id;
    }

    public void setId(OwnerSubordinateEntryPK id) {
        this.id = id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
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

    public String getStoreId() {
        return id != null ? id.getStoreId() : null;
    }

    public String getOwnerId() {
        return id != null ? id.getOwnerId() : null;
    }
}