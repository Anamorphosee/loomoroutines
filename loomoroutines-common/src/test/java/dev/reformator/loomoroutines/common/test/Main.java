package dev.reformator.loomoroutines.common.test;

import dev.reformator.loomoroutines.common.internal.utils.Utils;
import dev.reformator.loomoroutines.utils.GeneratorIterable;

import java.math.BigInteger;
import java.util.stream.StreamSupport;

public class Main {
    public static void main(String[] args) {
        checkGenerator();
        checkInnerScope();
    }

    private static void checkInnerScope() {
        System.out.println("call0: " + Utils.getRunningCoroutines());
        var point1 = Utils.createCoroutine("context1", () -> {
            System.out.println("call1: " + Utils.getRunningCoroutines());
            var point2 = Utils.createCoroutine("context2", () -> {
                System.out.println("call2: " + Utils.getRunningCoroutines());
                Utils.getRunningCoroutines().get(0).suspend();
                System.out.println("call3: " + Utils.getRunningCoroutines());
                Utils.getRunningCoroutines().get(1).suspend();
                System.out.println("call4: " + Utils.getRunningCoroutines());
            }).resume();
            System.out.println("call5: " + Utils.getRunningCoroutines());
            point2.ifSuspended().resume();
            System.out.println("call6: " + Utils.getRunningCoroutines());
        }).resume();
        System.out.println("call7: " + Utils.getRunningCoroutines());
        point1.ifSuspended().resume();
        System.out.println("call8: " + Utils.getRunningCoroutines());
    }

    private static void checkGenerator() {
        var iterable = new GeneratorIterable<BigInteger>((scope) -> {
            var prev = BigInteger.ZERO;
            var current = BigInteger.ONE;
            while (true) {
                scope.emit(current);
                var tmp = prev.add(current);
                prev = current;
                current = tmp;
            }
        });
        StreamSupport.stream(iterable.spliterator(), false)
                .limit(20)
                .forEach(System.out::println);
    }
}
