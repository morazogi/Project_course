package DomainLayer.DomainServices;

import DomainLayer.IToken;
import DomainLayer.Order;
import InfrastructureLayer.OrderRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HistorTest {

    @Mock IToken          tokenSvc;
    @Mock OrderRepository orderRepo;
    @Mock UserRepository  userRepo;
    @Mock Order           o1;
    @Mock Order           o2;

    private History history;

    @BeforeEach
    void setUp() {
        history = new History(tokenSvc, orderRepo, userRepo);

        doNothing().when(tokenSvc).validateToken(anyString());
        when(tokenSvc.extractUsername("token")).thenReturn("alice");

        when(o1.toString()).thenReturn("o1");
        when(o2.toString()).thenReturn("o2");
    }

    @Test
    void getOrderHistory_success() throws Exception {
        when(orderRepo.findByUserID("alice")).thenReturn(List.of(o1, o2));

        List<String> list = history.getOrderHistory("token");
        assertEquals(List.of("o1", "o2"), list);
    }

    @Test
    void getOrderHistory_nullToken_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> history.getOrderHistory(null));
    }

    @Test
    void getOrderHistory_extractNull_throws() throws Exception {
        when(tokenSvc.extractUsername("token")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> history.getOrderHistory("token"));
    }
}
