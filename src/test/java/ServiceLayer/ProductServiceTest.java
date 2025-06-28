package ServiceLayer;

import DomainLayer.Product;
import InfrastructureLayer.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductService} that verify repository delegation and
 * basic business checks.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock ProductRepository repo;
    private ProductService  service;

    private Product apple;
    private Product tv;

    @BeforeEach
    void init() {
        service = new ProductService(repo);

        apple = new Product("store", "Apple", "", 3f, 10, 0, "Food");
        apple.setId("p1");

        tv = new Product("store", "TV", "", 500f, 2, 4, "Electronics");
        tv.setId("p2");
    }

    /* -----------------------------------------------------------
                         retrieval helpers
       ----------------------------------------------------------- */
    @Test
    void getProductById_returnsOptional() {
        when(repo.findById("p1")).thenReturn(Optional.of(apple));
        assertEquals(Optional.of(apple), service.getProductById("p1"));
    }

    @Test
    void getProductById_repoThrows_returnsEmpty() {
        when(repo.findById(any())).thenThrow(new RuntimeException());
        assertTrue(service.getProductById("x").isEmpty());
    }

    /* -----------------------------------------------------------
                           removeProduct
       ----------------------------------------------------------- */
    @Test
    void removeProduct_success_returnsTrue() {
        doNothing().when(repo).deleteById("p1");
        assertTrue(service.removeProduct("p1"));
        verify(repo).deleteById("p1");
    }

    @Test
    void removeProduct_repoThrows_returnsFalse() {
        doThrow(new RuntimeException()).when(repo).deleteById("p1");
        assertFalse(service.removeProduct("p1"));
    }

    /* -----------------------------------------------------------
                        increase / decrease quantity
       ----------------------------------------------------------- */
    @Test
    void increaseQuantity_happyPath_updatesAndSaves() {
        when(repo.findById("p1")).thenReturn(Optional.of(apple));

        assertTrue(service.increaseQuantity("p1", 5));  // 10 → 15

        ArgumentCaptor<Product> cap = ArgumentCaptor.forClass(Product.class);
        verify(repo).save(cap.capture());
        assertEquals(15, cap.getValue().getQuantity());
    }

    @Test
    void increaseQuantity_productMissing_returnsFalse() {
        when(repo.findById("pX")).thenReturn(Optional.empty());
        assertFalse(service.increaseQuantity("pX", 1));
    }

    @Test
    void decreaseQuantity_enoughStock_updatesAndSaves() {
        when(repo.findById("p2")).thenReturn(Optional.of(tv));  // qty = 2

        assertTrue(service.decreaseQuantity("p2", 1));          // 2 → 1
        verify(repo).save(argThat(p -> p.getQuantity() == 1));
    }

    @Test
    void decreaseQuantity_notEnoughStock_returnsFalse() {
        when(repo.findById("p2")).thenReturn(Optional.of(tv));  // qty = 2
        assertFalse(service.decreaseQuantity("p2", 5));
        verify(repo, never()).save(any());
    }

    /* -----------------------------------------------------------
                              updateRating
       ----------------------------------------------------------- */
    @Test
    void updateRating_validRange_savesNewRating() {
        when(repo.findById("p2")).thenReturn(Optional.of(tv));

        assertTrue(service.updateRating("p2", 5));
        verify(repo).save(argThat(p -> p.getRating() == 5));
    }

    @Test
    void updateRating_outOfRange_returnsFalse() {
        assertFalse(service.updateRating("p2", 8));  // > 5 not allowed
        verify(repo, never()).save(any());
    }

    /* -----------------------------------------------------------
                           getByCategory
       ----------------------------------------------------------- */
    @Test
    void getProductByCategory_filtersCorrectly() {
        when(repo.findAll()).thenReturn(List.of(apple, tv));

        List<Product> res = service.getProductByCategory("Food");

        assertEquals(List.of(apple), res);
    }
}
