package DomainLayer.DomainServices;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import DomainLayer.IOrderRepository;
import DomainLayer.IStoreRepository;
import DomainLayer.IToken;
import DomainLayer.IUserRepository;

public class History {
    private IToken Tokener;
    private ObjectMapper mapper = new ObjectMapper();
    private IOrderRepository orderRepository;
    private IUserRepository userRepository;


    public History(IToken Tokener, IOrderRepository orderRepository, IUserRepository userRepository) {
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
        return orderRepository.getOrderHistory(username);
    }
}
