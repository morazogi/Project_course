package UILayer;

import com.vaadin.flow.server.VaadinServlet;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

@WebServlet(urlPatterns = "/*", asyncSupported = true)
public class CustomVaadinServlet extends VaadinServlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        getServletContext().setInitParameter("productionMode", "false");
        getServletContext().setInitParameter("ui", "com.example.MainUI");
        getServletContext().setInitParameter("closeIdleSessions", "true");
    }
}
