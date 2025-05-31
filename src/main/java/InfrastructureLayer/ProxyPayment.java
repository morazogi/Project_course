package infrastructureLayer;

import DomainLayer.IPayment;
import ServiceLayer.ErrorLogger;
import org.springframework.stereotype.Repository;

@Repository
public class ProxyPayment implements IPayment {
    public ProxyPayment() {}
    public void processPayment(Double payment, String creditCardNumber, String expirationDate, String backNumber, String storeId , String paymentService) throws Exception {
        // behavior based on the payment service
    }

}
