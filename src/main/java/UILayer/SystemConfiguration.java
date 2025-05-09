package UILayer;

import DomainLayer.DomainServices.*;
import ServiceLayer.*;
import InfrastructureLayer.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemConfiguration {

    @Bean
    public OrderRepository OrderRepository() {
        return new OrderRepository();
    };

    @Bean
    public ProductRepository ProductRepository() {
        return new ProductRepository();
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
    public NotificationService NotificationService() {
        return new NotificationService();
    };

    @Bean
    public OrderService OrderService() {
        return new OrderService(OrderRepository());
    };

    @Bean
    public PaymentService PaymentService() {
        return new PaymentService(UserRepository(), ProductRepository(), ProxyPayment(), TokenService());
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
    public StoreService StoreService() {
        return new StoreService(StoreRepository(), ProductService());
    };

    @Bean
    public TokenService TokenService() {
        return new TokenService();
    };
    @Bean
    public UserCart UserCart() {
        return new UserCart(TokenService(), UserRepository(), StoreRepository(), ProductRepository(), OrderRepository());
    };
    @Bean
    public UserConnectivity UserConnectivity() {
        return new UserConnectivity(TokenService(), UserRepository());
    };
    @Bean
    public PaymentService paymentService() {
        //return new PaymentService(StoreRepository(), ProductRepository(), ProxyPayment());
        return null;
    };
    @Bean
    public UserService UserService() {
        return new UserService(TokenService(), StoreRepository(), UserRepository(), ProductRepository(), OrderRepository(), ShippingService(), PaymentService());
    };

    @Bean
    public OwnerManagerService ownerManagerService(){
        return new OwnerManagerService(UserRepository(),StoreRepository());
    };



}
