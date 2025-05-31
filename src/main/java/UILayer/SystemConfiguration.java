package UILayer;

import DomainLayer.IStoreRepository;
import ServiceLayer.*;
import InfrastructureLayer.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class SystemConfiguration {

    @Bean
    public DiscountRepository DiscountRepository() {
        return new DiscountRepository();
    };

    @Bean
    public OrderRepository OrderRepository() {
        return new OrderRepository();
    };

    @Bean
    public ProductRepository ProductRepository() {
        return new ProductRepository();
    };

    @Bean
    public NotificationRepository NotificationRepository() {
        return new NotificationRepository();
    };

    @Bean
    public ProxyPayment ProxyPayment() {
        return new ProxyPayment();
    };

    @Bean
    public ProxyShipping ProxyShipping() {
        return new ProxyShipping();
    };

    @Bean
    public StoreRepository StoreRepository() {
        return new StoreRepository();
    };

    @Bean
    public UserRepository UserRepository() {
        return new UserRepository();
    };

    @Bean
    public RegisteredService RegisteredService() {
        return new RegisteredService(TokenService(), StoreRepository(), UserRepository(), ProductRepository(), OrderRepository(), NotificationRepository());
    };

    @Bean
    public CustomerInquiryRepository CustomerInquiryRepository() {
        return new CustomerInquiryRepository();
    }

    @Bean
    public NotificationService NotificationService() {
        return new NotificationService();
    };

    @Bean
    public OrderService OrderService() {
        return new OrderService(OrderRepository());
    };

    @Bean
    public OwnerManagerService OwnerManagerService() {
        return new OwnerManagerService(UserRepository(), StoreRepository(), ProductRepository(), OrderRepository(), DiscountRepository());
    };

    @Bean
    public PaymentService PaymentService(DiscountRepository discountRepository) {
        return new PaymentService(UserRepository(), ProductRepository(), ProxyPayment(), TokenService(), DiscountRepository(), StoreRepository() );
    };

    @Bean
    public ProductService ProductService() {
        return new ProductService(ProductRepository());
    };

    @Bean
    public ShippingService ShippingService() {
        return new ShippingService(ProxyShipping(), TokenService(), UserRepository());
    };

    @Bean
    public TokenService TokenService() {
        return new TokenService();
    };


    @Bean
    public UserService UserService() {
        return new UserService(TokenService(), StoreRepository(), UserRepository(), ProductRepository(), OrderRepository(), ShippingService(), PaymentService(DiscountRepository()));
    };

    @Bean
    public NotificationClientRepository NotificationClientRepository() {
        return new NotificationClientRepository();
    };

    @Bean
    public WebSocketConfigure WebSocketConfigure() {
        return new WebSocketConfigure();
    };

    @Bean
    public WebSocketClient WebSocketClient() {
        return new StandardWebSocketClient();
    };

    @Bean
    public NotificationWebSocketHandler NotificationWebSocketHandler() {
        return new NotificationWebSocketHandler();
    };

}