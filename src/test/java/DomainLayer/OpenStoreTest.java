package DomainLayer;

import DomainLayer.DomainServices.OpenStore;
import DomainLayer.IToken;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.ShoppingCart;                // <-- cart lives here
import InfrastructureLayer.StoreRepository;
import InfrastructureLayer.UserRepository;
import DomainLayer.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenStoreTest {

    /* ------------ mocked collaborators ------------ */
    @Mock IToken          tokener;
    @Mock StoreRepository storeRepo;
    @Mock UserRepository  userRepo;

    private OpenStore service;

    @BeforeEach
    void setup() {
        service = new OpenStore(tokener, storeRepo, userRepo);
    }

    /* ------------------------------------------------
                       HAPPY-PATH
       ------------------------------------------------ */
    @Test
    void openStore_validToken_createsStoreAndReturnsId() throws Exception {
        String token      = "valid-token";
        String username   = "alice";
        String userId     = "alice-id";        // cart’s user-id
        String storeName  = "Alice-Shop";

        /* ---------- token & user stubs ---------- */
        doNothing().when(tokener).validateToken(token);
        when(tokener.extractUsername(token)).thenReturn(username);

        RegisteredUser mockUser = mock(RegisteredUser.class);
        ShoppingCart   cart     = mock(ShoppingCart.class);
        when(cart.getUserId()).thenReturn(userId);
        when(mockUser.getShoppingCart()).thenReturn(cart);
        when(userRepo.getById(username)).thenReturn(mockUser);

        /* ---------- make the repository ‘save’ assign an ID ---------- */
        doAnswer(inv -> {
            Store s = inv.getArgument(0, Store.class);
            s.setId("generated-id");
            return null;
        }).when(storeRepo).save(any(Store.class));

        /* ---------- call ---------- */
        String newId = service.openStore(token, storeName);

        /* ---------- verification ---------- */
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeRepo).save(captor.capture());
        Store saved = captor.getValue();

        assertEquals("generated-id", newId);
        assertEquals("generated-id", saved.getId());
        assertEquals(userId,         saved.getFounder());  // founder = cart’s user-id
        assertEquals(storeName,      saved.getName());
    }

    /* ------------------------------------------------
                       NEGATIVE CASES
       ------------------------------------------------ */
    @Test
    void openStore_nullToken_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.openStore(null, "Dummy"));
    }

    @Test
    void openStore_userMissing_throwsNullPointer() throws Exception {
        String token = "token-without-user";
        doNothing().when(tokener).validateToken(token);
        when(tokener.extractUsername(token)).thenReturn("ghost");
        when(userRepo.getById("ghost")).thenReturn(null);   // user not found

        assertThrows(NullPointerException.class,
                () -> service.openStore(token, "Ghost-Store"));
    }
}
