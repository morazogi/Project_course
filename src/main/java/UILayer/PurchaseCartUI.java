package UILayer;

import DomainLayer.*;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.DomainServices.DiscountPolicyMicroservice;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import InfrastructureLayer.ProductRepository;
import InfrastructureLayer.DiscountRepository;
import ServiceLayer.ErrorLogger;
import ServiceLayer.ProductService;
import ServiceLayer.RegisteredService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("/purchasecart")
public class PurchaseCartUI extends VerticalLayout {

    private final ProductService productService;
    private final RegisteredService registeredService;
    private final ProductRepository productRepository;
    private final IToken tokenService;
    private final UserRepository userRepository;
    private final DiscountRepository discountRepository;
    private final StoreRepository storeRepository;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public PurchaseCartUI(ProductService productService, RegisteredService registeredService, ProductRepository productRepository, IToken tokenService, UserRepository userRepository, StoreRepository storeRepository, DiscountRepository discountRepository) {
        this.productService = productService;
        this.registeredService = registeredService;
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);

        String username = tokenService.extractUsername(token);
        RegisteredUser user = null;
        try {
            user = userRepository.getById(username);
        } catch (Exception e) {
            ErrorLogger.logError(username,e.toString(),"user "+username+" not found in class purchaseCartUI");
        }
        // Buttons and Navigation
        Button signOut = new Button("🔐 Sign out", e -> {
            try {
                UI.getCurrent().getSession().setAttribute("token", registeredService.logoutRegistered(token));
                UI.getCurrent().navigate("");
            } catch (Exception exception) {
                Notification.show(exception.getMessage());
            }
        });

        Button homePage = new Button("🏠 Home", e -> {
            UI.getCurrent().navigate("");
        });

        Button confirmPurchase = new Button("✅ Confirm Purchase", e -> {
            // Logic for confirming purchase
            Notification.show("Purchase Confirmed!");
        });

        Button cancelPurchase = new Button("❌ Cancel Purchase", e -> {
            // Logic for canceling purchase
            Notification.show("Purchase Canceled!");
        });

        // Add to layout
        HorizontalLayout topLayout = new HorizontalLayout(signOut, new H1("Shopping Cart"), homePage);
        add(topLayout);

        // Get User Data (Assuming logged in user is saved in session)
        ShoppingCart shoppingCart = user.getShoppingCart();

        // Display Product Grid
        Grid<Product> productGrid = new Grid<>(Product.class);
        List<ShoppingBag> shoppingBags = shoppingCart.getShoppingBags();
//        for (ShoppingBag shoppingBag : shoppingBags) {
//            productGrid.setItems(); // creat presentor**
//        }
        productGrid.setColumns("name", "quantity", "description", "price");

        // Total price section
        TextField totalField = new TextField();
        totalField.setLabel("Total");
        double payment = 0;
        for (ShoppingBag shoppingBag : shoppingBags) {
            DiscountPolicyMicroservice discountPolicy = new DiscountPolicyMicroservice(storeRepository, userRepository, productRepository, discountRepository);
            Map<Product, Integer> products = new HashMap<Product, Integer>();
            for (String product : shoppingBag.getProducts().keySet()) {
                products.put(productRepository.getById(product), shoppingBag.getProducts().get(product));
            }

            Product firstProduct = products.keySet().iterator().next();
            Map<String, Integer> productsString = new HashMap<>();
            for (Map.Entry<Product, Integer> entry : products.entrySet()) {
                String storeId = entry.getKey().getStoreId();  // Extract key
                productsString.put(storeId, entry.getValue()); // Preserve value
            }
            payment = payment + discountPolicy.calculatePrice(firstProduct.getStoreId(), productsString);
        }

        totalField.setValue("$" + payment); // Assuming getTotalPrice() exists
        totalField.setReadOnly(true);

        // Buttons for Confirm/Cancel
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmPurchase, cancelPurchase);
        add(productGrid, totalField, buttonLayout);
        add(new Button("purchase cart", e -> {UI.getCurrent().navigate("/purchasecartfinal");}));
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        this.storeRepository = storeRepository;
    }

    public void connectToWebSocket(String token) {
        UI.getCurrent().getPage().executeJs("""
                window._shopWs?.close();
                window._shopWs = new WebSocket('ws://'+location.host+'/ws?token='+$0);
                window._shopWs.onmessage = ev => {
                  const txt = (()=>{try{return JSON.parse(ev.data).message}catch(e){return ev.data}})();
                  const n = document.createElement('vaadin-notification');
                  n.renderer = r => r.textContent = txt;
                  n.duration = 5000;
                  n.position = 'top-center';
                  document.body.appendChild(n);
                  n.opened = true;
                };
                """, token);
    }
}