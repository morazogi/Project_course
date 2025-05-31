package DomainLayer;

import DomainLayer.Product;
import DomainLayer.IProductRepository;
import DomainLayer.IStoreRepository;
import DomainLayer.Store;
import DomainLayer.DomainServices.Search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchTest {

    private IProductRepository productRepository;
    private IStoreRepository storeRepository;
    private Search search;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        productRepository = mock(IProductRepository.class);
        storeRepository = mock(IStoreRepository.class);
        search = new Search(productRepository, storeRepository);
    }

    @Test
    void searchByName_ShouldReturnMatchingProducts() throws JsonProcessingException {
        // Arrange
        Product product = new Product("1", "store1", "Socks", "Warm", 10, 5, 3.0, "clothing");
        when(productRepository.findAll()).thenReturn(List.of(product));

        // Act
        String json = search.searchByName("Socks");

        // Assert
        assertTrue(json.contains("Socks"));
        verify(productRepository).findAll();
    }

    @Test
    void searchByCategory_ShouldReturnMatchingProducts() throws JsonProcessingException {
        // Arrange
        Product product = new Product("2", "store1", "Hat", "Stylish", 20, 5, 4.5, "accessories");
        when(productRepository.findAll()).thenReturn(List.of(product));

        // Act
        String json = search.searchByCategory("accessories");

        // Assert
        assertTrue(json.contains("Hat"));
        verify(productRepository).findAll();
    }

//    @Test
//    void getProductsByStore_ShouldReturnStoreProducts() throws JsonProcessingException {
//        // Arrange
//        Product product = new Product("3", "store2", "Shoes", "Running shoes", 50, 10, 4.0, "shoes");
//        Store store = new Store();
//        store.setId("store2");
//        store.addNewProduct(product.getId(), 10);
//
//        when(storeRepository.getStore("store2")).thenReturn(mapper.writeValueAsString(store));
//        when(productRepository.getProduct("3")).thenReturn(product);
//
//        // Act
//        String json = search.getProductsByStore("store2");
//
//        // Assert
//        assertTrue(json.contains("Shoes"));
//        verify(storeRepository).getStore("store2");
//        verify(productRepository).getProduct("3");
//    }

//    @Test
//    void getProductsByStore_ShouldThrowException_WhenStoreNotFound() {
//        // Arrange
//        when(storeRepository.getStore("invalid-store")).thenReturn(null);
//
//        // Act & Assert
//        Exception e = assertThrows(IllegalArgumentException.class, () ->
//                search.getProductsByStore("invalid-store")
//        );
//        assertEquals("Store not found", e.getMessage());
//        verify(storeRepository).getStore("invalid-store");
//    }
}
