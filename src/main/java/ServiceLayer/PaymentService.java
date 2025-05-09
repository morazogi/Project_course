package ServiceLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.PaymentConnectivity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private PaymentConnectivity paymentConnectivity;
    private IToken tokenService;

    public PaymentService(IUserRepository userRepository, IProductRepository productRepository, IPayment proxyPayment, IToken tokenService) {
        this.paymentConnectivity = new PaymentConnectivity(proxyPayment, userRepository, productRepository);
        this.tokenService = tokenService;
    }

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