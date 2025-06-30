package DomainLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ManagerPermissionsTest {

    private String managerId;
    private String storeId;

    private boolean[] full9;
    private boolean[] partial9;
    private boolean[] none9;

    @BeforeEach
    void init() {
        managerId = "manager123";
        storeId   = "store456";

        /* index-to-flag mapping (per implementation)
           0  MANAGE_INVENTORY
           1  MANAGE_STAFF
           2  VIEW_STORE
           3  UPDATE_POLICY
           4  ADD_PRODUCT
           5  REMOVE_PRODUCT  + CLOSE_STORE
           6  UPDATE_PRODUCT  + OPEN_STORE
           7  (unused, reserved)
           8  (unused, reserved)                                    */

        full9   = new boolean[]{true,true,true,true,true,true,true,true,true};
        partial9= new boolean[]{true,false,true,false,true,false,true,false,false};
        none9   = new boolean[9];          // all false
    }

    /* ─────────────────── 1 · constructors ─────────────────── */

    @Test @DisplayName("Default ctor initialises nine flags to false")
    void defaultConstructor() {
        ManagerPermissions mp = new ManagerPermissions();

        assertFalse(mp.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_OPEN_STORE));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_CLOSE_STORE));
        assertEquals(9, mp.getPermissions().size());
    }

    @Test @DisplayName("Parameterized ctor – full permissions array")
    void parameterisedCtor_full() {
        ManagerPermissions mp = new ManagerPermissions(full9, managerId, storeId);

        assertTrue(mp.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertTrue(mp.getPermission(ManagerPermissions.PERM_OPEN_STORE));
        assertTrue(mp.getPermission(ManagerPermissions.PERM_CLOSE_STORE));

        assertEquals(managerId, mp.getManagerId());
        assertEquals(storeId,   mp.getStoreId());
    }

    @Test @DisplayName("Parameterized ctor – partial permissions array")
    void parameterisedCtor_partial() {
        ManagerPermissions mp = new ManagerPermissions(partial9, managerId, storeId);

        assertTrue (mp.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertTrue (mp.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
        assertTrue (mp.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertTrue (mp.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_CLOSE_STORE));
    }

    @Test @DisplayName("Parameterized ctor – all false")
    void parameterisedCtor_none() {
        ManagerPermissions mp = new ManagerPermissions(none9, managerId, storeId);
        mp.getPermissions().values().forEach(v -> assertFalse(v));
    }

    /* ─────────────────── 2 · setPermissionsFromAarray ─────── */

    @Test @DisplayName("setPermissionsFromAarray – valid 9-element array")
    void setPerms_valid() {
        ManagerPermissions mp = new ManagerPermissions();
        mp.setPermissionsFromAarray(partial9);

        assertTrue (mp.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertTrue (mp.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertFalse(mp.getPermission(ManagerPermissions.PERM_CLOSE_STORE));
        assertTrue (mp.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));
    }

    @Test
    @DisplayName("setPermissionsFromAarray – null keeps old map")
    void setPerms_null() {
        ManagerPermissions mp = new ManagerPermissions();
        Map<String,Boolean> orig = new HashMap<>(mp.getPermissions());

        mp.setPermissionsFromAarray(null);
        assertEquals(orig, mp.getPermissions());
    }

    @Test
    @DisplayName("setPermissionsFromAarray – too short array ignored")
    void setPerms_tooShort() {
        ManagerPermissions mp = new ManagerPermissions();
        Map<String,Boolean> orig = new HashMap<>(mp.getPermissions());

        mp.setPermissionsFromAarray(new boolean[]{true,true,true});
        assertEquals(orig, mp.getPermissions());
    }

    /* ─────────────────── 3 · individual flags ─────────────── */

    @Test void individualSetterGetter() {
        ManagerPermissions mp = new ManagerPermissions();
        assertFalse(mp.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));

        mp.setPermission(ManagerPermissions.PERM_UPDATE_POLICY,true);
        assertTrue(mp.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
    }

    @Test void getNonExistingFlag_returnsFalse() {
        ManagerPermissions mp = new ManagerPermissions();
        assertFalse(mp.getPermission("NO_SUCH_FLAG"));
    }

    /* ─────────────────── 4 · map getter / setter ───────────── */

    @Test void permissionsMap_getterSetter() {
        ManagerPermissions mp = new ManagerPermissions();

        Map<String,Boolean> custom = new HashMap<>();
        custom.put(ManagerPermissions.PERM_MANAGE_INVENTORY,true);
        custom.put("CUSTOM_PERM",true);
        mp.setPermissions(custom);

        assertEquals(custom, mp.getPermissions());
        assertTrue (mp.getPermission("CUSTOM_PERM"));
    }

    /* ─────────────────── 5 · ID handling ───────────────────── */

    @Test void idGetterSetter() {
        ManagerPermissions mp = new ManagerPermissions();

        ManagerPermissionsPK pk = new ManagerPermissionsPK(managerId,storeId);
        mp.setId(pk);
        assertEquals(pk, mp.getId());
        assertEquals(managerId, mp.getManagerId());
        assertEquals(storeId,   mp.getStoreId());
    }

    @Test void managerIdStoreId_whenIdNull() {
        ManagerPermissions mp = new ManagerPermissions();
        mp.setId(null);
        assertNull(mp.getManagerId());
        assertNull(mp.getStoreId());
    }

    /* ─────────────────── 6 · equals / hashCode ─────────────── */

    @Test void equalsHashCode() {
        ManagerPermissionsPK pk = new ManagerPermissionsPK(managerId,storeId);
        ManagerPermissions a  = new ManagerPermissions(); a.setId(pk);
        ManagerPermissions b  = new ManagerPermissions(); b.setId(pk);
        ManagerPermissions c  = new ManagerPermissions(); c.setId(
                new ManagerPermissionsPK("other",storeId));

        assertEquals(a,a);
        assertEquals(a,b);
        assertNotEquals(a,c);
        assertNotEquals(a,null);
        assertNotEquals(a,"string");

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    /* ─────────────────── 7 · constant sanity check ─────────── */

    @Test void constantDefinitions() {
        assertEquals("PERM_MANAGE_INVENTORY", ManagerPermissions.PERM_MANAGE_INVENTORY);
        assertEquals("PERM_OPEN_STORE",       ManagerPermissions.PERM_OPEN_STORE);
        assertEquals("PERM_CLOSE_STORE",      ManagerPermissions.PERM_CLOSE_STORE);
    }

    /* ─────────────────── 8 · array-order mapping ───────────── */

    @Test void arrayOrderMapping() {
        boolean[] arr = {true,false,true,false,true,false,true,true,false}; // 9 elements
        ManagerPermissions mp = new ManagerPermissions();
        mp.setPermissionsFromAarray(arr);

        assertEquals(arr[0], mp.getPermission(ManagerPermissions.PERM_MANAGE_INVENTORY));
        assertEquals(arr[1], mp.getPermission(ManagerPermissions.PERM_MANAGE_STAFF));
        assertEquals(arr[2], mp.getPermission(ManagerPermissions.PERM_VIEW_STORE));
        assertEquals(arr[3], mp.getPermission(ManagerPermissions.PERM_UPDATE_POLICY));
        assertEquals(arr[4], mp.getPermission(ManagerPermissions.PERM_ADD_PRODUCT));
        assertEquals(arr[5], mp.getPermission(ManagerPermissions.PERM_REMOVE_PRODUCT));
        assertEquals(arr[6], mp.getPermission(ManagerPermissions.PERM_UPDATE_PRODUCT));
        assertEquals(arr[6], mp.getPermission(ManagerPermissions.PERM_OPEN_STORE));   // shares idx 6
        assertEquals(arr[5], mp.getPermission(ManagerPermissions.PERM_CLOSE_STORE));  // shares idx 5
    }
}
