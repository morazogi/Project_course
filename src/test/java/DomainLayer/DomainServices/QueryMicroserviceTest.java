package DomainLayer.DomainServices;

import DomainLayer.ICustomerInquiryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryMicroserviceTest {

    @Mock ICustomerInquiryRepository repo;
    private QueryMicroservice        svc;

    @BeforeEach
    void setUp() {
        svc = new QueryMicroservice(repo);
    }

    /* ─────────── get inquiries ─────────── */

    @Test
    void getCustomerInquiries_nullArgs_returnsEmpty() {
        assertTrue(svc.getCustomerInquiries(null, "store").isEmpty());
        assertTrue(svc.getCustomerInquiries("owner", null).isEmpty());
    }

    @Test
    void getCustomerInquiries_valid_delegates() {
        List<Map<String, Object>> data = List.of(Map.of("id", "1"));
        when(repo.getInquiriesByStore("store")).thenReturn(data);

        assertEquals(data, svc.getCustomerInquiries("owner", "store"));
    }

    /* ─────────── add inquiry ─────────── */

    @Test
    void addCustomerInquiry_valid_returnsId_andSaves() {
        when(repo.getNextInquiryId()).thenReturn("123");

        String id = svc.addCustomerInquiry("cust", "store", "message");
        assertEquals("123", id);

        ArgumentCaptor<Map<String, Object>> cap = ArgumentCaptor.forClass(Map.class);
        verify(repo).addInquiry(eq("store"), cap.capture());
        assertEquals("123", cap.getValue().get("id"));
    }

    @Test
    void addCustomerInquiry_invalid_returnsNull() {
        assertNull(svc.addCustomerInquiry(null, "store", "msg"));
        assertNull(svc.addCustomerInquiry("cust", "store", ""));
        verify(repo, never()).addInquiry(anyString(), any());
    }

    /* ─────────── respond to inquiry ─────────── */

    @Test
    void respondToCustomerInquiry_valid_updatesRepo() {
        when(repo.updateInquiry(eq("store"), eq("inq"), anyMap())).thenReturn(true);
        assertTrue(svc.respondToCustomerInquiry("owner", "store", "inq", "OK"));
    }

    @Test
    void respondToCustomerInquiry_invalid_returnsFalse() {
        assertFalse(svc.respondToCustomerInquiry("owner", "store", null, "OK"));
        assertFalse(svc.respondToCustomerInquiry("owner", "store", "id", ""));
    }

    /* ─────────── customer-centric inquiry fetch ─────────── */

    @Test
    void getCustomerInquiriesByCustomer_valid_delegates() {
        List<Map<String, Object>> list = List.of(Map.of("id", "1"));
        when(repo.getInquiriesByCustomer("cust")).thenReturn(list);

        assertEquals(list, svc.getCustomerInquiriesByCustomer("cust"));
    }

    @Test
    void getCustomerInquiriesByCustomer_null_returnsEmpty() {
        assertTrue(svc.getCustomerInquiriesByCustomer(null).isEmpty());
        verify(repo, never()).getInquiriesByCustomer(any());
    }
}
