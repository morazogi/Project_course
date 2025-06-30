/* ────────────────────────────────────────────────────────────────
   src/test/java/DomainLayer/DomainServices/StoreManagementMicroserviceTest.java
   ──────────────────────────────────────────────────────────────── */
package DomainLayer.DomainServices;

import static DomainLayer.ManagerPermissions.PERM_MANAGE_STAFF;   // ← ensure constant is imported

import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static DomainLayer.ManagerPermissions.PERM_UPDATE_POLICY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StoreManagementMicroserviceTest {

    /* ─────────────────── repositories ─────────────────── */
    @Mock StoreRepository storeRepo;
    @Mock UserRepository  userRepo;

    /* ─────────────────────  store & users ───────────────────── */
    @Mock Store          store;
    @Mock RegisteredUser founder;
    @Mock RegisteredUser candidate;
    @Mock RegisteredUser intruder;
    @Mock RegisteredUser manager;
    @Mock RegisteredUser sub1;
    @Mock RegisteredUser sub2;

    private StoreManagementMicroservice svc;

    /* ───────────────────────── setup ──────────────────────── */
    @BeforeEach
    void setUp() {
        svc = new StoreManagementMicroservice(storeRepo, userRepo);

        when(storeRepo.getById("store")).thenReturn(store);

        when(userRepo.getById("founder")).thenReturn(founder);
        when(userRepo.getById("candidate")).thenReturn(candidate);
        when(userRepo.getById("intruder")).thenReturn(intruder);
        when(userRepo.getById("manager")).thenReturn(manager);
        when(userRepo.getById("sub1")).thenReturn(sub1);
        when(userRepo.getById("sub2")).thenReturn(sub2);

        when(store.getId()).thenReturn("store");
        when(store.getFounder()).thenReturn("founder");

        when(store.isFounder("founder")).thenReturn(true);
        when(store.userIsOwner("founder")).thenReturn(true);
        when(founder.isOwnerOf("store")).thenReturn(true);

        when(store.userHasPermissions(anyString(), anyString())).thenReturn(true);
    }

    /* ──────────────────────────────────────────────────────── *
     *                    ALREADY-EXISTING TESTS               *
     * ──────────────────────────────────────────────────────── */

    @Test
    void appointStoreOwner_byFounder_success() {
        boolean ok = svc.appointStoreOwner("founder", "store", "candidate");
        assertTrue(ok);
        verify(store).addOwner("founder", "candidate");
        verify(userRepo).update(candidate);
    }

    @Test
    void appointStoreOwner_withoutPermission_fails() {
        when(store.userHasPermissions(eq("intruder"), anyString())).thenReturn(false);

        boolean ok = svc.appointStoreOwner("intruder", "store", "candidate");
        assertFalse(ok);
        verify(store, never()).addOwner(any(), any());
    }

    @Test
    void closeStore_asFounder_updatesRepo() {
        boolean ok = svc.closeStore("founder", "store");
        assertTrue(ok);
        verify(store).closeTheStore();
        verify(storeRepo).update(store);
    }

    @Test
    void closeStore_notFounder_throws() {
        when(store.isFounder("intruder")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.closeStore("intruder", "store"));
        assertEquals("User is not store founder", ex.getMessage());
    }

    @Test
    void reopenStore_flipsFlag_andUpdatesRepo() {
        boolean ok = svc.reopenStore("founder", "store");
        assertTrue(ok);
        verify(store).openTheStore();
        verify(storeRepo).update(store);
    }

    @Test
    void removeStoreOwner_superiorFlow_updatesAll() {
        when(store.checkIfSuperior("founder", "candidate")).thenReturn(true);
        when(store.getAllSubordinates("candidate")).thenReturn(new LinkedList<>());

        boolean ok = svc.removeStoreOwner("founder", "store", "candidate");
        assertTrue(ok);
        verify(store).terminateOwnership("candidate");
        verify(userRepo).update(candidate);
    }

    @Test
    void isFounderOrOwner_variousCases() {
        when(store.userIsOwner("candidate")).thenReturn(true);

        assertTrue(svc.isFounderOrOwner("founder",   "store"));
        assertTrue(svc.isFounderOrOwner("candidate", "store"));
        assertFalse(svc.isFounderOrOwner("intruder", "store"));
    }

    /* ─────────────────────────── NEW TESTS ─────────────────────────── */

    /* ---------- manager-permission flows ---------- */

    @Test
    void updateManagerPermissions_superiorTrue_returnsTrue() {
        when(store.checkIfSuperior("founder", "manager")).thenReturn(true);
        when(store.userIsManager("manager")).thenReturn(true);

        boolean ok = svc.updateManagerPermissions("founder", "store",
                "manager", new boolean[]{true,true,true});
        assertTrue(ok);
        verify(store).changeManagersPermissions(eq("manager"), any());
    }

    @Test
    void updateManagerPermissions_notSuperior_returnsFalse() {
        when(store.checkIfSuperior("intruder","manager")).thenReturn(false);
        boolean ok = svc.updateManagerPermissions("intruder","store",
                "manager", new boolean[3]);
        assertFalse(ok);
        verify(store, never()).changeManagersPermissions(any(), any());
    }

    /* ---------- appoint store manager ---------- */

    @Test
    void appointStoreManager_success() {
        boolean ok = svc.appointStoreManager("founder","store","manager",
                new boolean[]{true,true,true});
        assertTrue(ok);
        verify(store).addManager(eq("founder"), eq("manager"), any());
        verify(userRepo).update(manager);
        verify(storeRepo).update(store);
    }

    @Test
    void appointStoreManager_noPermission_returnsFalse() {
        when(store.userHasPermissions(eq("intruder"), anyString())).thenReturn(false);
        boolean ok = svc.appointStoreManager("intruder","store","manager",
                new boolean[]{false,false,false});
        assertFalse(ok);
        verify(store, never()).addManager(any(), any(), any());
    }

    /* ---------- remove / relinquish manager ---------- */

    @Test
    void removeStoreManager_happyPath_updatesRepo() {
        when(store.checkIfSuperior("founder", "manager")).thenReturn(true);
        boolean ok = svc.removeStoreManager("founder","store","manager");
        assertTrue(ok);
        verify(store).terminateManagment("manager");
        verify(userRepo).update(manager);
        verify(storeRepo).update(store);
    }

    @Test
    void removeStoreManager_noSuperior_returnsFalse() {
        when(store.checkIfSuperior("intruder", "manager")).thenReturn(false);

        boolean ok = svc.removeStoreManager("intruder", "store", "manager");
        assertFalse(ok);
        verify(store, never()).terminateManagment(anyString());
    }

    @Test
    void relinquishManagement_success_updatesStore() {
        when(store.isFounder("manager")).thenReturn(false);
        when(store.userIsManager("manager")).thenReturn(true);

        boolean ok = svc.relinquishManagement("manager","store");
        assertTrue(ok);
        verify(store).terminateManagment("manager");
        verify(manager).removeStore("store");
    }

    /* ---------- owner flows ---------- */

    @Test
    void relinquishOwnership_success_cleansSubordinates() {
        when(store.isFounder("candidate")).thenReturn(false);
        when(store.userIsOwner("candidate")).thenReturn(true);

        LinkedList<String> subs = new LinkedList<>(List.of("sub1", "sub2"));
        when(store.getAllSubordinates("candidate")).thenReturn(subs);

        boolean ok = svc.relinquishOwnership("candidate","store");
        assertTrue(ok);

        verify(store).terminateOwnership("candidate");
        verify(candidate).removeStore("store");
        verify(sub1).removeStore("store");
        verify(sub2).removeStore("store");
    }

    @Test
    void relinquishOwnership_notOwner_returnsFalse() {
        when(store.userIsOwner("intruder")).thenReturn(false);
        assertFalse(svc.relinquishOwnership("intruder","store"));
    }

    /* ---------- proposal & appointment responses ---------- */

    @Test
    void sendOwnershipProposal_valid_createsMessage() {
        when(store.userIsOwner("candidate")).thenReturn(false);
        when(store.userIsManager("candidate")).thenReturn(false);

        String msg = svc.sendOwnershipProposal("candidate","store",
                "Fancy owning?");
        assertTrue(msg.contains("store"));
        assertTrue(msg.startsWith("Hi,"));
    }

    @Test
    void sendOwnershipProposal_userAlreadyOwner_throws() {
        when(store.userIsOwner("candidate")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> svc.sendOwnershipProposal("candidate","store","ignored"));
    }

    @Test
    void respondToOwnerAppointment_accept_invokesInternalFlow() {
        StoreManagementMicroservice spySvc =
                Mockito.spy(new StoreManagementMicroservice(storeRepo,userRepo));

        doReturn(true).when(spySvc)
                .appointStoreOwner(anyString(), anyString(), anyString());

        boolean ok = spySvc.respondToOwnerAppointment("candidate","store", true);
        assertTrue(ok);
        verify(spySvc).appointStoreOwner("candidate","store","candidate");
    }

    @Test
    void respondToOwnerAppointment_decline_returnsFalse() {
        assertFalse(svc.respondToOwnerAppointment("candidate","store", false));
    }

    /* ---------- getters ---------- */

    @Test
    void getStoreRoleInfo_manager_returnsRoles() {
        when(store.userIsManager("manager")).thenReturn(true);
        when(store.getRoles()).thenReturn("json");
        String json = svc.getStoreRoleInfo("manager","store");
        assertEquals("json", json);
    }

    @Test
    void getStoreRoleInfo_notRelated_returnsEmpty() {
        when(store.userIsManager("intruder")).thenReturn(false);
        assertEquals("", svc.getStoreRoleInfo("intruder","store"));
    }

    @Test
    void getManagerPermissions_ownerSuperior_returnsMap() {
        Map<String,Boolean> p = new HashMap<>();
        p.put("permA", true);

        when(store.checkIfSuperior("founder","manager")).thenReturn(true);
        when(store.userIsManager("manager")).thenReturn(true);
        when(store.getPremissions("manager")).thenReturn(p);

        Map<String,Boolean> out =
                svc.getManagerPermissions("founder","store","manager");
        assertSame(p, out);
    }

    @Test
    void getManagerPermissions_notAllowed_returnsEmptyMap() {
        when(store.checkIfSuperior("intruder","manager")).thenReturn(false);
        Map<String,Boolean> out =
                svc.getManagerPermissions("intruder","store","manager");
        assertTrue(out.isEmpty());
    }

    /* ---------- exceptional path ---------- */

    @Test
    void appointStoreFounder_alwaysThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.appointStoreFounder("someone","store"));
        assertTrue(ex.getMessage().contains("store founder can't be appointed"));
    }

    /* ─────────────────────────── ADDITIONAL EDGE & UTILITY TESTS ─────────────────────────── */

    @Test
    void updateManagerPermissions_missingStoreRepository_returnsFalse() {
        // Simulate a broken configuration where the store repository is unavailable
        svc.setRepositories(null, userRepo);

        boolean ok = svc.updateManagerPermissions("founder", "store", "manager", new boolean[3]);
        assertFalse(ok);
        verify(store, never()).changeManagersPermissions(anyString(), any());
    }

    @Test
    void removeStoreOwner_notSuperior_returnsFalse() {
        when(store.checkIfSuperior("intruder", "candidate")).thenReturn(false);

        boolean ok = svc.removeStoreOwner("intruder", "store", "candidate");
        assertFalse(ok);
        verify(store, never()).terminateOwnership(anyString());
    }

    @Test
    void relinquishManagement_asFounder_returnsFalse() {
        when(store.isFounder("founder")).thenReturn(true);
        when(store.userIsManager("founder")).thenReturn(true);

        boolean ok = svc.relinquishManagement("founder", "store");
        assertFalse(ok);
        verify(store, never()).terminateManagment(anyString());
    }

    @Test
    void removeStoreOwnerWithUserSync_happyPath_updatesAllUsers() {
        when(store.checkIfSuperior("founder", "candidate")).thenReturn(true);
        when(store.getAllSubordinates("candidate")).thenReturn(new LinkedList<>(List.of("sub1", "sub2")));

        boolean ok = svc.removeStoreOwnerWithUserSync("founder", "store", "candidate");
        assertTrue(ok);

        verify(store).terminateOwnership("candidate");
        verify(candidate).removeStore("store");
        verify(sub1).removeStore("store");
        verify(sub2).removeStore("store");
    }

    @Test
    void respondToManagerAppointment_accept_invokesAppointFlow() {
        StoreManagementMicroservice spySvc = Mockito.spy(svc);
        doReturn(true).when(spySvc).appointStoreManager(anyString(), anyString(), anyString(), any());

        boolean ok = spySvc.respondToManagerAppointment("candidate", "store", true);
        assertTrue(ok);
        verify(spySvc).appointStoreManager(eq("candidate"), eq("store"), eq("candidate"), any());
    }

    @Test
    void respondToManagerAppointment_decline_returnsFalse() {
        assertFalse(svc.respondToManagerAppointment("candidate", "store", false));
    }

    @Test
    void canUpdateDiscountPolicy_managerWithFlag_returnsTrue() {
        when(store.userIsManager("manager")).thenReturn(true);
        when(store.userHasPermissions("manager", PERM_UPDATE_POLICY)).thenReturn(true);

        assertTrue(svc.canUpdateDiscountPolicy("manager", "store"));
    }

    @Test
    void canUpdateDiscountPolicy_managerWithoutFlag_returnsFalse() {
        when(store.userIsManager("manager")).thenReturn(true);
        when(store.userHasPermissions("manager", PERM_UPDATE_POLICY)).thenReturn(false);

        assertFalse(svc.canUpdateDiscountPolicy("manager", "store"));
    }

    @Test
    void appointStoreOwner_intruderReappointsFounder_throws() {
        // Intruder has no manage‑staff permission
        when(store.userHasPermissions(eq("intruder"), eq(PERM_MANAGE_STAFF))).thenReturn(false);
        // Founder already appears in owner list
        when(store.getOwners()).thenReturn(List.of("founder"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> svc.appointStoreOwner("intruder", "store", "founder"));
        assertEquals("User is already the founder of the store", ex.getMessage());
    }

    @Test
    void appointStoreOwner_permissionMissingButFounderStillAppointed() {
        // founder NOT yet in owners list → should proceed
        when(store.userHasPermissions(eq("intruder"), eq(PERM_MANAGE_STAFF))).thenReturn(false);
        when(store.getOwners()).thenReturn(List.of());

        boolean ok = svc.appointStoreOwner("intruder", "store", "founder");
        assertTrue(ok);
        verify(store).addOwner("intruder", "founder");
        verify(userRepo).update(founder);
    }

    @Test
    void updateManagerPermissions_managerWithoutStaffPerm_returnsFalse() {
        // Manager tries to edit their own permissions without the flag
        when(store.userIsManager("manager")).thenReturn(true);
        when(manager.isManagerOf("store")).thenReturn(true);
        when(store.userHasPermissions("manager", PERM_MANAGE_STAFF)).thenReturn(false);
        // Superior check will still be true (self‑superior allowed by code path)
        when(store.checkIfSuperior("manager", "manager")).thenReturn(true);

        boolean ok = svc.updateManagerPermissions("manager", "store", "manager", new boolean[3]);
        assertFalse(ok);
        verify(store, never()).changeManagersPermissions(anyString(), any());
    }

    @Test
    void isFounderOwnerOrManager_managerViaUserRepo_returnsTrue() {
        // Store unaware of manager, but User object knows
        when(store.userIsManager("manager")).thenReturn(false);
        when(manager.isManagerOf("store")).thenReturn(true);
        assertTrue(svc.isFounderOwnerOrManager("manager", "store"));
    }

    @Test
    void canUpdateDiscountPolicy_ownerOnlyInUserRepo_returnsTrue() {
        // Store unaware that candidate is owner, but User object flags ownership
        when(store.userIsOwner("candidate")).thenReturn(false);
        when(candidate.isOwnerOf("store")).thenReturn(true);
        assertTrue(svc.canUpdateDiscountPolicy("candidate", "store"));
    }

    @Test
    void canUpdateDiscountPolicy_managerViaUserRepoWithFlag_returnsTrue() {
        when(store.userIsManager("manager")).thenReturn(false);   // store unaware
        when(manager.isManagerOf("store")).thenReturn(true);
        when(store.userHasPermissions("manager", PERM_UPDATE_POLICY)).thenReturn(true);
        assertTrue(svc.canUpdateDiscountPolicy("manager", "store"));
    }

    @Test
    void removeManagerWithUserSync_updatesRepository() {
        when(store.checkIfSuperior("founder", "manager")).thenReturn(true);
        boolean ok = svc.removeManagerWithUserSync("founder", "store", "manager");
        assertTrue(ok);
        verify(store).terminateManagment("manager");
        verify(manager).removeStore("store");
    }

    @Test
    void relinquishManagerWithUserSync_happyPath_updatesEntities() {
        when(store.userIsManager("manager")).thenReturn(true);
        when(store.isFounder("manager")).thenReturn(false);

        boolean ok = svc.relinquishManagerWithUserSync("manager", "store");
        assertTrue(ok);
        verify(store).terminateManagment("manager");
        verify(manager).removeStore("store");
        verify(storeRepo).update(store);
        verify(userRepo).update(manager);
    }

    @Test
    void sendManagementProposal_userAlreadyManager_noException() {
        when(store.userIsOwner("manager")).thenReturn(false);
        when(store.userIsManager("manager")).thenReturn(true); // Already manager, should early‑return

        assertDoesNotThrow(() -> svc.sendManagementProposal("manager", "store", "pls manage"));
        // No side‑effects expected, but at least we cover the branch
    }
}
