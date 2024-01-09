![Maven Central](https://img.shields.io/maven-central/v/dev.reformator.loomoroutines/loomoroutines-common)
# Loomoroutines
Library for native Java coroutines utilizing [Project Loom](https://openjdk.org/projects/loom/).

Supports JDK 19 or higher.

## Motivation
Project Loom brings [Virtual Threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) which is very valuable in server-side development.
But its internal implementation allows to create full-fledged [coroutines](https://en.wikipedia.org/wiki/Coroutine), which allows to write asynchronous code in the regular synchronous style, which can be useful for GUI applications.
This library provides API for those native Java coroutines.

## Usage
Loomoroutines consists of 3 artifacts:

### Loomoroutines Dispatcher API
The dependency is `dev.reformator.loomoroutines:loomoroutines-dispatcher:1.0.0`. It implements The Dispatcher Pattern and allows to switch between different dispatchers during code executions.
The API is contained in the utility class `dev.reformator.loomoroutines.dispatcher.DispatcherUtils`. Developers can implement their own dispatchers by implementing the interface `dev.reformator.loomoroutines.dispatcher.Dispatcher`.
Usage example in a GUI application:
```java
import dev.reformator.loomoroutines.dispatcher.SwingDispatcher;
import dev.reformator.loomoroutines.dispatcher.VirtualThreadsDispatcher;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.regex.Pattern;

import static dev.reformator.loomoroutines.dispatcher.DispatcherUtils.*;

public class ExampleSwing {
    private static int pickingCatCounter = 0;

    private static final Pattern urlPattern = Pattern.compile("\"url\":\"([^\"]+)\"");

    public static void main(String[] args) {
        var frame = new JFrame("Cats");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        var button = new JButton("Pick a cat");
        var imagePanel = new ImagePanel();
        panel.add(button);
        panel.add(imagePanel);
        frame.add(panel);
        frame.setSize(1000, 500);
        frame.setVisible(true);

        button.addActionListener(e -> dispatch(SwingDispatcher.INSTANCE, () -> {
            pickingCatCounter++;
            if (pickingCatCounter % 2 == 0) {
                button.setText("Pick another cat");
                return null;
            } else {
                button.setText("This one!");
                var cachedPickingCatCounter = pickingCatCounter;

                try {
                    while (true) {
                        var bufferedImage = doIn(VirtualThreadsDispatcher.INSTANCE, ExampleSwing::loadCatImage);
                        if (pickingCatCounter != cachedPickingCatCounter) {
                            return null;
                        }

                        imagePanel.setImage(bufferedImage);
                        delay(Duration.ofSeconds(1));

                        if (pickingCatCounter != cachedPickingCatCounter) {
                            return null;
                        }
                    }
                } catch (Throwable ex) {
                    if (pickingCatCounter == cachedPickingCatCounter) {
                        ex.printStackTrace();
                        pickingCatCounter++;
                        button.setText("Exception: " + ex.getMessage() + ". Try again?");
                    }
                    return null;
                }
            }
        }));
    }

    private static BufferedImage loadCatImage() {
        String url;
        {
            String json;
            try (var stream = URI.create("https://api.thecatapi.com/v1/images/search").toURL().openStream()) {
                json = new String(stream.readAllBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            var mather = urlPattern.matcher(json);
            if (!mather.find()) {
                throw new RuntimeException("cat url is not found in json '" + json + "'");
            }
            url = mather.group(1);
        }
        try (var stream = URI.create(url).toURL().openStream()) {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

class ImagePanel extends JPanel {
    private BufferedImage image = null;

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }
}
```
Pay attention that the button click event handler contains potentionally long-running operations(like loading image through the Internet), but it executes them in a different dispatcher so the UI thread is not blocking.

### Loomoroutines common API
The dependency is `dev.reformator.loomoroutines:loomoroutines-common:1.0.0`.
Contains low-level coroutines API. Most of which is located in the utility class `dev.reformator.loomoroutines.common.CoroutineUtils`.
Moreover, there is an implementation of the Generator Pattern. Example:
```java
import java.math.BigInteger;

import static dev.reformator.loomoroutines.common.GeneratorUtils.loomStream;

public class ExampleGenerator {
    public static void main(String[] args) {
        var fibinacciStream = loomStream(scope -> {
            var previous = BigInteger.ZERO;
            var current = BigInteger.ONE;
            while (true) {
                scope.emit(current);
                var tmp = previous.add(current);
                previous = current;
                current = tmp;
            }
        });
        fibinacciStream.limit(50).forEach(System.out::println);
    }
}
```

### Support library for bypassing JPMS
To use Loomoroutines you have to add JVM command line argument `--add-exports java.base/jdk.internal.vm=ALL-UNNAMED` or add this artifact in the classpath or modulepath.
The corresponding dependency is `dev.reformator.loomoroutines:loomoroutines-bypassjpms:1.0.0`.
