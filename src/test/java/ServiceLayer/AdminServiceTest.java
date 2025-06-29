package ServiceLayer;

import DomainLayer.DomainServices.AdminOperationsMicroservice;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository              userRepo;
    @Mock StoreRepository             storeRepo;
    @Mock TokenService                tokenService;
    @Mock AdminOperationsMicroservice micro;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(userRepo, storeRepo, tokenService);

        /* simply swap the real microservice with our mock */
        ReflectionTestUtils.setField(service, "adminService", micro);
    }

    /* ──────────────── happy paths ──────────────── */

    @Test
    void adminCloseStore_success() {
        when(micro.adminCloseStore("admin", "store")).thenReturn(true);

        assertTrue(service.adminCloseStore("admin", "store"));
        verify(micro).adminCloseStore("admin", "store");
    }

    @Test
    void adminSuspendMember_success() {
        when(micro.suspendMember("admin", "user")).thenReturn(true);

        assertTrue(service.adminSuspendMember("admin", "user"));
        verify(tokenService).suspendUser("user");
    }

    @Test
    void adminUnSuspendMember_success() {
        when(micro.unSuspendMember("admin", "user")).thenReturn(true);

        assertTrue(service.adminUnSuspendMember("admin", "user"));
        verify(tokenService).unsuspendUser("user");
    }

    /* ──────────────── failure / edge cases ──────────────── */

    @Test
    void adminCloseStore_exception_returnsFalse() {
        when(micro.adminCloseStore(anyString(), anyString()))
                .thenThrow(new RuntimeException("boom"));

        assertFalse(service.adminCloseStore("admin", "store"));
    }

    @Test
    void adminSuspendMember_microserviceReturnsFalse() {
        when(micro.suspendMember(anyString(), anyString())).thenReturn(false);

        assertFalse(service.adminSuspendMember("admin", "user"));
        verify(tokenService, never()).suspendUser(anyString());
    }
}
