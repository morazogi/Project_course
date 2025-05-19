package UILayer;

import DomainLayer.Product;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@Route("/searchproduct/:storeid")
public class SearchProductInStoreUI extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    public SearchProductInStoreUI(UserService configuredUserService, String storeId) {
        this.userService = configuredUserService;

        TextField lowestPrice = new TextField("lowest price");
        TextField highestPrice = new TextField("highest price");
        TextField lowestProductRating = new TextField("lowest product rating");
        TextField highestProductRating = new TextField("highest product rating");
        TextField category = new TextField("category");

        TextField productName = new TextField("product name");
        Button searchProduct = new Button("search product by name", e -> {
            try {
                String token = (String) UI.getCurrent().getSession().getAttribute("token");
                List<String> items = userService.findProduct(token, productName.getValue(), "");
                List<Product> products = items.stream().map(item -> {
                            try {
                                if (mapper.readValue(item, Product.class).getStoreId().equals(storeId)) {
                                    return mapper.readValue(item, Product.class);
                                }
                                return null;
                            } catch (Exception exception) {
                                return null;
                            }
                        }).filter(Objects::isNull).filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating.getValue()))
                        .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating.getValue()))
                        .filter(item -> category.equals("") ? true : item.getCategory().equals(category.getValue()))
                        .toList();
                for (Product product : products) {
                    add(new Button(product.getName() + "\n" + product.getPrice(), choose -> {UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));
                }
            } catch (Exception exception) {
                Notification.show(exception.getMessage());
            }
        });

        TextField categoryName = new TextField("category name");
        Button searchProductByCategory = new Button("search product by category", e -> {
            try {
                String token = (String) UI.getCurrent().getSession().getAttribute("token");
                List<String> items = userService.findProduct(token, "", categoryName.getValue());
                List<Product> products = items.stream().map(item -> {
                            try {
                                if (mapper.readValue(item, Product.class).getStoreId().equals(storeId)) {
                                    return mapper.readValue(item, Product.class);
                                }
                                return null;
                            } catch (Exception exception) {
                                return null;
                            }
                        }).filter(Objects::isNull).filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating.getValue()))
                        .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating.getValue()))
                        .filter(item -> category.equals("") ? true : item.getCategory().equals(category.getValue()))
                        .toList();
                for (Product product : products) {
                    add(new Button(product.getName() + "\n" + product.getPrice(), choose -> {UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));
                }
            } catch (Exception exception) {
                Notification.show(exception.getMessage());
            }
        });


        add(new H1("search products"), new HorizontalLayout(lowestPrice, highestPrice, lowestProductRating, highestProductRating, category), new HorizontalLayout(productName, searchProduct), new HorizontalLayout(categoryName, searchProductByCategory));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        RouteParameters parameters = beforeEnterEvent.getRouteParameters();
        if (parameters.get("storeid").isPresent()) {
            String storeId = parameters.get("storeid").get();
        } else {
            add(new Span("No fitting store"));
        }
    }

}
