package dev.reformator.loomoroutines.common;

import dev.reformator.loomoroutines.common.internal.utils.Utils;
import dev.reformator.loomoroutines.utils.GeneratorIterator;

import java.math.BigInteger;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class Main {
    public static void main(String[] args) {
        checkGenerator();
        checkInnerScope();
    }

    private static void checkInnerScope() {
        var point1 = Utils.createCoroutine("context1", () -> {
            System.out.println("call1: " + Utils.getRunningCoroutines());
            var point2 = Utils.createCoroutine("context2", () -> {
                System.out.println("call2: " + Utils.getRunningCoroutines());
                Utils.getRunningCoroutines().get(0).suspend();
                System.out.println("call3: " + Utils.getRunningCoroutines());
                Utils.getRunningCoroutines().get(1).suspend();
            }).resume();
            System.out.println("call4: " + Utils.getRunningCoroutines());
            point2.ifSuspended().resume();
        }).resume();
        System.out.println("call5: " + Utils.getRunningCoroutines());
        point1.ifSuspended().resume();
    }

    private static void checkGenerator() {
        var iterator = new GeneratorIterator<BigInteger>((scope) -> {
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
                .limit(20)
                .forEach(System.out::println);
    }
}
