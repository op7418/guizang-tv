package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class BootStartPolicyTest {
    @Test public void supportedProfilesIncludeTclWithoutEnablingEveryDevice() {
        assertTrue(BootStartPolicy.supported("Smart TV Pro", "TCL", "TCL"));
        assertTrue(BootStartPolicy.supported("Smart TV Pro", "unknown", "tcl"));
        assertTrue(BootStartPolicy.supported("MiTV-MTSU0", "Xiaomi", "Xiaomi"));
        assertFalse(BootStartPolicy.supported("Other", "Other", "Other"));
        assertFalse(BootStartPolicy.supported("Other", "TCL", "TCL"));
        assertFalse(BootStartPolicy.supported(null, null, null));
    }
    @Test public void receiverAcceptsOnlyCompletedFullBoot() {
        assertTrue(BootStartPolicy.acceptsAction("android.intent.action.BOOT_COMPLETED", false));
        assertFalse(BootStartPolicy.acceptsAction("android.intent.action.MAIN", false));
        assertFalse(BootStartPolicy.acceptsAction("android.intent.action.LOCKED_BOOT_COMPLETED", false));
        assertFalse(BootStartPolicy.acceptsAction("android.intent.action.QUICKBOOT_POWERON", false));
        assertFalse(BootStartPolicy.acceptsAction("android.intent.action.SCREEN_ON", false));
        assertFalse(BootStartPolicy.acceptsAction(null, false));
    }
    @Test public void gateAcceptsOnlyMainLaunch() {
        assertTrue(BootStartPolicy.acceptsAction("android.intent.action.MAIN", true));
        assertFalse(BootStartPolicy.acceptsAction("android.intent.action.BOOT_COMPLETED", true));
        assertFalse(BootStartPolicy.acceptsAction("android.intent.action.VIEW", true));
        assertFalse(BootStartPolicy.acceptsAction(null, true));
    }
    @Test public void newBootCanEnter() {
        assertNull(BootStartPolicy.skipReason(true, true, 20, 19, 75000));
    }
    @Test public void disabledCannotEnterEvenDuringBoot() {
        assertNotNull(BootStartPolicy.skipReason(true, false, 20, 19, 75000));
    }
    @Test public void unsupportedModelCannotEnter() {
        assertNotNull(BootStartPolicy.skipReason(false, true, 20, 19, 75000));
    }
    @Test public void duplicateCannotTakeFocusFromHomeOrHdmi() {
        assertNotNull(BootStartPolicy.skipReason(true, true, 20, 20, 76000));
    }
    @Test public void unknownBootIsSkipped() {
        assertNotNull(BootStartPolicy.skipReason(true, true, -1, -1, 75000));
    }
    @Test public void lateDeliveryCannotInterruptUse() {
        assertNull(BootStartPolicy.skipReason(true, true, 20, 19, 600000));
        assertNotNull(BootStartPolicy.skipReason(true, true, 20, 19, 600001));
    }
    @Test public void invalidClockIsSkipped() {
        assertNotNull(BootStartPolicy.skipReason(true, true, 20, 19, -1));
    }
}
