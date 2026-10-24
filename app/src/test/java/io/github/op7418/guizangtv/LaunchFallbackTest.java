package io.github.op7418.guizangtv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class LaunchFallbackTest {
    @Test public void deniedVendorEntryContinuesToStandardSettings() {
        List<String> attempts = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        assertTrue(LaunchFallback.start(Arrays.asList("vendor", "standard", "unused"), candidate -> {
            attempts.add(candidate);
            if (candidate.equals("vendor")) throw new SecurityException("Not exported");
        }, (candidate, error) -> rejected.add(candidate)));
        assertEquals(Arrays.asList("vendor", "standard"), attempts);
        assertEquals(Arrays.asList("vendor"), rejected);
    }

    @Test public void missingAndFailedCandidatesReportNoEntry() {
        List<String> rejected = new ArrayList<>();
        assertFalse(LaunchFallback.start(Arrays.asList(null, "removed", "denied"), candidate -> {
            if (candidate.equals("removed")) throw new IllegalStateException("Removed since resolution");
            throw new SecurityException("Permission required");
        }, (candidate, error) -> rejected.add(candidate)));
        assertEquals(Arrays.asList("removed", "denied"), rejected);
    }

    @Test public void unavailablePackageCanUseExplicitStockComponent() {
        List<String> attempts = new ArrayList<>();
        assertTrue(LaunchFallback.start(Arrays.asList(null, null, "stock-home"), attempts::add,
                (candidate, error) -> fail("Unexpected rejection")));
        assertEquals(Arrays.asList("stock-home"), attempts);
    }
}
