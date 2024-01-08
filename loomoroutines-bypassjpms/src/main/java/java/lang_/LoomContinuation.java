package java.lang_;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

public class LoomContinuation<T> extends Continuation {
    private static final ContinuationScope scope = new ContinuationScope("Loomoroutines");
    private static final LoomContinuation<Object> failedLoomContinuation =
            new LoomContinuation<>(null, () -> { });
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
        var cachedNext = next;
        next = null;
        if (!Continuation.yield(scope) || next == failedLoomContinuation) {
            next = cachedNext;
            throw new IllegalStateException("Suspension has failed. Current thread is pinned.");
        }
    }

    public static LoomContinuation<?> getCurrentContinuation() {
        return (LoomContinuation<?>) Continuation.getCurrentContinuation(scope);
    }

    public void yieldInSuspend() {
        if (assertionEnabled && next == null) {
            throw new IllegalStateException("Assertion has failed.");
        }
        if (!Continuation.yield(scope)) {
            var suspendingContinuation = next;
            while (true) {
                if (suspendingContinuation.next != null) {
                    suspendingContinuation = suspendingContinuation.next;
                } else {
                    suspendingContinuation.next = failedLoomContinuation;
                    return;
                }
            }
        }
    }

    @Override
    protected void onPinned(Pinned reason) { }
}
