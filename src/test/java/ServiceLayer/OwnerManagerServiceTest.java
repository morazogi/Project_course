package ServiceLayer;

import DomainLayer.DomainServices.*;
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-tests for {@link OwnerManagerService}.
 *
 * <p>The real service creates its own micro-services in the constructor, so we
 * build the instance normally and then <em>replace</em> those micro-services
 * with Mockito mocks via {@link ReflectionTestUtils}.</p>
 */
@ExtendWith(MockitoExtension.class)
class OwnerManagerServiceTest {

    // ─── Repositories (constructor parameters) ──────────────────────────────────
    @Mock UserRepository            userRepository;
    @Mock StoreRepository           storeRepository;
    @Mock ProductRepository         productRepository;
    @Mock OrderRepository           orderRepository;
    @Mock DiscountRepository        discountRepository;

    // ─── Micro-services we want to verify ───────────────────────────────────────
    @Mock InventoryManagementMicroservice inventoryService;
    @Mock PurchasePolicyMicroservice      purchasePolicyService;
    @Mock DiscountPolicyMicroservice      discountPolicyService;
    @Mock StoreManagementMicroservice     storeManagementService;
    @Mock QueryMicroservice               notificationService;
    @Mock PurchaseHistoryMicroservice     purchaseHistoryService;

    private OwnerManagerService service;

    @BeforeEach
    void setUp() {
        service = new OwnerManagerService(
                userRepository, storeRepository, productRepository,
                orderRepository, discountRepository);

        // swap-in the mocks created above
        ReflectionTestUtils.setField(service, "inventoryService",        inventoryService);
        ReflectionTestUtils.setField(service, "purchasePolicyService",   purchasePolicyService);
        ReflectionTestUtils.setField(service, "discountPolicyService",   discountPolicyService);
        ReflectionTestUtils.setField(service, "storeManagementService",  storeManagementService);
        ReflectionTestUtils.setField(service, "notificationService",     notificationService);
        ReflectionTestUtils.setField(service, "purchaseHistoryService",  purchaseHistoryService);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Inventory management
    // ────────────────────────────────────────────────────────────────────────────




    @Test
    void removeProduct_success_returnsFriendlyMessage() {
        when(inventoryService.removeProduct("owner","store","prod"))
                .thenReturn(true);

        String msg = service.removeProduct("owner","store","prod");

        assertEquals("Removed product", msg);
    }

    @Test
    void removeProduct_notRemoved_returnsFailureMessage() {
        when(inventoryService.removeProduct(any(), any(), any()))
                .thenReturn(false);

        String msg = service.removeProduct("o","s","p");

        assertEquals("Failed to remove product", msg);
    }

    @Test
    void updateProductDetails_success_returnsFriendlyMessage() {
        when(inventoryService.updateProductDetails(
                "owner","store","prod","New","Desc",5.0,"CAT")).thenReturn(true);

        String msg = service.updateProductDetails(
                "owner","store","prod","New","Desc",5.0,"CAT");

        assertEquals("Updated product details", msg);
    }

    @Test
    void updateProductDetails_failure_returnsFailureMessage() {
        when(inventoryService.updateProductDetails(any(),any(),any(),any(),any(),anyDouble(),any()))
                .thenReturn(false);

        String msg = service.updateProductDetails("o","s","p",null,null,-1,"C");

        assertEquals("Failed to update product details", msg);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Purchase policy
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    void definePurchasePolicy_success_returnsPolicyId() {
        when(purchasePolicyService.definePurchasePolicy(
                "owner","store","MinAge", Collections.emptyMap()))
                .thenReturn("pol-7");

        String id = service.definePurchasePolicy(
                "owner","store","MinAge", Collections.emptyMap());

        assertEquals("pol-7", id);
    }

    @Test
    void updatePurchasePolicy_exception_returnsFalse() {
        when(purchasePolicyService.updatePurchasePolicy(any(),any(),any(),any()))
                .thenThrow(new RuntimeException("broken"));

        boolean ok = service.updatePurchasePolicy(
                "o","s","id", Map.of("k","v"));

        assertFalse(ok);
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Store-owner flow
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    void appointStoreOwner_success() {
        when(storeManagementService.appointStoreOwner("app","store","user"))
                .thenReturn(true);

        assertTrue(service.appointStoreOwner("app","store","user"));
        verify(storeManagementService).appointStoreOwner("app","store","user");
    }

    @Test
    void appointStoreOwner_failure() {
        when(storeManagementService.appointStoreOwner(any(),any(),any()))
                .thenThrow(new RuntimeException("boom"));

        assertFalse(service.appointStoreOwner("a","s","u"));
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Role info – simple happy-path
    // ────────────────────────────────────────────────────────────────────────────
    @Test
    void getStoreRoleInfo_success() {
        when(storeManagementService.getStoreRoleInfo("owner","store"))
                .thenReturn("{json:true}");

        String json = service.getStoreRoleInfo("owner", "store");

        assertEquals("{json:true}", json);
    }
}
