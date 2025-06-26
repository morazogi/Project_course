package DomainLayer.DomainServices;

import DomainLayer.*;
import DomainLayer.Roles.Guest;
import DomainLayer.Roles.RegisteredUser;
import ServiceLayer.EventLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import InfrastructureLayer.GuestRepository;
import InfrastructureLayer.UserRepository;
import utils.ProductKeyModule;

import java.util.List;

public class ShippingConnectivity {

    private final ObjectMapper mapper = new ObjectMapper();
    private final IShipping proxyShipping;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;

    public ShippingConnectivity(IShipping proxyShipping, UserRepository userRepository, GuestRepository guestRepository) {
        this.proxyShipping = proxyShipping;
        this.userRepository = userRepository;
        this.guestRepository = guestRepository;
        this.mapper.registerModule(new ProductKeyModule());
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String processShipping(String username, String state, String city, String address, String name, String zip) throws Exception {
        boolean isRegisterdUser = (!username.contains("Guest"));
        Guest user = null;
        if(isRegisterdUser) {
            try {
                user = (RegisteredUser) userRepository.getById(username);
            }
            catch (Exception e) {
                EventLogger.logEvent(username, "PROCESS_SHIPPING - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        }
        else {
            try {
                user = guestRepository.getById(username);
            }
            catch (Exception e) {
                EventLogger.logEvent(username, "PROCESS_SHIPPING - USER_NOT_FOUND:"+e.toString());
                throw new IllegalArgumentException("User not found");
            }
        }
        return proxyShipping.processShipping(state, city, address, null, name, zip);
    }

    public String cancelShipping(String username, String id) {
        try {
            boolean isRegisterdUser = (!username.contains("Guest"));
            Guest user;
            if(isRegisterdUser) {
                try {
                    user = (RegisteredUser) userRepository.getById(username);
                }
                catch (Exception e) {
                    EventLogger.logEvent(username, "CANCEL_SHIPPIN - USER_NOT_FOUND:"+e.toString());
                    throw new IllegalArgumentException("User not found");
                }
            }
            else {
                try {
                    user = guestRepository.getById(username);
                }
                catch (Exception e) {
                    EventLogger.logEvent(username, "CANCEL_SHIPPING - USER_NOT_FOUND:"+e.toString());
                    throw new IllegalArgumentException("User not found");
                }
            }
            return proxyShipping.cancelShipping(id);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
