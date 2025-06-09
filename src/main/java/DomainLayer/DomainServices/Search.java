package DomainLayer.DomainServices;


import DomainLayer.Product;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import ServiceLayer.EventLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public String getProductsByStore(String storeId) throws JsonProcessingException {
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
        return mapper.writeValueAsString(result);
    }

    public List<String> findProduct(String name, String category) {
        try {
            List<Product> products = productRepository.getAll();
            List<String> result = new ArrayList<>();
            for (Product product : products) {
                if (product.getName().toLowerCase().contains(name.toLowerCase()) &&
                        (category == null || product.getCategory().equalsIgnoreCase(category))) {
                    result.add(product.getId());
                }
            }
            EventLogger.logEvent("SEARCH_BY_PRODUCT", "Name=" + name + " Category=" + category + " Matches=" + result.size());
            return result;
        } catch (Exception e) {
            System.out.println("ERROR finding product by Name:" + e.getMessage());
            return Collections.emptyList();
        }
    }
}