package io.github.op7418.guizangtv;

/** Main-thread request state; browsing may cancel a choice, but not the initial image. */
final class WallpaperLoadState {
    private int generation;
    private boolean loading, selection, hasImage, destroyed;

    int begin(boolean userSelection) {
        if (destroyed) return -1;
        loading = true;
        selection = userSelection;
        return ++generation;
    }

    boolean accepts(int request) {
        return !destroyed && loading && request == generation;
    }

    boolean complete(int request, boolean applied) {
        if (!accepts(request)) return false;
        loading = false;
        selection = false;
        hasImage |= applied;
        return true;
    }

    void cancelSelection() {
        if (!loading || !selection) return;
        generation++;
        loading = false;
        selection = false;
    }

    boolean useCurrentImage() {
        if (destroyed || !hasImage) return false;
        // An explicit confirmation must also supersede an in-flight rotation.
        generation++;
        loading = false;
        selection = false;
        return true;
    }

    boolean needsImage() { return !destroyed && !hasImage && !loading; }
    boolean hasImage() { return hasImage; }
    boolean loading() { return loading; }
    boolean destroyed() { return destroyed; }

    void destroy() {
        destroyed = true;
        generation++;
        loading = false;
        selection = false;
    }
}
