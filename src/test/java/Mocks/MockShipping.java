package Mocks;

import DomainLayer.IShipping;

public class MockShipping implements IShipping {
    public MockShipping() {}

    public void processShipping(String state, String city, String street, String homeNumber) throws Exception {
        if(state == null | state.length() == 0) {
            throw new Exception("Empty state");
        }
        if(city == null | city.length() == 0) {
            throw new Exception("Empty city");
        }
        if(street == null | street.length() == 0) {
            throw new Exception("Empty street");
        }
        try {
            Integer intHomeNumber = Integer.valueOf(homeNumber);
            if(intHomeNumber < 1) {
                throw new Exception("negative home number");
            }
        } catch (Exception e) {
            throw new Exception("Invalid home number");
        }

    }
}
