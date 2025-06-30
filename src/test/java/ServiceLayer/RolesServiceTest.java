package ServiceLayer;

import DomainLayer.DomainServices.StoreManagementMicroservice;
import InfrastructureLayer.RolesRepository;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

    @Mock StoreRepository storeRepo;
    @Mock UserRepository  userRepo;
    @Mock RolesRepository rolesRepo;

    private RolesService service;

    @BeforeEach
    void setUp() {
        // real ctor builds a concrete RolesRepository – swap it with our mock
        service = new RolesService(storeRepo, userRepo);
        ReflectionTestUtils.setField(service, "rolesRepository", rolesRepo);
    }

    /* ─────── success path hits every public method ─────── */

    @Test
    void allOperations_successfulFlow() {
        when(rolesRepo.appointStoreOwner("a", "s", "u")).thenReturn(true);
        when(rolesRepo.removeStoreOwner ("r", "s", "o")).thenReturn(true);
        when(rolesRepo.appointStoreManager("a","s","u", new boolean[0])).thenReturn(true);
        when(rolesRepo.removeStoreManager ("r","s","m")).thenReturn(true);
        when(rolesRepo.updateManagerPermissions("o","s","m", new boolean[0])).thenReturn(true);
        when(rolesRepo.relinquishOwnership("o","s")).thenReturn(true);
        when(rolesRepo.relinquishManagement("m","s")).thenReturn(true);
        when(rolesRepo.getManagerPermissions("o","s","m"))
                .thenReturn(Map.of("X", true));
        when(rolesRepo.getStoreRoleInfo("q","s")).thenReturn("info");
        when(rolesRepo.isFounderOrOwner("u","s")).thenReturn(true);

        assertTrue(service.appointStoreOwner("a","s","u"));
        assertTrue(service.removeStoreOwner ("r","s","o"));
        assertTrue(service.appointStoreManager("a","s","u", new boolean[0]));
        assertTrue(service.removeStoreManager ("r","s","m"));
        assertTrue(service.updateManagerPermissions("o","s","m", new boolean[0]));
        assertTrue(service.relinquishOwnership("o","s"));
        assertTrue(service.relinquishManagement("m","s"));
        assertEquals(Map.of("X", true),
                service.getManagerPermissions("o","s","m"));
        assertEquals("info", service.getStoreRoleInfo("q","s"));
        assertTrue(service.isFounderOrOwner("u","s"));
    }

    /* ─────── example failure path to hit catch-blocks ─────── */

    @Test
    void appointStoreOwner_whenRepoThrows_wrapsInRuntime() {
        when(rolesRepo.appointStoreOwner(any(), any(), any()))
                .thenThrow(new RuntimeException("DB down"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.appointStoreOwner("a","s","u"));
        assertEquals("Failed to appoint store owner", ex.getMessage());
    }

    @Test
    void isFounderOrOwner_repoThrows_returnsFalse() {
        when(rolesRepo.isFounderOrOwner(any(), any()))
                .thenThrow(new RuntimeException("oops"));

        assertFalse(service.isFounderOrOwner("u","s"));
    }
}
