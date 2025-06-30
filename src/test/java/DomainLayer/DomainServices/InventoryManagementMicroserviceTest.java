package DomainLayer.DomainServices;

import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static DomainLayer.ManagerPermissions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InventoryManagementMicroservice}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryManagementMicroserviceTest {

    /* ── Mocked Repositories ─────────────────────────────────────────────── */
    @Mock StoreRepository   storeRepo;
    @Mock ProductRepository productRepo;

    /* ── Mocked Domain Objects ───────────────────────────────────────────── */
    @Mock Store   store;
    @Mock Product product;                 // used by update-tests

    /* ── System-Under-Test ──────────────────────────────────────────────── */
    InventoryManagementMicroservice sut;

    /* ── Test constants ─────────────────────────────────────────────────── */
    static final String STORE_ID    = "store-1";
    static final String OWNER_ID    = "owner-1";
    static final String MANAGER_OK  = "manager-ok";
    static final String MANAGER_BAD = "manager-bad";
    static final String PROD_ID     = "prod-123";

    @BeforeEach
    void init() {
        sut = new InventoryManagementMicroservice(storeRepo, productRepo);

        /* Basic store lookup */
        when(storeRepo.getById(STORE_ID)).thenReturn(store);

        /* Owner stubs */
        when(store.userIsOwner(anyString())).thenReturn(false);
        when(store.userIsOwner(eq(OWNER_ID))).thenReturn(true);

        /* Manager stubs */
        when(store.userIsManager(anyString())).thenReturn(false);
        when(store.userIsManager(eq(MANAGER_OK))).thenReturn(true);
        when(store.userIsManager(eq(MANAGER_BAD))).thenReturn(true);

        /* Permission stubs */
        when(store.userHasPermissions(eq(MANAGER_OK), anyString())).thenReturn(true);
        when(store.userHasPermissions(eq(MANAGER_BAD), anyString())).thenReturn(false);

        /* Simulate JPA assigning an ID on save(...) */
        when(productRepo.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId("generated-id");
            return p;
        });
    }

    /* ─── addProduct(..) ────────────────────────────────────────────────── */
    @Nested @DisplayName("addProduct")
    class AddProduct {

        @Test @DisplayName("manager with permission ⇒ returns new id")
        void managerWithPermission() {
            String id = sut.addProduct(MANAGER_OK, STORE_ID,
                    "Apple", "Fresh", 1.5f, 10, "Fruit");

            assertNotNull(id);
            verify(productRepo).save(any(Product.class));
            verify(store).addProduct(eq(id), eq(10));
            verify(storeRepo).update(store);
        }

        @Test @DisplayName("manager without permission ⇒ null")
        void managerWithoutPermission() {
            assertNull(sut.addProduct(MANAGER_BAD, STORE_ID,
                    "Apple", "Fresh", 1.5f, 10, "Fruit"));

            verify(productRepo, never()).save(any());
        }



        @Test @DisplayName("missing store ⇒ IllegalArgumentException")
        void unknownStore() {
            when(storeRepo.getById("missing")).thenReturn(null);

            assertThrows(IllegalArgumentException.class,
                    () -> sut.addProduct(MANAGER_OK, "missing",
                            "Apple", "Fresh", 1.5f, 10, "Fruit"));
        }
    }

    /* ─── removeProduct(..) ─────────────────────────────────────────────── */
    @Nested @DisplayName("removeProduct")
    class RemoveProduct {

        @BeforeEach
        void grantPerm() {
            when(store.userHasPermissions(MANAGER_OK, PERM_REMOVE_PRODUCT)).thenReturn(true);
        }

        @Test @DisplayName("store accepts ⇒ true + repos updated")
        void happyFlow() {
            when(store.removeProduct(PROD_ID)).thenReturn(true);

            assertTrue(sut.removeProduct(MANAGER_OK, STORE_ID, PROD_ID));
            verify(productRepo).deleteById(PROD_ID);
            verify(storeRepo).update(store);
        }

        @Test @DisplayName("store rejects ⇒ false")
        void storeRejects() {
            when(store.removeProduct(PROD_ID)).thenReturn(false);

            assertFalse(sut.removeProduct(MANAGER_OK, STORE_ID, PROD_ID));
            verify(productRepo, never()).deleteById(anyString());
        }
    }

    /* ─── updateProductDetails(..) ──────────────────────────────────────── */
    @Nested @DisplayName("updateProductDetails")
    class UpdateDetails {

        @BeforeEach
        void setup() {
            when(store.userHasPermissions(MANAGER_OK, PERM_UPDATE_PRODUCT)).thenReturn(true);

            when(productRepo.getById(PROD_ID)).thenReturn(product);
            when(product.getQuantity()).thenReturn(7);
            when(product.getRating()).thenReturn(0.0);
        }

        @Test @DisplayName("domain accepts ⇒ replacement persisted")
        void success() {
            when(store.updateProductDetails(PROD_ID, "Orange", "Sweet", 2.2, "Fruit"))
                    .thenReturn(true);

            assertTrue(sut.updateProductDetails(MANAGER_OK, STORE_ID, PROD_ID,
                    "Orange", "Sweet", 2.2, "Fruit"));

            verify(productRepo).delete(product);
            verify(productRepo).update(any(Product.class));
            verify(storeRepo).update(store);
        }
    }

    /* ─── updateProductQuantity(..) ─────────────────────────────────────── */
    @Nested @DisplayName("updateProductQuantity")
    class UpdateQuantity {

        @BeforeEach
        void grantPerm() {
            when(store.userHasPermissions(MANAGER_OK, PERM_UPDATE_PRODUCT)).thenReturn(true);
        }

        @Test @DisplayName("store accepts ⇒ true + repo updated")
        void success() {
            when(store.updateProductQuantity(PROD_ID, 99)).thenReturn(true);

            assertTrue(sut.updateProductQuantity(MANAGER_OK, STORE_ID, PROD_ID, 99));
            verify(storeRepo).update(store);
        }

        @Test @DisplayName("store rejects ⇒ false")
        void reject() {
            when(store.updateProductQuantity(PROD_ID, 99)).thenReturn(false);

            assertFalse(sut.updateProductQuantity(MANAGER_OK, STORE_ID, PROD_ID, 99));
            verify(storeRepo, never()).update(any());
        }
    }
}
