package PresentorLayer;

import DomainLayer.Product;
import DomainLayer.Store;
import ServiceLayer.BidService;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.UserService;

public class BidManagerPresenter {

    private final BidService          bidService;
    private final UserService         userService;
    private final OwnerManagerService ownerMgr;
    private final String manager;

    public BidManagerPresenter(String manager,
                               BidService bidService,
                               UserService userService,
                               OwnerManagerService ownerMgr) {

        this.manager   = manager;
        this.bidService= bidService;
        this.userService = userService;
        this.ownerMgr    = ownerMgr;
    }

    /*──────────────────────────────────────────────────────────────*/
    public void startBid(String token,
                         String storeName,
                         String productName,
                         String startPrice,
                         String minIncrease,
                         String duration) throws Exception {

        double price = Double.parseDouble(startPrice);
        double inc   = Double.parseDouble(minIncrease);

        /* “1.0” → 1   (rounds, never throws) */
        int minutes  = (int) Math.round(Double.parseDouble(duration));

        /* store & permission */
        String storeId = userService.searchStoreByName(token, storeName)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("store not found"))
                .getId();

        if (!ownerMgr.hasInventoryPermission(manager, storeId))
            throw new RuntimeException("no permission to create bids in this store");

        /* product inside store */
        String productId = null;
        for (Product p : userService.getProductsInStore(storeId))
            if (p.getName().equalsIgnoreCase(productName)) { productId = p.getId(); break; }
        if (productId == null) throw new RuntimeException("product not in store");

        /* launch */
        bidService.start(storeId, productId, price, inc, minutes);
    }
}
