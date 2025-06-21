package DomainLayer;

public interface IPayment{
    public String processPayment(Double payment, String creditCardNumber, String expirationDate, String backNumber, String Id , String name) throws Exception;
    public String cancelPayment(String Id) throws Exception;
}