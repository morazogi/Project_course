package ServiceLayer;

import DomainLayer.*;
import DomainLayer.DomainServices.*;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerManagerServiceTest {

    // Mocked repositories
    @Mock
    private IDiscountRepository discountRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private IStoreRepository storeRepository;
    @Mock
    private ICustomerInquiryRepository inquiryRepository;
    @Mock
    private IProductRepository productRepository;
    @Mock
    private IOrderRepository orderRepository;

    // Mocked microservices
    @Mock
    private InventoryManagementMicroservice inventoryService;
    @Mock
    private PurchasePolicyMicroservice purchasePolicyService;
    @Mock
    private DiscountPolicyMicroservice discountPolicyService;
    @Mock
    private StoreManagementMicroservice storeManagementService;
    @Mock
    private QueryMicroservice queryMicroservice;
    @Mock
    private PurchaseHistoryMicroservice purchaseHistoryService;

    // Class under test
    private OwnerManagerService ownerManagerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a test instance with constructor injection
        ownerManagerService = new OwnerManagerService(userRepository, storeRepository,productRepository, orderRepository, discountRepository);

        // Replace the microservices with mocks using reflection
        try {
            java.lang.reflect.Field inventoryServiceField = OwnerManagerService.class.getDeclaredField("inventoryService");
            inventoryServiceField.setAccessible(true);
            inventoryServiceField.set(ownerManagerService, inventoryService);

            java.lang.reflect.Field purchasePolicyServiceField = OwnerManagerService.class.getDeclaredField("purchasePolicyService");
            purchasePolicyServiceField.setAccessible(true);
            purchasePolicyServiceField.set(ownerManagerService, purchasePolicyService);

            java.lang.reflect.Field discountPolicyServiceField = OwnerManagerService.class.getDeclaredField("discountPolicyService");
            discountPolicyServiceField.setAccessible(true);
            discountPolicyServiceField.set(ownerManagerService, discountPolicyService);

            java.lang.reflect.Field storeManagementServiceField = OwnerManagerService.class.getDeclaredField("storeManagementService");
            storeManagementServiceField.setAccessible(true);
            storeManagementServiceField.set(ownerManagerService, storeManagementService);

            java.lang.reflect.Field notificationServiceField = OwnerManagerService.class.getDeclaredField("notificationService");
            notificationServiceField.setAccessible(true);
            notificationServiceField.set(ownerManagerService, queryMicroservice);

            java.lang.reflect.Field purchaseHistoryServiceField = OwnerManagerService.class.getDeclaredField("purchaseHistoryService");
            purchaseHistoryServiceField.setAccessible(true);
            purchaseHistoryServiceField.set(ownerManagerService, purchaseHistoryService);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    // ==================== 1. Inventory Management Tests ====================

    @Test
    void addProduct_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productName = "Test Product";
        String description = "Test Description";
        double price = 10.0;
        int quantity = 5;
        String category = "Test Category";
        String expectedProductId = "product1";

        when(inventoryService.addProduct(ownerId, storeId, productName, description, price, quantity, category))
            .thenReturn(expectedProductId);

        // Act
        String result = ownerManagerService.addProduct(ownerId, storeId, productName, description, price, quantity, category);

        // Assert
        assertEquals(expectedProductId, result);
        verify(inventoryService).addProduct(ownerId, storeId, productName, description, price, quantity, category);
    }

    @Test
    void addProduct_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productName = "Test Product";
        String description = "Test Description";
        double price = 10.0;
        int quantity = 5;
        String category = "Test Category";

        when(inventoryService.addProduct(ownerId, storeId, productName, description, price, quantity, category))
            .thenThrow(new RuntimeException("Failed to add product"));

        // Act
        String result = ownerManagerService.addProduct(ownerId, storeId, productName, description, price, quantity, category);

        // Assert
        assertNull(result);
        verify(inventoryService).addProduct(ownerId, storeId, productName, description, price, quantity, category);
    }

    @Test
    void removeProduct_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productId = "product1";

        when(inventoryService.removeProduct(ownerId, storeId, productId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.removeProduct(ownerId, storeId, productId);

        // Assert
        assertTrue(result);
        verify(inventoryService).removeProduct(ownerId, storeId, productId);
    }

    @Test
    void removeProduct_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productId = "product1";

        when(inventoryService.removeProduct(ownerId, storeId, productId))
            .thenThrow(new RuntimeException("Failed to remove product"));

        // Act
        boolean result = ownerManagerService.removeProduct(ownerId, storeId, productId);

        // Assert
        assertFalse(result);
        verify(inventoryService).removeProduct(ownerId, storeId, productId);
    }

    @Test
    void updateProductDetails_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productId = "product1";
        String productName = "Updated Product";
        String description = "Updated Description";
        double price = 15.0;
        String category = "Updated Category";

        when(inventoryService.updateProductDetails(ownerId, storeId, productId, productName, description, price, category))
            .thenReturn(true);

        // Act
        boolean result = ownerManagerService.updateProductDetails(ownerId, storeId, productId, productName, description, price, category);

        // Assert
        assertTrue(result);
        verify(inventoryService).updateProductDetails(ownerId, storeId, productId, productName, description, price, category);
    }

    @Test
    void updateProductDetails_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productId = "product1";
        String productName = "Updated Product";
        String description = "Updated Description";
        double price = 15.0;
        String category = "Updated Category";

        when(inventoryService.updateProductDetails(ownerId, storeId, productId, productName, description, price, category))
            .thenThrow(new RuntimeException("Failed to update product details"));

        // Act
        boolean result = ownerManagerService.updateProductDetails(ownerId, storeId, productId, productName, description, price, category);

        // Assert
        assertFalse(result);
        verify(inventoryService).updateProductDetails(ownerId, storeId, productId, productName, description, price, category);
    }

    @Test
    void updateProductQuantity_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productId = "product1";
        int newQuantity = 10;

        when(inventoryService.updateProductQuantity(ownerId, storeId, productId, newQuantity))
            .thenReturn(true);

        // Act
        boolean result = ownerManagerService.updateProductQuantity(ownerId, storeId, productId, newQuantity);

        // Assert
        assertTrue(result);
        verify(inventoryService).updateProductQuantity(ownerId, storeId, productId, newQuantity);
    }

    @Test
    void updateProductQuantity_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String productId = "product1";
        int newQuantity = 10;

        when(inventoryService.updateProductQuantity(ownerId, storeId, productId, newQuantity))
            .thenThrow(new RuntimeException("Failed to update product quantity"));

        // Act
        boolean result = ownerManagerService.updateProductQuantity(ownerId, storeId, productId, newQuantity);

        // Assert
        assertFalse(result);
        verify(inventoryService).updateProductQuantity(ownerId, storeId, productId, newQuantity);
    }

    // ==================== 2. Purchase and Discount Policy Tests ====================

    @Test
    void definePurchasePolicy_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String policyType = "MinAge";
        Map<String, Object> policyParams = new HashMap<>();
        policyParams.put("minAge", 18);
        String expectedPolicyId = "policy1";

        when(purchasePolicyService.definePurchasePolicy(ownerId, storeId, policyType, policyParams))
            .thenReturn(expectedPolicyId);

        // Act
        String result = ownerManagerService.definePurchasePolicy(ownerId, storeId, policyType, policyParams);

        // Assert
        assertEquals(expectedPolicyId, result);
        verify(purchasePolicyService).definePurchasePolicy(ownerId, storeId, policyType, policyParams);
    }

    @Test
    void definePurchasePolicy_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String policyType = "MinAge";
        Map<String, Object> policyParams = new HashMap<>();
        policyParams.put("minAge", 18);

        when(purchasePolicyService.definePurchasePolicy(ownerId, storeId, policyType, policyParams))
            .thenThrow(new RuntimeException("Failed to define purchase policy"));

        // Act
        String result = ownerManagerService.definePurchasePolicy(ownerId, storeId, policyType, policyParams);

        // Assert
        assertNull(result);
        verify(purchasePolicyService).definePurchasePolicy(ownerId, storeId, policyType, policyParams);
    }

    @Test
    void updatePurchasePolicy_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String policyId = "policy1";
        Map<String, Object> policyParams = new HashMap<>();
        policyParams.put("minAge", 21);

        when(purchasePolicyService.updatePurchasePolicy(ownerId, storeId, policyId, policyParams))
            .thenReturn(true);

        // Act
        boolean result = ownerManagerService.updatePurchasePolicy(ownerId, storeId, policyId, policyParams);

        // Assert
        assertTrue(result);
        verify(purchasePolicyService).updatePurchasePolicy(ownerId, storeId, policyId, policyParams);
    }

    @Test
    void updatePurchasePolicy_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String policyId = "policy1";
        Map<String, Object> policyParams = new HashMap<>();
        policyParams.put("minAge", 21);

        when(purchasePolicyService.updatePurchasePolicy(ownerId, storeId, policyId, policyParams))
            .thenThrow(new RuntimeException("Failed to update purchase policy"));

        // Act
        boolean result = ownerManagerService.updatePurchasePolicy(ownerId, storeId, policyId, policyParams);

        // Assert
        assertFalse(result);
        verify(purchasePolicyService).updatePurchasePolicy(ownerId, storeId, policyId, policyParams);
    }

    @Test
    void removePurchasePolicy_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String policyId = "policy1";

        when(purchasePolicyService.removePurchasePolicy(ownerId, storeId, policyId))
            .thenReturn(true);

        // Act
        boolean result = ownerManagerService.removePurchasePolicy(ownerId, storeId, policyId);

        // Assert
        assertTrue(result);
        verify(purchasePolicyService).removePurchasePolicy(ownerId, storeId, policyId);
    }

    @Test
    void removePurchasePolicy_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String policyId = "policy1";

        when(purchasePolicyService.removePurchasePolicy(ownerId, storeId, policyId))
            .thenThrow(new RuntimeException("Failed to remove purchase policy"));

        // Act
        boolean result = ownerManagerService.removePurchasePolicy(ownerId, storeId, policyId);

        // Assert
        assertFalse(result);
        verify(purchasePolicyService).removePurchasePolicy(ownerId, storeId, policyId);
    }

    // Discount policy tests have been removed due to changes in the microservices

    // ==================== 3. Owner Appointment Tests ====================

    @Test
    void appointStoreOwner_Success() {
        // Arrange
        String appointerId = "owner1";
        String storeId = "store1";
        String userId = "user1";

        when(storeManagementService.appointStoreOwner(appointerId, storeId, userId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.appointStoreOwner(appointerId, storeId, userId);

        // Assert
        assertTrue(result);
        verify(storeManagementService).appointStoreOwner(appointerId, storeId, userId);
    }

    @Test
    void appointStoreOwner_Failure() {
        // Arrange
        String appointerId = "owner1";
        String storeId = "store1";
        String userId = "user1";

        when(storeManagementService.appointStoreOwner(appointerId, storeId, userId))
            .thenThrow(new RuntimeException("Failed to appoint store owner"));

        // Act
        boolean result = ownerManagerService.appointStoreOwner(appointerId, storeId, userId);

        // Assert
        assertFalse(result);
        verify(storeManagementService).appointStoreOwner(appointerId, storeId, userId);
    }

    @Test
    void sendOwnershipProposal_Success() {
        // Arrange
        String userId = "user1";
        String storeId = "store1";
        String proposalText = "Would you like to be an owner?";
        String expectedProposal = "Hi, would you like to become an owner of the store store1? \n";

        when(storeManagementService.sendOwnershipProposal(userId, storeId, proposalText))
            .thenReturn(expectedProposal);

        // Act
        ownerManagerService.sendOwnershipProposal(userId, storeId, proposalText);

        // Assert
        verify(storeManagementService).sendOwnershipProposal(userId, storeId, proposalText);
    }

    @Test
    void sendOwnershipProposal_Failure() {
        // Arrange
        String userId = "user1";
        String storeId = "store1";
        String proposalText = "Would you like to be an owner?";

        when(storeManagementService.sendOwnershipProposal(userId, storeId, proposalText))
            .thenThrow(new RuntimeException("Failed to send ownership proposal"));

        // Act & Assert
        // The method doesn't return anything, so we just verify it handles the exception
        ownerManagerService.sendOwnershipProposal(userId, storeId, proposalText);
        verify(storeManagementService).sendOwnershipProposal(userId, storeId, proposalText);
    }

    @Test
    void respondToOwnerAppointment_Success() {
        // Arrange
        String userId = "user1";
        String storeId = "store1";
        boolean accept = true;

        when(storeManagementService.respondToOwnerAppointment(userId, storeId, accept)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.respondToOwnerAppointment(userId, storeId, accept);

        // Assert
        assertTrue(result);
        verify(storeManagementService).respondToOwnerAppointment(userId, storeId, accept);
    }

    @Test
    void respondToOwnerAppointment_Failure() {
        // Arrange
        String userId = "user1";
        String storeId = "store1";
        boolean accept = true;

        when(storeManagementService.respondToOwnerAppointment(userId, storeId, accept))
            .thenThrow(new RuntimeException("Failed to respond to owner appointment"));

        // Act
        boolean result = ownerManagerService.respondToOwnerAppointment(userId, storeId, accept);

        // Assert
        assertFalse(result);
        verify(storeManagementService).respondToOwnerAppointment(userId, storeId, accept);
    }

    @Test
    void removeStoreOwner_Success() {
        // Arrange
        String removerId = "owner1";
        String storeId = "store1";
        String ownerId = "owner2";

        when(storeManagementService.removeStoreOwner(removerId, storeId, ownerId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.removeStoreOwner(removerId, storeId, ownerId);

        // Assert
        assertTrue(result);
        verify(storeManagementService).removeStoreOwner(removerId, storeId, ownerId);
    }

    @Test
    void removeStoreOwner_Failure() {
        // Arrange
        String removerId = "owner1";
        String storeId = "store1";
        String ownerId = "owner2";

        when(storeManagementService.removeStoreOwner(removerId, storeId, ownerId))
            .thenThrow(new RuntimeException("Failed to remove store owner"));

        // Act
        boolean result = ownerManagerService.removeStoreOwner(removerId, storeId, ownerId);

        // Assert
        assertFalse(result);
        verify(storeManagementService).removeStoreOwner(removerId, storeId, ownerId);
    }

    @Test
    void relinquishOwnership_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";

        when(storeManagementService.relinquishOwnership(ownerId, storeId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.relinquishOwnership(ownerId, storeId);

        // Assert
        assertTrue(result);
        verify(storeManagementService).relinquishOwnership(ownerId, storeId);
    }

    @Test
    void relinquishOwnership_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";

        when(storeManagementService.relinquishOwnership(ownerId, storeId))
            .thenThrow(new RuntimeException("Failed to relinquish ownership"));

        // Act
        boolean result = ownerManagerService.relinquishOwnership(ownerId, storeId);

        // Assert
        assertFalse(result);
        verify(storeManagementService).relinquishOwnership(ownerId, storeId);
    }

    // ==================== 4. Manager Appointment Tests ====================

    @Test
    void appointStoreManager_Success() {
        // Arrange
        String appointerId = "owner1";
        String storeId = "store1";
        String userId = "user1";
        boolean[] permissions = {true, false, true};

        when(storeManagementService.appointStoreManager(appointerId, storeId, userId, permissions)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.appointStoreManager(appointerId, storeId, userId, permissions);

        // Assert
        assertTrue(result);
        verify(storeManagementService).appointStoreManager(appointerId, storeId, userId, permissions);
    }

    @Test
    void appointStoreManager_Failure() {
        // Arrange
        String appointerId = "owner1";
        String storeId = "store1";
        String userId = "user1";
        boolean[] permissions = {true, false, true};

        when(storeManagementService.appointStoreManager(appointerId, storeId, userId, permissions))
            .thenThrow(new RuntimeException("Failed to appoint store manager"));

        // Act
        boolean result = ownerManagerService.appointStoreManager(appointerId, storeId, userId, permissions);

        // Assert
        assertFalse(result);
        verify(storeManagementService).appointStoreManager(appointerId, storeId, userId, permissions);
    }

    @Test
    void sendManagementProposal_Success() {
        // Arrange
        String userId = "user1";
        String storeId = "store1";
        String proposalText = "Would you like to be a manager?";

        doNothing().when(storeManagementService).sendManagementProposal(userId, storeId, proposalText);

        // Act
        ownerManagerService.sendManagementProposal(userId, storeId, proposalText);

        // Assert
        verify(storeManagementService).sendManagementProposal(userId, storeId, proposalText);
    }

    @Test
    void sendManagementProposal_Failure() {
        // Arrange
        String userId = "user1";
        String storeId = "store1";
        String proposalText = "Would you like to be a manager?";

        doThrow(new RuntimeException("Failed to send management proposal"))
            .when(storeManagementService).sendManagementProposal(userId, storeId, proposalText);

        // Act & Assert
        // The method doesn't return anything, so we just verify it handles the exception
        ownerManagerService.sendManagementProposal(userId, storeId, proposalText);
        verify(storeManagementService).sendManagementProposal(userId, storeId, proposalText);
    }

    @Test
    void respondToManagerAppointment_Success() {
        // Arrange
        String userId = "user1";
        String appointmentId = "appointment1";
        boolean accept = true;

        when(storeManagementService.respondToManagerAppointment(userId, appointmentId, accept)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.respondToManagerAppointment(userId, appointmentId, accept);

        // Assert
        assertTrue(result);
        verify(storeManagementService).respondToManagerAppointment(userId, appointmentId, accept);
    }

    @Test
    void respondToManagerAppointment_Failure() {
        // Arrange
        String userId = "user1";
        String appointmentId = "appointment1";
        boolean accept = true;

        when(storeManagementService.respondToManagerAppointment(userId, appointmentId, accept))
            .thenThrow(new RuntimeException("Failed to respond to manager appointment"));

        // Act
        boolean result = ownerManagerService.respondToManagerAppointment(userId, appointmentId, accept);

        // Assert
        assertFalse(result);
        verify(storeManagementService).respondToManagerAppointment(userId, appointmentId, accept);
    }

    @Test
    void updateManagerPermissions_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String managerId = "manager1";
        boolean[] permissions = {true, true, false};

        when(storeManagementService.updateManagerPermissions(ownerId, storeId, managerId, permissions)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.updateManagerPermissions(ownerId, storeId, managerId, permissions);

        // Assert
        assertTrue(result);
        verify(storeManagementService).updateManagerPermissions(ownerId, storeId, managerId, permissions);
    }

    @Test
    void updateManagerPermissions_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String managerId = "manager1";
        boolean[] permissions = {true, true, false};

        when(storeManagementService.updateManagerPermissions(ownerId, storeId, managerId, permissions))
            .thenThrow(new RuntimeException("Failed to update manager permissions"));

        // Act
        boolean result = ownerManagerService.updateManagerPermissions(ownerId, storeId, managerId, permissions);

        // Assert
        assertFalse(result);
        verify(storeManagementService).updateManagerPermissions(ownerId, storeId, managerId, permissions);
    }

    @Test
    void removeStoreManager_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String managerId = "manager1";

        when(storeManagementService.removeStoreManager(ownerId, storeId, managerId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.removeStoreManager(ownerId, storeId, managerId);

        // Assert
        assertTrue(result);
        verify(storeManagementService).removeStoreManager(ownerId, storeId, managerId);
    }

    @Test
    void removeStoreManager_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String managerId = "manager1";

        when(storeManagementService.removeStoreManager(ownerId, storeId, managerId))
            .thenThrow(new RuntimeException("Failed to remove store manager"));

        // Act
        boolean result = ownerManagerService.removeStoreManager(ownerId, storeId, managerId);

        // Assert
        assertFalse(result);
        verify(storeManagementService).removeStoreManager(ownerId, storeId, managerId);
    }

    @Test
    void relinquishManagement_Success() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";

        when(storeManagementService.relinquishManagement(managerId, storeId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.relinquishManagement(managerId, storeId);

        // Assert
        assertTrue(result);
        verify(storeManagementService).relinquishManagement(managerId, storeId);
    }

    @Test
    void relinquishManagement_Failure() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";

        when(storeManagementService.relinquishManagement(managerId, storeId))
            .thenThrow(new RuntimeException("Failed to relinquish management"));

        // Act
        boolean result = ownerManagerService.relinquishManagement(managerId, storeId);

        // Assert
        assertFalse(result);
        verify(storeManagementService).relinquishManagement(managerId, storeId);
    }

    // ==================== 5. Store Operations Tests ====================

    @Test
    void closeStore_Success() {
        // Arrange
        String founderId = "founder1";
        String storeId = "store1";

        when(storeManagementService.closeStore(founderId, storeId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.closeStore(founderId, storeId);

        // Assert
        assertTrue(result);
        verify(storeManagementService).closeStore(founderId, storeId);
    }

    @Test
    void closeStore_Failure() {
        // Arrange
        String founderId = "founder1";
        String storeId = "store1";

        when(storeManagementService.closeStore(founderId, storeId))
            .thenThrow(new RuntimeException("Failed to close store"));

        // Act
        boolean result = ownerManagerService.closeStore(founderId, storeId);

        // Assert
        assertFalse(result);
        verify(storeManagementService).closeStore(founderId, storeId);
    }

    @Test
    void reopenStore_Success() {
        // Arrange
        String founderId = "founder1";
        String storeId = "store1";

        when(storeManagementService.reopenStore(founderId, storeId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.reopenStore(founderId, storeId);

        // Assert
        assertTrue(result);
        verify(storeManagementService).reopenStore(founderId, storeId);
    }

    @Test
    void reopenStore_Failure() {
        // Arrange
        String founderId = "founder1";
        String storeId = "store1";

        when(storeManagementService.reopenStore(founderId, storeId))
            .thenThrow(new RuntimeException("Failed to reopen store"));

        // Act
        boolean result = ownerManagerService.reopenStore(founderId, storeId);

        // Assert
        assertFalse(result);
        verify(storeManagementService).reopenStore(founderId, storeId);
    }

    @Test
    void getStoreRoleInfo_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String expectedRoleInfo = "Role information";

        when(storeManagementService.getStoreRoleInfo(ownerId, storeId)).thenReturn(expectedRoleInfo);

        // Act
        String result = ownerManagerService.getStoreRoleInfo(ownerId, storeId);

        // Assert
        assertEquals(expectedRoleInfo, result);
        verify(storeManagementService).getStoreRoleInfo(ownerId, storeId);
    }

    @Test
    void getStoreRoleInfo_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";

        when(storeManagementService.getStoreRoleInfo(ownerId, storeId))
            .thenThrow(new RuntimeException("Failed to get store role info"));

        // Act
        String result = ownerManagerService.getStoreRoleInfo(ownerId, storeId);

        // Assert
        assertNull(result);
        verify(storeManagementService).getStoreRoleInfo(ownerId, storeId);
    }

    @Test
    void getManagerPermissions_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String managerId = "manager1";
        Map<String, Boolean> expectedPermissions = new HashMap<>();
        expectedPermissions.put("canEditInventory", true);
        expectedPermissions.put("canEditPolicies", false);

        when(storeManagementService.getManagerPermissions(ownerId, storeId, managerId)).thenReturn(expectedPermissions);

        // Act
        Map<String, Boolean> result = ownerManagerService.getManagerPermissions(ownerId, storeId, managerId);

        // Assert
        assertEquals(expectedPermissions, result);
        verify(storeManagementService).getManagerPermissions(ownerId, storeId, managerId);
    }

    @Test
    void getManagerPermissions_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String managerId = "manager1";

        when(storeManagementService.getManagerPermissions(ownerId, storeId, managerId))
            .thenThrow(new RuntimeException("Failed to get manager permissions"));

        // Act
        Map<String, Boolean> result = ownerManagerService.getManagerPermissions(ownerId, storeId, managerId);

        // Assert
        assertNull(result);
        verify(storeManagementService).getManagerPermissions(ownerId, storeId, managerId);
    }

    // ==================== 6. Customer Inquiry Tests ====================

    @Test
    void getCustomerInquiries_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        List<Map<String, Object>> expectedInquiries = new ArrayList<>();
        Map<String, Object> inquiry = new HashMap<>();
        inquiry.put("id", "inquiry1");
        inquiry.put("message", "Test inquiry");
        expectedInquiries.add(inquiry);

        when(queryMicroservice.getCustomerInquiries(ownerId, storeId)).thenReturn(expectedInquiries);

        // Act
        List<Map<String, Object>> result = ownerManagerService.getCustomerInquiries(ownerId, storeId);

        // Assert
        assertEquals(expectedInquiries, result);
        verify(queryMicroservice).getCustomerInquiries(ownerId, storeId);
    }

    @Test
    void getCustomerInquiries_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";

        when(queryMicroservice.getCustomerInquiries(ownerId, storeId))
            .thenThrow(new RuntimeException("Failed to get customer inquiries"));

        // Act
        List<Map<String, Object>> result = ownerManagerService.getCustomerInquiries(ownerId, storeId);

        // Assert
        assertNull(result);
        verify(queryMicroservice).getCustomerInquiries(ownerId, storeId);
    }

    @Test
    void respondToCustomerInquiry_Success() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String inquiryId = "inquiry1";
        String response = "Thank you for your inquiry";

        when(queryMicroservice.respondToCustomerInquiry(ownerId, storeId, inquiryId, response)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.respondToCustomerInquiry(ownerId, storeId, inquiryId, response);

        // Assert
        assertTrue(result);
        verify(queryMicroservice).respondToCustomerInquiry(ownerId, storeId, inquiryId, response);
    }

    @Test
    void respondToCustomerInquiry_Failure() {
        // Arrange
        String ownerId = "owner1";
        String storeId = "store1";
        String inquiryId = "inquiry1";
        String response = "Thank you for your inquiry";

        when(queryMicroservice.respondToCustomerInquiry(ownerId, storeId, inquiryId, response))
            .thenThrow(new RuntimeException("Failed to respond to customer inquiry"));

        // Act
        boolean result = ownerManagerService.respondToCustomerInquiry(ownerId, storeId, inquiryId, response);

        // Assert
        assertFalse(result);
        verify(queryMicroservice).respondToCustomerInquiry(ownerId, storeId, inquiryId, response);
    }

    // ==================== 7. Purchase History Tests ====================

    // Purchase history tests have been removed due to changes in the microservices

    // ==================== 8. Manager Functions Tests ====================

    @Test
    void managerAddProduct_Success() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productName = "Test Product";
        String description = "Test Description";
        double price = 10.0;
        int quantity = 5;
        String category = "Test Category";
        String expectedProductId = "product1";

        when(inventoryService.addProduct(managerId, storeId, productName, description, price, quantity, category))
            .thenReturn(expectedProductId);

        // Act
        String result = ownerManagerService.managerAddProduct(managerId, storeId, productName, description, price, quantity, category);

        // Assert
        assertEquals(expectedProductId, result);
        verify(inventoryService).addProduct(managerId, storeId, productName, description, price, quantity, category);
    }

    @Test
    void managerAddProduct_Failure() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productName = "Test Product";
        String description = "Test Description";
        double price = 10.0;
        int quantity = 5;
        String category = "Test Category";

        when(inventoryService.addProduct(managerId, storeId, productName, description, price, quantity, category))
            .thenThrow(new RuntimeException("Failed to add product"));

        // Act
        String result = ownerManagerService.managerAddProduct(managerId, storeId, productName, description, price, quantity, category);

        // Assert
        assertNull(result);
        verify(inventoryService).addProduct(managerId, storeId, productName, description, price, quantity, category);
    }

    @Test
    void managerUpdateProductDetails_Success() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productId = "product1";
        String productName = "Updated Product";
        String description = "Updated Description";
        double price = 15.0;
        String category = "Updated Category";

        when(inventoryService.updateProductDetails(managerId, storeId, productId, productName, description, price, category))
            .thenReturn(true);

        // Act
        boolean result = ownerManagerService.managerUpdateProductDetails(managerId, storeId, productId, productName, description, price, category);

        // Assert
        assertTrue(result);
        verify(inventoryService).updateProductDetails(managerId, storeId, productId, productName, description, price, category);
    }

    @Test
    void managerUpdateProductDetails_Failure() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productId = "product1";
        String productName = "Updated Product";
        String description = "Updated Description";
        double price = 15.0;
        String category = "Updated Category";

        when(inventoryService.updateProductDetails(managerId, storeId, productId, productName, description, price, category))
            .thenThrow(new RuntimeException("Failed to update product details"));

        // Act
        boolean result = ownerManagerService.managerUpdateProductDetails(managerId, storeId, productId, productName, description, price, category);

        // Assert
        assertFalse(result);
        verify(inventoryService).updateProductDetails(managerId, storeId, productId, productName, description, price, category);
    }

    @Test
    void managerUpdateProductQuantity_Success() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productId = "product1";
        int newQuantity = 10;

        when(inventoryService.updateProductQuantity(managerId, storeId, productId, newQuantity))
            .thenReturn(true);

        // Act
        boolean result = ownerManagerService.managerUpdateProductQuantity(managerId, storeId, productId, newQuantity);

        // Assert
        assertTrue(result);
        verify(inventoryService).updateProductQuantity(managerId, storeId, productId, newQuantity);
    }

    @Test
    void managerUpdateProductQuantity_Failure() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productId = "product1";
        int newQuantity = 10;

        when(inventoryService.updateProductQuantity(managerId, storeId, productId, newQuantity))
            .thenThrow(new RuntimeException("Failed to update product quantity"));

        // Act
        boolean result = ownerManagerService.managerUpdateProductQuantity(managerId, storeId, productId, newQuantity);

        // Assert
        assertFalse(result);
        verify(inventoryService).updateProductQuantity(managerId, storeId, productId, newQuantity);
    }

    @Test
    void managerRemoveProduct_Success() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productId = "product1";

        when(inventoryService.removeProduct(managerId, storeId, productId)).thenReturn(true);

        // Act
        boolean result = ownerManagerService.managerRemoveProduct(managerId, storeId, productId);

        // Assert
        assertTrue(result);
        verify(inventoryService).removeProduct(managerId, storeId, productId);
    }

    @Test
    void managerRemoveProduct_Failure() {
        // Arrange
        String managerId = "manager1";
        String storeId = "store1";
        String productId = "product1";

        when(inventoryService.removeProduct(managerId, storeId, productId))
            .thenThrow(new RuntimeException("Failed to remove product"));

        // Act
        boolean result = ownerManagerService.managerRemoveProduct(managerId, storeId, productId);

        // Assert
        assertFalse(result);
        verify(inventoryService).removeProduct(managerId, storeId, productId);
    }

    // Admin operations have been moved to AdminService class
}
