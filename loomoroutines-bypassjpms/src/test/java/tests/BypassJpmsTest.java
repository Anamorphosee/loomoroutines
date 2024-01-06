package tests;

import dev.reformator.loomoroutines.common.CompletedCoroutine;
import dev.reformator.loomoroutines.common.CoroutineUtils;
import dev.reformator.loomoroutines.common.GeneratorUtils;
import dev.reformator.loomoroutines.common.SuspendedCoroutine;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

public class BypassJpmsTest {
    @Test
    public void testGenerators() {
        var iterator = GeneratorUtils.loomIterator(scope -> {
            for (int i = 0; i < 10; i++) {
                scope.emit(i);
            }
            scope.emit(10);
            for (var value: GeneratorUtils.loomIterable(innerScope  -> {
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

        for (int i = 0; i <= 30; i++) {
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertEquals(i, iterator.next());
        }
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testCoroutineContextApi() {
        var context1CallCounter = new MutableInt();
        CoroutineUtils.createCoroutine("context1", () -> {
            context1CallCounter.increment();

            Assertions.assertEquals(
                    List.of("context1"),
                    CoroutineUtils.getRunningCoroutinesContexts()
            );
            Assertions.assertEquals(1, CoroutineUtils.getRunningCoroutinesNumber());

            var callCounter = new MutableInt();
            Assertions.assertEquals("context1", CoroutineUtils.getRunningCoroutineContext(context -> {
                callCounter.increment();
                Assertions.assertEquals("context1", context);
                return true;
            }));
            Assertions.assertEquals(1, callCounter.intValue());

            var context2CallCounter = new MutableInt();
            CoroutineUtils.createCoroutine("context2", () -> {
                context2CallCounter.increment();

                Assertions.assertEquals(
                        List.of("context2", "context1"),
                        CoroutineUtils.getRunningCoroutinesContexts()
                );
                Assertions.assertEquals(2, CoroutineUtils.getRunningCoroutinesNumber());

                callCounter.setValue(0);
                Assertions.assertEquals("context1", CoroutineUtils.getRunningCoroutineContext(context -> {
                    boolean result;
                    if (callCounter.intValue() == 0) {
                        Assertions.assertEquals("context2", context);
                        result = false;
                    } else if (callCounter.intValue() == 1) {
                        Assertions.assertEquals("context1", context);
                        result = true;
                    } else {
                        throw Assertions.<RuntimeException>fail("invalid callCounter: " + callCounter.intValue());
                    }
                    callCounter.increment();
                    return result;
                }));
                Assertions.assertEquals(2, callCounter.intValue());

                var context3CallCounter = new MutableInt();
                var coroutine3 = CoroutineUtils.createCoroutine(List.of(120), () -> {
                    context3CallCounter.increment();

                    callCounter.setValue(0);
                    Assertions.assertEquals(
                            List.of(120),
                            CoroutineUtils.getRunningCoroutineContext(List.class, list -> {
                                callCounter.increment();
                                Assertions.assertEquals(120, list.get(0));
                                return true;
                            })
                    );
                    Assertions.assertEquals(1, callCounter.intValue());

                    Assertions.assertEquals(List.of(120), CoroutineUtils.getRunningCoroutineContext(List.class));
                });
                Assertions.assertEquals(0, context3CallCounter.intValue());
                Assertions.assertEquals(
                        List.of(120),
                        Assertions.assertInstanceOf(CompletedCoroutine.class, coroutine3.resume()).getCoroutineContext()
                );
                Assertions.assertEquals(1, context3CallCounter.intValue());
            }).resume();
            Assertions.assertEquals(1, context2CallCounter.intValue());
        }).resume();
        Assertions.assertEquals(1, context1CallCounter.intValue());
    }

    @Test
    public void testCoroutineSuspensionApi() {
        var callCounter = new MutableInt();
        var coroutine1 = CoroutineUtils.createCoroutine("context1", () -> {
            Assertions.assertEquals(1, callCounter.getAndIncrement());
            Assertions.assertFalse(CoroutineUtils.trySuspendCoroutine(context -> {
                Assertions.assertEquals(2, callCounter.getAndIncrement());
                Assertions.assertEquals("context1", context);
                return false;
            }));
            Assertions.assertEquals(3, callCounter.getAndIncrement());
            Assertions.assertTrue(CoroutineUtils.trySuspendCoroutine(context -> {
                Assertions.assertEquals(4, callCounter.getAndIncrement());
                Assertions.assertEquals("context1", context);
                return true;
            }));
            Assertions.assertEquals(6, callCounter.getAndIncrement());
        });
        Assertions.assertEquals("context1", coroutine1.getCoroutineContext());
        Assertions.assertEquals(0, callCounter.getAndIncrement());
        var coroutine2 = Assertions.assertInstanceOf(SuspendedCoroutine.class, coroutine1.resume());
        Assertions.assertEquals("context1", coroutine2.getCoroutineContext());
        Assertions.assertEquals(5, callCounter.getAndIncrement());
        var coroutine3 = Assertions.assertInstanceOf(CompletedCoroutine.class, coroutine2.resume());
        Assertions.assertEquals(7, callCounter.getAndIncrement());
        Assertions.assertEquals("context1", coroutine3.getCoroutineContext());
    }
}
