package o3_TOOLS_CLASSES;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * WONDERVILLE v2 - animated GIF "stage".
 *
 * What changed compared to v1:
 *  - STREAMING: v1 decoded every GIF frame into a BufferedImage (a 400-frame 1200x675 GIF = ~1.3 GB),
 *    which is what killed the JVM in hs_err_pid*.log. v2 lets the AWT GIF decoder keep only the
 *    current frame in memory (a few MB per screen) and honours the delay stored in the GIF.
 *  - RESPONSIVE: the GIF is drawn at its 16:9 aspect ratio (no more stretching), smoothly scaled,
 *    centred ("stage"), with a themed backdrop in the letter-box bars. Children are laid out in
 *    1200x675 "stage coordinates" and scaled with the window (see place()).
 *  - SMOOTH SWITCHING: screens cross-fade from a snapshot of the previous screen.
 *  - TOAST bubble for "coming soon" messages.
 */
public class TOOL_GIF_PANEL extends JPanel {

    private static final long serialVersionUID = 2L;

    /** Reference ("design") size - the size of the artwork. All place() coordinates use it. */
    public static final int REF_W = 1200;
    public static final int REF_H = 675;

    private static final String STAGE_KEY = "wonderville.stage";
    private static final String FULL_KEY  = "wonderville.full";

    /** Children that need to know the current scale (to size fonts) implement this. */
    public interface StageAware { void stageScaled(double scale); }

    private final URL gifUrl;
    private TOOL_GIF_PLAYER player;
    private volatile boolean closed;
    private boolean missing;

    // fade-in from the previous screen
    private BufferedImage fadeFrom;
    private float fadeAlpha;
    private Timer fadeTimer;

    // toast
    private String toastText;
    private float toastAlpha;
    private Timer toastTimer;
    private long toastUntil;

    // letter-box backdrop cache
    private BufferedImage backdrop;

    public TOOL_GIF_PANEL(URL gifUrl) {
        this.gifUrl = gifUrl;
        setLayout(new StageLayout());
        setOpaque(true);
        setBackground(new Color(255, 245, 225));
        setPreferredSize(new Dimension(REF_W, REF_H));
        setDoubleBuffered(true);
    }

    // ═════════════════════════════ factories ═════════════════════════════

    /**
     * Creates the panel and waits (max ~6 s) until the first frame is decoded.
     * Safe to call from a worker thread (it does not touch any Swing component).
     */
    public static TOOL_GIF_PANEL prepare(String gifPath) {
        TOOL_GIF_PANEL p = new TOOL_GIF_PANEL(TOOL_GIF_PANEL.class.getResource(gifPath));
        if (p.gifUrl == null) {
            System.err.println("[TOOL_GIF_PANEL] resource not found: " + gifPath);
            p.missing = true;
        } else {
            p.load();
        }
        return p;
    }

    /** v1-compatible one-liner: prepare + attach. Must be called on the EDT. */
    public static TOOL_GIF_PANEL set(JFrame frame, String gifPath, int width, int height) {
        TOOL_GIF_PANEL p = prepare(gifPath);
        p.attachTo(frame);
        return p;
    }

    /** Makes this panel the frame's content pane (EDT only) and releases the previous GIF. */
    public void attachTo(JFrame frame) {
        Container old = frame.getContentPane();
        if (old instanceof TOOL_GIF_PANEL) {
            TOOL_GIF_PANEL prev = (TOOL_GIF_PANEL) old;
            if (prev.getWidth() > 0 && prev.getHeight() > 0) fadeFrom = prev.snapshot();
            prev.unload();
        }
        frame.setContentPane(this);
        setFocusable(true);
        frame.getRootPane().revalidate();
        frame.getRootPane().repaint();
        startFade();
        requestFocusInWindow();            // so no button shows a keyboard-focus ring until the user presses Tab
    }

    // ═════════════════════════════ GIF loading ═════════════════════════════

    private void load() {
        player = new TOOL_GIF_PLAYER(gifUrl, true, new Color(150, 215, 245), () -> {
            if (!closed) { Rectangle r = stageRect(); repaint(0, r.x, r.y, r.width, r.height); }
        });
        player.start();
        long end = System.currentTimeMillis() + 8000;           // wait for the first frame
        while (System.currentTimeMillis() < end && player.getFrame() == null) {
            try { Thread.sleep(15); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    public boolean isLoaded() { return player != null && !closed; }

    /** Releases the GIF decoder + any bitmaps. Called automatically when another screen replaces this one. */
    public void unload() {
        closed = true;
        if (fadeTimer != null)  fadeTimer.stop();
        if (toastTimer != null) toastTimer.stop();
        if (player != null) { player.stop(); player = null; }
        fadeFrom = null;
        backdrop = null;
    }

    public void stopAnimation()  { /* kept for v1 compatibility - streaming GIFs pause when not painted */ }
    public void startAnimation() { repaint(); }

    // ═════════════════════════════ stage maths ═════════════════════════════

    public double scale() {
        if (getWidth() <= 0 || getHeight() <= 0) return 1.0;
        return Math.min(getWidth() / (double) REF_W, getHeight() / (double) REF_H);
    }

    /** The 16:9 rectangle the GIF occupies inside the panel (letter-boxed). */
    public Rectangle stageRect() {
        double s = scale();
        int w = (int) Math.round(REF_W * s), h = (int) Math.round(REF_H * s);
        return new Rectangle((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
    }

    /** Adds a child positioned in 1200x675 stage coordinates; it follows the window size. */
    public <T extends Component> T place(T c, int x, int y, int w, int h) {
        ((JComponent) c).putClientProperty(STAGE_KEY, new Rectangle(x, y, w, h));
        add(c, 0);          // Swing paints index 0 LAST, so later place() calls end up on top
        revalidate();       // components added after the screen is shown need a layout pass
        return c;
    }

    /** Adds a full-window overlay (modal cards, dialogs) on top of everything else. */
    public void addOverlay(JComponent c) {
        c.putClientProperty(FULL_KEY, Boolean.TRUE);
        add(c, 0);                       // index 0 = top of the z-order
        revalidate();
        repaint();
        c.requestFocusInWindow();
    }

    public void removeOverlay(JComponent c) {
        remove(c);
        revalidate();
        repaint();
    }

    /** Re-position an already placed child (stage coordinates). */
    public void move(JComponent c, int x, int y, int w, int h) {
        c.putClientProperty(STAGE_KEY, new Rectangle(x, y, w, h));
        revalidate();
    }

    private static final class StageLayout implements LayoutManager2 {
        public void addLayoutComponent(Component c, Object constraints) {}
        public void addLayoutComponent(String name, Component c) {}
        public void removeLayoutComponent(Component c) {}
        public Dimension preferredLayoutSize(Container p) { return new Dimension(REF_W, REF_H); }
        public Dimension minimumLayoutSize(Container p)   { return new Dimension(640, 360); }
        public Dimension maximumLayoutSize(Container p)   { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }
        public float getLayoutAlignmentX(Container p) { return .5f; }
        public float getLayoutAlignmentY(Container p) { return .5f; }
        public void invalidateLayout(Container p) {}
        public void layoutContainer(Container parent) {
            TOOL_GIF_PANEL p = (TOOL_GIF_PANEL) parent;
            Rectangle st = p.stageRect();
            double s = p.scale();
            for (Component c : p.getComponents()) {
                if (!(c instanceof JComponent)) continue;
                if (Boolean.TRUE.equals(((JComponent) c).getClientProperty(FULL_KEY))) {
                    if (c instanceof StageAware) ((StageAware) c).stageScaled(s);
                    c.setBounds(0, 0, p.getWidth(), p.getHeight());
                    continue;
                }
                Object o = ((JComponent) c).getClientProperty(STAGE_KEY);
                if (!(o instanceof Rectangle)) continue;
                Rectangle r = (Rectangle) o;
                if (c instanceof StageAware) ((StageAware) c).stageScaled(s);
                c.setBounds(st.x + (int) Math.round(r.x * s), st.y + (int) Math.round(r.y * s),
                            (int) Math.round(r.width * s),   (int) Math.round(r.height * s));
            }
        }
    }

    // ═════════════════════════════ painting ═════════════════════════════

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        int W = getWidth(), H = getHeight();
        Rectangle r = stageRect();

        // letter-box bars (only when the window is not 16:9)
        if (r.width < W || r.height < H) g.drawImage(backdrop(W, H), 0, 0, null);

        BufferedImage frame = player == null ? null : player.getFrame();
        if (frame != null && !missing) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(frame, r.x, r.y, r.width, r.height, null);
        } else {
            g.setPaint(new GradientPaint(0, 0, TOOL_THEME.MAYA_BLUE, W, H, TOOL_THEME.RUBY));
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        g.dispose();
    }

    @Override
    protected void paintChildren(Graphics g0) {
        super.paintChildren(g0);
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);

        // cross-fade overlay (previous screen)
        if (fadeFrom != null && fadeAlpha > 0f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, fadeAlpha)));
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(fadeFrom, 0, 0, getWidth(), getHeight(), null);
            g.setComposite(AlphaComposite.SrcOver);
        }

        // toast bubble
        if (toastText != null && toastAlpha > 0f) {
            double s = scale();
            Rectangle st = stageRect();
            Font f = TOOL_THEME.display(Font.BOLD, (float) Math.max(12, 26 * s));
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            int padX = (int) (28 * s), padY = (int) (14 * s);
            int tw = fm.stringWidth(toastText);
            int bw = tw + padX * 2, bh = fm.getHeight() + padY;
            int bx = st.x + (st.width - bw) / 2;
            int by = st.y + st.height - bh - (int) (46 * s);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, toastAlpha)));
            g.setColor(new Color(0, 0, 0, 50));
            g.fill(new RoundRectangle2D.Float(bx + 2, by + 4, bw, bh, bh, bh));
            g.setColor(TOOL_THEME.RUBY);
            g.fill(new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh));
            g.setColor(TOOL_THEME.SUN);
            g.setStroke(new java.awt.BasicStroke(Math.max(2f, (float) (3 * s))));
            g.draw(new RoundRectangle2D.Float(bx + 1, by + 1, bw - 2, bh - 2, bh, bh));
            g.setColor(Color.WHITE);
            g.drawString(toastText, bx + padX, by + padY / 2 + fm.getAscent());
        }
        g.dispose();
    }

    private BufferedImage backdrop(int W, int H) {
        if (backdrop != null && backdrop.getWidth() == W && backdrop.getHeight() == H) return backdrop;
        BufferedImage bi = new BufferedImage(Math.max(1, W), Math.max(1, H), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        TOOL_THEME.smooth(g);
        g.setPaint(new GradientPaint(0, 0, TOOL_THEME.MAYA_BLUE, W, H, TOOL_THEME.RUBY));
        g.fillRect(0, 0, W, H);
        Random rnd = new Random(7);
        Color[] cs = { TOOL_THEME.SUN, TOOL_THEME.SKY, Color.WHITE, TOOL_THEME.ORANGE };
        for (int i = 0; i < 36; i++) {
            int d = 18 + rnd.nextInt(60);
            g.setColor(TOOL_THEME.alpha(cs[rnd.nextInt(cs.length)], 70));
            g.fillOval(rnd.nextInt(Math.max(1, W)), rnd.nextInt(Math.max(1, H)), d, d);
        }
        g.dispose();
        backdrop = bi;
        return bi;
    }

    // ═════════════════════════════ fade + toast ═════════════════════════════

    private BufferedImage snapshot() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        paint(g);
        g.dispose();
        return bi;
    }

    private void startFade() {
        if (fadeFrom == null) return;
        fadeAlpha = 1f;
        final long t0 = System.currentTimeMillis();
        fadeTimer = new Timer(16, null);
        fadeTimer.addActionListener(e -> {
            float p = (System.currentTimeMillis() - t0) / 380f;
            fadeAlpha = 1f - Math.min(1f, p);
            if (p >= 1f) { fadeFrom = null; fadeTimer.stop(); }
            repaint();
        });
        fadeTimer.start();
    }

    /** Small rounded message at the bottom of the stage (e.g. "Coming soon!"). */
    public void showToast(String text) {
        toastText = text;
        toastAlpha = 1f;
        toastUntil = System.currentTimeMillis() + 1700;
        if (toastTimer != null) toastTimer.stop();
        toastTimer = new Timer(30, e -> {
            long left = toastUntil - System.currentTimeMillis();
            toastAlpha = left > 300 ? 1f : Math.max(0f, left / 300f);
            if (left <= 0) { toastText = null; toastTimer.stop(); }
            repaint();
        });
        toastTimer.start();
        repaint();
    }
}
