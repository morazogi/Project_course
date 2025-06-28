package utils;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notifications {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();
    @Column(name = "message", nullable = false)
    private String message;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "store_id", nullable = false)
    private String storeId;

    public Notifications() {
    }

    public Notifications(String message, String userId, String storeId) {
        this.message = message;
        this.userId = userId;
        this.storeId = storeId;
    }
    //============getters===========
    public String getId() {
        return id;
    }
    public String getMessage() {
        return message;
    }
    public String getUserId() {
        return userId;
    }
    public String getStoreId() {
        return storeId;
    }
    //============setters===========
    public void setId(String id) {
        this.id = id;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
