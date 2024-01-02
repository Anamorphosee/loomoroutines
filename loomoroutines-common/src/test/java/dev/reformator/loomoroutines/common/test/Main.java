package dev.reformator.loomoroutines.common.test;

import dev.reformator.loomoroutines.common.CoroutineUtils;
import dev.reformator.loomoroutines.common.GeneratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        checkGenerator();
        checkInnerScope();
    }

    private static void checkInnerScope() {
        log.atInfo().log(() -> "call0: " + CoroutineUtils.getRunningCoroutinesContexts());
        var point1 = CoroutineUtils.createCoroutine("context1", () -> {
            log.atInfo().log(() -> "call1: " + CoroutineUtils.getRunningCoroutinesContexts());
            var point2 = CoroutineUtils.createCoroutine("context2", () -> {
                log.atInfo().log(() -> "call2: " + CoroutineUtils.getRunningCoroutinesContexts());
                CoroutineUtils.suspendCoroutine(context -> Objects.equals(context, "context1"));
                log.atInfo().log(() -> "call3: " + CoroutineUtils.getRunningCoroutinesContexts());
                CoroutineUtils.suspendCoroutine(context -> Objects.equals(context, "context2"));
                log.atInfo().log(() -> "call4: " + CoroutineUtils.getRunningCoroutinesContexts());
            }).resume();
            log.atInfo().log(() -> "call5: " + CoroutineUtils.getRunningCoroutinesContexts());
            CoroutineUtils.toSuspended(point2).resume();
            log.atInfo().log(() -> "call6: " + CoroutineUtils.getRunningCoroutinesContexts());
        }).resume();
        log.atInfo().log(() -> "call7: " + CoroutineUtils.getRunningCoroutinesContexts());
        CoroutineUtils.toSuspended(point1).resume();
        log.atInfo().log(() -> "call8: " + CoroutineUtils.getRunningCoroutinesContexts());
    }

    private static void checkGenerator() {
        var stream = GeneratorUtils.loomStream((scope) -> {
            var prev = BigInteger.ZERO;
            var current = BigInteger.ONE;
            while (true) {
                scope.emit(current);
                var tmp = prev.add(current);
                prev = current;
                current = tmp;
            }
        });
        stream.limit(150).forEach(it -> log.atInfo().log(() -> "generator value: " + it));
    }
}
