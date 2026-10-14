package o3_TOOLS_CLASSES;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * WONDERVILLE v2 - memory-safe animated GIF player.
 *
 * v1 decoded EVERY frame up front (400 frames x 1200x675 x 4 bytes = ~1.3 GB -> OutOfMemoryError in
 * hs_err_pid*.log). This player streams: a daemon thread decodes one frame at a time, composes it on a
 * single "master" canvas (honouring GIF disposal methods and transparency) and publishes it as the
 * current frame, then waits for the delay stored in the GIF. Memory = 3 canvases, whatever the length.
 */
public class TOOL_GIF_PLAYER implements Runnable {

    private final URL url;
    private final boolean loop;
    private final Color background;          // null = keep transparency (icons), colour = opaque (backgrounds)
    private final Runnable onFrame;

    private volatile BufferedImage front;
    private BufferedImage back, master, saved;
    private volatile boolean stopped;
    private Thread thread;

    public TOOL_GIF_PLAYER(URL url, boolean loop, Color background, Runnable onFrame) {
        this.url = url; this.loop = loop; this.background = background; this.onFrame = onFrame;
    }

    public void start() {
        thread = new Thread(this, "wonderville-gif");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        stopped = true;
        if (thread != null) thread.interrupt();
        front = null; back = null; master = null; saved = null;
    }

    /** Latest composed frame, or null until the first frame is ready. */
    public BufferedImage getFrame() { return front; }

    @Override
    public void run() {
        try {
            boolean any;
            do { any = playOnce(); } while (loop && any && !stopped);
        } catch (Throwable t) {
            if (!stopped) t.printStackTrace();
        }
    }

    private ImageInputStream open() throws Exception {
        if ("file".equals(url.getProtocol())) return ImageIO.createImageInputStream(new File(url.toURI()));
        return ImageIO.createImageInputStream(url.openStream());
    }

    private boolean playOnce() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        boolean any = false;
        try (ImageInputStream in = open()) {
            reader.setInput(in, false, false);
            int prevDisposal = 0;
            Rectangle prevRect = new Rectangle();
            for (int i = 0; !stopped; i++) {
                long t0 = System.nanoTime();
                BufferedImage frame;
                try { frame = reader.read(i); } catch (IndexOutOfBoundsException end) { break; }
                IIOMetadata md = reader.getImageMetadata(i);

                int ox = 0, oy = 0, delay = 10; String disposal = "none";
                Node tree = md.getAsTree("javax_imageio_gif_image_1.0");
                NodeList kids = tree.getChildNodes();
                for (int k = 0; k < kids.getLength(); k++) {
                    Node n = kids.item(k); NamedNodeMap at = n.getAttributes();
                    if ("ImageDescriptor".equals(n.getNodeName())) {
                        ox = Integer.parseInt(at.getNamedItem("imageLeftPosition").getNodeValue());
                        oy = Integer.parseInt(at.getNamedItem("imageTopPosition").getNodeValue());
                    } else if ("GraphicControlExtension".equals(n.getNodeName())) {
                        disposal = at.getNamedItem("disposalMethod").getNodeValue();
                        delay = Integer.parseInt(at.getNamedItem("delayTime").getNodeValue());
                    }
                }
                if (delay <= 1) delay = 10;                           // same rule browsers use

                if (master == null || i == 0) {
                    if (master == null) master = new BufferedImage(reader.getWidth(0), reader.getHeight(0), BufferedImage.TYPE_INT_ARGB);
                    clear(master, null);
                    prevDisposal = 0;
                }
                // 1. dispose the PREVIOUS frame
                if (prevDisposal == 2) clear(master, prevRect);                       // restoreToBackgroundColor
                else if (prevDisposal == 3 && saved != null) restore(master, saved); // restoreToPrevious
                // 2. remember canvas if THIS frame must be undone afterwards
                if ("restoreToPrevious".equals(disposal)) {
                    if (saved == null) saved = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    restore(saved, master);
                }
                // 3. draw the frame
                Graphics2D g = master.createGraphics();
                g.drawImage(frame, ox, oy, null);
                g.dispose();
                prevRect = new Rectangle(ox, oy, frame.getWidth(), frame.getHeight());
                prevDisposal = "restoreToBackgroundColor".equals(disposal) ? 2 : "restoreToPrevious".equals(disposal) ? 3 : 0;

                publish();
                any = true;
                if (onFrame != null) onFrame.run();

                long spent = (System.nanoTime() - t0) / 1000000L;
                long wait = delay * 10L - spent;
                if (wait > 0) { try { Thread.sleep(wait); } catch (InterruptedException e) { if (stopped) break; } }
            }
        } finally {
            reader.dispose();
        }
        return any;
    }

    private void publish() {
        BufferedImage b = back;
        if (b == null || b.getWidth() != master.getWidth() || b.getHeight() != master.getHeight())
            b = new BufferedImage(master.getWidth(), master.getHeight(),
                                  background == null ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = b.createGraphics();
        if (background == null) { g.setComposite(AlphaComposite.Clear); g.fillRect(0, 0, b.getWidth(), b.getHeight()); g.setComposite(AlphaComposite.SrcOver); }
        else { g.setColor(background); g.fillRect(0, 0, b.getWidth(), b.getHeight()); }
        g.drawImage(master, 0, 0, null);
        g.dispose();
        back = front;      // the old front becomes the next back buffer
        front = b;
    }

    private static void clear(BufferedImage img, Rectangle r) {
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        if (r == null) g.fillRect(0, 0, img.getWidth(), img.getHeight()); else g.fillRect(r.x, r.y, r.width, r.height);
        g.dispose();
    }

    private static void restore(BufferedImage dst, BufferedImage src) {
        Graphics2D g = dst.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, null);
        g.dispose();
    }
}
