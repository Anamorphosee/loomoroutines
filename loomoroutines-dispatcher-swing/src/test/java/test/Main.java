package test;

import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.DispatcherUtils;
import dev.reformator.loomoroutines.dispatcher.test.swing.SwingDispatcher;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("Main");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 500);
        var button = new JButton("Test");
        button.addActionListener(event -> DispatcherUtils.dispatch(SwingDispatcher.instance, () -> {
            button.setText("CLICKED!");
            button.setText(DispatcherUtils.doIn(Dispatcher.virtualThreads, () -> {
                String str;
                try {
                    var bytes = new URL("https://raw.githubusercontent.com/Anamorphosee/stacktrace-decoroutinator/master/README.md")
                            .openStream()
                            .readAllBytes();
                    str = new String(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return str;
            }));
            DispatcherUtils.delay(Duration.ofSeconds(3));
            mainFrame.setTitle("3 SECONDS PAST!");
            DispatcherUtils.delay(Duration.ofSeconds(3));
            mainFrame.setTitle("Over!");
            button.setText("Over!");
            return null;
        }));
        mainFrame.add(button);
        mainFrame.setVisible(true);
    }
}
