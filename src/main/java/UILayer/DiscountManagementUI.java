package UILayer;

import DomainLayer.Store;
import InfrastructureLayer.UserRepository;
import PresentorLayer.DiscountPolicyPresenter;
import PresentorLayer.PermissionsPresenter;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import ServiceLayer.RegisteredService;
import ServiceLayer.UserService;
import DomainLayer.IToken;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("/discount-management")
public class DiscountManagementUI extends VerticalLayout {

    private final DiscountPolicyPresenter presenter;
    private final String token;

    private final ComboBox<Store>   storeBox     = new ComboBox<>("Store");
    private final ComboBox<String>  existingBox  = new ComboBox<>("Existing discounts");
    private final ComboBox<String>  scopeBox     = new ComboBox<>("Scope");
    private final NumberField       percentField = new NumberField("Percent %");
    private final ComboBox<String>  condBox      = new ComboBox<>("Condition");
    private final NumberField       limitField   = new NumberField("Value");
    private final TextField         itemField    = new TextField("Item / Category");

    /* ─── NEW controls for “combine” ─────────────────────────────────────── */
    private final ComboBox<String> logicBox  = new ComboBox<>("Logic");
    private final ComboBox<String> numBox    = new ComboBox<>("Numeric rule");
    private final MultiSelectComboBox<String> childBox =
            new MultiSelectComboBox<>("Children to combine");

    /* --------------------------------------------------------------------- */
    @Autowired
    public DiscountManagementUI(UserService       userService,
                                RegisteredService registeredService,
                                OwnerManagerService ownerMgrService,
                                IToken            tokenService,
                                UserRepository    userRepository) {

        /* presenters the same way UserHomePageUI builds them */
        UserConnectivityPresenter userConn =
                new UserConnectivityPresenter(userService, registeredService,
                        ownerMgrService, tokenService, userRepository);

        PermissionsPresenter perms =
                new PermissionsPresenter(ownerMgrService, tokenService, userRepository);

        presenter = new DiscountPolicyPresenter(userConn, ownerMgrService, perms);

        token = (String) UI.getCurrent().getSession().getAttribute("token");

        /* ----------------------- UI wiring -------------------------------- */
        List<Store> stores = presenter.updatableStores(token);
        storeBox.setItems(stores);
        storeBox.setItemLabelGenerator(Store::getName);
        storeBox.addValueChangeListener(e -> refreshDiscountList());

        scopeBox.setItems("Store", "Category", "Product");
        scopeBox.setValue("Store");

        condBox.setItems("None", "Min price", "Min quantity", "Max quantity");
        condBox.setValue("None");

        logicBox.setItems("UNDEFINED-(single)", "AND", "OR", "XOR");
        logicBox.setValue("UNDEFINED-(single)");

        numBox.setItems("Additive", "Maximum", "Multiplication");
        numBox.setValue("Additive");

        Button addBtn     = new Button("Add",     e -> addDiscount());
        Button combineBtn = new Button("Combine", e -> combineDiscounts());
        Button removeBtn  = new Button("Remove",  e -> removeDiscount());

        add(
                new H2("Discount Management"),
                storeBox,
                existingBox,
                scopeBox,
                percentField,
                itemField,
                condBox,
                limitField,
                logicBox,
                numBox,
                childBox,
                new HorizontalLayout(addBtn, combineBtn, removeBtn)
        );

        setWidth("460px");
        setAlignItems(Alignment.STRETCH);
    }

    /* --------------------------------------------------------------------- */
    private void refreshDiscountList() {
        Store s = storeBox.getValue();
        if (s == null) {
            existingBox.setItems(List.of());
            childBox.setItems(List.of());
        } else {
            List<String> ids = presenter.storeDiscountIds(token, s.getId());
            existingBox.setItems(ids);
            childBox.setItems(ids);
        }
    }

    /* -------------------------- simple add ------------------------------- */
    private void addDiscount() {
        Store s = storeBox.getValue();
        if (s == null) { Notification.show("Pick a store"); return; }

        float level = switch (scopeBox.getValue()) {
            case "Product"  -> 1f;
            case "Category" -> 2f;
            default         -> 3f;
        };

        float percent = percentField.getValue() == null ? 0f : percentField.getValue().floatValue();
        String discOn = itemField.getValue() == null  ? "" : itemField.getValue();

        float conditional = switch (condBox.getValue()) {
            case "Min price"    -> 1f;
            case "Min quantity" -> 2f;
            case "Max quantity" -> 3f;
            default             -> -1f;
        };

        float limit = limitField.getValue() == null ? -1f : limitField.getValue().floatValue();

        String msg = presenter.addDiscount(token,
                s.getName(),
                level,
                0f,          // logicComposition – single voucher
                0f,          // numericalComposition – additive
                percent,
                discOn,
                conditional, // discountCondition
                limit,       // discountLimiter
                conditional, // conditional (pass again)
                discOn);     // conditionalDiscounted

        Notification.show(msg);
        refreshDiscountList();
    }

    /* -------------------------- combine ---------------------------------- */
    private void combineDiscounts() {
        Store store = storeBox.getValue();
        if (store == null) { Notification.show("Pick a store first"); return; }

        var children = childBox.getSelectedItems();
        if (children.isEmpty()) { Notification.show("Choose at least one child"); return; }

        float logic = switch (logicBox.getValue()) {
            case "XOR" -> 1f;
            case "AND" -> 2f;
            case "OR"  -> 3f;
            default    -> 0f;           // UNDEFINED
        };
        float numeric = switch (numBox.getValue()) {
            case "Maximum"        -> 1f;
            case "Multiplication" -> 2f;
            default               -> 0f; // additive
        };

        String msg = presenter.addCompositeDiscount(
                token,
                store.getName(),
                logic,
                numeric,
                List.copyOf(children)      // immutable snapshot
        );
        Notification.show(msg);
        refreshDiscountList();
    }

    /* -------------------------- remove ----------------------------------- */
    private void removeDiscount() {
        Store s = storeBox.getValue();
        String id = existingBox.getValue();
        if (s == null || id == null) {
            Notification.show("Select store and discount ID");
            return;
        }
        Notification.show(presenter.removeDiscount(token, s.getId(), id));
        refreshDiscountList();
    }
}
