package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class WallpaperCycleTest {
    @org.junit.Test public void transitionDurationBoundsAndSteps() {
        for(int value=700;value<=1500;value+=100) assertEquals(value, WallpaperCycle.fadeMs(value));
        for(int value : new int[]{-1, 0, 600, 750, 1600, Integer.MAX_VALUE})
            assertEquals(700, WallpaperCycle.fadeMs(value));
    }
    @org.junit.Test public void transitionLabelsShowTenths() {
        assertEquals("0.7 秒", WallpaperCycle.fadeLabel(700));
        assertEquals("1.0 秒", WallpaperCycle.fadeLabel(1000));
        assertEquals("1.5 秒", WallpaperCycle.fadeLabel(1500));
    }

    @Test public void wrapsThirteenImagesWithoutRepeatingLast() {
        assertEquals(0, WallpaperCycle.next(12, 13));
        assertEquals(6, WallpaperCycle.next(5, 13));
    }
    @Test public void removedOrInvalidSelectionStartsAtFirst() {
        assertEquals(0, WallpaperCycle.index(13, 13));
        assertEquals(0, WallpaperCycle.index(-1, 13));
    }
    @Test public void emptyAndSingleCatalogAreSafe() {
        assertEquals(0, WallpaperCycle.next(0, 0));
        assertEquals(0, WallpaperCycle.next(0, 1));
    }
    @Test public void badPersistedIntervalReturnsDefault() {
        assertEquals(5, WallpaperCycle.minutes(0));
        assertEquals(5, WallpaperCycle.minutes(Integer.MAX_VALUE));
        assertEquals(1, WallpaperCycle.minutes(1));
        assertEquals(30, WallpaperCycle.minutes(30));
    }
}
