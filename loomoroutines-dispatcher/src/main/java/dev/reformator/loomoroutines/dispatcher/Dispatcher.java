package dev.reformator.loomoroutines.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface Dispatcher {
    default void execute(@NotNull Runnable action) {
        scheduleExecute(Duration.ZERO, action);
    }

    void scheduleExecute(@NotNull Duration duration, @NotNull Runnable action);

    default boolean canExecuteInCurrentThread() {
        return false;
    }

//    Dispatcher VirtualThread = new Dispatcher() {
//        @Override
//        public void execute(@NotNull Runnable action) {
//            Thread.ofVirtual().start(action);
//        }
//
//        @Override
//
//        public void scheduleExecute(@NotNull Duration duration, @NotNull Runnable action) {
//            Thread.ofVirtual().start(() -> {
//                try {
//                    Thread.sleep(duration);
//                } catch (InterruptedException e) {
//                    throw CommonUtils.throwUnchecked(e);
//                }
//                action.run();
//            });
//        }
//
//        @Override
//        public boolean doDispatchInCurrentThread() {
//            return Thread.currentThread().isVirtual();
//        }
//    };
}
