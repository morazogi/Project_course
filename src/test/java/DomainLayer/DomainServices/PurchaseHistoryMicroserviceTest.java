package DomainLayer.DomainServices;

import DomainLayer.Order;
import InfrastructureLayer.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseHistoryMicroServiceTest {

    private PurchaseHistoryMicroservice createService(OrderRepository repo) {
        PurchaseHistoryMicroservice svc = new PurchaseHistoryMicroservice();
        // inject mocked repository using the existing init-like method
        svc.PurchaseHistoryMicroservice(repo);
        return svc;
    }

    @Test
    @DisplayName("Returns empty list when repository has no orders")
    void getStorePurchaseHistory_returnsEmpty_whenNoOrders() throws JsonProcessingException {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        Mockito.when(repo.findByStoreID("store-1")).thenReturn(Collections.emptyList());

        PurchaseHistoryMicroservice svc = createService(repo);
        List<String> result = svc.getStorePurchaseHistory(
                "owner-1",
                "store-1",
                new Date(0),
                new Date()
        );

        assertTrue(result.isEmpty(), "Expected an empty purchase history list");
        Mockito.verify(repo).findByStoreID("store-1");
    }

    @Test
    @DisplayName("Filters orders strictly between start and end dates")
    void getStorePurchaseHistory_filtersByDateRange() throws JsonProcessingException {
        Date start = new Date(1_000_000L);
        Date inside = new Date(2_000_000L);
        Date end = new Date(3_000_000L);

        Order before = Mockito.mock(Order.class);
        Mockito.when(before.getDate()).thenReturn(new Date(500_000L));
        Mockito.when(before.getId()).thenReturn("before");

        Order between = Mockito.mock(Order.class);
        Mockito.when(between.getDate()).thenReturn(inside);
        Mockito.when(between.getId()).thenReturn("inside");

        Order onStart = Mockito.mock(Order.class);
        Mockito.when(onStart.getDate()).thenReturn(start);
        Mockito.when(onStart.getId()).thenReturn("onStart");

        Order onEnd = Mockito.mock(Order.class);
        Mockito.when(onEnd.getDate()).thenReturn(end);
        Mockito.when(onEnd.getId()).thenReturn("onEnd");

        Order after = Mockito.mock(Order.class);
        Mockito.when(after.getDate()).thenReturn(new Date(4_000_000L));
        Mockito.when(after.getId()).thenReturn("after");

        List<Order> allOrders = Arrays.asList(before, between, onStart, onEnd, after);

        OrderRepository repo = Mockito.mock(OrderRepository.class);
        Mockito.when(repo.findByStoreID("store-1")).thenReturn(allOrders);

        PurchaseHistoryMicroservice svc = createService(repo);
        List<String> result = svc.getStorePurchaseHistory("owner-1", "store-1", start, end);

        assertEquals(1, result.size(), "Exactly one order should match the date range");
        assertEquals("inside", result.get(0));
    }

    @Test
    @DisplayName("Does not throw JsonProcessingException with normal input")
    void getStorePurchaseHistory_doesNotThrow() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        Mockito.when(repo.findByStoreID(Mockito.anyString())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> {
            PurchaseHistoryMicroservice svc = createService(repo);
            svc.getStorePurchaseHistory("owner-1", "store-1", new Date(0), new Date());
        });
    }
}
