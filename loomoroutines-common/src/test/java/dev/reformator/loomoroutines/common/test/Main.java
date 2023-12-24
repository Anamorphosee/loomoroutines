package dev.reformator.loomoroutines.common.test;

import dev.reformator.loomoroutines.utils.CoroutineUtils;
import dev.reformator.loomoroutines.utils.GeneratorIterable;

import java.math.BigInteger;
import java.util.stream.StreamSupport;

public class Main {
    public static void main(String[] args) {
        checkGenerator();
        //checkInnerScope();
    }

    private static void checkInnerScope() {
        System.out.println("call0: " + CoroutineUtils.getRunningCoroutines());
        var point1 = CoroutineUtils.createCoroutine("context1", () -> {
            System.out.println("call1: " + CoroutineUtils.getRunningCoroutines());
            var point2 = CoroutineUtils.createCoroutine("context2", () -> {
                System.out.println("call2: " + CoroutineUtils.getRunningCoroutines());
                CoroutineUtils.getRunningCoroutines().get(0).suspend();
                System.out.println("call3: " + CoroutineUtils.getRunningCoroutines());
                CoroutineUtils.getRunningCoroutines().get(1).suspend();
                System.out.println("call4: " + CoroutineUtils.getRunningCoroutines());
            }).resume();
            System.out.println("call5: " + CoroutineUtils.getRunningCoroutines());
            point2.ifSuspended().resume();
            System.out.println("call6: " + CoroutineUtils.getRunningCoroutines());
        }).resume();
        System.out.println("call7: " + CoroutineUtils.getRunningCoroutines());
        point1.ifSuspended().resume();
        System.out.println("call8: " + CoroutineUtils.getRunningCoroutines());
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
                .limit(150)
                .forEach(System.out::println);
    }
}
