package UILayer;

import DomainLayer.ManagerPermissions;
import DomainLayer.Store;
import PresentorLayer.ProductPresenter;
import PresentorLayer.UserConnectivityPresenter;
import ServiceLayer.OwnerManagerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;

public class PermissionButtonsUI extends VerticalLayout {

    public PermissionButtonsUI(ProductPresenter productPresenter, UserConnectivityPresenter userConnectivityPresenter, String token, Store storeName, ManagerPermissions perms, OwnerManagerService ownerManagerService) {
        HorizontalLayout buttonLayout1 = new HorizontalLayout();
        HorizontalLayout buttonLayout2 = new HorizontalLayout();
        boolean hasAnyPermission = false;
        if (perms.getPermission(ManagerPermissions.PERM_VIEW_STORE)) {
            buttonLayout1.add(new Button("🏬 View Store"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY)) {
            buttonLayout1.add(new Button("📦 Manage Inventory", e -> {
                add(productPresenter.getInventoryList(token, storeName.getId(), ownerManagerService));
            }));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_MANAGE_STAFF)) {
            buttonLayout1.add(new Button("👥 Manage Staff"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_ADD_PRODUCT)) {
            buttonLayout1.add(new Button("➕ Add Product", e -> {
                TextField productName = new TextField("product name");
                TextField description = new TextField("description");
                TextField price = new TextField("price");
                TextField quantity = new TextField("quantity");
                TextField category = new TextField("category");
                Span message = new Span("");
                Button add = new Button("add", x -> {
                    message.setText(userConnectivityPresenter.addNewProductToStore(token, storeName.getName(), productName.getValue(), description.getValue(), price.getValue(), quantity.getValue(), category.getValue()));
                });
                add(new Span("add product to store " + storeName.getName()), new HorizontalLayout(productName, description, price, quantity, category), add, message);
            }));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT)) {
            buttonLayout2.add(new Button("❌ Remove Product", e -> {TextField productName = new TextField("product name");
                Span message = new Span("");
                Button remove = new Button("remove", x -> {
                    message.setText(userConnectivityPresenter.removeProductFromStore(token, storeName.getName(), productName.getValue()));
                });
                add(new Span("remove product from store " + storeName.getName()), productName, remove, message);}));

            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT)) {
            buttonLayout2.add(new Button("✏️ Update Product", e -> {TextField productName = new TextField("product name");
                Span detailxs = new Span("");
                TextField description = new TextField("description");
                TextField price = new TextField("price");
                TextField quantity = new TextField("New product name");
                TextField category = new TextField("category");
                Span message = new Span("");
                Button update = new Button("update", x -> {
                    message.setText(userConnectivityPresenter.updateProduct(token, storeName.getName(), productName.getValue(), description.getValue(), price.getValue(), quantity.getValue(), category.getValue()));
                });
                Button getInformation = new Button("get information about product", k -> {
                    detailxs.setText(userConnectivityPresenter.getInformationAboutProduct(token, storeName.getName(), productName.getValue()));
                    add(description, price, quantity, category, update);
                });

                this.add(new Span("update product in store " + storeName.getName()), productName, getInformation, detailxs, message);}));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_UPDATE_POLICY)) {
            buttonLayout2.add(new Button("📝 Update Policy"));
            hasAnyPermission = true;
        }
        if (perms.getPermission(ManagerPermissions.PERM_OPEN_STORE)) {
            buttonLayout2.add(new Button("open store", e -> {
                add(new Span(userConnectivityPresenter.openStore(token, storeName.getId())));
            }));
            hasAnyPermission = true;
        }

        if (perms.getPermission(ManagerPermissions.PERM_CLOSE_STORE)) {
            buttonLayout2.add(new Button("close store", e -> {
                add(new Span(userConnectivityPresenter.closeStore(token, storeName.getId())));
            }));
            hasAnyPermission = true;
        }



        if (!hasAnyPermission) {
            add(new Paragraph("⚠️ You currently don’t have permissions for any store management actions. Contact the store owner to update your role."));
        };

        add(buttonLayout1, buttonLayout2);

        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}
