package InfrastructureLayer;

import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

import java.util.*;

@Repository
public class GuestRepository implements IRepo<Guest> {

    private final HashMap<String, Guest> repo = new HashMap<>();

    public Guest save(Guest guest) {
        repo.put(guest.getUsername(), guest);
        return guest;
    }
    public Guest update(Guest guest) {
        if (!repo.containsKey(guest.getUsername()))
            throw new IllegalArgumentException("guest does not exist - thrown in GuestRepository");

        repo.put(guest.getUsername(), guest);   // overwrite
        return guest;                           // ← return the updated object
    }
    public Guest getById(String id) {
        if (!repo.containsKey(id)) {
            throw new IllegalArgumentException("guest does not exist - thrown in GuestRepository");
        }
        return repo.get(id);
    }
    public List<Guest> getAll() {
        return new ArrayList<>(repo.values());
    }
    public void deleteById(String userId) {
        repo.remove(userId);
    }
    public void delete(Guest guest) {
        repo.values().removeIf(g -> g.equals(guest));
    }
    public boolean existsById(String id) {
        return repo.containsKey(id);
    }
    // Optional method, not in base IRepo — get Guest by name
    public Guest getByName(String name) {
        return this.getById(name);
    }
}
