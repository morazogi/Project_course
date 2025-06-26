package DomainLayer.DomainServices;

import DomainLayer.ManagerPermissions;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static DomainLayer.ManagerPermissions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies the permission checks and repository interactions of
 * {@link InventoryManagementMicroservice}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryManagementMicroserviceTest {

    /* -------- repositories mocked -------- */
    @Mock StoreRepository    storeRepo;
    @Mock ProductRepository  productRepo;

    private InventoryManagementMicroservice service;
    private Store   store;             // mocked but reused
    private final String storeId  = "store-1";
    private final String ownerId  = "owner";
    private final String mgrId    = "mgr";

    @BeforeEach
    void init() {
        service = new InventoryManagementMicroservice(storeRepo, productRepo);

        store = mock(Store.class);
        when(storeRepo.getById(storeId)).thenReturn(store);
    }

    /* ================================================================
                              addProduct
       ================================================================ */
    @Test
    void addProduct_ownerHasAllPerms_returnsNewProductId() {
        when(store.userIsOwner(ownerId)).thenReturn(true);

        /* make the mocked save() assign an ID, like a DB would */
        doAnswer(inv -> {
            Product p = inv.getArgument(0, Product.class);
            p.setId("prod-123");
            return null;
        }).when(productRepo).save(any(Product.class));

        String returnedId = service.addProduct(
                ownerId, storeId, "Apple", "", 1.2f, 10, "Food");

        assertEquals("prod-123", returnedId);
        verify(productRepo).save(any(Product.class));
        verify(store).addProduct("prod-123", 10);
        verify(storeRepo).update(store);
    }

    @Test
    void addProduct_noPermission_returnsNull() {
        // user neither owner nor manager
        when(store.userIsOwner("stranger")).thenReturn(false);
        when(store.userIsManager("stranger")).thenReturn(false);

        String res = service.addProduct("stranger", storeId,
                "Beer", "", 2f, 5, "Drinks");

        assertNull(res);
        verify(productRepo, never()).save(any());
    }

    /* ================================================================
                              removeProduct
       ================================================================ */
    @Test
    void removeProduct_managerWithPerm_returnsTrue() {
        when(store.userIsOwner(mgrId)).thenReturn(false);
        when(store.userIsManager(mgrId)).thenReturn(true);
        when(store.userHasPermissions(mgrId, ManagerPermissions.PERM_REMOVE_PRODUCT))
                .thenReturn(true);
        when(store.removeProduct("prod-1")).thenReturn(true);

        assertTrue(service.removeProduct(mgrId, storeId, "prod-1"));
        verify(store).removeProduct("prod-1");
    }

    /* manager present but *without* permission */
    @Test
    void updateQuantity_managerWithoutPerm_returnsFalse() {
        when(store.userIsOwner(mgrId)).thenReturn(false);
        when(store.userIsManager(mgrId)).thenReturn(true);
        when(store.userHasPermissions(mgrId, PERM_UPDATE_PRODUCT)).thenReturn(false);

        boolean ok = service.updateProductQuantity(mgrId, storeId, "prod-1", 99);

        assertFalse(ok);
        verify(store, never()).updateProductQuantity(any(), anyInt());
    }
}
