/* ────────────────────────────────────────────────────────────────
   src/test/java/ServiceLayer/OwnerManagerServiceTest.java
   ──────────────────────────────────────────────────────────────── */
package ServiceLayer;

import DomainLayer.ManagerPermissions;
import DomainLayer.DomainServices.*;
import com.fasterxml.jackson.core.JsonProcessingException;   // add this import
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

    /* ──────────────────────── NEW tests for untouched paths ───────────────── */

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

    @Test void closeStore_failure_returnsFailureMessage() {
        when(storeManagementService.closeStore(any(),any())).thenReturn(false);
        assertEquals("Failed to close store", svc.closeStore("f","s"));
    }

    @Test void reopenStore_success_returnsOpenedStore() {
        when(storeManagementService.reopenStore("f","s")).thenReturn(true);
        assertEquals("Opened store", svc.reopenStore("f","s"));
    }

    @Test void reopenStore_failure_returnsFailedMessage() {
        when(storeManagementService.reopenStore(any(),any())).thenReturn(false);
        assertEquals("Failed to open store", svc.reopenStore("f","s"));
    }

    @Test void removePurchasePolicy_success_true() {
        when(purchasePolicyService.removePurchasePolicy("o","s","pid")).thenReturn(true);
        assertTrue(svc.removePurchasePolicy("o","s","pid"));
    }

    @Test void removeDiscountFromDiscountPolicy_success_true() {
        when(discountPolicyService.removeDiscountFromDiscountPolicy("o","s","did"))
                .thenReturn(true);
        assertTrue(svc.removeDiscountFromDiscountPolicy("o","s","did"));
    }

    @Test void respondToOwnerAppointment_success_true() {
        when(storeManagementService.respondToOwnerAppointment("u","sid",true))
                .thenReturn(true);
        assertTrue(svc.respondToOwnerAppointment("u","sid",true));
    }

    @Test void respondToOwnerAppointment_exception_false() {
        when(storeManagementService.respondToOwnerAppointment(any(),any(),anyBoolean()))
                .thenThrow(new RuntimeException("err"));
        assertFalse(svc.respondToOwnerAppointment("u","sid",false));
    }

    @Test void respondToCustomerInquiry_success_true() {
        when(notificationService.respondToCustomerInquiry("o","s","inq","resp"))
                .thenReturn(true);
        assertTrue(svc.respondToCustomerInquiry("o","s","inq","resp"));
    }

    @Test void getCustomerInquiries_success_returnsList() {
        when(notificationService.getCustomerInquiries("o","s"))
                .thenReturn(List.of(Map.of("id","1")));
        assertEquals(1, svc.getCustomerInquiries("o","s").size());
    }

    @Test
    void getStorePurchaseHistory_success_returnsData() throws JsonProcessingException {
        when(purchaseHistoryService.getStorePurchaseHistory(eq("o"), eq("s"),
                isNull(), isNull()))
                .thenReturn(List.of("rec1", "rec2"));

        assertEquals(2,
                svc.getStorePurchaseHistory("o", "s", null, null).size());
    }
}
