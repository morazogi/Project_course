package infrastructureLayer;

import DomainLayer.IShipping;
import DomainLayer.Product;
import DomainLayer.Store;
import DomainLayer.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProxyShipping implements IShipping {
    public ProxyShipping() {}
    public void processShipping(String userId, String state, String city, String street, Map<String, Integer> products, String homeNumber) throws Exception {
        //based on shipping service
    }

}
