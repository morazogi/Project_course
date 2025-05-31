package InfrastructureLayer;

import DomainLayer.ICustomerInquiryRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Repository implementation for customer inquiries
 * This class implements the ICustomerInquiryRepository interface and provides
 * methods for storing and retrieving customer inquiries
 */
@Repository
public class CustomerInquiryRepository implements ICustomerInquiryRepository {
    // Map of storeId -> List of inquiries
    private final Map<String, List<Map<String, Object>>> inquiriesByStore;
    
    // Counter for generating unique inquiry IDs
    private final AtomicInteger inquiryIdCounter;
    
    public CustomerInquiryRepository() {
        this.inquiriesByStore = new ConcurrentHashMap<>();
        this.inquiryIdCounter = new AtomicInteger(1);
    }
    
    @Override
    public List<Map<String, Object>> getInquiriesByStore(String storeId) {
        if (storeId == null) {
            return new ArrayList<>();
        }
        
        // Return a copy of the inquiries list to prevent external modification
        return inquiriesByStore.getOrDefault(storeId, new ArrayList<>())
                .stream()
                .map(HashMap::new) // Create a copy of each inquiry map
                .collect(Collectors.toList());
    }
    
    @Override
    public void addInquiry(String storeId, Map<String, Object> inquiry) {
        if (storeId == null || inquiry == null) {
            return;
        }
        
        // Add the inquiry to the store's list
        inquiriesByStore.computeIfAbsent(storeId, k -> new ArrayList<>()).add(inquiry);
    }
    
    @Override
    public boolean updateInquiry(String storeId, String inquiryId, Map<String, Object> updatedFields) {
        if (storeId == null || inquiryId == null || updatedFields == null) {
            return false;
        }
        
        List<Map<String, Object>> storeInquiries = inquiriesByStore.get(storeId);
        if (storeInquiries == null) {
            return false;
        }
        
        // Find the inquiry with the matching ID
        for (Map<String, Object> inquiry : storeInquiries) {
            if (inquiryId.equals(inquiry.get("id"))) {
                // Update the inquiry with the new fields
                inquiry.putAll(updatedFields);
                return true;
            }
        }
        
        return false; // Inquiry not found
    }
    
    @Override
    public List<Map<String, Object>> getInquiriesByCustomer(String customerId) {
        if (customerId == null) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> customerInquiries = new ArrayList<>();
        
        // Collect all inquiries for this customer across all stores
        for (List<Map<String, Object>> storeInquiries : inquiriesByStore.values()) {
            for (Map<String, Object> inquiry : storeInquiries) {
                if (customerId.equals(inquiry.get("customerId"))) {
                    customerInquiries.add(new HashMap<>(inquiry)); // Add a copy
                }
            }
        }
        
        return customerInquiries;
    }
    
    @Override
    public String getNextInquiryId() {
        return String.valueOf(inquiryIdCounter.getAndIncrement());
    }
}