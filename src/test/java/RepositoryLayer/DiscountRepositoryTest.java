package RepositoryLayer;

import DomainLayer.Discount;
import DomainLayer.IDiscountRepository;
import InfrastructureLayer.DiscountRepository;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscountRepositoryTest {

    private static ConfigurableApplicationContext ctx;
    private static DiscountRepository repo;

    /* ── boot / shutdown ─────────────────── */
    @BeforeAll
    static void boot() {
        ctx = SpringApplication.run(TestConfig.class,
                "--spring.datasource.url=jdbc:h2:mem:discdb;DB_CLOSE_DELAY=-1",
                "--spring.jpa.hibernate.ddl-auto=create-drop");
        repo = ctx.getBean(DiscountRepository.class);
    }

    @AfterAll
    static void shutdown() { ctx.close(); }

    /* ── helper ──────────────────────────── */
    private static Discount make() {
        return new Discount(
                "store1", 3, 0, 0,
                null, .15f, "", 0, -1, "");
    }

    /* ── tests ───────────────────────────── */
    @Test void saveAndFetch() {
        Discount d = repo.save(make());
        assertEquals(d.getId(), repo.getById(d.getId()).getId());
    }

    @Test void update() {
        Discount d = repo.save(make());
        d.setPercentDiscount(.25f);
        Discount merged = repo.update(d);                    // use returned managed entity
        assertEquals(.25f, merged.getPercentDiscount());
    }

    @Test void deleteById() {
        Discount d = repo.save(make());
        repo.deleteById(d.getId());
        assertFalse(repo.existsById(d.getId()));
    }

    @Test void getAll() {
        repo.save(make());
        List<Discount> all = repo.getAll();
        assertTrue(all.size() >= 1);
    }

    /* ── minimal Spring config ───────────── */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Discount.class)
    @EnableJpaRepositories(considerNestedRepositories = true)
    @Import(DiscountRepository.class)
    static class TestConfig { interface R extends IDiscountRepository {} }
}
