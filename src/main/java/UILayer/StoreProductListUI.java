package UILayer;

import DomainLayer.Product;
import ServiceLayer.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

public class StoreProductListUI extends HorizontalLayout {

    private final String storeId;
    private final UserService userService;

    @Autowired
    public StoreProductListUI(String configuredStoreId, UserService configuredUserService) {
        this.storeId = configuredStoreId;
        this.userService = configuredUserService;
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        List<Product> items = userService.getAllProducts(token);
        List<Product> products = items.stream().map(item -> {
            try {
                if (item.getStoreId().equals(storeId)) {
                    return item;
                }
                return null;
            } catch (Exception exception) {
                return null;
            }
        }).filter(Objects::isNull).toList();
        for (Product product : products) {
            add(new Button(product.getName() + "\n" + product.getPrice(), e -> {
                UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));

     }

    }

}
