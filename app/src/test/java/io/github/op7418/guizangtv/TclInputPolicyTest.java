package io.github.op7418.guizangtv;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class TclInputPolicyTest {
    @Test public void gatesTheObservedModelInsteadOfEveryTcl() {
        assertTrue(TclInputPolicy.supported("Smart TV Pro","TCL","unknown"));
        assertTrue(TclInputPolicy.supported("Smart TV Pro","unknown","tcl"));
        assertFalse(TclInputPolicy.supported("Other","TCL","TCL"));
        assertFalse(TclInputPolicy.supported("Smart TV Pro","Other","Other"));
        assertFalse(TclInputPolicy.supported(null,null,null));
    }
    @Test public void invalidPortsCannotProduceAnInputId() {
        assertNull(TclInputPolicy.hdmi(0));assertNull(TclInputPolicy.hdmi(5));
        assertEquals("com.tcl.tvinput/.TvPassThroughService/HW15",TclInputPolicy.hdmi(1));
        assertEquals("com.tcl.tvinput/.TvPassThroughService/HW18",TclInputPolicy.hdmi(4));
    }
    @Test public void listsOnlyRegisteredKnownInputsInPortOrder() {
        assertEquals(Arrays.asList("HDMI 1","HDMI 4"),new java.util.ArrayList<>(TclInputPolicy.registered(
                Arrays.asList(TclInputPolicy.hdmi(4),"other/service/HW15",TclInputPolicy.hdmi(1))).keySet()));
        assertTrue(TclInputPolicy.registered(Collections.emptyList()).isEmpty());
    }
    @Test public void tunerAndAvAreNotConfusedWithHdmi() {
        assertEquals(Arrays.asList("电视节目 (TV)","复合视频 (AV)"),new java.util.ArrayList<>(TclInputPolicy.registered(
                Arrays.asList("com.tcl.tvinput/.TunerInputService/HW2","com.tcl.tvinput/.TvPassThroughService/HW3")).keySet()));
    }
}
