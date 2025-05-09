package DomainLayer.DomainServices;

import DomainLayer.Product;
import DomainLayer.IProductRepository;
import DomainLayer.IStoreRepository;
import DomainLayer.Store;
import DomainLayer.domainServices.Search;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchTest {

    private IProductRepository productRepository;
    private IStoreRepository storeRepository;
    private Search search;

    @BeforeEach
    void setUp() {
        productRepository = mock(IProductRepository.class);
        storeRepository = mock(IStoreRepository.class);
        search = new Search(productRepository, storeRepository);
    }

    @Test
    void searchByName_ShouldReturnMatchingProducts() throws JsonProcessingException {
        Product product = new Product("1", "store1", "Socks", "Warm", 10, 5, 3.0, "clothing");
        when(productRepository.findAll()).thenReturn(List.of(product));

        String json = search.searchByName("Socks");

        assertTrue(json.contains("Socks"));
        verify(productRepository).findAll();
    }

    @Test
    void searchByCategory_ShouldReturnMatchingProducts() throws JsonProcessingException {
        Product product = new Product("2", "store1", "Hat", "Stylish", 20, 5, 4.5, "accessories");
        when(productRepository.findAll()).thenReturn(List.of(product));

        String json = search.searchByCategory("accessories");

        assertTrue(json.contains("Hat"));
        verify(productRepository).findAll();
    }

    @Test
    void getProductsByStore_ShouldReturnStoreProducts() throws JsonProcessingException {
        // Arrange
        Product product = new Product("3", "store2", "Shoes", "Running shoes", 50, 10, 4.0, "shoes");

        Store store = new Store();
        store.setId("store2");
        store.getProducts().put(product.getId(), 10);  // requires getProducts() in Store.java

        when(storeRepository.getStore("store2")).thenReturn(String.valueOf(store));
        when(productRepository.getProduct("3")).thenReturn(product);

        String json = search.getProductsByStore("store2");

        assertTrue(json.contains("Shoes"));
        verify(storeRepository).getStore("store2");
        verify(productRepository).getProduct("3");
    }

    @Test
    void getProductsByStore_ShouldThrowException_WhenStoreNotFound() {
        when(storeRepository.getStore("invalid-store")).thenReturn(null);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                search.getProductsByStore("invalid-store")
        );

        assertEquals("Store not found", e.getMessage());
        verify(storeRepository).getStore("invalid-store");
    }
}
