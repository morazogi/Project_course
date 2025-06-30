package DomainLayer;

import java.util.List;
import java.util.Map;

public interface IShipping {
    public String processShipping(String state, String city, String address, Map<String, Integer> products, String name, String zip) throws Exception;
    public String cancelShipping(String Id) throws Exception;

}