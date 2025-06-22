package DomainLayer.DomainServices;

import DomainLayer.Store;
import DomainLayer.Roles.RegisteredUser;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreManagementMicroserviceTest {

    private StoreRepository storeRepository;
    private UserRepository userRepository;
    private StoreManagementMicroservice service;
    private Store store;
    private RegisteredUser owner;
    private RegisteredUser user;
    private RegisteredUser manager;

    private final String storeId = "store-1";
    private final String ownerId = "owner-1";
    private final String userId = "user-2";
    private final String managerId = "manager-3";

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        userRepository = mock(UserRepository.class);
        service = new StoreManagementMicroservice(storeRepository, userRepository);

        store = spy(new Store(ownerId, "Test Store"));
        store.setId(storeId);

        owner = spy(new RegisteredUser(ownerId, "pw"));
        user = spy(new RegisteredUser(userId, "pw2"));
        manager = spy(new RegisteredUser(managerId, "pw3"));

        // Setup repository mocks
        when(storeRepository.getById(storeId)).thenReturn(store);
        when(userRepository.getById(ownerId)).thenReturn(owner);
        when(userRepository.getById(userId)).thenReturn(user);
        when(userRepository.getById(managerId)).thenReturn(manager);

        // Owner is founder and owner
        doReturn(true).when(store).userIsOwner(ownerId);
        doReturn(false).when(store).userIsOwner(userId);
        doReturn(false).when(store).userIsManager(userId);
        doReturn(false).when(store).userIsOwner(managerId);
        doReturn(true).when(store).userIsManager(managerId);
        doReturn(true).when(store).isFounder(ownerId);
        doReturn(false).when(store).isFounder(userId);
        doReturn(false).when(store).isFounder(managerId);

        // Permissions
        doReturn(true).when(store).userHasPermissions(ownerId, "MANAGE_STAFF");
        doReturn(false).when(store).userHasPermissions(userId, "MANAGE_STAFF");
        doReturn(true).when(store).userHasPermissions(managerId, "MANAGE_STAFF");
        doReturn(true).when(store).userIsManager(managerId);

        // Superior checks
        doReturn(true).when(store).checkIfSuperior(ownerId, managerId);
        doReturn(false).when(store).checkIfSuperior(userId, ownerId);
        doReturn(true).when(store).checkIfSuperior(ownerId, userId);
    }

    @Test
    void appointStoreOwner_success() {
        doReturn(false).when(store).userIsOwner(userId);
        doReturn(false).when(store).userIsManager(userId);

        boolean result = service.appointStoreOwner(ownerId, storeId, userId);

        assertTrue(result);
        verify(store).addOwner(ownerId, userId);
        verify(user).addOwnedStore(storeId);
    }

    @Test
    void appointStoreOwner_alreadyOwnerOrManager() {
        doReturn(true).when(store).userIsOwner(userId);

        boolean result = service.appointStoreOwner(ownerId, storeId, userId);

        assertFalse(result);
        verify(store, never()).addOwner(anyString(), anyString());
    }

    @Test
    void sendOwnershipProposal_success() {
        String proposal = service.sendOwnershipProposal(userId, storeId, "Join us!");

        assertTrue(proposal.contains("Hi, would you like to become an owner"));
    }

    @Test
    void sendOwnershipProposal_userAlreadyOwnerOrManager() {
        doReturn(true).when(store).userIsOwner(userId);

        assertThrows(IllegalArgumentException.class, () ->
                service.sendOwnershipProposal(userId, storeId, "Join us!"));
    }

    @Test
    void respondToOwnerAppointment_accept() {
        doReturn(false).when(store).userIsOwner(userId);
        doReturn(false).when(store).userIsManager(userId);

        boolean result = service.respondToOwnerAppointment(userId, storeId, true);

        assertTrue(result);
    }

    @Test
    void respondToOwnerAppointment_reject() {
        boolean result = service.respondToOwnerAppointment(userId, storeId, false);

        assertFalse(result);
    }

    @Test
    void removeStoreOwner_success() {
        Set<String> subordinates = new HashSet<>(List.of("sub1", "sub2"));
        doReturn(true).when(store).checkIfSuperior(ownerId, userId);
        doReturn(subordinates).when(store).getAllSubordinates(userId);

        // Mock userRepository.getById for all subordinates
        RegisteredUser sub1 = mock(RegisteredUser.class);
        RegisteredUser sub2 = mock(RegisteredUser.class);
        when(userRepository.getById("sub1")).thenReturn(sub1);
        when(userRepository.getById("sub2")).thenReturn(sub2);

        boolean result = service.removeStoreOwner(ownerId, storeId, userId);

        assertTrue(result);
        verify(store).terminateOwnership(userId);
        verify(userRepository, atLeastOnce()).getById(anyString());
        verify(user, atLeastOnce()).removeStore(storeId);
        verify(sub1, atLeastOnce()).removeStore(storeId);
        verify(sub2, atLeastOnce()).removeStore(storeId);
    }

    @Test
    void removeStoreOwner_notSuperior() {
        doReturn(false).when(store).checkIfSuperior(userId, ownerId);

        boolean result = service.removeStoreOwner(userId, storeId, ownerId);

        assertFalse(result);
        verify(store, never()).terminateOwnership(anyString());
    }

    @Test
    void relinquishOwnership_success() {
        doReturn(false).when(store).isFounder(userId);
        doReturn(true).when(store).userIsOwner(userId);
        doReturn(Collections.emptySet()).when(store).getAllSubordinates(userId);

        boolean result = service.relinquishOwnership(userId, storeId);

        assertTrue(result);
        verify(store).terminateOwnership(userId);
        verify(user).removeStore(storeId);
    }

    @Test
    void relinquishOwnership_founderCannotRelinquish() {
        doReturn(true).when(store).isFounder(ownerId);

        boolean result = service.relinquishOwnership(ownerId, storeId);

        assertFalse(result);
        verify(store, never()).terminateOwnership(anyString());
    }

    @Test
    void appointStoreManager_success() {
        boolean[] perms = {true, true, true};
        boolean result = service.appointStoreManager(ownerId, storeId, userId, perms);

        assertTrue(result);
        verify(store).addManager(ownerId, userId, perms);
        verify(user).addManagedStore(storeId);
        verify(userRepository).save(user);
    }

    @Test
    void appointStoreManager_noPermission() {
        doReturn(false).when(store).userHasPermissions(userId, "MANAGE_STAFF");
        boolean[] perms = {true, true, true};
        boolean result = service.appointStoreManager(userId, storeId, managerId, perms);

        assertFalse(result);
        verify(store, never()).addManager(anyString(), anyString(), any());
    }

    @Test
    void respondToManagerAppointment_accept() {
        // Prevent NPE by making userIsManager return false for this test
        doReturn(false).when(store).userIsManager(managerId);

        boolean result = service.respondToManagerAppointment(managerId, storeId, true);

        assertTrue(result);
    }

    @Test
    void respondToManagerAppointment_reject() {
        boolean result = service.respondToManagerAppointment(managerId, storeId, false);

        assertFalse(result);
    }

    @Test
    void updateManagerPermissions_success() {
        boolean[] perms = {true, false, true};

        // Mock ManagerPermissions for the managerId
        DomainLayer.ManagerPermissions mockPerms = mock(DomainLayer.ManagerPermissions.class);
        Map<String, DomainLayer.ManagerPermissions> managersMap = new HashMap<>();
        managersMap.put(managerId, mockPerms);
        doReturn(managersMap).when(store).getManagers();
        doReturn(true).when(store).checkIfSuperior(ownerId, managerId);

        boolean result = service.updateManagerPermissions(ownerId, storeId, managerId, perms);

        assertTrue(result);
        verify(store).changeManagersPermissions(managerId, perms);
    }

    @Test
    void updateManagerPermissions_noPermission() {
        doReturn(false).when(store).userHasPermissions(userId, "MANAGE_STAFF");
        boolean[] perms = {true, false, true};
        boolean result = service.updateManagerPermissions(userId, storeId, managerId, perms);

        assertFalse(result);
        verify(store, never()).changeManagersPermissions(anyString(), any());
    }

    @Test
    void removeStoreManager_success() {
        doReturn(true).when(store).checkIfSuperior(ownerId, managerId);

        boolean result = service.removeStoreManager(ownerId, storeId, managerId);

        assertTrue(result);
        verify(store).terminateManagment(managerId);
        verify(manager).removeStore(storeId);
    }

    @Test
    void removeStoreManager_notSuperior() {
        doReturn(false).when(store).checkIfSuperior(userId, managerId);

        boolean result = service.removeStoreManager(userId, storeId, managerId);

        assertFalse(result);
        verify(store, never()).terminateManagment(anyString());
    }

    @Test
    void closeStore_success() {
        doReturn(true).when(store).isFounder(ownerId);

        boolean result = service.closeStore(ownerId, storeId);

        assertTrue(result);
        verify(store).closeTheStore();
    }

    @Test
    void closeStore_notFounder() {
        doReturn(false).when(store).isFounder(userId);

        boolean result = service.closeStore(userId, storeId);

        assertFalse(result);
        verify(store, never()).closeTheStore();
    }

    @Test
    void reopenStore_success() {
        doReturn(true).when(store).isFounder(ownerId);

        boolean result = service.reopenStore(ownerId, storeId);

        assertTrue(result);
        verify(store).openTheStore();
    }

    @Test
    void reopenStore_notFounder() {
        doReturn(false).when(store).isFounder(userId);

        boolean result = service.reopenStore(userId, storeId);

        assertFalse(result);
        verify(store, never()).openTheStore();
    }

    @Test
    void getStoreRoleInfo_ownerOrManager() {
        doReturn(true).when(store).userIsManager(ownerId);

        String info = service.getStoreRoleInfo(ownerId, storeId);

        assertNotNull(info);
        verify(store).getRoles();
    }

    @Test
    void getStoreRoleInfo_notOwnerOrManager() {
        doReturn(false).when(store).userIsManager(userId);

        String info = service.getStoreRoleInfo(userId, storeId);

        assertEquals("", info);
    }

    @Test
    void getManagerPermissions_success() {
        doReturn(true).when(store).userIsManager(managerId);

        Map<String, Boolean> perms = new HashMap<>();
        perms.put("PERM_MANAGE_STAFF", true);
        doReturn(perms).when(store).getPremissions(managerId);

        Map<String, Boolean> result = service.getManagerPermissions(ownerId, storeId, managerId);

        assertEquals(perms, result);
    }

    @Test
    void getManagerPermissions_notAllowed() {
        doReturn(false).when(store).userIsManager(userId);

        Map<String, Boolean> result = service.getManagerPermissions(userId, storeId, userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void relinquishManagement_success() {
        doReturn(false).when(store).isFounder(managerId);
        doReturn(true).when(store).userIsManager(managerId);

        boolean result = service.relinquishManagement(managerId, storeId);

        assertTrue(result);
        verify(store).terminateManagment(managerId);
        verify(manager).removeStore(storeId);
    }

    @Test
    void relinquishManagement_founderCannotRelinquish() {
        doReturn(true).when(store).isFounder(ownerId);

        boolean result = service.relinquishManagement(ownerId, storeId);

        assertFalse(result);
        verify(store, never()).terminateManagment(anyString());
    }

    @Test
    void appointStoreFounder_throws() {
        assertThrows(RuntimeException.class, () ->
                service.appointStoreFounder(ownerId, storeId));
    }

    @Test
    void appointStoreOwner_nullStore_throws() {
        when(storeRepository.getById("bad-store")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            service.appointStoreOwner(ownerId, "bad-store", userId)
        );
    }

    @Test
    void appointStoreOwner_nullUser_throws() {
        when(userRepository.getById("bad-user")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            service.appointStoreOwner(ownerId, storeId, "bad-user")
        );
    }

    @Test
    void appointStoreManager_userNotFound_returnsFalse() {
        when(userRepository.getById("bad-user")).thenReturn(null);
        boolean[] perms = {true, true, true, true, true, true, true};
        boolean result = service.appointStoreManager(ownerId, storeId, "bad-user", perms);
        assertFalse(result);
    }

    @Test
    void removeStoreOwner_notOwner_returnsFalse() {
        doReturn(false).when(store).checkIfSuperior(ownerId, "not-an-owner");
        boolean result = service.removeStoreOwner(ownerId, storeId, "not-an-owner");
        assertFalse(result);
    }

    @Test
    void relinquishOwnership_notOwner_returnsFalse() {
        doReturn(false).when(store).userIsOwner("not-an-owner");
        boolean result = service.relinquishOwnership("not-an-owner", storeId);
        assertFalse(result);
    }

    @Test
    void updateManagerPermissions_notSuperior_returnsFalse() {
        doReturn(true).when(store).userHasPermissions(ownerId, "MANAGE_STAFF");
        doReturn(false).when(store).checkIfSuperior(ownerId, managerId);
        boolean[] perms = {true, true, true, true, true, true, true};
        boolean result = service.updateManagerPermissions(ownerId, storeId, managerId, perms);
        assertFalse(result);
    }

    @Test
    void removeStoreManager_notSuperior_returnsFalse() {
        doReturn(false).when(store).checkIfSuperior(ownerId, managerId);
        boolean result = service.removeStoreManager(ownerId, storeId, managerId);
        assertFalse(result);
    }

    @Test
    void closeStore_notFounder_returnsFalse() {
        doReturn(false).when(store).isFounder(userId);
        boolean result = service.closeStore(userId, storeId);
        assertFalse(result);
    }

    @Test
    void reopenStore_notFounder_returnsFalse() {
        doReturn(false).when(store).isFounder(userId);
        boolean result = service.reopenStore(userId, storeId);
        assertFalse(result);
    }

    @Test
    void getManagerPermissions_notManager_returnsEmptyMap() {
        doReturn(false).when(store).userIsManager("not-a-manager");
        Map<String, Boolean> result = service.getManagerPermissions(ownerId, storeId, "not-a-manager");
        assertTrue(result.isEmpty());
    }

    @Test
    void relinquishManagement_notManager_returnsFalse() {
        doReturn(false).when(store).userIsManager("not-a-manager");
        boolean result = service.relinquishManagement("not-a-manager", storeId);
        assertFalse(result);
    }

    @Test
    void relinquishManagement_isFounder_returnsFalse() {
        doReturn(true).when(store).isFounder(managerId);
        boolean result = service.relinquishManagement(managerId, storeId);
        assertFalse(result);
    }

    @Test
    void appointStoreOwner_concurrentAccess() throws InterruptedException {
        doReturn(false).when(store).userIsOwner(userId);
        doReturn(false).when(store).userIsManager(userId);

        Runnable task = () -> service.appointStoreOwner(ownerId, storeId, userId);
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        verify(store, atLeastOnce()).addOwner(ownerId, userId);
        verify(user, atLeastOnce()).addOwnedStore(storeId);
    }
}
