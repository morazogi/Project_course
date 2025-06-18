package InfrastructureLayer;

import DomainLayer.IShipping;
import DomainLayer.Store;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class ProxyShipping implements IShipping {
    public ProxyShipping() {}
    public void processShipping(String userId, String state, String city, String street, Map<String, Integer> products, String homeNumber) throws Exception {
        //based on shipping service
    }
}
