package ServiceLayer;
import DomainLayer.*;
import DomainLayer.DomainServices.ShippingConnectivity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    private final ShippingConnectivity shippingConnectivity;
    private final IToken tokenService;

    public ShippingService(IShipping ProxyShipping, IToken tokenService, IUserRepository userRepository) {
        this.shippingConnectivity = new ShippingConnectivity(ProxyShipping, userRepository);
        this.tokenService = tokenService;
    }

    @Transactional
    public boolean processShipping(String token, String state, String city, String street, String homeNumber) {
        try {
            String username = tokenService.extractUsername(token);
            shippingConnectivity.processShipping(username, state, city, street, homeNumber);
            EventLogger.logEvent(username, "Shipping successful");
            return true;
        } catch (Exception e) {
            ErrorLogger.logError(tokenService.extractUsername(token), "Error in shipping", e.getMessage());
            return false;
        }
    }

}