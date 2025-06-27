package UILayer;

import DomainLayer.*;
import DomainLayer.DomainServices.AdminOperationsMicroservice;
import DomainLayer.DomainServices.NotificationWebSocketHandler;
import PresentorLayer.AdminConsolePresenter;
import ServiceLayer.*;
import InfrastructureLayer.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class SystemConfiguration {

    /* ───────── plain bean declarations (unchanged) ───────── */

    @Bean public DiscountRepository DiscountRepository() { return new DiscountRepository(); }
    @Bean public OrderRepository    OrderRepository()    { return new OrderRepository(); }
    @Bean public ProductRepository  ProductRepository()  { return new ProductRepository(); }
    @Bean public NotificationRepository NotificationRepository() { return new NotificationRepository(); }
    @Bean public ProxyPayment  ProxyPayment()  { return new ProxyPayment();  }
    @Bean public ProxyShipping ProxyShipping() { return new ProxyShipping(); }
    @Bean public StoreRepository StoreRepository() { return new StoreRepository(); }
    @Bean public UserRepository  UserRepository()  { return new UserRepository(); }


    /* ← NEW admin micro-service bean */
    @Bean public AdminOperationsMicroservice AdminOperationsMicroservice() {
        return new AdminOperationsMicroservice(UserRepository(), StoreRepository());
    }

    @Bean
    public NotificationWebSocketHandler NotificationWebSocketHandler() {
        return new NotificationWebSocketHandler(TokenService(), NotificationRepository());
    };


    @Bean public RegisteredService RegisteredService() {
        return new RegisteredService(TokenService(), StoreRepository(), UserRepository(),
                ProductRepository(), OrderRepository(), NotificationRepository(),
                GuestRepository(), NotificationWebSocketHandler());
    }

    @Bean public GuestRepository GuestRepository() { return new GuestRepository(); }
    @Bean public CustomerInquiryRepository CustomerInquiryRepository() { return new CustomerInquiryRepository(); }
    @Bean public NotificationService NotificationService() {
        return new NotificationService(NotificationWebSocketHandler(), NotificationRepository(), TokenService(), UserRepository());
    }
    @Bean public OrderService OrderService() { return new OrderService(OrderRepository()); }
    @Bean public OwnerManagerService OwnerManagerService() {
        return new OwnerManagerService(UserRepository(), StoreRepository(), ProductRepository(),
                OrderRepository(), DiscountRepository());
    }
    @Bean public PaymentService PaymentService() {
        return new PaymentService(UserRepository(), ProductRepository(), ProxyPayment(), TokenService(),
                DiscountRepository(), StoreRepository(), GuestRepository());
    }
    @Bean
    public ShippingService ShippingService() {
        return new ShippingService(ProxyShipping(), TokenService(), UserRepository(), GuestRepository());
    }

    @Bean public TokenService TokenService() { return new TokenService(); }

    @Bean public UserService UserService() {
        return new UserService(TokenService(), StoreRepository(), UserRepository(),
                ProductRepository(), OrderRepository(), ShippingService(),
                PaymentService(), GuestRepository(), DiscountRepository());
    }

    @Bean public WebSocketConfigure WebSocketConfigure() { return new WebSocketConfigure(NotificationWebSocketHandler()); }



    @Bean
    public AuctionService AuctionService(PaymentService paymentService,
                                         ShippingService shippingService,
                                         IToken          tokenService,
                                         StoreRepository storeRepository,
                                         ProductRepository productRepository,
                                         OrderRepository orderRepository) {
        return new AuctionService(paymentService, shippingService, tokenService,
                storeRepository, productRepository, orderRepository);
    }

    @Bean
    public AdminConsolePresenter adminConsolePresenter(
            AdminOperationsMicroservice adminOps,
            StoreRepository             storeRepo,
            UserRepository              userRepo,
            TokenService                tokenSvc) {     // ← NEW
        return new AdminConsolePresenter(adminOps, storeRepo, userRepo, tokenSvc);
    }

    @Bean
    public BidService BidService(PaymentService paymentService,
                                 ShippingService shippingService,
                                 IToken          tokenService,
                                 StoreRepository storeRepository,
                                 ProductRepository productRepository,
                                 OrderRepository orderRepository) {
        return new BidService(paymentService, shippingService, tokenService,
                storeRepository, productRepository, orderRepository);
    }

    @Bean
    public RolesService RolesService() {
        return new RolesService(StoreRepository(), UserRepository());
    }

}


//    @Bean
//    public Module hibernateModule() {
//        // Use Hibernate6Module for Hibernate 6.x. For older versions, it might be Hibernate5Module.
//        return new Hibernate6Module();
//    }
