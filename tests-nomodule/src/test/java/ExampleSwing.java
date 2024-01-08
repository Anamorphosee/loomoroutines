import dev.reformator.loomoroutines.dispatcher.SwingDispatcher;
import dev.reformator.loomoroutines.dispatcher.VirtualThreadsDispatcher;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
        frame.setSize(600, 600);
        frame.setVisible(true);

        button.addActionListener(e -> dispatch(SwingDispatcher.INSTANCE, () -> {
            pickingCatCounter++;
            if (pickingCatCounter % 2 == 0) {
                button.setText("Pick another cat");
                return null;
            } else {
                button.setText("This one!");
                var cachedPickingCatCounter = pickingCatCounter;
                while (true) {
                    var bufferedImage = doIn(VirtualThreadsDispatcher.INSTANCE, () -> {
                        var bufferedImageMut = new MutableObject<BufferedImage>();
                        var thread = Thread.startVirtualThread(() -> {
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
                                bufferedImageMut.setValue(ImageIO.read(stream));
                            } catch (Throwable ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                        try {
                            thread.join();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        return bufferedImageMut.getValue();
                    });

                    if (pickingCatCounter != cachedPickingCatCounter) {
                        return null;
                    }

                    imagePanel.setImage(bufferedImage);

                    delay(Duration.ofSeconds(1));

                    if (pickingCatCounter != cachedPickingCatCounter) {
                        return null;
                    }
                }
            }
        }));
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
