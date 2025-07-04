package DomainLayer.DomainServices;
import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenStore {
    private IToken Tokener;
    private StoreRepository storeRepository;
    private UserRepository userRepository;
    private ObjectMapper mapper = new ObjectMapper();

    public OpenStore(IToken Tokener, StoreRepository storeRepository, UserRepository userRepository) {
        this.Tokener = Tokener;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    public String openStore(String token , String name) throws Exception {
        if (token == null) {
            throw new IllegalArgumentException("Invalid input");
        }
        Tokener.validateToken(token);
        String username = Tokener.extractUsername(token);
            RegisteredUser user = userRepository.getById(username);
            String userId = user.getShoppingCart().getUserId();
            if (userRepository.getById(username) == null) {
                throw new IllegalArgumentException("User does not exist");
            }
            Store store = new Store(username, name);
            store.setFounder(userId);
            storeRepository.save(store);
            user.addOwnedStore(store.getId());
            return store.getId();
    }
}
