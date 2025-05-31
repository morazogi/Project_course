package ServiceLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.PaymentConnectivity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private PaymentConnectivity paymentConnectivity;
    private IToken tokenService;

    public PaymentService(IUserRepository userRepository, IProductRepository productRepository, IPayment proxyPayment, IToken tokenService, IDiscountRepository discountRepository, IStoreRepository storeRepository) {
        this.paymentConnectivity = new PaymentConnectivity(proxyPayment, userRepository, productRepository, storeRepository, discountRepository);
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