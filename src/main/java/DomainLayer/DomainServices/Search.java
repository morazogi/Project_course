package DomainLayer.DomainServices;


import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import ServiceLayer.EventLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Search {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public Search(ProductRepository productRepository, StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    public String searchByName(String partialName) throws JsonProcessingException {
        List<Product> matches = productRepository.getAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(partialName.toLowerCase()))
                .toList();

        EventLogger.logEvent("SEARCH_BY_NAME", "Query=" + partialName + " Matches=" + matches.size());
        return mapper.writeValueAsString(matches);
    }

    public List<Store> searchStoreByName(String partialName) {
        List<Store> matches = storeRepository.getAll().stream()
                .filter(store -> {
                    try {
                    if(store.getName().toLowerCase().contains(partialName.toLowerCase())) {
                        return true;}
                    }
                    catch (Exception e) {
                        EventLogger.logEvent("username", "FIND_BY_STORE_FAILED - STORE_NOT_FOUND");
                        throw new IllegalArgumentException("Product not found");
                    } return false;})
                .toList();

        EventLogger.logEvent("SEARCH_STORE_BY_NAME", "Query=" + partialName + " Matches=" + matches.size());
        return matches;
    }

    public String searchByCategory(String category) throws JsonProcessingException {
        List<Product> matches = productRepository.getAll().stream()
                .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                .toList();

        EventLogger.logEvent("SEARCH_BY_CATEGORY", "Category=" + category + " Matches=" + matches.size());
        return mapper.writeValueAsString(matches);
    }

    public List<Product> getProductsByStore(String storeId) throws JsonProcessingException {
        Store store = storeRepository.getById(storeId);
        if (store == null) {
            EventLogger.logEvent("SEARCH_BY_STORE", "Store=" + storeId + " NOT_FOUND");
            throw new IllegalArgumentException("Store not found");
        }

        List<Product> result = new ArrayList<>();
        for (String productId : store.getProducts().keySet()) {
            Product product = productRepository.getById(productId);
            if (product != null) {
                result.add(product);
            }
        }

        EventLogger.logEvent("SEARCH_BY_STORE", "Store=" + storeId + " Matches=" + result.size());
        return result;
    }

    public List<String> findProduct(String name, String category) {
        try {
            List<String> result = new ArrayList<>();
            for (Product p : productRepository.getAll()) {
                boolean nameMatch = p.getName().toLowerCase()
                        .contains(name.toLowerCase());
                boolean catMatch  = category == null || category.isBlank()
                        || p.getCategory().equalsIgnoreCase(category);
                if (nameMatch && catMatch) result.add(p.getId());
            }
            EventLogger.logEvent("SEARCH_BY_PRODUCT",
                    "Name=" + name + " Category=" + category +
                            " Matches=" + result.size());
            return result;

        } catch (Exception e) {
            System.out.println("ERROR finding product by Name:" + e.getMessage());
            return Collections.emptyList();
        }
    }



    public List<String> getStoreByName(String name) {
        try {
            List<String> result = storeRepository.getAll().stream()
                    .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
                    .map(Store::getId)                // collect the store IDs
                    .toList();

            EventLogger.logEvent(
                    "SEARCH_STORE_BY_NAME",
                    "Query=" + name + " Matches=" + result.size());
            return result;
        } catch (Exception e) {
            EventLogger.logEvent("username", "SEARCH_STORE_BY_NAME_FAILED " + e.getMessage());
            throw new IllegalArgumentException("Failed to search store by name");
        }
    }
    public String getStoreById(String storeId) throws JsonProcessingException {
        Store store = storeRepository.getById(storeId);
        Hibernate.initialize(store);
        if (store == null) {
            EventLogger.logEvent("SEARCH_STORE_BY_ID", "Store=" + storeId + " NOT_FOUND");
            throw new IllegalArgumentException("Store not found");
        }
        EventLogger.logEvent("SEARCH_STORE_BY_ID", "Store=" + storeId + " FOUND");
        return mapper.writeValueAsString(store);
    }

}