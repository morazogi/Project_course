package UILayer;

import DomainLayer.Product;
import DomainLayer.Store;
import ServiceLayer.ProductService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@Route("/product/:productid/:storeid")
public class ProductPageUI extends VerticalLayout implements BeforeEnterObserver {

    private ProductService productService;
    private UserService userService;
    private RegisteredService registeredService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ProductPageUI(ProductService configuredProductService, UserService configuredUserService, RegisteredService configuredRegisteredService, String productId, String storeId) {
        this.productService = configuredProductService;
        this.userService = configuredUserService;
        this.registeredService = configuredRegisteredService;
        if (productService.getProductById(productId).isPresent()) {
            Product product = productService.getProductById(productId).get();
            String token = (String) UI.getCurrent().getSession().getAttribute("token");
            String storeName = null;
            try {
                String jsonStore = userService.getStoreById(token, storeId);
                Store store = mapper.readValue(jsonStore, Store.class);
                storeName = store.getName();
            } catch (Exception e) {
                Notification.show("store with id: " + storeId + "does not exist");
            }
            Button signOut = new Button("Sign out", e -> {
                try {
                    UI.getCurrent().getSession().setAttribute("token", registeredService.logoutRegistered(token));
                    UI.getCurrent().navigate("");
                } catch (Exception exception) {
                    Notification.show(exception.getMessage());
                }
            }
            );

            Button homePage = new Button("Home page", e -> {
                UI.getCurrent().navigate("/:token");
            });

            HorizontalLayout upwardsPage = new HorizontalLayout(signOut, new H1(product.getName()),new H1(storeName), homePage);
            upwardsPage.setAlignItems(Alignment.CENTER);

            add(upwardsPage);

            Button addToCart = new Button("add to cart", e -> {
                Notification.show(userService.addToCart(token, storeId, productId, 1));
            });

            HorizontalLayout bottomDescription = new HorizontalLayout(new Span(product.getDescription()), new Span("" + product.getPrice()));

            add(new VerticalLayout(addToCart, bottomDescription));

            setPadding(true);
            setAlignItems(Alignment.CENTER);

        } else {
            add(new Span("No product with id:" + productId));
        }
        RestTemplate restTemplate = new RestTemplate();
        //ResponseEntity<ApiResponse<Product>> responseEntity = restTemplate.getForEntity("/product/{productid}", ApiResponse.class, productId);

    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        RouteParameters parameters = beforeEnterEvent.getRouteParameters();
            if(parameters.get("productid").isPresent()) {
                String productId = (String) parameters.get("productid").get();
                if (parameters.get("storeid").isPresent()) {
                    String storeId = parameters.get("storeid").get();
                } else {
                    add(new Span("No fitting store"));
                }
            } else {
                add(new Span("No fitting product"));
            }
    }
}
