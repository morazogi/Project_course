package UILayer;

import ServiceLayer.*;
import InfrastructureLayer.*;
import com.vaadin.flow.spring.SpringServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
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
    public OrderService OrderService() {
        return new OrderService(OrderRepository());
    };

    @Bean
    public OwnerManagerService OwnerManagerService() {
        return new OwnerManagerService(UserRepository(), StoreRepository(), ProductRepository(), OrderRepository(), DiscountRepository(), TokenService());
    };

    @Bean
    public PaymentService PaymentService() {
        return new PaymentService(UserRepository(), ProductRepository(), ProxyPayment(), TokenService(), DiscountRepository(), StoreRepository() );
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
    public NotificationWebSocketHandler notificationWebSocketHandler() {
        return new NotificationWebSocketHandler();
    }

    @Bean
    public UserService UserService() {
        return new UserService(TokenService(), StoreRepository(), UserRepository(), ProductRepository(), OrderRepository(), ShippingService(), PaymentService(), DiscountRepository());
    };

    @Bean
    public NotificationService NotificationService(NotificationWebSocketHandler handler,
                                                   NotificationRepository repo,
                                                   TokenService tokenService) {
        return new NotificationService(handler, repo, tokenService);
    }





    @Bean
    public ServletRegistrationBean<SpringServlet> vaadinServlet(ApplicationContext context) {
        // configure other properties as needed
        SpringServlet servlet = new SpringServlet(context, true);
        return new ServletRegistrationBean<>(servlet, "/vaadin/*");
    }


}