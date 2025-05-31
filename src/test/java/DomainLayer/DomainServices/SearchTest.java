package DomainLayer.DomainServices;

import DomainLayer.*;
import DomainLayer.DomainServices.Search;
import ServiceLayer.EventLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the rewritten {@link Search} class.
 */
@ExtendWith(MockitoExtension.class)
class SearchTest {

    /* ----------- collaborators ----------- */
    @Mock IProductRepository productRepo;
    @Mock IStoreRepository   storeRepo;

    Search sut;                              // system under test
    ObjectMapper mapper = new ObjectMapper();// helper for JSON

    @BeforeEach
    void setUp() {
        sut = new Search(productRepo, storeRepo);
    }

    /* ====================================================================== */
    /* 1. searchByName                                                        */
    /* ====================================================================== */
    @Test
    void searchByName_happy_returnsMatchingJson() throws Exception {
        Product p1 = product("p1", "Chocolate Bar", "Snacks");
        Product p2 = product("p2", "Choco Crackers", "Snacks");
        Product p3 = product("p3", "Potato Chips",   "Snacks");
        when(productRepo.findAll()).thenReturn(List.of(p1, p2, p3));

        try (MockedStatic<EventLogger> logs = mockStatic(EventLogger.class)) {
            String json = sut.searchByName("cho");

            // deserialize to List<Product>
            List<Product> list = mapper.readValue(json, new TypeReference<>() {});
            assertEquals(2, list.size());
            assertTrue(list.stream().map(Product::getId).toList()
                           .containsAll(List.of("p1", "p2")));

            logs.verify(() -> EventLogger.logEvent(eq("SEARCH_BY_NAME"),
                                                   contains("Matches=2")));
        }
    }

    /* ====================================================================== */
    /* 2. searchByCategory                                                    */
    /* ====================================================================== */
    @Test
    void searchByCategory_happy_onlySelectedCategory() throws Exception {
        var all = List.of(
            product("p1","Milk","Dairy"),
            product("p2","Cheddar","Dairy"),
            product("p3","Apple","Fruit")
        );
        when(productRepo.findAll()).thenReturn(all);

        try (MockedStatic<EventLogger> logs = mockStatic(EventLogger.class)) {
            String json = sut.searchByCategory("Dairy");

            List<Product> list = mapper.readValue(json, new TypeReference<>() {});
            assertEquals(2, list.size());

            logs.verify(() -> EventLogger.logEvent(eq("SEARCH_BY_CATEGORY"),
                                                   contains("Matches=2")));
        }
    }

    /* ====================================================================== */
    /* 3. getProductsByStore                                                  */
    /* ====================================================================== */
    @Test
    void getProductsByStore_happy_storeExists() throws Exception {
        Product p1 = product("p1","TV","Electronics");
        Product p2 = product("p2","Mouse","Electronics");

        Store store = store("s1","Tech-Shop", Map.of("p1",10,"p2",5));
        when(storeRepo.getStore("s1")).thenReturn(mapper.writeValueAsString(store));
        when(productRepo.getReferenceById("p1")).thenReturn(p1);
        when(productRepo.getReferenceById("p2")).thenReturn(p2);

        try (MockedStatic<EventLogger> logs = mockStatic(EventLogger.class)) {
            String json = sut.getProductsByStore("s1");

            List<Product> list = mapper.readValue(json, new TypeReference<>() {});
            assertEquals(2, list.size());
            assertEquals(Set.of("p1","p2"),
                         list.stream().map(Product::getId).collect(java.util.stream.Collectors.toSet()));

            logs.verify(() -> EventLogger.logEvent(eq("SEARCH_BY_STORE"),
                                                   contains("Matches=2")));
        }
    }

    @Test
    void getProductsByStore_bad_storeMissingThrows() throws Exception {
        when(storeRepo.getStore("missing")).thenReturn("null");

        try (MockedStatic<EventLogger> logs = mockStatic(EventLogger.class)) {
            assertThrows(IllegalArgumentException.class,
                () -> sut.getProductsByStore("missing"));

            logs.verify(() -> EventLogger.logEvent(eq("SEARCH_BY_STORE"),
                                                   contains("NOT_FOUND")));
        }
    }

    /* ====================================================================== */
    /* 4. getStoreByName                                                      */
    /* ====================================================================== */
    @Test
    void getStoreByName_happy_filtersByPartialName() throws Exception {
        Store s1 = store("s1","MegaMart",  Map.of());
        Store s2 = store("s2","MiniMart",  Map.of());
        Store s3 = store("s3","Electro",   Map.of());

        when(storeRepo.findAll()).thenReturn(List.of(
            mapper.writeValueAsString(s1),
            mapper.writeValueAsString(s2),
            mapper.writeValueAsString(s3)));

        try (MockedStatic<EventLogger> logs = mockStatic(EventLogger.class)) {
            List<String> matches = sut.getStoreByName("mart");

            assertEquals(2, matches.size());              // MegaMart & MiniMart
            logs.verify(() -> EventLogger.logEvent(eq("SEARCH_BY_STORE_NAME"),
                                                   contains("Matches=2")));
        }
    }

    /* ====================================================================== */
    /* 5. findProduct                                                         */
    /* ====================================================================== */
    @Test
    void findProduct_happy_nameAndCategory() {
        Product p1 = product("p1","Nike Shoes","Sports");
        Product p2 = product("p2","Adidas Shoes","Sports");
        Product p3 = product("p3","Football","Sports");

        when(productRepo.findAll()).thenReturn(List.of(p1, p2, p3));

        try (MockedStatic<EventLogger> logs = mockStatic(EventLogger.class)) {
            List<String> ids = sut.findProduct("shoes", "Sports");
            assertEquals(List.of("p1","p2"), ids);

            logs.verify(() -> EventLogger.logEvent(eq("SEARCH_BY_PRODUCT"),
                                                   contains("Matches=2")));
        }
    }

    /* ====================================================================== */
    /* helpers                                                                */
    /* ====================================================================== */
    private static Product product(String id, String name, String category) {
        Product p = new Product();   // assumes default ctor + setters
        p.setId(id);
        p.setName(name);
        p.setCategory(category);
        return p;
    }

    private static Store store(String id, String name, Map<String,Integer> products) {
        Store s = new Store();
        s.setId(id);
        s.setName(name);
        s.setProducts(new java.util.HashMap<>(products)); // assumes setter
        return s;
    }
}
