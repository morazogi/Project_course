package DomainLayer.DomainServices;
import java.util.ArrayList;
import java.util.List;

import DomainLayer.Order;
import InfrastructureLayer.*;
import DomainLayer.IToken;

public class History {
    private IToken Tokener;
    private OrderRepository orderRepository;
    private UserRepository userRepository;


    public History(IToken Tokener, OrderRepository orderRepository, UserRepository userRepository) {
        this.Tokener = Tokener;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public List<String> getOrderHistory(String token) throws Exception {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        Tokener.validateToken(token);
        String username = Tokener.extractUsername(token);
        if (username == null) {
            throw new IllegalArgumentException("Invalid token");
        }
        ArrayList<String> ordersInString = new ArrayList<>();
        List<Order> orders = orderRepository.findByUserID(username);
        for (Order o : orders){
            ordersInString.add(o.toString());
        }
        return ordersInString;
    }
}
