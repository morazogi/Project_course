package RepositoryLayer;

import DomainLayer.IOrderRepository;
import DomainLayer.Order;
import InfrastructureLayer.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private static ConfigurableApplicationContext ctx;
    private static OrderRepository repo;

    @BeforeAll
    static void boot() {
        ctx = SpringApplication.run(TestConfig.class,
                "--spring.datasource.url=jdbc:h2:mem:orderdb;DB_CLOSE_DELAY=-1",
                "--spring.jpa.hibernate.ddl-auto=create-drop");
        repo = ctx.getBean(OrderRepository.class);
    }

    @AfterAll
    static void stop() { ctx.close(); }

    private static Order make(String store, String user) {
        return new Order("info", store, user, new Date());
    }

    @Test void saveAndFetch() {
        Order o = repo.save(make("s1", "u1"));
        assertEquals(o.getId(), repo.getById(o.getId()).getId());
    }

    @Test void findByStoreId() {
        Order o = repo.save(make("s-find", "u2"));
        List<Order> out = repo.findByStoreID("s-find");
        assertTrue(out.stream().anyMatch(or -> or.getId().equals(o.getId())));
    }

    @Test void findByUserId() {
        Order o = repo.save(make("s3", "user-find"));
        List<Order> out = repo.findByUserID("user-find");
        assertTrue(out.stream().anyMatch(or -> or.getId().equals(o.getId())));
    }

    @Test void deleteById() {
        Order o = repo.save(make("s4", "u4"));
        repo.deleteById(o.getId());
        assertFalse(repo.existsById(o.getId()));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Order.class)
    @EnableJpaRepositories(considerNestedRepositories = true)
    @Import(OrderRepository.class)
    static class TestConfig { interface R extends IOrderRepository {} }
}
