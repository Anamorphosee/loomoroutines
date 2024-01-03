package dev.reformator.loomoroutines.dispatcher.test;

import dev.reformator.loomoroutines.dispatcher.CloseableDispatcher;
import dev.reformator.loomoroutines.dispatcher.DispatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        testJoin();
        basic();
    }

    private static void basic() {
        try (var disp1 = createDispatcher(); var disp2 = createDispatcher()) {
            log.atInfo().log(() -> "call 0 isInDispatcher: " + DispatcherUtils.isInDispatcher());
            DispatcherUtils.dispatch(disp1, () -> {
                log.atInfo().log(() -> "call 1 isInDispatcher: " + DispatcherUtils.isInDispatcher());
                DispatcherUtils.delay(Duration.ofSeconds(3));
                log.atInfo().log(() -> "call 2 isInDispatcher: " + DispatcherUtils.isInDispatcher());
                DispatcherUtils.doIn(disp2, () -> {
                    log.atInfo().log(() -> "call 3 isInDispatcher: " + DispatcherUtils.isInDispatcher());
                    DispatcherUtils.delay(Duration.ofSeconds(3));
                    log.atInfo().log(() -> "call 4 isInDispatcher: " + DispatcherUtils.isInDispatcher());
                    return null;
                });
                log.atInfo().log(() -> "call 5 isInDispatcher: " + DispatcherUtils.isInDispatcher());
                DispatcherUtils.await(notifier -> {
                    log.atInfo().log(() -> "call 6 isInDispatcher: " + DispatcherUtils.isInDispatcher());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    notifier.invoke();
                });
                log.atInfo().log(() -> "call 7 isInDispatcher: " + DispatcherUtils.isInDispatcher());
                return null;
            }).join();
            log.atInfo().log(() -> "call 8 isInDispatcher: " + DispatcherUtils.isInDispatcher());
        }
    }

    private static void testJoin() {
        try (var disp1 = createDispatcher(); var disp2 = createDispatcher()) {
            var prom1 = DispatcherUtils.dispatch(disp1, () -> {
                log.info("started prom1");
                DispatcherUtils.delay(Duration.ofSeconds(3));
                log.info("finished prom1");
                return (Void) null;
            });

            DispatcherUtils.dispatch(disp2, () -> {
                log.info("started prom2");
                var joined = prom1.join();
                log.atInfo().log(() -> "prom1 joined with " + joined);
                return "finished";
            }).join();

            log.info("finished");
            prom1.join();
        }
    }

    private static CloseableDispatcher createDispatcher() {
        return DispatcherUtils.toDispatcher(Executors.newSingleThreadScheduledExecutor());
    }
}
