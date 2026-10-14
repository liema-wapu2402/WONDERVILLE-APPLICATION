package o3_TOOLS_CLASSES;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * WONDERVILLE v2 loader. Same API as v1 (showUntilReady + WorkCallback), plus:
 *  - scales with the window (card, dots and text),
 *  - never flashes: stays up for at least MIN_SHOW_MS,
 *  - a failing background job can no longer leave the dialog stuck on screen.
 */
public class TOOL_LoadingDialog extends JDialog {

    private static final long serialVersionUID = 2L;
    private static final long MIN_SHOW_MS = 450;

    private static final Color[] DOT_BRIGHT = { TOOL_THEME.SKY, TOOL_THEME.SUN, TOOL_THEME.RUBY };

    private Timer animTimer;
    private int tick = 0;
    private final int FW, FH;
    private final double scale;
    private final String message;

    // ───────────────────────────── factory ─────────────────────────────
    public static void showUntilReady(JFrame parent, String message, WorkCallback work) {
        final TOOL_LoadingDialog dialog = new TOOL_LoadingDialog(parent, message);

        Thread worker = new Thread(() -> {
            long t0 = System.currentTimeMillis();
            Runnable uiUpdate = null;
            try {
                uiUpdate = work.execute();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            long wait = MIN_SHOW_MS - (System.currentTimeMillis() - t0);
            if (wait > 0) { try { Thread.sleep(wait); } catch (InterruptedException ignored) { } }

            final Runnable ui = uiUpdate;
            SwingUtilities.invokeLater(() -> {
                dialog.close();
                if (ui != null) ui.run();
            });
        }, "wonderville-loader");
        worker.setDaemon(true);
        worker.start();

        dialog.setVisible(true);     // modal: returns when close() is called
    }

    public interface WorkCallback {
        /** Runs OFF the Event Dispatch Thread. Return a Runnable to run on the EDT afterwards (or null). */
        Runnable execute();
    }

    // ───────────────────────────── dialog ─────────────────────────────
    public TOOL_LoadingDialog(JFrame parent, String message) {
        super(parent, true);
        this.message = message;

        Insets in = parent.getInsets();
        FW = Math.max(200, parent.getWidth()  - in.left - in.right);
        FH = Math.max(120, parent.getHeight() - in.top  - in.bottom);
        scale = Math.max(0.6, Math.min(FW / 1200.0, FH / 675.0));

        Point origin;
        try { origin = parent.getContentPane().getLocationOnScreen(); }
        catch (Exception e) { origin = parent.getLocation(); }

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(FW, FH);
        setLocation(origin.x, origin.y);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setContentPane(new OverlayPanel());

        animTimer = new Timer(16, e -> { tick++; repaint(); });
        animTimer.start();
    }

    public void close() {
        if (animTimer != null) animTimer.stop();
        dispose();
    }

    private class OverlayPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        OverlayPanel() { setOpaque(false); setPreferredSize(new Dimension(FW, FH)); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            TOOL_THEME.smooth(g2);

            // soft veil in the theme colours
            g2.setPaint(new GradientPaint(0, 0, TOOL_THEME.alpha(TOOL_THEME.SKY, 150),
                                          FW, FH, TOOL_THEME.alpha(TOOL_THEME.RUBY, 150)));
            g2.fillRect(0, 0, FW, FH);
            g2.setPaint(new GradientPaint(FW * 0.1f, 0, TOOL_THEME.alpha(TOOL_THEME.SUN, 70),
                                          FW * 0.7f, 0, TOOL_THEME.alpha(TOOL_THEME.SUN, 0)));
            g2.fillRect(0, 0, FW, FH);

            // card
            int cw = (int) (260 * scale), ch = (int) (160 * scale);
            int cx = (FW - cw) / 2, cy = (FH - ch) / 2, arc = (int) (44 * scale);
            g2.setColor(new Color(0, 0, 0, 45));
            g2.fill(new RoundRectangle2D.Float(cx + 3, cy + 7, cw, ch, arc, arc));
            g2.setColor(new Color(255, 255, 255, 250));
            g2.fill(new RoundRectangle2D.Float(cx, cy, cw, ch, arc, arc));
            g2.setColor(TOOL_THEME.alpha(TOOL_THEME.RUBY, 170));
            g2.setStroke(new BasicStroke(Math.max(2f, (float) (3 * scale))));
            g2.draw(new RoundRectangle2D.Float(cx + 1.5f, cy + 1.5f, cw - 3, ch - 3, arc, arc));

            // bouncing dots
            int dot = (int) (22 * scale), gap = (int) (18 * scale), bounce = (int) (24 * scale);
            int totalW = 3 * dot + 2 * gap;
            int dx = cx + (cw - totalW) / 2;
            int baseY = cy + ch / 2 - (int) (14 * scale);
            for (int i = 0; i < 3; i++) {
                double s = Math.sin(tick * 0.09 - i * 0.65);
                double up = s > 0 ? s : 0;
                int y = baseY - (int) (up * bounce);
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillOval(dx + i * (dot + gap) + 2, baseY + dot + 4, dot - 4, Math.max(3, dot / 4));   // ground shadow
                g2.setColor(up > 0.35 ? DOT_BRIGHT[i] : TOOL_THEME.alpha(DOT_BRIGHT[i], 110));
                g2.fillOval(dx + i * (dot + gap), y, dot, dot);
            }

            // message
            g2.setColor(TOOL_THEME.RUBY);
            Font f = TOOL_THEME.display(Font.BOLD, (float) Math.max(12, 20 * scale));
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(message, cx + (cw - fm.stringWidth(message)) / 2, baseY + dot + (int) (44 * scale));
            g2.dispose();
        }
    }
}
