package UILayer;

import DomainLayer.IToken;
import InfrastructureLayer.UserRepository;
import PresentorLayer.ButtonPresenter;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
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

    @Autowired
    public EditStorePageUI(UserService userService, RegisteredService registeredService, OwnerManagerService ownerManagerService, IToken tokenService, UserRepository userRepository) {
        this.buttonPresenter = new ButtonPresenter(registeredService, tokenService);
        this.userConnectivityPresenter = new UserConnectivityPresenter(userService, registeredService, ownerManagerService, tokenService, userRepository);
        // Store Selection and Sign-out Section
        ComboBox<String> storeDropdown = new ComboBox<>("Store");
        //storeDropdown.setItems("Store 1", "Store 2", "Store 3");
        storeDropdown.setPlaceholder("Select Store");
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        Button signOutButton = buttonPresenter.signOutButton(token);

        HorizontalLayout topBar = new HorizontalLayout(
                new H2("Store Manager Dashboard"),
                signOutButton
        );
        topBar.setAlignItems(Alignment.CENTER);
        add(topBar);

        /*
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
                addProductButton
        );



        // Set Add Product Form Styling and Padding
        addProductForm.setPadding(true);
        addProductForm.setAlignItems(Alignment.CENTER);
        add(addProductForm);
 */

        // Set New Discount Section
        TextField storeName = new TextField("Store name");
        NumberField discountLevel = new NumberField("Discount Level");
        NumberField logicComposition = new NumberField("Logic Composition");
        NumberField numericalComposition = new NumberField("Numerical Composition");
        NumberField percentDiscount = new NumberField("Percent Discount");
        TextField discountedItem = new TextField("Discounted Item");
        NumberField discountCondition = new NumberField("Condition");
        NumberField discountLimiter = new NumberField("Limiter");
        NumberField conditional = new NumberField("1 = Minimum total price\n" +
                "2 = Minimum quantity of item\n" +
                "3 = Maximum quantity of item\n");
        TextField conditionalDiscounted = new TextField("Conditional Discounted");

        Button addDiscountButton = new Button("Add Discount", e -> {
                String str = this.userConnectivityPresenter.addDiscount(token, storeName.getValue(), discountLevel.getValue().floatValue(), logicComposition.getValue().floatValue(),
                        numericalComposition.getValue().floatValue(), percentDiscount.getValue().floatValue(), discountedItem.getValue(), discountCondition.getValue().floatValue(),
                        discountLimiter.getValue().floatValue(), conditional.getValue().floatValue(), conditionalDiscounted.getValue());
            //add(new Span(userConnectivityPresenter.addDiscount(token, storeName.getValue(), discountLevel.getValue().floatValue(), logicComposition.getValue().floatValue(),
            //       numericalComposition.getValue().floatValue(), percentDiscount.getValue().floatValue(), discountedItem.getValue(), discountCondition.getValue().floatValue(),
            //      discountLimiter.getValue().floatValue(), conditional.getValue().floatValue(),  conditionalDiscounted.getValue())));
        });

        VerticalLayout addDiscountForm = new VerticalLayout(
                new Span("Set New Discount"),
                storeName,
                discountLevel,
                logicComposition,
                numericalComposition,
                percentDiscount,
                discountedItem,
                discountCondition,
                discountLimiter,
                conditionalDiscounted,
                conditional,
                addDiscountButton
        );
        connectToWebSocket(token);

        // Set Add Discount Form Styling and Padding
        addDiscountForm.setPadding(true);
        addDiscountForm.setAlignItems(Alignment.CENTER);
        add(addDiscountForm);

        // Notification Example for Adding Product/Discount
        Notification notification = new Notification("Product or Discount Added!", 3000);
        notification.open();
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