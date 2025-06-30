package ServiceLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.PaymentConnectivity;
import InfrastructureLayer.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private PaymentConnectivity paymentConnectivity;
    private IToken tokenService;

    public PaymentService(UserRepository userRepository, ProductRepository productRepository, IPayment proxyPayment, IToken tokenService, DiscountRepository discountRepository, StoreRepository storeRepository, GuestRepository guestRepository) {
        this.paymentConnectivity = new PaymentConnectivity(proxyPayment, userRepository, productRepository, storeRepository, discountRepository, guestRepository);
        this.tokenService = tokenService;
    }

    @Transactional
    public String processPayment(String token, String name, String creditCardNumber, String expirationDate, String backNumber, String id) {
        try {
            String response = paymentConnectivity.processPayment(tokenService.extractUsername(token), creditCardNumber, expirationDate, backNumber, name, id);
            EventLogger.logEvent(tokenService.extractUsername(token), "Successfully payed for cart");
            return response;
        } catch (Exception e) {
            ErrorLogger.logError(tokenService.extractUsername(token), "Failed to pay " , e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public String cancelPayment(String token, String id) {
        try {
            String response = paymentConnectivity.cancelPayment(tokenService.extractUsername(token), id);
            EventLogger.logEvent(tokenService.extractUsername(token), "Successfully canceled payment for cart");
            return response;
        } catch (Exception e) {
            ErrorLogger.logError(tokenService.extractUsername(token), "Failed to cancel payment" , e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}