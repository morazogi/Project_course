package DomainLayer.DomainServices;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.DiscountRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.ErrorLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiscountPolicyMicroservice {

    private StoreRepository storeRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private DiscountRepository discountRepository;
    private ObjectMapper mapper = new ObjectMapper();


    private Store getStoreById(String storeId) {
        if (storeRepository == null) {
            return null;
        }
        try {
            return storeRepository.getById(storeId);
        } catch (EntityNotFoundException e) {
            ErrorLogger.logError("username-null","EntityNotFoundException: '"+ e.toString() + "'. Store not found in  --> getById" ,"couldn't find store");
        }
        return null;
    }
    private RegisteredUser getUserById(String userId) {
        if (userRepository == null) {
            return null;
        }
        try {
            RegisteredUser user = (RegisteredUser) userRepository.getById(userId);
            return user;
        } catch (EntityNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Product getProductById(String ProductId) {
        if (productRepository == null) {
            throw new IllegalArgumentException("Didnt set productRepository");
        }
        Product product = productRepository.getById(ProductId);
        if (productRepository.getById(ProductId) == null) {
            throw new IllegalArgumentException("Store does not exist");
        }
        return product;
    }


    private Discount getDiscountById(String DiscountId){
        return discountRepository.getById(DiscountId);
    }

    private boolean checkPermission(String userId, String storeId, String permissionType) {
        // Get the store
        Store store = getStoreById(storeId);
        if (store == null) {
            return false;
        }

        // Owners have all permissions
        if (store.userIsOwner(userId)) {
            return true;
        }

        // Check if manager has specific permission
        if (store.userIsManager(userId)) {
            return store.userHasPermissions(userId, permissionType);
        }

        if (store.getFounder().equals(userId)) {  // founder has every right
            return true;
        }

        return false;
    }

    public DiscountPolicyMicroservice(StoreRepository storeRepository, UserRepository userRepository, ProductRepository productRepository, DiscountRepository discountRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
    }

    public boolean removeDiscountFromDiscountPolicy(String ownerId, String storeId, String discountId) {
        if (checkPermission(ownerId, storeId, ManagerPermissions.PERM_UPDATE_POLICY)) {
            Store store =  getStoreById(storeId);
            if (store != null) {
                store.removeDiscount(discountId);
                discountRepository.deleteById(discountId);
                storeRepository.update(store);
            }
        }
        return false;
    }

    public boolean addDiscountToDiscountPolicy(String ownerId, String storeId, String discountId,
                                        float level,
                                        float logicComposition,
                                        float numericalComposition,
                                        List<String> discountsId,
                                        float percentDiscount,
                                        String discounted,
                                        float conditional,
                                        float limiter,
                                        String conditionalDiscounted) {


        if (checkPermission(ownerId, storeId, ManagerPermissions.PERM_UPDATE_POLICY)) {
            Store store = getStoreById(storeId);
            if (store != null) {

                // 1. create & save the discount
                Discount discount = new Discount(
                        storeId,
                        level, logicComposition, numericalComposition,
                        discountsId, percentDiscount, discounted,
                        conditional, limiter, conditionalDiscounted
                );
                if (discountRepository.save(discount) == null) {
                    return false;
                }

                store.addDiscount(discountId);

                storeRepository.update(store);
                return true;
            }
        }
        return false;
    }

    public boolean removeDiscountPolicy(String ownerId, String storeId) {
        if(checkPermission(ownerId, storeId, ManagerPermissions.PERM_UPDATE_POLICY)) {
            Store store = getStoreById(storeId);
            List<String> discountPolicy = store.getDiscountPolicy();
            for (String discountId : discountPolicy) {
                if (discountId == null) {
                    continue;
                }
                discountRepository.deleteById(discountId);
                storeRepository.update(store);
            }
            store.setDiscountPolicy(new ArrayList<>());
            storeRepository.update(store);
            return true;
        }

        return false;
    }



    public float calculatePrice(String storeId,
                                Map<String, Integer> productsStringQuantity) {

        /* ---------- fetch store and the discounts it references ---------- */
        Store store = getStoreById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }

        List<Discount> allDiscounts = store.getDiscountPolicy().stream()
                .map(this::getDiscountById)
                .filter(Objects::nonNull)
                .toList();

    /* -----------------------------------------------------------------
       Start every *new* basket evaluation with a clean slate:
       clear “alreadyUsed” on every discount (including nested ones)
       ----------------------------------------------------------------- */
        for (Discount d : allDiscounts) {
            resetUsedRecursively(d);
        }

        /* ---------- turn id→qty map into Product→qty map ---------- */
        Map<Product, Integer> productsQuantity = new HashMap<>();
        for (Map.Entry<String, Integer> e : productsStringQuantity.entrySet()) {
            Product p = getProductById(e.getKey());
            if (p != null) {
                productsQuantity.put(p, e.getValue());
            }
        }

        /* ---------- base price and initial multipliers (1.0 = no discount) ---------- */
        float originalPrice = 0f;
        Map<Product, Float> productMultipliers = new HashMap<>();
        for (Map.Entry<Product, Integer> e : productsQuantity.entrySet()) {
            originalPrice += e.getKey().getPrice() * e.getValue();
            productMultipliers.put(e.getKey(), 1f);
        }

        /* ---------- apply every *top-level* discount in the store’s policy ---------- */
        for (Discount top : allDiscounts) {
            List<Discount> nested = new ArrayList<>();
            for (String id : top.getDiscounts()) {
                Discount child = getDiscountById(id);
                if (child != null) nested.add(child);
            }
            productMultipliers = top.applyDiscount(originalPrice,
                    productsQuantity,
                    productMultipliers,
                    nested);
        }

        /* ---------- final total ---------- */
        float total = 0f;
        for (Map.Entry<Product, Float> e : productMultipliers.entrySet()) {
            Product p  = e.getKey();
            float mul  = e.getValue();
            int   qty  = productsQuantity.getOrDefault(p, 0);
            total += p.getPrice() * mul * qty;
        }

        return total;
    }

    /* ---------------------------------------------------------------
       Utility: recursively clears the “alreadyUsed” flag so that a
       discount can participate again in a fresh calculation.
       --------------------------------------------------------------- */



    private void resetUsedRecursively(Discount disc) {
        if (disc == null) return;
        disc.setAlreadyUsed(false);
        for (String id : disc.getDiscounts()) {
            resetUsedRecursively(getDiscountById(id));
        }
    }






    //helpful
    private boolean removeDiscount(String discountId) {
        boolean removed = false;

        Discount discount = discountRepository.getById(discountId);
        List<String> discountsString = discount.getDiscounts();

        List<Discount> discounts = new ArrayList<>();
        for (String id : discountsString) {
            Discount d = discountRepository.getById(id);
            if (d != null) discounts.add(d);
        }

        Iterator<Discount> iterator = discounts.iterator();
        while (iterator.hasNext()) {
            Discount d = iterator.next();
            if (d.getId().equals(discountId)) {
                iterator.remove();
                removed = true;
            }
        }

        // Process each discount in the current list to remove the discountId from their nested discounts
        List<Discount> currentDiscounts = new ArrayList<>(discounts); // Create a copy to iterate safely
        for (Discount d : currentDiscounts) {
            boolean childRemoved = removeDiscountFromDiscount(d, discountId);
            removed = removed || childRemoved;
        }

        return removed;
    }

    private boolean removeDiscountFromDiscount(Discount parentDiscount, String discountId) {
        boolean removed = false;


        Discount discount = discountRepository.getById(discountId);
        List<String> discountsString = discount.getDiscounts();

        List<Discount> discounts = new ArrayList<>();
        for (String id : discountsString) {
            Discount d = discountRepository.getById(id);
            if (d != null) discounts.add(d);
        }

        Iterator<Discount> iterator = discounts.iterator();
        while (iterator.hasNext()) {
            Discount d = iterator.next();
            if (d.getId().equals(discountId)) {
                iterator.remove();
                removed = true;
            } else {
                // Recursively process the nested discounts of the current discount
                boolean childRemoved = removeDiscountFromDiscount(d, discountId);
                removed = removed || childRemoved;
            }
        }
        return removed;
    }
}
