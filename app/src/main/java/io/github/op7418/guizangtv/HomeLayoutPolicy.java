package io.github.op7418.guizangtv;

/** Distribute spare viewport pixels without shrinking the measured app rows. */
final class HomeLayoutPolicy {
    static int gapAfter(int index, int count, int rowsHeight, int viewportHeight, int minimumGap) {
        if (count <= 1 || index >= count - 1) return 0;
        int gaps = count - 1;
        int spare = Math.max(0, viewportHeight - rowsHeight - minimumGap * gaps);
        return minimumGap + spare / gaps + (index < spare % gaps ? 1 : 0);
    }
}
