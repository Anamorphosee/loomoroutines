package java.lang_;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

public class LoomContinuation<T> extends Continuation {
    private static final ContinuationScope scope = new ContinuationScope("Loomoroutines");
    public static boolean assertionEnabled = true;
    public final T context;
    public LoomContinuation<?> next = null;

    public LoomContinuation(T context, Runnable target) {
        super(scope, target);
        this.context = context;
    }

    public void suspend() {
        if (assertionEnabled) {
            var currentContinuation = getCurrentContinuation();
            while (currentContinuation != null && currentContinuation != this) {
                currentContinuation = currentContinuation.next;
            }
            if (currentContinuation == null) {
                throw new IllegalStateException("Continuation is not in the scope.");
            }
        }
        next = null;
        Continuation.yield(scope);
    }

    public static LoomContinuation<?> getCurrentContinuation() {
        return (LoomContinuation<?>) Continuation.getCurrentContinuation(scope);
    }

    public static void yield() {
        Continuation.yield(scope);
    }
}
