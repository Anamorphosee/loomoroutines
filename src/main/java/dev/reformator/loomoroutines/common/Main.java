package dev.reformator.loomoroutines.common;

import dev.reformator.loomoroutines.impl.BaseLoomCoroutine;
import dev.reformator.loomoroutines.utils.GeneratorIterable;
import dev.reformator.loomoroutines.utils.GeneratorIterator;
import jdk.internal.vm.ContinuationScope;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

class TestCoroutine extends BaseLoomCoroutine<TestCoroutine> {
    public TestCoroutine(@NotNull Runnable body) {
        super(body);
    }
}

public class Main {
    public static void main(String[] args) {
        checkInnerScope();
    }

    private static void checkInnerScope() {
        new TestCoroutine(() -> {
            System.out.println("call1: " + Coroutine.getCoroutinesInScope());
            new TestCoroutine(() -> {
                System.out.println("call2: " + Coroutine.getCoroutinesInScope());
                Coroutine.getCoroutinesInScope().get(0).suspend((context) -> {
                    System.out.println("call3: " + Coroutine.getCoroutinesInScope());
                    context.resume();
                });
                System.out.println("call4: " + Coroutine.getCoroutinesInScope());
                Coroutine.getCoroutinesInScope().get(1).suspend((context) -> {
                    System.out.println("call5: " + Coroutine.getCoroutinesInScope());
                    context.resume();
                });
            }).resume();
        }).resume();
    }

    private static void checkGenerator() {
        var iterator = GeneratorIterator.newInstance((scope) -> {
            var prev = BigInteger.ZERO;
            var current = BigInteger.ONE;
            while (true) {
                scope.emit(current);
                var tmp = prev.add(current);
                prev = current;
                current = tmp;
            }
        });
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
                .limit(10)
                .forEach(System.out::println);
    }
}
