package PresentorLayer;

import DomainLayer.Product;
import ServiceLayer.OwnerManagerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;


public class QuantiyButton extends VerticalLayout {

    public QuantiyButton(String userId, OwnerManagerService ownerManagerService, Product product) {
        Button productAnQuantity = new Button(product.getName() + "\n" + product.getQuantity(), e -> {
            TextField quantity = new TextField("new quantity");
            Span mesrag = new Span("");
            Button updateNumber = new Button("update quantity", b -> {
                Integer integerQuantity = 0;
            try {
                integerQuantity = Integer.valueOf(quantity.getValue());
            } catch (Exception A) {
                mesrag.setText("Invalid quantity");
            }
              mesrag.setText(ownerManagerService.updateProductQuantity(userId, product.getStoreId(), product.getId(), integerQuantity));
            });
        add(quantity, mesrag, updateNumber);
        });
        add(productAnQuantity);
    }
}
