package dev.reformator.loomoroutines.dispatcher.test;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.DispatcherUtils;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        var dispatcher1 = new Dispatcher() {
            @Override
            public boolean canExecuteInCurrentThread() {
                return Thread.currentThread().isVirtual();
            }

            @Override
            public void execute(Runnable action) {
                Thread.ofVirtual().start(action);
            }

            @Override
            public void scheduleExecute(Duration delay, Runnable action) {
                Thread.ofVirtual().start(() -> {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    action.run();
                });
            }
        };

        var dispatcher2 = new Dispatcher() {
            private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

            @Override
            public void scheduleExecute(Duration delay, Runnable action) {
                service.schedule(action, delay.toMillis(), TimeUnit.MILLISECONDS);
            }
        };

        DispatcherUtils.dispatch(dispatcher1, () -> {
            System.out.println("call 1" + Thread.currentThread());
            DispatcherUtils.delay(Duration.ofSeconds(3));
            System.out.println("call 2" + Thread.currentThread());
            DispatcherUtils.doIn(dispatcher2, () -> {
                System.out.println("call 3" + Thread.currentThread());
                DispatcherUtils.delay(Duration.ofSeconds(3));
                System.out.println("call 4" + Thread.currentThread());
                return null;
            });
            System.out.println("call 5" + Thread.currentThread());
            DispatcherUtils.await(awakener -> {
                System.out.println("call 6" + Thread.currentThread());
                System.out.println("isInDispatcher: " + DispatcherUtils.isInDispatcher());
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                awakener.run();
            });
            System.out.println("call 7" + Thread.currentThread());
            return null;
        });
    }
}
