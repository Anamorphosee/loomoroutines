package tests;

import dev.reformator.loomoroutines.common.CompletedCoroutine;
import dev.reformator.loomoroutines.common.GeneratorUtils;
import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static dev.reformator.loomoroutines.common.CoroutineUtils.*;
import static dev.reformator.loomoroutines.common.GeneratorUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class BypassJpmsTest {
    @Test
    public void testGenerators() {
        var iterator = loomIterator(scope -> {
            for (int i = 0; i < 10; i++) {
                scope.emit(i);
            }
            scope.emit(10);
            for (var value: loomIterable(innerScope  -> {
                for (int i = 11; i < 20; i++) {
                    if (i % 2 == 0) {
                        innerScope.emit(i);
                    } else {
                        scope.emit(i);
                    }
                }

                GeneratorUtils.<Integer>loomStream((innerInnerScope) -> {
                    for (int i = 20; i <= 30; i++) {
                        if (i % 3 == 0) {
                            innerInnerScope.emit(i);
                        } else if (i % 3 == 1) {
                            innerScope.emit(i);
                        } else {
                            scope.emit(i);
                        }
                    }
                }).forEach(i -> {
                    if (i % 2 == 0) {
                        innerScope.emit(i);
                    } else {
                        scope.emit(i);
                    }
                });
            })) {
                scope.emit(value);
            }
        });

        for (int i = 0; i<= 30; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i, iterator.next());
        }
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testCoroutineContextApi() {
        var context1CallCounter = new MutableInt();
        createCoroutine("context1", () -> {
            context1CallCounter.increment();

            assertEquals(
                    List.of("context1"),
                    getRunningCoroutinesContexts()
            );
            assertEquals(1, getRunningCoroutinesNumber());

            var callCounter = new MutableInt();
            assertEquals("context1", getRunningCoroutineContext(context -> {
                callCounter.increment();
                assertEquals("context1", context);
                return true;
            }));
            assertEquals(1, callCounter.intValue());

            var context2CallCounter = new MutableInt();
            createCoroutine("context2", () -> {
                context2CallCounter.increment();

                assertEquals(
                        List.of("context2", "context1"),
                        getRunningCoroutinesContexts()
                );
                assertEquals(2, getRunningCoroutinesNumber());

                callCounter.setValue(0);
                assertEquals("context1", getRunningCoroutineContext(context -> {
                    boolean result;
                    if (callCounter.intValue() == 0) {
                        assertEquals("context2", context);
                        result = false;
                    } else if (callCounter.intValue() == 1) {
                        assertEquals("context1", context);
                        result = true;
                    } else {
                        throw Assertions.<RuntimeException>fail("invalid callCounter: " + callCounter.intValue());
                    }
                    callCounter.increment();
                    return result;
                }));
                assertEquals(2, callCounter.intValue());

                var context3CallCounter = new MutableInt();
                var coroutine3 = createCoroutine(List.of(120), () -> {
                    context3CallCounter.increment();

                    callCounter.setValue(0);
                    assertEquals(
                            List.of(120),
                            getRunningCoroutineContext(List.class, list -> {
                                callCounter.increment();
                                assertEquals(120, list.get(0));
                                return true;
                            })
                    );
                    assertEquals(1, callCounter.intValue());

                    assertEquals(List.of(120), getRunningCoroutineContext(List.class));
                });
                assertEquals(0, context3CallCounter.intValue());
                assertEquals(
                        List.of(120),
                        assertInstanceOf(CompletedCoroutine.class, coroutine3.resume()).getCoroutineContext()
                );
                assertEquals(1, context3CallCounter.intValue());
            }).resume();
            assertEquals(1, context2CallCounter.intValue());
        }).resume();
        assertEquals(1, context1CallCounter.intValue());
    }

    @Test
    public void testCoroutineSuspensionApi() {
        var callCounter = new MutableInt();
        var coroutine1 = createCoroutine("context1", () -> {
            assertEquals(1, callCounter.getAndIncrement());
            assertFalse(trySuspendCoroutine(context -> {
                assertEquals(2, callCounter.getAndIncrement());
                assertEquals("context1", context);
                return false;
            }));
            assertEquals(3, callCounter.getAndIncrement());
            assertTrue(trySuspendCoroutine(context -> {
                assertEquals(4, callCounter.getAndIncrement());
                assertEquals("context1", context);
                return true;
            }));
            assertEquals(6, callCounter.getAndIncrement());
        });
        assertEquals("context1", coroutine1.getCoroutineContext());
        assertEquals(0, callCounter.getAndIncrement());
        var coroutine2 = assertInstanceOf(SuspendedCoroutine.class, coroutine1.resume());
        assertEquals("context1", coroutine2.getCoroutineContext());
        assertEquals(5, callCounter.getAndIncrement());
        var coroutine3 = assertInstanceOf(CompletedCoroutine.class, coroutine2.resume());
        assertEquals(7, callCounter.getAndIncrement());
        assertEquals("context1", coroutine3.getCoroutineContext());
    }

    @Test
    public void testPinnedVirtualThread() throws InterruptedException {
        var completed =new MutableBoolean(false);
        Thread.startVirtualThread(() -> {
            synchronized (new Object()) {
                createCoroutine("context1", () -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).resume();
            }
            completed.setTrue();
        }).join();
        assertTrue(completed.getValue());
    }

    @Test
    public void testFailedSuspend() {
        try {
            createCoroutine("context1", () -> {
                synchronized (new Object()) {
                    createCoroutine("context2", () -> {
                        suspendCoroutine(c -> Objects.equals(c, "context1"));
                    }).resume();
                }
            }).resume();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Current thread is pinned"));
            return;
        }
        fail();
    }

    @Test
    public void testNotFailedSuspend() {
        createCoroutine("context1", () -> {
            synchronized (new Object()) {
                createCoroutine("context2", () -> {
                    suspendCoroutine(c -> Objects.equals(c, "context2"));
                }).resume();
            }
        }).resume();
    }
}
