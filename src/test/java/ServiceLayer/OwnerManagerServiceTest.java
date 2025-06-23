package ServiceLayer;

import DomainLayer.DomainServices.*;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OwnerManagerService} that verify its façade logic
 * without touching real micro-service internals.  We intercept each inner
 * micro-service with {@code Mockito.mockConstruction}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OwnerManagerServiceTest {

    /* -------- repositories injected into the service ctor -------- */
    @Mock UserRepository      userRepo;
    @Mock StoreRepository     storeRepo;
    @Mock ProductRepository   productRepo;
    @Mock OrderRepository     orderRepo;
    @Mock DiscountRepository  discountRepo;

    /* -- will be built fresh in each test (inside try-with-resources) -- */
    private OwnerManagerService service;

    /* ================================================================
                                 addProduct
       ================================================================ */
    @Test
    void addProduct_happyPath_returnsProductId() throws Exception {
        try (MockedConstruction<InventoryManagementMicroservice> invMock =
                     Mockito.mockConstruction(InventoryManagementMicroservice.class,
                             (mock, ctx) ->
                                     when(mock.addProduct(
                                             eq("owner-1"), eq("store-1"),
                                             eq("Beer"), eq("Cold brew"),
                                             eq(12f), eq(20), eq("Drinks")
                                     )).thenReturn("prod-123"));
             /* the other micro-services are constructed but not used here,
                so we don’t need to stub behaviour – just intercept them */
             MockedConstruction<PurchasePolicyMicroservice> _1 =
                     Mockito.mockConstruction(PurchasePolicyMicroservice.class);
             MockedConstruction<DiscountPolicyMicroservice> _2 =
                     Mockito.mockConstruction(DiscountPolicyMicroservice.class);
             MockedConstruction<StoreManagementMicroservice> _3 =
                     Mockito.mockConstruction(StoreManagementMicroservice.class);
             MockedConstruction<QueryMicroservice> _4 =
                     Mockito.mockConstruction(QueryMicroservice.class);
             MockedConstruction<PurchaseHistoryMicroservice> _5 =
                     Mockito.mockConstruction(PurchaseHistoryMicroservice.class))
        {
            service = new OwnerManagerService(userRepo, storeRepo, productRepo,
                    orderRepo, discountRepo);

            String id = service.addProduct(
                    "owner-1", "store-1", "Beer", "Cold brew",
                    12f, 20, "Drinks");

            assertEquals("prod-123", id);
            // verify the delegation really happened
            InventoryManagementMicroservice inv =
                    invMock.constructed().get(0);
            verify(inv).addProduct("owner-1", "store-1",
                    "Beer", "Cold brew",
                    12f, 20, "Drinks");
        }
    }

    @Test
    void addProduct_whenInnerThrows_returnsNull() {
        try (MockedConstruction<InventoryManagementMicroservice> invMock =
                     Mockito.mockConstruction(InventoryManagementMicroservice.class,
                             (mock, ctx) -> when(mock.addProduct(any(), any(), any(), any(),
                                     anyFloat(), anyInt(), any()))
                                     .thenThrow(new RuntimeException("boom")));
             MockedConstruction<PurchasePolicyMicroservice> _1 =
                     Mockito.mockConstruction(PurchasePolicyMicroservice.class);
             MockedConstruction<DiscountPolicyMicroservice> _2 =
                     Mockito.mockConstruction(DiscountPolicyMicroservice.class);
             MockedConstruction<StoreManagementMicroservice> _3 =
                     Mockito.mockConstruction(StoreManagementMicroservice.class);
             MockedConstruction<QueryMicroservice> _4 =
                     Mockito.mockConstruction(QueryMicroservice.class);
             MockedConstruction<PurchaseHistoryMicroservice> _5 =
                     Mockito.mockConstruction(PurchaseHistoryMicroservice.class))
        {
            service = new OwnerManagerService(userRepo, storeRepo, productRepo,
                    orderRepo, discountRepo);

            String result = service.addProduct(
                    "owner-1", "store-1", "Fail", "desc", 5f, 1, "Misc");

            assertNull(result);  // facade converts exception → null
        }
    }

    /* ================================================================
                          appointStoreOwner
       ================================================================ */
    @Test
    void appointStoreOwner_success_returnsTrue() {
        try (MockedConstruction<InventoryManagementMicroservice> _inv =
                     Mockito.mockConstruction(InventoryManagementMicroservice.class);
             MockedConstruction<PurchasePolicyMicroservice> _1 =
                     Mockito.mockConstruction(PurchasePolicyMicroservice.class);
             MockedConstruction<DiscountPolicyMicroservice> _2 =
                     Mockito.mockConstruction(DiscountPolicyMicroservice.class);
             MockedConstruction<StoreManagementMicroservice> smMock =
                     Mockito.mockConstruction(StoreManagementMicroservice.class,
                             (mock, ctx) ->
                                     when(mock.appointStoreOwner("alice", "store-1", "bob"))
                                             .thenReturn(true));
             MockedConstruction<QueryMicroservice> _4 =
                     Mockito.mockConstruction(QueryMicroservice.class);
             MockedConstruction<PurchaseHistoryMicroservice> _5 =
                     Mockito.mockConstruction(PurchaseHistoryMicroservice.class))
        {
            service = new OwnerManagerService(userRepo, storeRepo, productRepo,
                    orderRepo, discountRepo);

            boolean ok = service.appointStoreOwner("alice", "store-1", "bob");

            assertTrue(ok);
            StoreManagementMicroservice sm = smMock.constructed().get(0);
            verify(sm).appointStoreOwner("alice", "store-1", "bob");
        }
    }

    /* ================================================================
                              removeProduct
       ================================================================ */
    @Test
    void removeProduct_failure_returnsFalse() {
        try (MockedConstruction<InventoryManagementMicroservice> invMock =
                     Mockito.mockConstruction(InventoryManagementMicroservice.class,
                             (mock, ctx) ->
                                     when(mock.removeProduct(any(), any(), any()))
                                             .thenThrow(new RuntimeException("nope")));
             MockedConstruction<PurchasePolicyMicroservice> _1 =
                     Mockito.mockConstruction(PurchasePolicyMicroservice.class);
             MockedConstruction<DiscountPolicyMicroservice> _2 =
                     Mockito.mockConstruction(DiscountPolicyMicroservice.class);
             MockedConstruction<StoreManagementMicroservice> _3 =
                     Mockito.mockConstruction(StoreManagementMicroservice.class);
             MockedConstruction<QueryMicroservice> _4 =
                     Mockito.mockConstruction(QueryMicroservice.class);
             MockedConstruction<PurchaseHistoryMicroservice> _5 =
                     Mockito.mockConstruction(PurchaseHistoryMicroservice.class))
        {
            service = new OwnerManagerService(userRepo, storeRepo, productRepo,
                    orderRepo, discountRepo);

            boolean ok = service.removeProduct("owner-1", "store-1", "prod-x");

            assertFalse(ok);     // exception → false
        }
    }

    /* ================================================================
                          getManagerPermissions
       ================================================================ */
    @Test
    void getManagerPermissions_happy_returnsMap() {
        Map<String, Boolean> perms = Map.of("view", true, "edit", false);

        try (MockedConstruction<InventoryManagementMicroservice> _inv =
                     Mockito.mockConstruction(InventoryManagementMicroservice.class);
             MockedConstruction<PurchasePolicyMicroservice> _1 =
                     Mockito.mockConstruction(PurchasePolicyMicroservice.class);
             MockedConstruction<DiscountPolicyMicroservice> _2 =
                     Mockito.mockConstruction(DiscountPolicyMicroservice.class);
             MockedConstruction<StoreManagementMicroservice> smMock =
                     Mockito.mockConstruction(StoreManagementMicroservice.class,
                             (mock, ctx) ->
                                     when(mock.getManagerPermissions(
                                             "alice", "store-1", "mgr-1"))
                                             .thenReturn(perms));
             MockedConstruction<QueryMicroservice> _4 =
                     Mockito.mockConstruction(QueryMicroservice.class);
             MockedConstruction<PurchaseHistoryMicroservice> _5 =
                     Mockito.mockConstruction(PurchaseHistoryMicroservice.class))
        {
            service = new OwnerManagerService(userRepo, storeRepo, productRepo,
                    orderRepo, discountRepo);

            Map<String, Boolean> result =
                    service.getManagerPermissions("alice", "store-1", "mgr-1");

            assertEquals(perms, result);
            StoreManagementMicroservice sm = smMock.constructed().get(0);
            verify(sm).getManagerPermissions("alice", "store-1", "mgr-1");
        }
    }
}
