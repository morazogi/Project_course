package RepositoryLayer;

import DomainLayer.IStoreRepository;
import DomainLayer.Store;
import InfrastructureLayer.StoreRepository;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StoreRepositoryTest {

    private static ConfigurableApplicationContext ctx;
    private static StoreRepository repo;

    @BeforeAll
    static void boot() {
        ctx = SpringApplication.run(TestConfig.class,
                "--spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "--spring.datasource.driverClassName=org.h2.Driver",
                "--spring.datasource.username=sa",
                "--spring.jpa.hibernate.ddl-auto=create-drop",
                "--spring.jpa.show-sql=false");
        repo = ctx.getBean(StoreRepository.class);
    }

    @AfterAll
    static void shutdown() {
        if (ctx != null) ctx.close();
    }

    private static Store newStore(String name) {
        return new Store("founder", name);
    }

    @Test
    void saveAndRetrieve() {
        Store s = repo.save(newStore("shop-a"));
        assertEquals(s.getId(), repo.getById(s.getId()).getId());
    }

    @Test
    void update() {
        Store s = repo.save(newStore("shop-b"));
        s.setName("shop-b-updated");
        repo.update(s);
        Store found = repo.getStoreByName("shop-b-updated");
        assertEquals("shop-b-updated", found.getName());
    }

    @Test
    void deleteById() {
        Store s = repo.save(newStore("shop-c"));
        repo.deleteById(s.getId());
        assertFalse(repo.existsById(s.getId()));
    }

    @Test
    void deleteEntity() {
        Store s = repo.save(newStore("shop-d"));
        repo.delete(s);
        assertFalse(repo.existsById(s.getId()));
    }

    @Test
    void existsById() {
        Store s = repo.save(newStore("shop-e"));
        assertTrue(repo.existsById(s.getId()));
        assertFalse(repo.existsById(UUID.randomUUID().toString()));
    }

    @Test
    void getAll() {
        repo.save(newStore("shop-f1"));
        repo.save(newStore("shop-f2"));
        List<Store> all = repo.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void findByNameContaining() {
        Store hello = repo.save(newStore("hello-world"));
        repo.save(newStore("another-store"));
        assertEquals(hello.getId(), repo.getStoreByName("hello").getId());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Store.class)
    @EnableJpaRepositories(considerNestedRepositories = true)
    @Import(StoreRepository.class)
    static class TestConfig {
        interface TestStoreRepo extends IStoreRepository {}
    }
}
