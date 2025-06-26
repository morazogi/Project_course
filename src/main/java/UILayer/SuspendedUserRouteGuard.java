package UILayer;

import ServiceLayer.TokenService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Global guard: the moment a suspended user’s browser
 * contacts the server (navigation OR background poll),
 * the session is cleared and the user is taken to /guesthomepage.
 */
@Component          // discovered automatically by Spring-Boot + Vaadin
public class SuspendedUserRouteGuard implements VaadinServiceInitListener {

    private final TokenService tokens;

    @Autowired
    public SuspendedUserRouteGuard(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInit -> {
            UI ui = uiInit.getUI();

            /* ① Kick on every navigation */
            ui.addBeforeEnterListener(this::kickIfSuspended);

            /* ② Kick “passively” once per second (no click needed) */
            ui.setPollInterval(1000);                     // 1 s
            ui.addPollListener(poll -> kickIfSuspended(ui));
        });
    }

    /* ---------- helpers ---------- */

    private void kickIfSuspended(BeforeEnterEvent e) {
        if ("guesthomepage".equals(e.getLocation().getPath())) return;
        if (isSuspended()) e.forwardTo("/guesthomepage");
    }

    private void kickIfSuspended(UI ui) {
        if (isSuspended()) ui.access(() -> ui.navigate("/guesthomepage"));
    }

    /** @return true ⇢ user is logged in AND suspended. */
    private boolean isSuspended() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) return false;

        String token = (String) session.getAttribute("token");
        if (token == null) return false;

        String user;
        try { user = tokens.extractUsername(token); }
        catch (Exception ex) { return false; }

        if (!tokens.showSuspended().contains(user)) return false;

        // clean-up (black-list + remove from session)
        try { tokens.invalidateToken(token); } catch (Exception ignored) {}
        session.setAttribute("token", null);
        return true;
    }
}
