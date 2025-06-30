package Mocks;

import DomainLayer.IShipping;
import DomainLayer.Store;

import java.util.Map;

public class MockShipping implements IShipping {
    public MockShipping() {}

    public String processShipping(String state, String city, String address, Map<String, Integer> products, String name, String zip) throws Exception {
        if(state == null | state.length() == 0) {
            return "Empty state";
        }
        if(city == null | city.length() == 0) {
            return "Empty city";
        }
        if(address == null | address.length() == 0) {
            return "Empty street";
        }
        try {
            Integer intHomeNumber = Integer.valueOf(zip);
            if(intHomeNumber < 1) {
                return "negative home number";
            }
        } catch (Exception e) {
            return "Invalid home number";
        }
        return "Shipping successful";
    }

    public String cancelShipping(String id) {
        return "Cancel shipping successful";
    }
}
