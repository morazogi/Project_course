package Mocks;

import DomainLayer.IShipping;
import DomainLayer.Store;

import java.util.Map;

public class MockShipping implements IShipping {
    public MockShipping() {}

    public String processShipping(String state, String city, String address, Map<String, Integer> products, String name, String zip) throws Exception {
        if(state == null | state.length() == 0) {
            throw new Exception("Empty state");
        }
        if(city == null | city.length() == 0) {
            throw new Exception("Empty city");
        }
        if(address == null | address.length() == 0) {
            throw new Exception("Empty street");
        }
        try {
            Integer intHomeNumber = Integer.valueOf(zip);
            if(intHomeNumber < 1) {
                throw new Exception("negative home number");
            }
        } catch (Exception e) {
            throw new Exception("Invalid home number");
        }
        return "Shipping successful";
    }

    public String cancelShipping(String id) {
        return "Cancel shipping successful";
    }
}
