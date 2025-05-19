package UILayer;

import DomainLayer.Product;
import DomainLayer.Store;
import ServiceLayer.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("/searchproduct")
public class SearchProductUI extends VerticalLayout {

    private final UserService userService;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public SearchProductUI(UserService configuredUserService) {
        this.userService = configuredUserService;
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        TextField lowestPrice = new TextField("lowest price");
        TextField highestPrice = new TextField("highest price");
        TextField lowestProductRating = new TextField("lowest product rating");
        TextField highestProductRating = new TextField("highest product rating");
        TextField category = new TextField("category");
        TextField lowestStoreRating = new TextField("lowest store rating");
        TextField highestStoreRating = new TextField("highest store rating");

        TextField productName = new TextField("product name");
        Button searchProduct = new Button("search product by name", e -> {
            try {
                List<String> items = userService.findProduct(token, productName.getValue(), "");
                List<Product> products = items.stream().map(item -> {
                            try {
                                return mapper.readValue(item, Product.class);
                            } catch (Exception exception) {
                                return null;
                            }
                        }).filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating.getValue()))
                        .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating.getValue()))
                        .filter(item -> category.equals("") ? true : item.getCategory().equals(category.getValue()))
                        .filter(item -> {
                            try {
                                return lowestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() >= Integer.valueOf(lowestStoreRating.getValue());
                            } catch (JsonProcessingException ex) {
                                Notification.show(ex.getMessage());
                                return false;
                            }
                        })
                        .filter(item -> {
                            try {
                                return highestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() <= Integer.valueOf(highestStoreRating.getValue());
                            } catch (JsonProcessingException ex) {
                                Notification.show(ex.getMessage());
                                return false;
                            }
                        })
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
                List<String> items = userService.findProduct(token, productName.getValue(), "");
                List<Product> products = items.stream().map(item -> {
                            try {
                                return mapper.readValue(item, Product.class);
                            } catch (Exception exception) {
                                return null;
                            }
                        }).filter(item -> lowestPrice.equals("") ? true : item.getPrice() >= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> highestPrice.equals("") ? true : item.getPrice() <= Integer.valueOf(lowestPrice.getValue()))
                        .filter(item -> lowestProductRating.equals("") ? true : item.getRating() >= Integer.valueOf(lowestProductRating.getValue()))
                        .filter(item -> highestProductRating.equals("") ? true : item.getRating() <= Integer.valueOf(highestProductRating.getValue()))
                        .filter(item -> category.equals("") ? true : item.getCategory().equals(category.getValue()))
                        .filter(item -> {
                            try {
                                return lowestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() >= Integer.valueOf(lowestStoreRating.getValue());
                            } catch (JsonProcessingException ex) {
                                Notification.show(ex.getMessage());
                                return false;
                            }
                        })
                        .filter(item -> {
                            try {
                                return highestStoreRating.equals("") ? true : mapper.readValue(userService.getStoreById(item.getStoreId(), token), Store.class).getRating() <= Integer.valueOf(highestStoreRating.getValue());
                            } catch (JsonProcessingException ex) {
                                Notification.show(ex.getMessage());
                                return false;
                            }
                        })
                        .toList();
                for (Product product : products) {
                    add(new Button(product.getName() + "\n" + product.getPrice(), choose -> {UI.getCurrent().navigate("/product/" + product.getId() + "/" + product.getStoreId());}));
                }
            } catch (Exception exception) {
                Notification.show(exception.getMessage());
            }
        });


        add(new H1("search products"), new HorizontalLayout(lowestPrice, highestPrice, lowestProductRating, highestProductRating, category, lowestStoreRating, highestStoreRating), new HorizontalLayout(productName, searchProduct), new HorizontalLayout(categoryName, searchProductByCategory));
    }
}
