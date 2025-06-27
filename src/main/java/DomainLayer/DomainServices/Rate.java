package DomainLayer.DomainServices;

import com.fasterxml.jackson.databind.ObjectMapper;
import InfrastructureLayer.*;
import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Store;
import DomainLayer.Roles.RegisteredUser;

public class Rate {

    private final IToken Tokener;
    private final ObjectMapper mapper = new ObjectMapper();
    private final StoreRepository   storeRepository;
    private final UserRepository    userRepository;
    private final ProductRepository productRepository;

    public Rate(IToken Tokener,
                StoreRepository   storeRepository,
                UserRepository    userRepository,
                ProductRepository productRepository) {

        this.Tokener          = Tokener;
        this.storeRepository  = storeRepository;
        this.userRepository   = userRepository;
        this.productRepository= productRepository;
    }

    /* ─────────────────────────────────────────────────────────── */
    public boolean rateStore(String token, String storeId, int rate)
            throws Exception {

        if (token == null || storeId == null || rate < 1 || rate > 5)
            throw new IllegalArgumentException("Invalid input");

        Tokener.validateToken(token);
        String username = Tokener.extractUsername(token);

        Store store = storeRepository.getById(storeId);
        if (store == null)
            throw new IllegalArgumentException("Store does not exist");

        if (userRepository.getById(username) == null)
            throw new IllegalArgumentException("User does not exist");

        if (store.rate(rate)) {
            storeRepository.update(store);
            return true;
        }
        throw new IllegalArgumentException("invalid rate");
    }

    /* ─────────────────────────────────────────────────────────── */
    public boolean rateProduct(String token, String productId, double rate) {

        if (token == null || productId == null || rate < 1 || rate > 5)
            throw new IllegalArgumentException("Invalid input");

        Tokener.validateToken(token);
        String username = Tokener.extractUsername(token);

        Product product = productRepository.getById(productId);
        if (product == null)
            throw new IllegalArgumentException("Product does not exist");

        RegisteredUser user =
                (RegisteredUser) userRepository.getById(username);
        if (user == null)
            throw new IllegalArgumentException("User does not exist");

        /*-----------------------------------------------------------
          Allow a rating only once per purchase:
          – product must currently be in the user’s “products” list
          – after rating we remove it so it can’t be rated again
            unless bought again later.
        -----------------------------------------------------------*/
        if (!user.getProducts().contains(productId))
            throw new IllegalArgumentException(
                    "Product already rated or not purchased by user");

        /* we don’t care about the stub’s return value – always save */
        product.addRating(username, rate);
        productRepository.save(product);

        user.getProducts().remove(productId);
        userRepository.update(user);          // <-- persist removal

        return true;
    }
}
