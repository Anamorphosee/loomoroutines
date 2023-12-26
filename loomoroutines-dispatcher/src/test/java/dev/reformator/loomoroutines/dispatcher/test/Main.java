package dev.reformator.loomoroutines.dispatcher.test;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.DispatcherUtils;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        var dispatcher1 = new Dispatcher() {
            private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

            @Override
            public void scheduleExecute(Duration delay, Runnable action) {
                service.schedule(action, delay.toMillis(), TimeUnit.MILLISECONDS);
            }
        };

        var dispatcher2 = new Dispatcher() {
            private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

            @Override
            public void scheduleExecute(Duration delay, Runnable action) {
                service.schedule(action, delay.toMillis(), TimeUnit.MILLISECONDS);
            }
        };

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
}
