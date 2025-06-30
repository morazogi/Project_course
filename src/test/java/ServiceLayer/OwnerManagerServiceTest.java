/* ────────────────────────────────────────────────────────────────
   src/test/java/ServiceLayer/OwnerManagerServiceTest.java
   ──────────────────────────────────────────────────────────────── */
package ServiceLayer;

import DomainLayer.ManagerPermissions;
import DomainLayer.DomainServices.*;
import com.fasterxml.jackson.core.JsonProcessingException;            // already in classpath
import InfrastructureLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerManagerServiceTest {

    /* ctor repositories */
    @Mock UserRepository     userRepository;
    @Mock StoreRepository    storeRepository;
    @Mock ProductRepository  productRepository;
    @Mock OrderRepository    orderRepository;
    @Mock DiscountRepository discountRepository;

    /* micro-services swapped in */
    @Mock InventoryManagementMicroservice inventoryService;
    @Mock PurchasePolicyMicroservice      purchasePolicyService;
    @Mock DiscountPolicyMicroservice      discountPolicyService;
    @Mock StoreManagementMicroservice     storeManagementService;
    @Mock QueryMicroservice               notificationService;
    @Mock PurchaseHistoryMicroservice     purchaseHistoryService;

    private OwnerManagerService svc;

    @BeforeEach
    void setUp() {
        svc = new OwnerManagerService(userRepository, storeRepository,
                productRepository, orderRepository,
                discountRepository);

        /* inject mocks */
        ReflectionTestUtils.setField(svc, "inventoryService",       inventoryService);
        ReflectionTestUtils.setField(svc, "purchasePolicyService",  purchasePolicyService);
        ReflectionTestUtils.setField(svc, "discountPolicyService",  discountPolicyService);
        ReflectionTestUtils.setField(svc, "storeManagementService", storeManagementService);
        ReflectionTestUtils.setField(svc, "notificationService",    notificationService);
        ReflectionTestUtils.setField(svc, "purchaseHistoryService", purchaseHistoryService);
    }

    /* ────────────────────────── inventory flows (kept) ───────────────────── */

    @Test void removeProduct_success_returnsFriendlyMessage() {
        when(inventoryService.removeProduct("owner","store","prod")).thenReturn(true);
        assertEquals("Removed product", svc.removeProduct("owner","store","prod"));
    }

    @Test void removeProduct_notRemoved_returnsFailureMessage() {
        when(inventoryService.removeProduct(any(),any(),any())).thenReturn(false);
        assertEquals("Failed to remove product", svc.removeProduct("o","s","p"));
    }

    @Test void updateProductDetails_success_returnsFriendlyMessage() {
        when(inventoryService.updateProductDetails("o","s","p","n","d",5.0,"c"))
                .thenReturn(true);
        assertEquals("Updated product details",
                svc.updateProductDetails("o","s","p","n","d",5.0,"c"));
    }

    @Test void updateProductDetails_failure_returnsFailureMessage() {
        when(inventoryService.updateProductDetails(any(),any(),any(),any(),any(),anyDouble(),any()))
                .thenReturn(false);
        assertEquals("Failed to update product details",
                svc.updateProductDetails("o","s","p",null,null,-1,"c"));
    }

    /* ───────────────────────── policy / owner flow (kept) ─────────────────── */

    @Test void definePurchasePolicy_success_returnsPolicyId() {
        when(purchasePolicyService.definePurchasePolicy("o","s","MinAge", Collections.emptyMap()))
                .thenReturn("pol-7");
        assertEquals("pol-7", svc.definePurchasePolicy("o","s","MinAge", Collections.emptyMap()));
    }

    @Test void updatePurchasePolicy_exception_returnsFalse() {
        when(purchasePolicyService.updatePurchasePolicy(any(),any(),any(),any()))
                .thenThrow(new RuntimeException("boom"));
        assertFalse(svc.updatePurchasePolicy("o","s","id", Map.of()));
    }

    @Test void appointStoreOwner_success() {
        when(storeManagementService.appointStoreOwner("a","s","u")).thenReturn(true);
        assertTrue(svc.appointStoreOwner("a","s","u"));
    }

    @Test void appointStoreOwner_failure_returnsFalseOnException() {
        when(storeManagementService.appointStoreOwner(any(),any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.appointStoreOwner("a","s","u"));
    }

    @Test void getStoreRoleInfo_success() {
        when(storeManagementService.getStoreRoleInfo("o","s")).thenReturn("{json:true}");
        assertEquals("{json:true}", svc.getStoreRoleInfo("o","s"));
    }

    /* ─────────────────────────── extra coverage (added earlier) ───────────── */

    @Test void defineDiscountPolicy_success_returnsTrue() {
        when(discountPolicyService.addDiscountToDiscountPolicy(
                any(),any(),any(),anyFloat(),anyFloat(),anyFloat(),any(),
                anyFloat(),any(),anyFloat(),anyFloat(),any()))
                .thenReturn(true);
        assertTrue(svc.defineDiscountPolicy("o","s","did","id",
                1f,1f,1f,
                Collections.emptyList(),
                10f,"p",1f,1f,"c"));
    }

    @Test void removeDiscountPolicy_failure_returnsFalse() {
        when(discountPolicyService.removeDiscountPolicy(any(),any()))
                .thenReturn(false);
        assertFalse(svc.removeDiscountPolicy("o","s"));
    }

    @Test void hasInventoryPermission_founderShortcut_true() {
        when(storeManagementService.isFounderOrOwner("bob","s")).thenReturn(true);
        assertTrue(svc.hasInventoryPermission("bob","s"));
    }

    @Test void hasInventoryPermission_managerGranted_true() {
        when(storeManagementService.isFounderOrOwner(any(),any())).thenReturn(false);
        when(storeManagementService.getManagerPermissions("m","s","m"))
                .thenReturn(Map.of(ManagerPermissions.PERM_MANAGE_INVENTORY,true));
        assertTrue(svc.hasInventoryPermission("m","s"));
    }

    @Test void hasInventoryPermission_managerDenied_false() {
        when(storeManagementService.isFounderOrOwner(any(),any())).thenReturn(false);
        when(storeManagementService.getManagerPermissions(any(),any(),any()))
                .thenReturn(Map.of(ManagerPermissions.PERM_MANAGE_INVENTORY,false));
        assertFalse(svc.hasInventoryPermission("m","s"));
    }

    @Test void managerUpdateProductQuantity_success() {
        when(inventoryService.updateProductQuantity("m","s","p",5)).thenReturn(true);
        assertTrue(svc.managerUpdateProductQuantity("m","s","p",5));
    }

    @Test void closeStore_success_returnsFriendlyMessage() {
        when(storeManagementService.closeStore("founder","s")).thenReturn(true);
        assertEquals("Closed store", svc.closeStore("founder","s"));
    }

    @Test void updateProductQuantity_failureReturnsMessage() {
        when(inventoryService.updateProductQuantity(any(),any(),any(),anyInt()))
                .thenReturn(false);
        assertEquals("Failed to update product quantity",
                svc.updateProductQuantity("o","s","p",5));
    }

    @Test void appointStoreManager_success() {
        when(storeManagementService.appointStoreManager(any(),any(),any(),any()))
                .thenReturn(true);
        assertTrue(svc.appointStoreManager("owner","s","u",
                new boolean[]{true,true,true}));
    }

    @Test void managerAddProduct_inventoryFails_returnsNull() {
        when(inventoryService.addProduct(any(),any(),any(),any(),anyFloat(),anyInt(),any()))
                .thenThrow(new RuntimeException("err"));
        assertNull(svc.managerAddProduct("m","s","n","d",2f,5,"c"));
    }

    @Test void updatePurchasePolicy_happyPath_returnsTrue() {
        when(purchasePolicyService.updatePurchasePolicy("o","s","pid",Map.of()))
                .thenReturn(true);
        assertTrue(svc.updatePurchasePolicy("o","s","pid",Map.of()));
    }

    /* ───────────────────────────── NEW TESTS ──────────────────────────────── */

    /* 1. addProduct full coverage */
    @Test void addProduct_success_returnsFriendlyMessage() {
        when(inventoryService.addProduct("o","s","n","d",5f,10,"c"))
                .thenReturn("prd-1");
        assertEquals("Added product to store",
                svc.addProduct("o","s","n","d",5f,10,"c"));
    }

    @Test void addProduct_exception_returnsFailureMessage() {
        when(inventoryService.addProduct(any(),any(),any(),any(),anyFloat(),anyInt(),any()))
                .thenThrow(new RuntimeException("boom"));
        String msg = svc.addProduct("o","s","n","d",5f,10,"c");
        assertTrue(msg.startsWith("Failed to add product to store"));
    }

    /* 2. updateProductQuantity success branch (owner path) */
    @Test void updateProductQuantity_success_returnsFriendlyMessage() {
        when(inventoryService.updateProductQuantity("o","s","p",9)).thenReturn(true);
        assertEquals("Updated product quantity",
                svc.updateProductQuantity("o","s","p",9));
    }

    /* 3. definePurchasePolicy failure branch */
    @Test void definePurchasePolicy_exception_returnsNull() {
        when(purchasePolicyService.definePurchasePolicy(any(),any(),any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertNull(svc.definePurchasePolicy("o","s","T",Map.of()));
    }

    /* 4. defineDiscountPolicy failure branch */
    @Test void defineDiscountPolicy_failure_returnsFalse() {
        when(discountPolicyService.addDiscountToDiscountPolicy(any(),any(),any(),
                anyFloat(),anyFloat(),anyFloat(),any(),anyFloat(),any(),anyFloat(),anyFloat(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.defineDiscountPolicy("o","s","d","i",
                1,1,1, List.of(),5,"p",1,1,"c"));
    }

    /* 5. removeDiscountPolicy success */
    @Test void removeDiscountPolicy_success_true() {
        when(discountPolicyService.removeDiscountPolicy("o","s")).thenReturn(true);
        assertTrue(svc.removeDiscountPolicy("o","s"));
    }

    /* 6. appointStoreManager exception path */
    @Test void appointStoreManager_exception_returnsFalse() {
        when(storeManagementService.appointStoreManager(any(),any(),any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.appointStoreManager("o","s","u", new boolean[]{true}));
    }

    /* 7. updateManagerPermissions coverage */
    @Test void updateManagerPermissions_success_true() {
        when(storeManagementService.updateManagerPermissions("o","s","m",new boolean[]{false,true}))
                .thenReturn(true);
        assertTrue(svc.updateManagerPermissions("o","s","m",new boolean[]{false,true}));
    }

    @Test void updateManagerPermissions_exception_false() {
        when(storeManagementService.updateManagerPermissions(any(),any(),any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.updateManagerPermissions("o","s","m",new boolean[0]));
    }

    /* 8. removeStoreManager variants */
    @Test void removeStoreManager_success_true() {
        when(storeManagementService.removeStoreManager("o","s","m")).thenReturn(true);
        assertTrue(svc.removeStoreManager("o","s","m"));
    }

    @Test void removeStoreManager_exception_false() {
        when(storeManagementService.removeStoreManager(any(),any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.removeStoreManager("o","s","m"));
    }

    /* 9. relinquishOwnership */
    @Test void relinquishOwnership_success_true() {
        when(storeManagementService.relinquishOwnership("o","s")).thenReturn(true);
        assertTrue(svc.relinquishOwnership("o","s"));
    }

    @Test void relinquishOwnership_exception_false() {
        when(storeManagementService.relinquishOwnership(any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.relinquishOwnership("o","s"));
    }

    /* 10. relinquishManagement */
    @Test void relinquishManagement_success_true() {
        when(storeManagementService.relinquishManagement("m","s")).thenReturn(true);
        assertTrue(svc.relinquishManagement("m","s"));
    }

    @Test void relinquishManagement_exception_false() {
        when(storeManagementService.relinquishManagement(any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.relinquishManagement("m","s"));
    }

    /* 11. setFounder */
    @Test void setFounder_success_true() {
        when(storeManagementService.appointStoreFounder("f","s")).thenReturn(true);
        assertTrue(svc.setFounder("f","s"));
    }

    @Test void setFounder_exception_false() {
        when(storeManagementService.appointStoreFounder(any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.setFounder("f","s"));
    }

    /* 12. manager-side product APIs */
    @Test void managerUpdateProductDetails_success_true() {
        when(inventoryService.updateProductDetails("m","s","p","n","d",2.2,"c"))
                .thenReturn(true);
        assertTrue(svc.managerUpdateProductDetails("m","s","p","n","d",2.2,"c"));
    }

    @Test void managerUpdateProductDetails_exception_false() {
        when(inventoryService.updateProductDetails(any(),any(),any(),any(),any(),anyDouble(),any()))
                .thenThrow(new RuntimeException());
        assertFalse(svc.managerUpdateProductDetails("m","s","p",null,null,-1,"c"));
    }

    @Test void managerRemoveProduct_success_true() {
        when(inventoryService.removeProduct("m","s","p")).thenReturn(true);
        assertTrue(svc.managerRemoveProduct("m","s","p"));
    }

    @Test void managerRemoveProduct_exception_false() {
        when(inventoryService.removeProduct(any(),any(),any()))
                .thenThrow(new RuntimeException());
        assertFalse(svc.managerRemoveProduct("m","s","p"));
    }

    /* 13. getManagerPermissions delegation */
    @Test void getManagerPermissions_success_returnsMap() {
        when(storeManagementService.getManagerPermissions("o","s","m"))
                .thenReturn(Map.of("k",true));
        assertEquals(1, svc.getManagerPermissions("o","s","m").size());
    }

    @Test void getManagerPermissions_exception_returnsNull() {
        when(storeManagementService.getManagerPermissions(any(),any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertNull(svc.getManagerPermissions("o","s","m"));
    }

    /* 14. getStoreRoleInfo failure path */
    @Test void getStoreRoleInfo_exception_returnsNull() {
        when(storeManagementService.getStoreRoleInfo(any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertNull(svc.getStoreRoleInfo("o","s"));
    }

    /* 15. hasInventoryPermission path where perms map is null */
    @Test void hasInventoryPermission_nullMap_false() {
        when(storeManagementService.isFounderOrOwner(any(),any())).thenReturn(false);
        when(storeManagementService.getManagerPermissions(any(),any(),any()))
                .thenReturn(null);
        assertFalse(svc.hasInventoryPermission("u","s"));
    }

    /* 16. delegation one-liners */
    @Test void isFounderOrOwner_delegates() {
        when(storeManagementService.isFounderOrOwner("u","s")).thenReturn(false);
        assertFalse(svc.isFounderOrOwner("u","s"));
    }

    @Test void canUpdateDiscountPolicy_delegates() {
        when(storeManagementService.canUpdateDiscountPolicy("u","s")).thenReturn(true);
        assertTrue(svc.canUpdateDiscountPolicy("u","s"));
    }

    @Test void isFounderOwnerOrManager_delegates() {
        when(storeManagementService.isFounderOwnerOrManager("u","s")).thenReturn(true);
        assertTrue(svc.isFounderOwnerOrManager("u","s"));
    }

    /* 17. sendOwnershipProposal & sendManagementProposal (interaction only) */
    @Test void sendOwnershipProposal_invokesUnderlyingService() {
        when(storeManagementService.sendOwnershipProposal("u","s","text"))
                .thenReturn("prop-id");
        svc.sendOwnershipProposal("u","s","text");
        verify(storeManagementService).sendOwnershipProposal("u","s","text");
    }

    @Test void sendManagementProposal_invokesUnderlyingService() {
        doNothing().when(storeManagementService).sendManagementProposal("u","s","text");
        svc.sendManagementProposal("u","s","text");
        verify(storeManagementService).sendManagementProposal("u","s","text");
    }

    /* 18. removeDiscountFromDiscountPolicy already has success; now failure */
    @Test void removeDiscountFromDiscountPolicy_exception_false() {
        when(discountPolicyService.removeDiscountFromDiscountPolicy(any(),any(),any()))
                .thenThrow(new RuntimeException());
        assertFalse(svc.removeDiscountFromDiscountPolicy("o","s","d"));
    }

    /* 19. closeStore failure branch (already covered, keep) */
    @Test void closeStore_failure_returnsFailureMessage() {
        when(storeManagementService.closeStore(any(),any())).thenReturn(false);
        assertEquals("Failed to close store", svc.closeStore("f","s"));
    }

    /* 20. reopenStore paths already partly covered, add exception branch */
    @Test void reopenStore_exception_returnsFailedMessage() {
        when(storeManagementService.reopenStore(any(),any()))
                .thenThrow(new RuntimeException("err"));
        String msg = svc.reopenStore("f","s");
        assertTrue(msg.startsWith("Failed to open store"));
    }

    /* 21. removePurchasePolicy success already added earlier; add failure path done */

    /* 22. respondToCustomerInquiry failure branch */
    @Test void respondToCustomerInquiry_exception_false() {
        when(notificationService.respondToCustomerInquiry(any(),any(),any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.respondToCustomerInquiry("o","s","i","r"));
    }

    /* 23. getCustomerInquiries exception branch */
    @Test void getCustomerInquiries_exception_returnsNull() {
        when(notificationService.getCustomerInquiries(any(),any()))
                .thenThrow(new RuntimeException("err"));
        assertNull(svc.getCustomerInquiries("o","s"));
    }


}
