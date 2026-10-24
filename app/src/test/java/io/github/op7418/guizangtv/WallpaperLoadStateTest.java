package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class WallpaperLoadStateTest {
    @Test public void openingAndClosingGalleryCannotCancelFirstImage() {
        WallpaperLoadState state = new WallpaperLoadState();
        int firstImage = state.begin(false);
        state.cancelSelection(); // Gallery focus.
        state.cancelSelection(); // Back/dismiss, before decoding finishes.
        assertTrue(state.accepts(firstImage));
        assertTrue(state.complete(firstImage, true));
        assertTrue(state.hasImage());
        assertFalse(state.needsImage());
    }

    @Test public void cancellingEarlyChoiceRestartsBaseAndRejectsBothOldResults() {
        WallpaperLoadState state = new WallpaperLoadState();
        int initial = state.begin(false);
        int choice = state.begin(true);
        state.cancelSelection();
        assertTrue(state.needsImage());
        int restored = state.begin(false);
        assertFalse(state.complete(initial, true));
        assertFalse(state.complete(choice, true));
        state.cancelSelection();
        assertTrue(state.complete(restored, true));
        assertTrue(state.hasImage());
    }

    @Test public void cancelledChoiceKeepsAlreadyDisplayedImage() {
        WallpaperLoadState state = new WallpaperLoadState();
        assertTrue(state.complete(state.begin(false), true));
        int choice = state.begin(true);
        state.cancelSelection();
        assertFalse(state.accepts(choice));
        assertFalse(state.complete(choice, true));
        assertTrue(state.hasImage());
        assertFalse(state.needsImage());
        assertFalse(state.loading());
    }

    @Test public void rapidChoicesOnlyApplyLastDecodedRequest() {
        WallpaperLoadState state = new WallpaperLoadState();
        int first = state.begin(true);
        int second = state.begin(true);
        assertFalse(state.complete(first, true));
        assertTrue(state.loading());
        assertTrue(state.complete(second, true));
        assertFalse(state.complete(second, true));
        assertTrue(state.hasImage());
    }

    @Test public void confirmingCurrentImageSupersedesPendingRotation() {
        WallpaperLoadState state = new WallpaperLoadState();
        assertFalse(state.useCurrentImage());
        assertTrue(state.complete(state.begin(false), true));
        int rotation = state.begin(false);
        assertTrue(state.useCurrentImage());
        assertFalse(state.complete(rotation, true));
        assertFalse(state.loading());
        assertTrue(state.hasImage());
        state.destroy();
        assertFalse(state.useCurrentImage());
    }

    @Test public void failedFirstDecodeCanRetryAndFailureKeepsAnExistingImage() {
        WallpaperLoadState state = new WallpaperLoadState();
        assertTrue(state.complete(state.begin(false), false));
        assertTrue(state.needsImage());
        assertTrue(state.complete(state.begin(false), true));
        assertTrue(state.complete(state.begin(true), false));
        assertTrue(state.hasImage());
        assertFalse(state.needsImage());
    }

    @Test public void destroyedControllerRejectsDelayedDecodeAndCannotReload() {
        WallpaperLoadState state = new WallpaperLoadState();
        int decode = state.begin(false);
        state.destroy();
        assertFalse(state.accepts(decode));
        assertFalse(state.complete(decode, true));
        assertFalse(state.needsImage());
        assertFalse(state.loading());
        assertEquals(-1, state.begin(true));
    }
}
