package UILayer;

import DomainLayer.IToken;
import ServiceLayer.NotificationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/notification")
public class NotificationSUI extends VerticalLayout {

    private final NotificationService notificationService;

    @Autowired
    public NotificationSUI(NotificationService notificationService) {
        this.notificationService = notificationService;

        TextField username = new TextField("username");
        TextField message  = new TextField("message");
        TextField storeId = new TextField("store name");
        String token = (String) UI.getCurrent().getSession().getAttribute("token");
        connectToWebSocket(token);

        //Button connect = new Button("connect", e -> {
         //   String tokenToNotifications = tokenService.getToken(username.getValue());
        //
           //  UI.getCurrent().getPage().executeJs("""
            //    window._shopWs?.close();
              //  window._shopWs = new WebSocket('ws://'+location.host+'/ws?token='+$0);
               // window._shopWs.onmessage = ev => {
                 // const txt = (()=>{try{return JSON.parse(ev.data).message}catch(e){return ev.data}})();
          //        const n = document.createElement('vaadin-notification');
            //      n.renderer = r => r.textContent = txt;
              //    n.duration = 5000;
                //  n.position = 'top-center';
      //            document.body.appendChild(n);
        //          n.opened = true;
          //      };
            //    """, tokenToNotifications);
    //    });

        Button send = new Button("send", e ->
                notificationService.notifyUser(username.getValue(), message.getValue(), storeId.getValue()));

        add(new H1("notificationss"), username, message, storeId, send);
        setAlignItems(Alignment.START);
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