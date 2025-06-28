package PresentorLayer;

import DomainLayer.Discount;
import DomainLayer.Store;
import ServiceLayer.OwnerManagerService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DiscountPolicyPresenter {

    private final UserConnectivityPresenter userConn;
    private final OwnerManagerService ownerMgr;
    private final PermissionsPresenter perms;

    public DiscountPolicyPresenter(UserConnectivityPresenter userConn,
                                   OwnerManagerService ownerMgr,
                                   PermissionsPresenter perms) {
        this.userConn = userConn;
        this.ownerMgr = ownerMgr;
        this.perms    = perms;
    }

    /* ---------------- stores current user can update ------------------ */
    public List<Store> updatableStores(String token) {
        try {
            return userConn.getUserStoresName(token).stream()
                    .filter(s -> {
                        Map<String,Boolean> p = perms.getPremissions(token, s.getId());
                        return p != null && Boolean.TRUE.equals(p.get("PERM_UPDATE_POLICY"));
                    })
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /* ---------------- fetch discount IDs for a store ------------------ */
    public List<String> storeDiscountIds(String token, String storeId) {
        Store s = userConn.getStore(token, storeId);
        return s == null ? List.of() : s.getDiscountPolicy();
    }

    /* ---------------- readable label for a discount ------------------- */
    /* ------------------------------------------------------------------
     *  Human-friendly label for a discount ID
     * ------------------------------------------------------------------ */
    public String discountLabel(String discountId) {
        if (discountId == null) return "";

        Discount d = ownerMgr.getDiscountById(discountId);
        if (d == null) {                       // fallback: shorten the raw id
            return discountId.length() <= 8 ? discountId
                    : discountId.substring(0, 8) + "...";
        }

        /* -------- scope text (Product / Category / Store-wide) -------- */
        // up-cast to Enum<?> to avoid the package-private enum visibility issue
        String levelName = ((Enum<?>) d.getLevel()).name();
        String target;
        switch (levelName) {
            case "PRODUCT"  -> target = d.getDiscounted().isBlank()
                    ? "Product"
                    : d.getDiscounted();
            case "CATEGORY" -> target = (d.getDiscounted().isBlank()
                    ? "Category"
                    : d.getDiscounted()) + " (cat)";
            default         -> target = "Store-wide";
        }

        int pct = Math.round(d.getPercentDiscount() * 100);

        /* parent “block” discounts have 0 % */
        if (pct == 0) {
            String logicName = ((Enum<?>) d.getLogicComposition()).name();
            return "[" + logicName + "] " + target;
        }
        return target + " " + pct + "%";
    }

    /* ---------------- simple voucher ---------------------------------- */
    public String addDiscount(String token,
                              String storeName,
                              float level,
                              float logicComp,
                              float numComp,
                              float percent,
                              String discounted,
                              float discountCondition,
                              float limiter,
                              float conditional,
                              String conditionalDiscounted) {
        return userConn.addDiscount(
                token, storeName, level, logicComp, numComp, percent, discounted,
                discountCondition, limiter, conditional, conditionalDiscounted);
    }

    /* ---------------- composite (parent) block ------------------------ */
    public String addCompositeDiscount(String token,
                                       String storeName,
                                       float logicComp,
                                       float numComp,
                                       List<String> children) {

        return userConn.addDiscount(
                token,
                storeName,
                0f,                     // level UNDEFINED (store)
                logicComp,
                numComp,
                0f,                     // percent
                "",                     // discounted
                -1f,                    // no condition
                -1f,
                -1f,
                "",
                children                // nested children
        );
    }

    /* ---------------- removal ----------------------------------------- */
    public String removeDiscount(String token, String storeId, String discountId) {
        String user = userConn.getUsername(token);
        boolean ok  = ownerMgr.removeDiscountFromDiscountPolicy(user, storeId, discountId);
        return ok ? "Discount removed" : "Failed to remove discount";
    }
}
