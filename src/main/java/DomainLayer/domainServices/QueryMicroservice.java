package DomainLayer.domainServices;

import DomainLayer.ICustomerInquiryRepository;
import infrastructureLayer.CustomerInquiryRepository;
import java.util.*;

/**
 * Microservice for handling customer inquiries and notifications
 * This class implements the requirements for store owners to receive and respond to customer inquiries
 */
public class QueryMicroservice {
    // Repository for customer inquiries
    private final ICustomerInquiryRepository inquiryRepository;

    /**
     * Constructor with repository injection
     * 
     * @param inquiryRepository Repository for customer inquiries
     */
    public QueryMicroservice(ICustomerInquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    /**
     * Default constructor for backward compatibility
     * Creates a default repository implementation
     */
    public QueryMicroservice() {
        this.inquiryRepository = new CustomerInquiryRepository();
    }

    /**
     * Get all customer inquiries for a specific store and owner
     * 
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @return List of inquiries as maps with inquiry details
     */
    public List<Map<String, Object>> getCustomerInquiries(String ownerId, String storeId) {
        if (ownerId == null || storeId == null) {
            return new ArrayList<>();
        }

        // Get inquiries from repository
        return inquiryRepository.getInquiriesByStore(storeId);
    }

    /**
     * Respond to a specific customer inquiry
     * 
     * @param ownerId ID of the store owner
     * @param storeId ID of the store
     * @param inquiryId ID of the inquiry
     * @param response Response text
     * @return true if successful, false otherwise
     */
    public boolean respondToCustomerInquiry(String ownerId, String storeId, String inquiryId, String response) {
        if (ownerId == null || storeId == null || inquiryId == null || response == null || response.isEmpty()) {
            return false;
        }

        // Create map of fields to update
        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("response", response);
        updatedFields.put("respondedBy", ownerId);
        updatedFields.put("responseTimestamp", System.currentTimeMillis());
        updatedFields.put("status", "Answered");

        // Update inquiry in repository
        return inquiryRepository.updateInquiry(storeId, inquiryId, updatedFields);
    }

    /**
     * Add a new customer inquiry
     * 
     * @param customerId ID of the customer
     * @param storeId ID of the store
     * @param message Inquiry message
     * @return ID of the created inquiry if successful, null otherwise
     */
    public String addCustomerInquiry(String customerId, String storeId, String message) {
        if (customerId == null || storeId == null || message == null || message.isEmpty()) {
            return null;
        }

        // Create a new inquiry
        Map<String, Object> inquiry = new HashMap<>();
        String inquiryId = inquiryRepository.getNextInquiryId();

        inquiry.put("id", inquiryId);
        inquiry.put("customerId", customerId);
        inquiry.put("message", message);
        inquiry.put("timestamp", System.currentTimeMillis());
        inquiry.put("status", "Pending");

        // Add the inquiry to the repository
        inquiryRepository.addInquiry(storeId, inquiry);

        return inquiryId;
    }

    /**
     * Get customer inquiries for a specific customer
     * 
     * @param customerId ID of the customer
     * @return List of inquiries as maps with inquiry details
     */
    public List<Map<String, Object>> getCustomerInquiriesByCustomer(String customerId) {
        if (customerId == null) {
            return new ArrayList<>();
        }

        // Get inquiries from repository
        return inquiryRepository.getInquiriesByCustomer(customerId);
    }
}
