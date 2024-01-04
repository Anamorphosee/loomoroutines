package tests;

import dev.reformator.loomoroutines.dispatcher.DispatcherUtils;
import dev.reformator.loomoroutines.dispatcher.PromiseState;
import dev.reformator.loomoroutines.dispatcher.VirtualThreadsDispatcher;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

@SuppressWarnings("Since15")
public class DispatcherTest {
    @Test
    public void testDispatcherApi() {
        Assertions.assertFalse(DispatcherUtils.isInDispatcher());
        var callCounter = new MutableInt();
        var promise = DispatcherUtils.dispatch(VirtualThreadsDispatcher.INSTANCE, () -> {
            Assertions.assertEquals(0, callCounter.getAndIncrement());
            Assertions.assertTrue(Thread.currentThread().isVirtual());
            Assertions.assertTrue(DispatcherUtils.isInDispatcher());
            DispatcherUtils.await(notifier -> {
                Assertions.assertEquals(1, callCounter.getAndIncrement());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Assertions.assertEquals(2, callCounter.getAndIncrement());
                notifier.invoke();
            });
            Assertions.assertEquals(3, callCounter.getAndIncrement());
            Assertions.assertTrue(Thread.currentThread().isVirtual());
            Assertions.assertTrue(DispatcherUtils.isInDispatcher());
            var currentTime = System.currentTimeMillis();
            DispatcherUtils.delay(Duration.ofMillis(100));
            Assertions.assertTrue(System.currentTimeMillis() >= currentTime + 100);
            Assertions.assertEquals(4, callCounter.getAndIncrement());
            Assertions.assertTrue(Thread.currentThread().isVirtual());
            Assertions.assertTrue(DispatcherUtils.isInDispatcher());
            Object doInResult;
            try (var dispatcher = DispatcherUtils.toDispatcher(Executors.newSingleThreadScheduledExecutor())) {
                doInResult = DispatcherUtils.doIn(dispatcher, () -> {
                    Assertions.assertEquals(5, callCounter.getAndIncrement());
                    Assertions.assertFalse(Thread.currentThread().isVirtual());
                    Assertions.assertTrue(DispatcherUtils.isInDispatcher());
                    var innerCurrentTime = System.currentTimeMillis();
                    DispatcherUtils.delay(Duration.ofMillis(100));
                    Assertions.assertTrue(System.currentTimeMillis() >= innerCurrentTime + 100);
                    Assertions.assertEquals(6, callCounter.getAndIncrement());
                    Assertions.assertFalse(Thread.currentThread().isVirtual());
                    Assertions.assertTrue(DispatcherUtils.isInDispatcher());
                    return "result";
                });
            }
            Assertions.assertEquals(7, callCounter.getAndIncrement());
            Assertions.assertTrue(Thread.currentThread().isVirtual());
            Assertions.assertTrue(DispatcherUtils.isInDispatcher());
            Assertions.assertEquals("result", doInResult);
            return List.of(123);
        });
        Assertions.assertEquals(PromiseState.RUNNING, promise.getState());
        Assertions.assertFalse(DispatcherUtils.isInDispatcher());
        var list = promise.join();
        Assertions.assertEquals(8, callCounter.getAndIncrement());
        Assertions.assertEquals(PromiseState.COMPLETED, promise.getState());
        Assertions.assertFalse(DispatcherUtils.isInDispatcher());
        Assertions.assertEquals(List.of(123), list);
    }
}
