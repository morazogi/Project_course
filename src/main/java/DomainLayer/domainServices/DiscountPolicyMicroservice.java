package DomainLayer.DomainServices;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscountPolicyMicroservice {

    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private IDiscountRepository discountRepository;
    private ObjectMapper mapper = new ObjectMapper();


    private Store getStoreById(String storeId) {
        if (storeRepository == null) {
            return null;
        }
        try {
            Store store = mapper.readValue(storeRepository.getStore(storeId), Store.class);
            if (storeRepository.getStore(storeId) == null) {
                throw new IllegalArgumentException("Store does not exist");
            }
            return store;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private RegisteredUser getUserById(String userId) {
        if (userRepository == null) {
            return null;
        }
        try {
            RegisteredUser user = mapper.readValue(userRepository.getUser(userId), RegisteredUser.class);
            if (userRepository.getUser(userId) == null) {
                throw new IllegalArgumentException("User does not exist");
            }
            return user;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Product getProductById(String ProductId) {
        if (productRepository == null) {
            throw new IllegalArgumentException("Didnt set productRepository");
        }
        Product product = productRepository.getReferenceById(ProductId);
        if (productRepository.getReferenceById(ProductId) == null) {
            throw new IllegalArgumentException("Store does not exist");
        }
        return product;
    }


    private Discount getDiscountById(String DiscountId){
        return discountRepository.getReferenceById(DiscountId);
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

    public DiscountPolicyMicroservice(IStoreRepository storeRepository, IUserRepository userRepository, IProductRepository productRepository, IDiscountRepository discountRepository) {
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
                try {
                    storeRepository.updateStore(storeId, mapper.writeValueAsString(store));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    public boolean addDiscountToDiscountPolicy(String ownerId, String storeId, String discountId,
                                        String Id,
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
                        Id, storeId,
                        level, logicComposition, numericalComposition,
                        discountsId, percentDiscount, discounted,
                        conditional, limiter, conditionalDiscounted
                );
                if (discountRepository.save(discount) == null) {
                    return false;
                }

                store.addDiscount(discountId);

                try {
                    storeRepository.updateStore(storeId, mapper.writeValueAsString(store));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
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
                try {
                    storeRepository.updateStore(storeId, mapper.writeValueAsString(store));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            store.setDiscountPolicy(new ArrayList<>());
            try {
                storeRepository.updateStore(storeId, mapper.writeValueAsString(store));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        return false;
    }



    public float calculatePrice(String storeId, Map<String,Integer> productsStringQuantity){
        Store store = getStoreById(storeId);

        List<Discount> Alldiscounts = store.getDiscountPolicy().stream()
                .map(this::getDiscountById)
                .filter(Objects::nonNull)
                .toList();

        Map<Product, Integer> productsQuantity = new HashMap<>();
        for (Map.Entry<String, Integer> e : productsStringQuantity.entrySet()) {
            Product p = getProductById(e.getKey());
            if (p != null) {
                productsQuantity.put(p, e.getValue());
            }
        }






        float originalPrice = 0f;

        for (Map.Entry<Product, Integer> e : productsQuantity.entrySet()) {
            originalPrice += e.getKey().getPrice() * e.getValue();
        }


        Map<Product, Float> productDiscounts = new HashMap<>();
        for (Product p : productsQuantity.keySet()) {
            productDiscounts.put(p, 1f);
        }


        for (Discount discount : Alldiscounts) {
            List<Discount> nestedDiscounts = new ArrayList<>();
            for (String id : discount.getDiscounts()) {
                Discount d = getDiscountById(id);
                if (d != null) {
                    nestedDiscounts.add(d);
                }
            }
            productDiscounts =  discount.applyDiscount(originalPrice, productsQuantity, productDiscounts, nestedDiscounts);
        }








        float total = 0f;
        for (Map.Entry<Product, Float> e : productDiscounts.entrySet()) {
            Product p = e.getKey();
            float discount = e.getValue();
            int qty = productsQuantity.getOrDefault(p, 0);
            total += p.getPrice() * discount * qty;
        }

        return total;
    }










    //helpful
    private boolean removeDiscount(String discountId) {
        boolean removed = false;

        Discount discount = discountRepository.getReferenceById(discountId);
        List<String> discountsString = discount.getDiscounts();

        List<Discount> discounts = new ArrayList<>();
        for (String id : discountsString) {
            Discount d = discountRepository.getReferenceById(id);
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


        Discount discount = discountRepository.getReferenceById(discountId);
        List<String> discountsString = discount.getDiscounts();

        List<Discount> discounts = new ArrayList<>();
        for (String id : discountsString) {
            Discount d = discountRepository.getReferenceById(id);
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
