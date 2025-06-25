//package AcceptanceTest;
//
//import DomainLayer.*;
//import DomainLayer.DomainServices.*;
//import DomainLayer.Roles.RegisteredUser;
//import InfrastructureLayer.*;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class RealFlow1 {
//
//    private static ConfigurableApplicationContext ctx;
//
//    private static UserRepository userRepo;
//    private static StoreRepository storeRepo;
//    private static ProductRepository productRepo;
//    private static DiscountRepository discountRepo;
//    private static OrderRepository orderRepo;
//    private static GuestRepository guestRepo;
//
//    private static StubTokenService tokenSvc;
//    private static OpenStore openStoreMS;
//    private static InventoryManagementMicroservice inventoryMS;
//    private static DiscountPolicyMicroservice discountMS;
//    private static UserCart cartMS;
//
//    @BeforeAll
//    static void boot() {
//        ctx = SpringApplication.run(TestConfig.class,
//                "--spring.datasource.url=jdbc:h2:mem:flowdb;DB_CLOSE_DELAY=-1",
//                "--spring.jpa.hibernate.ddl-auto=create-drop");
//
//        userRepo = ctx.getBean(UserRepository.class);
//        storeRepo = ctx.getBean(StoreRepository.class);
//        productRepo = ctx.getBean(ProductRepository.class);
//        discountRepo = ctx.getBean(DiscountRepository.class);
//        orderRepo = ctx.getBean(OrderRepository.class);
//        guestRepo = ctx.getBean(GuestRepository.class);
//        tokenSvc = ctx.getBean(StubTokenService.class);
//
//        openStoreMS = new OpenStore(tokenSvc, storeRepo, userRepo);
//        inventoryMS = new InventoryManagementMicroservice(storeRepo, productRepo);
//        discountMS = new DiscountPolicyMicroservice(storeRepo, userRepo, productRepo, discountRepo);
//        cartMS = new UserCart(tokenSvc, userRepo, storeRepo, productRepo, orderRepo, guestRepo);
//    }
//
//    @AfterAll
//    static void shutdown() {
//        if (ctx != null) ctx.close();
//    }
//
//    @Test
//    void fullBackendHappyPath() throws Exception {
//        userRepo.save(new RegisteredUser("alice", "pw"));
//        String tAlice = tokenSvc.generateToken("alice");
//
//        String storeId = openStoreMS.openStore(tAlice, "Alice-Shop");
//
//        String pApple = inventoryMS.addProduct("alice", storeId,
//                "Apple", "Red", 2.0f, 10, "fruit");
//        String pBanana = inventoryMS.addProduct("alice", storeId,
//                "Banana", "Yellow", 1.0f, 10, "fruit");
//
//        discountMS.addDiscountToDiscountPolicy("alice", storeId, "",
//                3, 0, 0, List.of(), .10f, "", -1, -1, "");
//
//        userRepo.save(new RegisteredUser("bob", "pw"));
//        String tBob = tokenSvc.generateToken("bob");
//
//        cartMS.addToCart(tBob, storeId, pApple, 2);
//        cartMS.addToCart(tBob, storeId, pBanana, 3);
//
//        float expected = 6.3f;
//        float price = discountMS.calculatePrice(storeId, Map.of(pApple, 2, pBanana, 3));
//        assertEquals(expected, price, 0.0001);
//
//        double reserved = cartMS.reserveCart(tBob);
//        assertEquals(expected, reserved, 0.0001);
//
//        cartMS.purchaseCart(tBob, reserved);
//    }
//
//    @SpringBootConfiguration
//    @EnableAutoConfiguration
//    @EntityScan(basePackageClasses = {
//            Store.class,
//            Product.class,
//            Discount.class,
//            ShoppingCart.class,
//            ShoppingBag.class,
//            RegisteredUser.class,
//            DomainLayer.Order.class
//    })
//    @EnableJpaRepositories(considerNestedRepositories = true)
//    @Import({
//            StoreRepository.class,
//            ProductRepository.class,
//            DiscountRepository.class,
//            UserRepository.class,
//            OrderRepository.class,
//            GuestRepository.class,
//            StubTokenService.class
//    })
//    static class TestConfig {
//        interface _S extends DomainLayer.IStoreRepository {}
//        interface _P extends DomainLayer.IProductRepository {}
//        interface _D extends DomainLayer.IDiscountRepository {}
//        interface _U extends DomainLayer.IUserRepository {}
//        interface _O extends DomainLayer.IOrderRepository {}
//    }
//
//    @org.springframework.stereotype.Component
//    static class StubTokenService implements IToken {
//        public String generateToken(String u) { return "T|" + u; }
//        public void validateToken(String t) {}
//        public String extractUsername(String t) { return t.substring(2); }
//        public java.util.Date extractExpiration(String t) { return null; }
//        public <T> T extractClaim(String a, java.util.function.Function<io.jsonwebtoken.Claims, T> b) { return null; }
//        public io.jsonwebtoken.Claims extractAllClaims(String t) { return null; }
//        public void invalidateToken(String t) {}
//        public void suspendUser(String t) {}
//        public void unsuspendUser(String t) {}
//        public List<String> showSuspended() { return List.of(); }
//    }
//}
