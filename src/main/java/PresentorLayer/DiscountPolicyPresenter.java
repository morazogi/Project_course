package PresentorLayer;

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

    /* ---------------- stores current user can update -------------------- */
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

    /* ---------------- fetch discount IDs for a store -------------------- */
    public List<String> storeDiscountIds(String token, String storeId) {
        Store s = userConn.getStore(token, storeId);
        return s == null ? List.of() : s.getDiscountPolicy();
    }

    /* ---------------- simple voucher ------------------------------------ */
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
        return userConn.addDiscount(token, storeName, level, logicComp, numComp, percent,
                discounted, discountCondition, limiter, conditional, conditionalDiscounted);
    }

    /* ---------------- composite (parent) block -------------------------- */
    public String addCompositeDiscount(String token,
                                       String storeName,
                                       float logicComp,
                                       float numComp,
                                       List<String> children) {

        /* Level 0 → UNDEFINED (treated as STORE);  percent = 0;  no condition */
        return userConn.addDiscount(
                token,
                storeName,
                0f,                // level
                logicComp,
                numComp,
                0f,                // percent
                "",                // discounted item
                -1f,               // discountCondition
                -1f,               // limiter
                -1f,               // conditional
                "",                // conditionalDiscounted
                children           // nested children
        );
    }

    /* ---------------- removal ------------------------------------------- */
    public String removeDiscount(String token, String storeId, String discountId) {
        String user = userConn.getUsername(token);
        boolean ok  = ownerMgr.removeDiscountFromDiscountPolicy(user, storeId, discountId);
        return ok ? "Discount removed" : "Failed to remove discount";
    }
}
