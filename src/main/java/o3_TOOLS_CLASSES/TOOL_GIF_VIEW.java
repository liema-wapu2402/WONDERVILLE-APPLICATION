package o3_TOOLS_CLASSES;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.JComponent;

/**
 * WONDERVILLE v2 - small animated-GIF viewer for result icons (correct / incorrect).
 * Fits the GIF inside its bounds (keeps aspect ratio, smooth scaling, keeps transparency), restarts
 * from the first frame on every play() (a plain JLabel/ImageIcon never restarts) and frees its
 * decoder on stop().
 */
public class TOOL_GIF_VIEW extends JComponent {

    private static final long serialVersionUID = 1L;

    private final URL url;
    private TOOL_GIF_PLAYER player;

    public TOOL_GIF_VIEW(String path) {
        this.url = TOOL_GIF_VIEW.class.getResource(path);
        setOpaque(false);
    }

    public void play() {
        stop();
        if (url == null) return;
        player = new TOOL_GIF_PLAYER(url, true, null, () -> repaint());
        player.start();
        setVisible(true);
    }

    public void stop() {
        if (player != null) { player.stop(); player = null; }
        repaint();
    }

    @Override public void removeNotify() { stop(); super.removeNotify(); }

    @Override
    protected void paintComponent(Graphics g0) {
        BufferedImage img = player == null ? null : player.getFrame();
        if (img == null) return;
        double s = Math.min(getWidth() / (double) img.getWidth(), getHeight() / (double) img.getHeight());
        int w = (int) Math.round(img.getWidth() * s), h = (int) Math.round(img.getHeight() * s);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        g.dispose();
    }
}
