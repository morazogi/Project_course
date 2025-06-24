package DomainLayer.DomainServices;

import DomainLayer.IToken;
import DomainLayer.Product;
import DomainLayer.Roles.RegisteredUser;
import DomainLayer.Store;
import InfrastructureLayer.ProductRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Rate}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateTest {

    /* -------- mocked collaborators -------- */
    @Mock IToken            tokener;
    @Mock StoreRepository   storeRepo;
    @Mock UserRepository    userRepo;
    @Mock ProductRepository productRepo;

    private Rate service;            // system under test
    private final RegisteredUser dummyUser = Mockito.mock(RegisteredUser.class);

    @BeforeEach
    void init() {
        service = new Rate(tokener, storeRepo, userRepo, productRepo);
    }

    /* =============================================================
                           rateStore
       ============================================================= */
    @Test
    void rateStore_happyPath_updatesRepoAndReturnsTrue() throws Exception {
        String token   = "tok";
        String user    = "alice";
        String storeId = "s1";

        when(tokener.extractUsername(token)).thenReturn(user);
        doNothing().when(tokener).validateToken(token);

        Store store = mock(Store.class);
        when(storeRepo.getById(storeId)).thenReturn(store);
        when(userRepo.getById(user)).thenReturn(dummyUser);
        when(store.rate(4)).thenReturn(true);

        assertTrue(service.rateStore(token, storeId, 4));
        verify(storeRepo).update(store);
    }

    @Test
    void rateStore_invalidRateValue_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.rateStore("t", "s", 6));   // >5
    }

    @Test
    void rateStore_nonExistingStore_throws() {
        when(tokener.extractUsername("tok")).thenReturn("user");
        doNothing().when(tokener).validateToken("tok");
        when(storeRepo.getById("noStore")).thenReturn(null);

        when(userRepo.getById("user")).thenReturn(dummyUser);

        assertThrows(IllegalArgumentException.class,
                () -> service.rateStore("tok", "noStore", 3));
    }

    /* =============================================================
                           rateProduct
       ============================================================= */
    @Test
    void rateProduct_happyPath_savesAndReturnsTrue() {
        String token = "tok";
        String user  = "bob";
        String pid   = "p1";

        when(tokener.extractUsername(token)).thenReturn(user);
        doNothing().when(tokener).validateToken(token);

        Product prod = mock(Product.class);
        when(productRepo.getById(pid)).thenReturn(prod);
        when(userRepo.getById(user)).thenReturn(dummyUser);
        when(prod.addRating(user, 5)).thenReturn(true);

        assertTrue(service.rateProduct(token, pid, 5));
        verify(productRepo).save(prod);
    }

    @Test
    void rateProduct_duplicateRating_returnsFalseAndNoSave() {
        String token = "tok";
        String user  = "bob";
        String pid   = "p1";

        when(tokener.extractUsername(token)).thenReturn(user);
        doNothing().when(tokener).validateToken(token);

        Product prod = mock(Product.class);
        when(productRepo.getById(pid)).thenReturn(prod);
        when(userRepo.getById(user)).thenReturn(dummyUser);
        when(prod.addRating(user, 4)).thenReturn(false);        // already rated

        assertFalse(service.rateProduct(token, pid, 4));
        verify(productRepo, never()).save(any());
    }
}
