package RepositoryLayer;

import DomainLayer.IProductRepository;
import DomainLayer.Product;
import InfrastructureLayer.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.UUID;                  // ← added

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest {

    private static ConfigurableApplicationContext ctx;
    private static ProductRepository repo;

    /* ── boot / shutdown ─────────────────── */
    @BeforeAll
    static void boot() {
        ctx = SpringApplication.run(TestConfig.class,
                "--spring.datasource.url=jdbc:h2:mem:proddb;DB_CLOSE_DELAY=-1",
                "--spring.jpa.hibernate.ddl-auto=create-drop");
        repo = ctx.getBean(ProductRepository.class);
    }

    @AfterAll
    static void stop() { ctx.close(); }

    /* ── helper ──────────────────────────── */
    private static Product make(String name) {
        Product p = new Product("store1", name, "desc", 10f, 5, 0, "cat");
        p.setId(UUID.randomUUID().toString());               // ensure non-null PK
        return p;
    }

    /* ── tests ───────────────────────────── */
    @Test void saveAndGet() {
        Product p = repo.save(make("p-a"));
        assertEquals(p.getId(), repo.getById(p.getId()).getId());
    }

    @Test void findByName() {
        Product pa = repo.save(make("hello-world"));
        repo.save(make("something-else"));
        assertEquals(pa.getId(), repo.getProductByName("hello").getId());
    }

    @Test void delete() {
        Product p = repo.save(make("p-b"));
        repo.delete(p);
        assertFalse(repo.existsById(p.getId()));
    }

    @Test void getAll() {
        repo.save(make("p-c1"));
        repo.save(make("p-c2"));
        List<Product> all = repo.getAll();
        assertTrue(all.size() >= 2);
    }

    /* ── minimal Spring config ───────────── */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Product.class)
    @EnableJpaRepositories(considerNestedRepositories = true)
    @Import(ProductRepository.class)
    static class TestConfig { interface R extends IProductRepository {} }
}
