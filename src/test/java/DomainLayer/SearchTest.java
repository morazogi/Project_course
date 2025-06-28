package DomainLayer;

import DomainLayer.DomainServices.Search;
import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchTest {

    /* ------------ mocked repositories ------------ */
    @Mock ProductRepository productRepo;
    @Mock StoreRepository   storeRepo;

    private Search service;
    private final ObjectMapper mapper = new ObjectMapper();

    /* ------------ domain fixtures ------------ */
    private Store   store;
    private Product apple;          // Food
    private Product headphones;     // Tech

    @BeforeEach
    void setup() {
        service = new Search(productRepo, storeRepo);

        /* ---------- store ---------- */
        store = new Store("owner-1", "TechMart");
        store.setId("store-1");

        /* ---------- products ---------- */
        apple = new Product(store.getId(), "Apple", "", 15f, 100, 0d, "Food");
        apple.setId("p-apple");

        headphones = new Product(store.getId(), "Headphones", "", 22f, 100, 0d, "Tech");
        headphones.setId("p-head");
    }

    /* ------------------------------------------------
                          searchByName
       ------------------------------------------------ */
    @Test
    void searchByName_partialMatch_returnsOneProduct() throws Exception {
        when(productRepo.getAll()).thenReturn(List.of(apple, headphones));

        String json = service.searchByName("app");           // matches “Apple”
        List<Product> result = mapper.readValue(json, new TypeReference<>() {});

        assertEquals(1, result.size());
        assertEquals(apple.getId(), result.get(0).getId());  // <-- get(0) works on Java 17
    }

    /* ------------------------------------------------
                        searchByCategory
       ------------------------------------------------ */
    @Test
    void searchByCategory_caseInsensitive() throws Exception {
        when(productRepo.getAll()).thenReturn(List.of(apple, headphones));

        String json = service.searchByCategory("FOOD");      // uppercase on purpose
        List<Product> result = mapper.readValue(json, new TypeReference<>() {});

        assertEquals(List.of(apple.getId()),
                result.stream().map(Product::getId).toList());
    }

    /* ------------------------------------------------
                       getProductsByStore
       ------------------------------------------------ */
    @Test
    void getProductsByStore_returnsAllProductsInStore() throws Exception {
        // store contains 2 apples & 1 headphones
        store.setProducts(Map.of(apple.getId(), 2, headphones.getId(), 1));

        when(storeRepo.getById(store.getId())).thenReturn(store);
        when(productRepo.getById(apple.getId())).thenReturn(apple);
        when(productRepo.getById(headphones.getId())).thenReturn(headphones);

        List<Product> result = service.getProductsByStore(store.getId());

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(apple, headphones)));
    }

    @Test
    void getProductsByStore_storeMissing_throwsIllegalArgument() {
        when(storeRepo.getById("no-such")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.getProductsByStore("no-such"));
    }

    /* ------------------------------------------------
                           findProduct
       ------------------------------------------------ */
    @Test
    void findProduct_nameAndCategory_filtersCorrectly() {
        when(productRepo.getAll()).thenReturn(List.of(apple, headphones));

        List<String> ids = service.findProduct("head", "tech");   // case-insensitive
        assertEquals(List.of(headphones.getId()), ids);
    }

    /* ------------------------------------------------
                           getStoreById
       ------------------------------------------------ */
    @Test
    void getStoreById_returnsJsonOfStore() throws Exception {
        when(storeRepo.getById(store.getId())).thenReturn(store);

        String json = service.getStoreById(store.getId());
        assertTrue(json.contains("\"name\":\"TechMart\""));
    }
}
