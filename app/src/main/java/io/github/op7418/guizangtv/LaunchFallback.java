package io.github.op7418.guizangtv;

/** Try the next route when Android rejects a launch after intent resolution. */
final class LaunchFallback {
    interface Starter<T> { void start(T candidate); }
    interface Rejected<T> { void record(T candidate, RuntimeException error); }

    private LaunchFallback() { }

    static <T> boolean start(Iterable<T> candidates, Starter<T> starter, Rejected<T> rejected) {
        for (T candidate : candidates) {
            if (candidate == null) continue;
            try {
                starter.start(candidate);
                return true;
            } catch (RuntimeException error) {
                rejected.record(candidate, error);
            }
        }
        return false;
    }
}
