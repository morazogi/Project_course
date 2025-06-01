package DomainLayer.domainServices;

import com.fasterxml.jackson.databind.ObjectMapper;

import DomainLayer.IProductRepository;
import DomainLayer.IStoreRepository;
import DomainLayer.IToken;
import DomainLayer.IUserRepository;
import DomainLayer.Product;
import DomainLayer.Store;

public class Rate {
    private IToken Tokener;
    private ObjectMapper mapper = new ObjectMapper();
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;

    public Rate(IToken Tokener, IStoreRepository storeRepository, IUserRepository userRepository, IProductRepository productRepository) {
        this.Tokener = Tokener;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public boolean rateStore(String token, String storeId, int rate) throws Exception {
        if (token == null || storeId == null || rate < 1 || rate > 5) {
            throw new IllegalArgumentException("Invalid input");
        }
        Tokener.validateToken(token);
        String username = Tokener.extractUsername(token);
        Store store = mapper.readValue(storeRepository.getStore(storeId), Store.class);
        if (store == null) {
            throw new IllegalArgumentException("Store does not exist");
        }
        if (userRepository.getUser(username) == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        if(store.rate(rate)){
            storeRepository.updateStore(storeId, mapper.writeValueAsString(store));
            return true;
        }
        throw new IllegalArgumentException("invalid rate");
    }

    public boolean rateProduct(String token, String productId, double rate) {
        if (token == null || productId == null || rate < 1 || rate > 5) {
            throw new IllegalArgumentException("Invalid input");
        }
        Tokener.validateToken(token);
        String username = Tokener.extractUsername(token);
        Product product = productRepository.getReferenceById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product does not exist");
        }
        if (userRepository.getUser(username) == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        if(product.addRating(username , rate)){
            productRepository.save(product);
            return true;
        }
        return false;
    }
}
