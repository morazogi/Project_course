package PresentorLayer;

import ServiceLayer.BidService;
import ServiceLayer.UserService;
import DomainLayer.Product;
import DomainLayer.Store;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BidManagerPresenter {

    private final BidService bidService;
    private final UserService userService;
    private final String manager;
    private final ObjectMapper mapper = new ObjectMapper();

    public BidManagerPresenter(String manager,
                               BidService bidService,
                               UserService userService) {
        this.manager     = manager;
        this.bidService  = bidService;
        this.userService = userService;
    }

    public void startBid(String token,
                         String storeName,
                         String productName,
                         String startPrice,
                         String minIncrease,
                         String duration) throws Exception {

        double price  = Double.parseDouble(startPrice);
        double inc    = Double.parseDouble(minIncrease);
        int minutes   = Integer.parseInt(duration);

        /* find store ID */
        String storeId = userService.searchStoreByName(token, storeName)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("store not found"))
                .getId();

        /* product in that store */
        String productId=null;
        for (Product p : userService.getProductsInStore(storeId))
            if (p.getName().equalsIgnoreCase(productName)) { productId=p.getId(); break; }
        if (productId==null) throw new RuntimeException("product not in store");

        bidService.start(storeId, productId, price, inc, minutes);
    }
}
