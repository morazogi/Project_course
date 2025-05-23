package UILayer;

import DomainLayer.Product;
import DomainLayer.ShoppingBag;
import DomainLayer.ShoppingCart;
import DomainLayer.Store;
import ServiceLayer.ProductService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class ProductListUI extends HorizontalLayout {

    private final ShoppingCart shoppingCart;
    private final ProductService productService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ProductListUI(ShoppingCart configuredShoppingCart, ProductService configuredProductService, UserService configuredUserService) {
        this.shoppingCart = configuredShoppingCart;
        this.productService = configuredProductService;
        this.userService = configuredUserService;
        add(new VerticalLayout(new Span("store"), new Span("product\namount\nprice")));
        double totalPayment = 0;
        for (ShoppingBag shoppingBag: shoppingCart.getShoppingBags()) {
            VerticalLayout productList = new VerticalLayout();
            String token = (String) UI.getCurrent().getSession().getAttribute("token");
            try {
                String storeId = shoppingBag.getStoreId();
                String jsonStore = userService.getStoreById(token, storeId);
                Store store = mapper.readValue(jsonStore, Store.class);
                String storeName = store.getName();
                productList.add(new Span(storeName));
            } catch (Exception e) {
                Notification.show("store with id: " + shoppingBag.getStoreId() + "does not exist");
            }
            for (Map.Entry<String, Integer> product : shoppingBag.getProducts().entrySet()) {
                productList.add(new Span(productService.getProductById(product.getKey()).get().getName() + "\n" + product.getValue() + "\n" +productService.getProductById(product.getKey()).get().getPrice()));
                totalPayment = totalPayment + product.getValue()  * productService.getProductById(product.getKey()).get().getPrice();
            }
            add(productList, new Span("total payment :" + totalPayment));
        }

    }

}
