package io.github.op7418.guizangtv;

/** Shared bounds for persisted wallpaper selections and slideshow intervals. */
final class WallpaperCycle {
    static final int DEFAULT_MINUTES = 5;
    static final int[] MINUTES = {1, 5, 15, 30};

    static final int DEFAULT_FADE_MS = 700;
    static int fadeMs(int value) {
        return value >= 700 && value <= 1500 && value % 100 == 0 ? value : DEFAULT_FADE_MS;
    }
    static String fadeLabel(int value) {
        return String.format(java.util.Locale.CHINA, "%.1f 秒", value / 1000f);
    }

    static int index(int value, int count) {
        return count > 0 && value >= 0 && value < count ? value : 0;
    }

    static int next(int value, int count) {
        return count > 0 ? (index(value, count) + 1) % count : 0;
    }

    static int minutes(int value) {
        for (int allowed : MINUTES) if (value == allowed) return value;
        return DEFAULT_MINUTES;
    }
}
