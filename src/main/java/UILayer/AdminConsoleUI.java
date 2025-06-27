package UILayer;

import DomainLayer.IToken;
import PresentorLayer.AdminConsolePresenter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/admin")
public class AdminConsoleUI extends VerticalLayout {

    @Autowired
    public AdminConsoleUI(AdminConsolePresenter presenter, IToken tokenSvc) {

        /* ── access-control: only user “1” ── */
        String token = (String) VaadinSession.getCurrent().getAttribute("token");
        if (token == null) {
            UI.getCurrent().navigate("/guesthomepage");
            return;
        }
        try {
            tokenSvc.validateToken(token);
            if (!"1".equals(tokenSvc.extractUsername(token))) {
                throw new IllegalArgumentException();
            }
        } catch (Exception ex) {
            tokenSvc.invalidateToken(token);
            VaadinSession.getCurrent().setAttribute("token", null);
            UI.getCurrent().navigate("/guesthomepage");
            return;
        }

        /* ── UI ─────────────────────────── */
        Span status = new Span();

        ComboBox<String> users = new ComboBox<>("Users");
        users.setItems(presenter.allUsers());

        Button suspend = new Button("Suspend", e -> {
            String u = users.getValue();
            boolean ok = u != null && presenter.suspend(u);
            Notification.show(ok ? "User suspended" : "Failed");
        });
        Button unsuspend = new Button("Un-suspend", e -> {
            String u = users.getValue();
            boolean ok = u != null && presenter.unSuspend(u);
            Notification.show(ok ? "User restored" : "Failed");
        });

        add(
                new H2("System-admin console"),
                new HorizontalLayout(users, suspend, unsuspend),
                status
        );
        setPadding(true);
        setAlignItems(Alignment.CENTER);
    }
}
