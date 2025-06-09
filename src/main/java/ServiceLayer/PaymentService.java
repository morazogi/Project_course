package ServiceLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.PaymentConnectivity;
import InfrastructureLayer.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private PaymentConnectivity paymentConnectivity;
    private IToken tokenService;

    public PaymentService(UserRepository userRepository, ProductRepository productRepository, IPayment proxyPayment, IToken tokenService, DiscountRepository discountRepository, StoreRepository storeRepository, GuestRepository guestRepository) {
        this.paymentConnectivity = new PaymentConnectivity(proxyPayment, userRepository, productRepository, storeRepository, discountRepository, guestRepository);
        this.tokenService = tokenService;
    }

    @Transactional
    public boolean processPayment(String token, String paymentService, String creditCardNumber, String expirationDate, String backNumber) {
        try {
            paymentConnectivity.processPayment(tokenService.extractUsername(token), creditCardNumber, expirationDate, backNumber, paymentService);
            EventLogger.logEvent(tokenService.extractUsername(token), "Successfully payed for cart");
            return true;
        } catch (Exception e) {
            ErrorLogger.logError(tokenService.extractUsername(token), "Failed to pay " , e.getMessage());
            return false;
        }
    }
}