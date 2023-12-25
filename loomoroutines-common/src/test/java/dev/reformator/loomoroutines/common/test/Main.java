package dev.reformator.loomoroutines.common.test;

import dev.reformator.loomoroutines.common.CoroutineUtils;
import dev.reformator.loomoroutines.common.GeneratorUtils;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        checkGenerator();
        //checkInnerScope();
    }

    private static void checkInnerScope() {
        System.out.println("call0: " + CoroutineUtils.getRunningCoroutines());
        var point1 = CoroutineUtils.createCoroutine("context1", () -> {
            System.out.println("call1: " + CoroutineUtils.getRunningCoroutines());
            var point2 = CoroutineUtils.toSuspended(CoroutineUtils.createCoroutine("context2", () -> {
                System.out.println("call2: " + CoroutineUtils.getRunningCoroutines());
                CoroutineUtils.getRunningCoroutines().get(0).suspend();
                System.out.println("call3: " + CoroutineUtils.getRunningCoroutines());
                CoroutineUtils.getRunningCoroutines().get(1).suspend();
                System.out.println("call4: " + CoroutineUtils.getRunningCoroutines());
            })).resume();
            System.out.println("call5: " + CoroutineUtils.getRunningCoroutines());
            CoroutineUtils.toSuspended(point2).resume();
            System.out.println("call6: " + CoroutineUtils.getRunningCoroutines());
        }).resume();
        System.out.println("call7: " + CoroutineUtils.getRunningCoroutines());
        CoroutineUtils.toSuspended(point1).resume();
        System.out.println("call8: " + CoroutineUtils.getRunningCoroutines());
    }

    private static void checkGenerator() {
        var stream = GeneratorUtils.stream((scope) -> {
            var prev = BigInteger.ZERO;
            var current = BigInteger.ONE;
            while (true) {
                scope.emit(current);
                var tmp = prev.add(current);
                prev = current;
                current = tmp;
            }
        });
        stream.limit(150).forEach(System.out::println);
    }
}
