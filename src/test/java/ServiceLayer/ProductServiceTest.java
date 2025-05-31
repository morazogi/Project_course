/*
package ServiceLayer;

 import DomainLayer.Product;
 import DomainLayer.IProductRepository;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.mockito.Mockito;

 import java.util.Optional;
 import java.util.List;
 import java.util.ArrayList;

 import static org.junit.jupiter.api.Assertions.*;
         import static org.mockito.Mockito.*;

 class ProductServiceTest {

     private IProductRepository productRepo;
     private ProductService productService;

     @BeforeEach
     void setUp() {
         productRepo = Mockito.mock(IProductRepository.class);
         productService = new ProductService(productRepo);
     }

     @Test
     void addProduct_ShouldCallSaveAndReturnTrue() {
         // Arrange
         Product product = new Product("p1", "1", "Laptop", "Gaming", 3000, 5 , 3.0, "category");

         // Act
         boolean result = productService.addProduct(product);

         // Assert
         assertTrue(result);
         verify(productRepo, times(1)).save(product);
     }

     @Test
     void getProductById_ShouldReturnProduct_WhenExists() {
         // Arrange
         Product product = new Product("p2", "1", "Phone", "Smartphone", 2000, 10 , 3.0, "category");
         when(productRepo.findById("p2")).thenReturn(Optional.of(product));

         // Act
         Optional<Product> result = productService.getProductById("p2");

         // Assert
         assertTrue(result.isPresent());
         assertEquals("Phone", result.get().getName());
     }

     @Test
     void increaseQuantity_ShouldAddToQuantityAndSave() {
         // Arrange
         Product product = new Product("p3", "1", "Tablet", "Android tablet", 1500, 3 , 3.0, "category");
         when(productRepo.findById("p3")).thenReturn(Optional.of(product));

         // Act
         boolean result = productService.increaseQuantity("p3", 2);

         // Assert
         assertTrue(result);
         assertEquals(5, product.getQuantity());
         verify(productRepo).save(product);
     }

     @Test
     void decreaseQuantity_ShouldFail_WhenNotEnoughStock() {
         // Arrange
         Product product = new Product("p4", "1", "Monitor", "4K Monitor", 1000, 1 , 3.0, "category");
         when(productRepo.findById("p4")).thenReturn(Optional.of(product));

         // Act
         boolean result = productService.decreaseQuantity("p4", 2);

         // Assert
         assertFalse(result);
         assertEquals(1, product.getQuantity()); // לא השתנה
         verify(productRepo, never()).save(product);
     }

     @Test
     void getAllProducts_ShouldReturnList() {
         // Arrange
         List<Product> products = new ArrayList<>();
         products.add(new Product("p1", "1", "Laptop", "Gaming", 3000, 5 , 3.0, "category"));
         products.add(new Product("p2", "1", "Phone", "Smartphone", 2000, 10 , 3.0, "category"));
         when(productRepo.findAll()).thenReturn(products);

         // Act
         List<Product> result = productService.getAllProducts();

         // Assert
         assertEquals(2, result.size());
     }
 }
*/