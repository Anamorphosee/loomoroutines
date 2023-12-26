package test;

import dev.reformator.loomoroutines.common.internal.utils.CommonUtils;
import dev.reformator.loomoroutines.dispatcher.Dispatcher;
import dev.reformator.loomoroutines.dispatcher.test.swing.SwingDispatcher;
import dev.reformator.loomoroutines.dispatcher.utils.DispatcherUtils;

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
            button.setText(DispatcherUtils.executeInDispatcher(Dispatcher.VirtualThread, () -> {
                String str;
                try {
                   var bytes = new URL("https://raw.githubusercontent.com/Anamorphosee/stacktrace-decoroutinator/master/README.md")
                           .openStream()
                           .readAllBytes();
                   str = new String(bytes);
                } catch (IOException e) {
                    throw CommonUtils.throwUnchecked(e);
                }
                return str;
            }));
            DispatcherUtils.delay(Duration.ofSeconds(3));
            mainFrame.setTitle("3 SECONDS PAST!");
        }));
        mainFrame.add(button);
        mainFrame.setVisible(true);
    }
}
