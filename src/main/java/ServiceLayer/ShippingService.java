package ServiceLayer;
import DomainLayer.*;
import DomainLayer.DomainServices.ShippingConnectivity;
import InfrastructureLayer.GuestRepository;
import InfrastructureLayer.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    private final ShippingConnectivity shippingConnectivity;
    private final IToken tokenService;

    public ShippingService(IShipping ProxyShipping, IToken tokenService, UserRepository userRepository, GuestRepository guestRepository) {
        this.shippingConnectivity = new ShippingConnectivity(ProxyShipping, userRepository, guestRepository);
        this.tokenService = tokenService;
    }

    @Transactional
    public String processShipping(String token, String state, String city, String address, String name, String zip) {
        try {
            String username = tokenService.extractUsername(token);
            String result = shippingConnectivity.processShipping(username, state, city, address, name, zip);
            EventLogger.logEvent(username, "Shipping successful");
            return result;
        } catch (Exception e) {
            ErrorLogger.logError(tokenService.extractUsername(token), "Error in shipping", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public String cancelShipping(String token, String id) {
        try {
            String response = shippingConnectivity.cancelShipping(tokenService.extractUsername(token), id);
            EventLogger.logEvent(tokenService.extractUsername(token), "Successfully canceled shipping for cart");
            return response;
        } catch (Exception e) {
            ErrorLogger.logError(tokenService.extractUsername(token), "Failed to cancel shipping" , e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}