//package DomainLayer.DomainServices;
//
//import DomainLayer.DomainServices.InventoryManagementMicroservice;
//import DomainLayer.Store;
//import DomainLayer.Product;
//import InfrastructureLayer.ProductRepository;
//import InfrastructureLayer.StoreRepository;
//import static DomainLayer.ManagerPermissions.*;
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//public class InventoryManagementMicroserviceTest {
//
//    @Mock
//    private StoreRepository storeRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private Store store;
//
//    private InventoryManagementMicroservice inventoryManagementMicroservice;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        inventoryManagementMicroservice = new InventoryManagementMicroservice(storeRepository, productRepository);
//    }
//
//    @Test
//    public void testAddProductWithPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productName = "Product 1";
//        String description = "Product description";
//        float price = 10.0f;
//        int quantity = 100;
//        String category = "Category A";
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(true);  // Owner has permission
//
//        // Mock productRepository save method
//        Product mockProduct = new Product(storeId, productName, description, price, quantity, -1, category);
//        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
//
//        // Call the method
//        String result = inventoryManagementMicroservice.addProduct(userId, storeId, productName, description, price, quantity, category);
//
//        // Verify and assert the result
//        verify(storeRepository).update(store);
//        assertNotNull(result);  // Check that a product ID was returned
//    }
//
//    @Test
//    public void testAddProductWithoutPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productName = "Product 1";
//        String description = "Product description";
//        float price = 10.0f;
//        int quantity = 100;
//        String category = "Category A";
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(false);
//        when(store.userIsManager(userId)).thenReturn(true);
//        when(store.userHasPermissions(userId, PERM_ADD_PRODUCT)).thenReturn(false); // No permission for manager
//
//        // Call the method
//        String result = inventoryManagementMicroservice.addProduct(userId, storeId, productName, description, price, quantity, category);
//
//        // Verify and assert the result
//        verify(storeRepository, never()).update(store); // Ensure update was not called
//        assertNull(result);  // No product added due to lack of permission
//    }
//
//    @Test
//    public void testRemoveProductWithPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productId = "product123";
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(true);  // Owner has permission
//
//        // Mock store behavior
//        when(store.removeProduct(productId)).thenReturn(true);
//
//        // Call the method
//        boolean result = inventoryManagementMicroservice.removeProduct(userId, storeId, productId);
//
//        // Verify and assert the result
//        assertTrue(result);  // Product should be removed
//        verify(storeRepository).update(store);  // Ensure the store was updated
//    }
//
//    @Test
//    public void testRemoveProductWithoutPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productId = "product123";
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(false);
//        when(store.userIsManager(userId)).thenReturn(true);
//        when(store.userHasPermissions(userId, PERM_REMOVE_PRODUCT)).thenReturn(false);  // No permission for manager
//
//        // Call the method
//        boolean result = inventoryManagementMicroservice.removeProduct(userId, storeId, productId);
//
//        // Verify and assert the result
//        assertFalse(result);  // Product should not be removed
//        verify(storeRepository, never()).update(store);  // Ensure the store was not updated
//    }
//
//    @Test
//    public void testUpdateProductDetailsWithPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productId = "product123";
//        String productName = "Updated Product";
//        String description = "Updated description";
//        double price = 15.0;
//        String category = "Updated Category";
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(true);  // Owner has permission
//
//        // Mock store behavior
//        when(store.updateProductDetails(productId, productName, description, price, category)).thenReturn(true);
//
//        // Call the method
//        boolean result = inventoryManagementMicroservice.updateProductDetails(userId, storeId, productId, productName, description, price, category);
//
//        // Verify and assert the result
//        assertTrue(result);  // Product details should be updated
//        verify(storeRepository).update(store);  // Ensure the store was updated
//    }
//
//    @Test
//    public void testUpdateProductDetailsWithoutPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productId = "product123";
//        String productName = "Updated Product";
//        String description = "Updated description";
//        double price = 15.0;
//        String category = "Updated Category";
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(false);
//        when(store.userIsManager(userId)).thenReturn(true);
//        when(store.userHasPermissions(userId, PERM_UPDATE_PRODUCT)).thenReturn(false);  // No permission for manager
//
//        // Call the method
//        boolean result = inventoryManagementMicroservice.updateProductDetails(userId, storeId, productId, productName, description, price, category);
//
//        // Verify and assert the result
//        assertFalse(result);  // Product details should not be updated
//        verify(storeRepository, never()).update(store);  // Ensure the store was not updated
//    }
//
//    @Test
//    public void testUpdateProductQuantityWithPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productId = "product123";
//        int newQuantity = 200;
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(true);  // Owner has permission
//
//        // Mock store behavior
//        when(store.updateProductQuantity(productId, newQuantity)).thenReturn(true);
//
//        // Call the method
//        boolean result = inventoryManagementMicroservice.updateProductQuantity(userId, storeId, productId, newQuantity);
//
//        // Verify and assert the result
//        assertTrue(result);  // Product quantity should be updated
//        verify(storeRepository).update(store);  // Ensure the store was updated
//    }
//
//    @Test
//    public void testUpdateProductQuantityWithoutPermission() {
//        // Setup
//        String userId = "user123";
//        String storeId = "store123";
//        String productId = "product123";
//        int newQuantity = 200;
//
//        // Mock behavior for storeRepository and productRepository
//        when(storeRepository.getById(storeId)).thenReturn(store);
//        when(store.userIsOwner(userId)).thenReturn(false);
//        when(store.userIsManager(userId)).thenReturn(true);
//        when(store.userHasPermissions(userId, PERM_UPDATE_PRODUCT)).thenReturn(false);  // No permission for manager
//
//        // Call the method
//        boolean result = inventoryManagementMicroservice.updateProductQuantity(userId, storeId, productId, newQuantity);
//
//        // Verify and assert the result
//        assertFalse(result);  // Product quantity should not be updated
//        verify(storeRepository, never()).update(store);  // Ensure the store was not updated
//    }
//}