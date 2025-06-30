package DomainLayer.DomainServices;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.DiscountRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import ServiceLayer.ErrorLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Micro-service that stores, evaluates and manages discount policies.
 * <p>
 *  – All public methods perform permission checks.<br>
 *  – Discounts are kept as separate entities and only <em>referenced</em>
 *    from stores / other discounts by ID – enabling nested compositions.
 */
@Service
public class DiscountPolicyMicroservice {

    /* ======================================================================
       Dependencies
       ====================================================================== */
    private final StoreRepository    storeRepository;
    private final UserRepository     userRepository;
    private final ProductRepository  productRepository;
    private final DiscountRepository discountRepository;
    private final ObjectMapper       mapper = new ObjectMapper();

    public DiscountPolicyMicroservice(StoreRepository storeRepository,
                                      UserRepository userRepository,
                                      ProductRepository productRepository,
                                      DiscountRepository discountRepository)
    {
        this.storeRepository    = storeRepository;
        this.userRepository     = userRepository;
        this.productRepository  = productRepository;
        this.discountRepository = discountRepository;
    }

    /* ======================================================================
       Small helper functions
       ====================================================================== */

    private Store getStoreById(String storeId) {
        try { return storeRepository.getById(storeId); }
        catch (EntityNotFoundException e) {
            ErrorLogger.logError("username-null",
                    "EntityNotFoundException: '"+ e +"'. Store not found in getById",
                    "couldn't find store");
            return null;
        }
    }

    private RegisteredUser getUserById(String userId) {
        try { return (RegisteredUser) userRepository.getById(userId); }
        catch (EntityNotFoundException e) { return null; }
    }

    private Product getProductById(String productId) {
        Product p = productRepository.getById(productId);
        if (p == null) throw new IllegalArgumentException("Product not found");
        return p;
    }

    private Discount getDiscountById(String discountId) {
        return discountRepository.getById(discountId);
    }

    private boolean checkPermission(String userId, String storeId, String permissionType) {
        Store store = getStoreById(storeId);
        if (store == null) return false;

        /* founder & owners have every permission                               */
        if (store.getFounder().equals(userId) || store.userIsOwner(userId))
            return true;

        /* managers – check their permissions                                   */
        if (store.userIsManager(userId))
            return store.userHasPermissions(userId, permissionType);

        return false;
    }

    /* ======================================================================
       1)  Add discount to policy
       ====================================================================== */

    /* ======================================================================
       1)  Add discount to policy   (UPDATED)
       ====================================================================== */
    public boolean addDiscountToDiscountPolicy(String ownerId,
                                               String storeId,
                                               String discountId,          // parent-discount ID ("" → top-level)
                                               float level,
                                               float logicComposition,
                                               float numericalComposition,
                                               List<String> discountsId,   // pre-existing children to attach
                                               float percentDiscount,
                                               String discounted,
                                               float conditional,
                                               float limiter,
                                               String conditionalDiscounted)
    {
        /* ----- permission check ----- */
        if (!checkPermission(ownerId, storeId, ManagerPermissions.PERM_UPDATE_POLICY))
            return false;

        Store store = getStoreById(storeId);
        if (store == null) return false;

        /* ----- 1. create & persist the new discount ----- */
        Discount newDisc = new Discount(
                storeId,
                level, logicComposition, numericalComposition,
                discountsId, percentDiscount, discounted,
                conditional, limiter, conditionalDiscounted
        );
        if (discountRepository.save(newDisc) == null) return false;

        /* ----- 2. attach it either to the store or to an existing parent ----- */
        if (discountId == null || discountId.isBlank()) {
            /* top-level parent */
            store.addDiscount(newDisc.getId());
        } else {
            /* nested inside an existing parent */
            Discount parent = discountRepository.getById(discountId);
            if (parent != null) {
                List<String> children = new ArrayList<>(parent.getDiscounts());
                children.add(newDisc.getId());
                parent.setDiscounts(children);
                discountRepository.update(parent);
            } else {
                store.addDiscount(newDisc.getId());     // fallback: treat as top-level
            }
        }

        /* ----- ★ NEW: detach the children we just nested from the top level ----- */
        if (discountsId != null && !discountsId.isEmpty()) {
            List<String> topPolicy = new ArrayList<>(store.getDiscountPolicy());
            topPolicy.removeAll(discountsId);           // only detach – do NOT delete entities
            store.setDiscountPolicy(topPolicy);
        }

        /* ----- 3. persist store ----- */
        storeRepository.update(store);
        return true;
    }


    /* ======================================================================
       2)  Remove discount from policy   (FIXED)
       ====================================================================== */

    public boolean removeDiscountFromDiscountPolicy(String ownerId,
                                                    String storeId,
                                                    String discountId)
    {
        if (!checkPermission(ownerId, storeId, ManagerPermissions.PERM_UPDATE_POLICY))
            return false;

        Store store = getStoreById(storeId);
        if (store == null) return false;

        /* A) try to remove from the store’s top-level list                    */
        boolean removed = store.removeDiscount(discountId);

        /* B) otherwise search recursively through nested discounts            */
        if (!removed) {
            for (String topId : store.getDiscountPolicy()) {
                Discount top = getDiscountById(topId);
                if (top != null && removeDiscountFromDiscount(top, discountId)) {
                    removed = true;
                    break;
                }
            }
        }

        /* C) if reference removed – delete entity and save store             */
        if (removed) {
            discountRepository.deleteById(discountId);
            storeRepository.update(store);
            return true;
        }
        return false;   // nothing found
    }

    /* ======================================================================
       3)  Remove entire policy (unchanged)
       ====================================================================== */

    public boolean removeDiscountPolicy(String ownerId, String storeId) {
        if (!checkPermission(ownerId, storeId, ManagerPermissions.PERM_UPDATE_POLICY))
            return false;

        Store store = getStoreById(storeId);
        if (store == null) return false;

        List<String> policy = new ArrayList<>(store.getDiscountPolicy());
        for (String id : policy) {
            if (id == null) continue;
            discountRepository.deleteById(id);
        }
        store.setDiscountPolicy(new ArrayList<>());
        storeRepository.update(store);
        return true;
    }

    /* ======================================================================
       4)  Price calculation (unchanged)
       ====================================================================== */

    public float calculatePrice(String storeId,
                                Map<String, Integer> productsStringQuantity)
    {
        /* --- fetch store & its discounts ----------------------------------- */
        Store store = getStoreById(storeId);
        if (store == null) throw new IllegalArgumentException("Store not found");

        List<Discount> allDiscounts = store.getDiscountPolicy().stream()
                .map(this::getDiscountById)
                .filter(Objects::nonNull)
                .toList();

        /* --- reset 'alreadyUsed' flag before evaluating -------------------- */
        for (Discount d : allDiscounts) resetUsedRecursively(d);

        /* --- turn id→qty into Product→qty map ------------------------------ */
        Map<Product, Integer> productsQuantity = new HashMap<>();
        for (Map.Entry<String, Integer> e : productsStringQuantity.entrySet()) {
            Product p = getProductById(e.getKey());
            if (p != null) productsQuantity.put(p, e.getValue());
        }

        /* --- base price & initial multipliers ------------------------------ */
        float originalPrice = 0f;
        Map<Product, Float> productMultipliers = new HashMap<>();
        for (Map.Entry<Product, Integer> e : productsQuantity.entrySet()) {
            originalPrice += e.getKey().getPrice() * e.getValue();
            productMultipliers.put(e.getKey(), 1f);
        }

        /* --- apply every top-level discount -------------------------------- */
        for (Discount top : allDiscounts) {
            List<Discount> nested = new ArrayList<>();
            for (String id : top.getDiscounts()) {
                Discount d = getDiscountById(id);
                if (d != null) nested.add(d);
            }
            productMultipliers = top.applyDiscount(originalPrice,
                    productsQuantity,
                    productMultipliers,
                    nested);
        }

        /* --- final total ---------------------------------------------------- */
        float total = 0f;
        for (Map.Entry<Product, Float> e : productMultipliers.entrySet()) {
            Product p  = e.getKey();
            float  mul = e.getValue();
            int    qty = productsQuantity.getOrDefault(p, 0);
            total += p.getPrice() * mul * qty;
        }
        return total;
    }

    /* ======================================================================
       5)  Internal recursive helpers
       ====================================================================== */

    /** Clears the {@code alreadyUsed} flag for a discount and all its children. */
    private void resetUsedRecursively(Discount disc) {
        if (disc == null) return;
        disc.setAlreadyUsed(false);
        for (String id : disc.getDiscounts()) {
            resetUsedRecursively(getDiscountById(id));
        }
    }

    /**
     * Removes {@code discountId} from the nested list of {@code parentDiscount}
     * (works recursively).  Returns {@code true} iff a reference was removed.
     */
    private boolean removeDiscountFromDiscount(Discount parentDiscount,
                                               String discountId)
    {
        /* -- build child list as objects ------------------------------------ */
        List<Discount> children = new ArrayList<>();
        for (String id : parentDiscount.getDiscounts()) {
            Discount d = getDiscountById(id);
            if (d != null) children.add(d);
        }

        /* -- remove direct child if present --------------------------------- */
        boolean removed = children.removeIf(d -> d.getId().equals(discountId));

        /* -- recurse into children ------------------------------------------ */
        for (Discount child : children) {
            removed |= removeDiscountFromDiscount(child, discountId);
        }

        /* -- if something changed – persist parent -------------------------- */
        if (removed) {
            List<String> newIds = children.stream()
                    .map(Discount::getId)
                    .toList();
            parentDiscount.setDiscounts(newIds);
            discountRepository.update(parentDiscount);
        }
        return removed;
    }

    /* ----------------------------------------------------------------------
       Legacy helper (untouched) – still used elsewhere in code base
       ---------------------------------------------------------------------- */
    private boolean removeDiscount(String discountId) {
        boolean removed = false;

        Discount discount = discountRepository.getById(discountId);
        if (discount == null) return false;

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

        for (Discount d : new ArrayList<>(discounts)) {
            removed |= removeDiscountFromDiscount(d, discountId);
        }
        return removed;
    }
}