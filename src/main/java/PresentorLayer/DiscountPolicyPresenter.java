package PresentorLayer;

import DomainLayer.Store;
import ServiceLayer.OwnerManagerService;
import java.util.Collections;
import java.util.List;

public class DiscountPolicyPresenter {

    private final UserConnectivityPresenter userConn;
    private final OwnerManagerService       ownerMgr;
    private final PermissionsPresenter      perms;   // kept for any other screens

    public DiscountPolicyPresenter(UserConnectivityPresenter userConn,
                                   OwnerManagerService ownerMgr,
                                   PermissionsPresenter perms) {
        this.userConn = userConn;
        this.ownerMgr = ownerMgr;
        this.perms    = perms;
    }

    /* ------------------------------------------------------------------ */
    /*  Which stores should appear in the ComboBox?                       */
    /* ------------------------------------------------------------------ */
    public List<Store> updatableStores(String token) {
        try {
            /* ① fetch the **internal** user-ID (shopping-cart ID), not login name */
            String userId = userConn.getUserId(token);              // ←★ FIX

            return userConn.getUserStoresName(token).stream()
                    .filter(s -> ownerMgr.canUpdateDiscountPolicy(userId, s.getId()))
                    .toList();

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Store> homepageStores(String token) {            // ★ NEW
        try {
            return userConn.getUserStoresName(token);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /* ---------------- rest of the presenter unchanged ----------------- */

    public List<String> storeDiscountIds(String token, String storeId) {
        Store s = userConn.getStore(token, storeId);
        return s == null ? List.of() : s.getDiscountPolicy();
    }

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

        return userConn.addDiscountByInternalId(
                token, storeName, level, logicComp, numComp, percent,
                discounted, discountCondition, limiter,
                conditional, conditionalDiscounted);
    }


    public String addCompositeDiscount(String token,
                                       String storeName,
                                       float logicComp,
                                       float numComp,
                                       List<String> children) {

        /* level 0, percent 0, no conditions */
        return userConn.addDiscountByInternalId(
                token, storeName,
                0f, logicComp, numComp, 0f,
                "", -1f, -1f, -1f, "", children);
    }


    public String removeDiscount(String token, String storeId, String discountId) {
        String internalId = userConn.getUserId(token);           // internal UUID
        boolean ok = ownerMgr.removeDiscountFromDiscountPolicy(
                internalId, storeId, discountId);
        return ok ? "Discount removed" : "Failed to remove discount";
    }

}
