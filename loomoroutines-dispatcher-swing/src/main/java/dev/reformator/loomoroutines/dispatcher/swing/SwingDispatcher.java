package dev.reformator.loomoroutines.dispatcher.swing;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SwingDispatcher implements Dispatcher {
    private SwingDispatcher() { }

    public static final SwingDispatcher instance = new SwingDispatcher();
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    @Override
    public void execute(@NotNull Runnable action) {
        SwingUtilities.invokeLater(action);
    }

    @Override
    public void scheduleExecute(@NotNull Duration duration, @NotNull Runnable action) {
        scheduledExecutor.schedule(
                () -> SwingUtilities.invokeLater(action),
                duration.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean canExecuteInCurrentThread() {
        return SwingUtilities.isEventDispatchThread();
    }
}
