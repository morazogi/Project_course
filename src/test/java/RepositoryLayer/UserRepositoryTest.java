package RepositoryLayer;

import DomainLayer.IUserRepository;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;           // ← new
import InfrastructureLayer.UserRepository;
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

class UserRepositoryTest {

    private static ConfigurableApplicationContext ctx;
    private static UserRepository repo;

    /* ── boot / shutdown ─────────────────── */
    @BeforeAll
    static void boot() {
        ctx = SpringApplication.run(TestConfig.class,
                "--spring.datasource.url=jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1",
                "--spring.jpa.hibernate.ddl-auto=create-drop");
        repo = ctx.getBean(UserRepository.class);
    }

    @AfterAll
    static void stop() { ctx.close(); }

    /* ── helper ──────────────────────────── */
    private static RegisteredUser make(String username) {
        return new RegisteredUser(username, "hashedPw");
    }

    /* ── tests ───────────────────────────── */
    @Test void saveAndFetch() {
        RegisteredUser u = repo.save(make("bob"));
        assertEquals(u.getUsername(),
                repo.getById(u.getUsername()).getUsername());
    }

    @Test void findByNameContains() {
        RegisteredUser r = repo.save(make("alice-hello"));
        repo.save(make("other"));
        assertEquals(r.getUsername(),
                repo.getByName("hello").getUsername());
    }

    @Test void delete() {
        RegisteredUser u = repo.save(make("to-delete"));
        repo.delete(u);
        assertFalse(repo.existsById(u.getUsername()));
    }

    @Test void getAll() {
        repo.save(make("u1"));
        repo.save(make("u2"));
        List<RegisteredUser> all = repo.getAll();
        assertTrue(all.size() >= 2);
    }

    /* ── minimal Spring config ───────────── */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {RegisteredUser.class, ShoppingCart.class})
    @EnableJpaRepositories(considerNestedRepositories = true)
    @Import(UserRepository.class)
    static class TestConfig { interface R extends IUserRepository {} }
}
