package dev.reformator.loomoroutines.dispatcher.test;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.DispatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        testJoin();
    }

    private static void basic() {
        var dispatcher1 = createDispatcher();
        var dispatcher2 = createDispatcher();

        System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
        DispatcherUtils.dispatch(dispatcher1, () -> {
            System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
            System.out.println("call 1: " + Thread.currentThread());
            DispatcherUtils.delay(Duration.ofSeconds(3));
            System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
            System.out.println("call 2: " + Thread.currentThread());
            DispatcherUtils.doIn(dispatcher2, () -> {
                System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
                System.out.println("call 3: " + Thread.currentThread());
                DispatcherUtils.delay(Duration.ofSeconds(3));
                System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
                System.out.println("call 4: " + Thread.currentThread());
                return null;
            });
            System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
            System.out.println("call 5: " + Thread.currentThread());
            DispatcherUtils.await(awakener -> {
                System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
                System.out.println("call 6: " + Thread.currentThread());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                awakener.run();
            });
            System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
            System.out.println("call 7: " + Thread.currentThread());
            return null;
        }).join();
        System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
        System.out.println("call 8: " + Thread.currentThread());
    }

    private static void testJoin() {
        var disp1 = createDispatcher();
        var disp2 = createDispatcher();

        var prom1 = DispatcherUtils.dispatch(disp1, () -> {
            log.atInfo().log(() -> "started prom1 in " + Thread.currentThread());
            DispatcherUtils.delay(Duration.ofSeconds(3));
            log.atInfo().log(() -> "finished prom1 in " + Thread.currentThread());
            return (Void) null;
        });

        var prom2 = DispatcherUtils.dispatch(disp2, () -> {
            log.atInfo().log(() -> "started prom2 in " + Thread.currentThread());
            var joined = prom1.join();
            log.atInfo().log(() -> "prom1 joined with " + joined);
            return "finished";
        }).join();
    }

    private static Dispatcher createDispatcher() {
        return new Dispatcher() {
            private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

            @Override
            public void scheduleExecute(Duration delay, Runnable action) {
                service.schedule(action, delay.toMillis(), TimeUnit.MILLISECONDS);
            }
        };
    }
}
