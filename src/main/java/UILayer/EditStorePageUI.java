package UILayer;

import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/edit-store")
public class EditStorePageUI extends VerticalLayout {

    private final ButtonPresenter buttonPresenter;
    private final UserConnectivityPresenter userConnectivityPresenter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final IToken tokenService;

    @Autowired
    public EditStorePageUI(RegisteredService registeredService, UserService userService, OwnerManagerService ownerManagerService, IToken tokenService, InfrastructureLayer.UserRepository userRepository) {
        this.userConnectivityPresenter = new UserConnectivityPresenter(userService, registeredService, ownerManagerService, tokenService, userRepository);
        this.buttonPresenter = new ButtonPresenter(registeredService);
        this.tokenService = tokenService;
        // Store Selection and Sign-out Section
        ComboBox<String> storeDropdown = new ComboBox<>("Store");
        storeDropdown.setItems("Store 1", "Store 2", "Store 3"); // Example store names
        storeDropdown.setPlaceholder("Select Store");
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        Button signOutButton = buttonPresenter.signOutButton(token);
        String username = tokenService.extractUsername(token);

        String jsonUser = userRepository.getUser(username);
        RegisteredUser user = null;
        try {
            user = mapper.readValue(jsonUser, RegisteredUser.class);
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }


        HorizontalLayout topBar = new HorizontalLayout(
                new H2("Store Manager Dashboard"),
                storeDropdown,
                signOutButton
        );
        topBar.setAlignItems(Alignment.CENTER);
        add(topBar);

        // Add New Product Section
        TextField productName = new TextField("Product Name");
        TextField productDescription = new TextField("Description");
        NumberField productPrice = new NumberField("Price");
        NumberField productQuantity = new NumberField("Quantity");
        NumberField productRating = new NumberField("Rating");
        TextField productCategory = new TextField("Category");
        TextField productStore = new TextField("Store");

        Button addProductButton = new Button("Add Product", e -> {
            // need to give all this parameters to the contracture of the product, but i dont find the store handler

        });

        VerticalLayout addProductForm = new VerticalLayout(
                new Span("Add New Product"),
                productName,
                productDescription,
                productPrice,
                productQuantity,
                productRating,
                productCategory,
                productStore,
                addProductButton
        );

        // Set Add Product Form Styling and Padding
        addProductForm.setPadding(true);
        addProductForm.setAlignItems(Alignment.CENTER);
        add(addProductForm);

        // Set New Discount Section
        TextField storeId = new TextField("storeId");
        TextField discountId = new TextField("discountId");
        TextField Id = new TextField("Id");
        NumberField discountLevel = new NumberField("Discount Level");
        NumberField logicComposition = new NumberField("Logic Composition");
        NumberField numericalComposition = new NumberField("Numerical Composition");
        TextField discountsId = new TextField("list of discounts seperated by _");
        NumberField percentDiscount = new NumberField("Percent Discount");
        TextField discountedItem = new TextField("Discounted Item");
        NumberField discountCondition = new NumberField("Condition");
        NumberField discountLimiter = new NumberField("Limiter");
        TextField conditionalDiscounted = new TextField("Conditional Discounted");

        final String ownerId1 = user.getID();

        Button addDiscountButton = new Button("Add Discount", e -> {
            this.userConnectivityPresenter.applyDiscount(ownerId1,
                    storeId.getValue(),
                    discountId.getValue(),
                    Id.getValue(),
                    (float)discountLevel.getValue().doubleValue(),
                    (float)logicComposition.getValue().doubleValue(),
                    (float)numericalComposition.getValue().doubleValue(),
                    discountsId.getValue(),
                    (float)percentDiscount.getValue().doubleValue(),
                    discountedItem.getValue(),
                    (float)discountCondition.getValue().doubleValue(),
                    (float)discountLimiter.getValue().doubleValue(),
                    conditionalDiscounted.getValue());
        });

        VerticalLayout addDiscountForm = new VerticalLayout(
                new Span("Set New Discount"),
                storeId,
                discountId,
                Id,
                discountLevel,
                logicComposition,
                numericalComposition,
                discountsId,
                percentDiscount,
                discountedItem,
                discountCondition,
                discountLimiter,
                conditionalDiscounted,
                addDiscountButton
        );

        // Set Add Discount Form Styling and Padding
        addDiscountForm.setPadding(true);
        addDiscountForm.setAlignItems(Alignment.CENTER);
        add(addDiscountForm);

        // Notification Example for Adding Product/Discount
        Notification notification = new Notification("Product or Discount Added!", 3000);
        notification.open();
    }
}