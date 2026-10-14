package o3_TOOLS_CLASSES;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * WONDERVILLE v2 - clickable colour card (emoji + title + hint) used by About and Help.
 * Everything is sized from the component height so it scales with the window.
 */
public class TOOL_CARD extends JComponent {

    private static final long serialVersionUID = 1L;

    private final String title, emoji, hint;
    private final Color accent;
    private float t = 0f;
    private boolean over;
    private final Timer anim;

    public TOOL_CARD(String title, String emoji, Color accent, String hint, Runnable action) {
        this.title = title; this.emoji = emoji; this.accent = accent; this.hint = hint;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(false);
        anim = new Timer(16, e -> {
            float target = over ? 1f : 0f;
            t += (target - t) * 0.3f;
            if (Math.abs(target - t) < 0.02f) { t = target; ((Timer) e.getSource()).stop(); }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { over = true;  anim.start(); }
            @Override public void mouseExited (MouseEvent e) { over = false; anim.start(); }
            @Override public void mouseClicked(MouseEvent e) { if (action != null) action.run(); }
        });
    }

    @Override public void removeNotify() { anim.stop(); super.removeNotify(); }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);
        int w = getWidth(), h = getHeight();
        float lift = h * 0.04f * t;
        float arc = h * 0.22f, sh = h * 0.05f;
        float y = -lift;

        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new RoundRectangle2D.Float(3, sh + 3, w - 6, h - sh - 3, arc, arc));
        Color base = TOOL_THEME.mix(TOOL_THEME.mix(accent, Color.WHITE, .35f), accent, t);
        g.setPaint(new GradientPaint(0, 0, TOOL_THEME.mix(base, Color.WHITE, .25f), 0, h, base));
        g.fill(new RoundRectangle2D.Float(2, y + 2, w - 4, h - sh - 2, arc, arc));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(2f, h * 0.03f)));
        g.draw(new RoundRectangle2D.Float(2, y + 2, w - 4, h - sh - 2, arc, arc));

        FontMetrics fm;
        g.setFont(TOOL_THEME.body(Font.PLAIN, h * 0.09f));
        fm = g.getFontMetrics();
        g.setColor(TOOL_THEME.alpha(TOOL_THEME.INK, 170));
        String hn = hint + "  \u25B6";
        g.drawString(hn, (w - fm.stringWidth(hn)) / 2, y + h * 0.17f);

        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, Math.max(12, (int) (h * 0.36f))));
        fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(emoji, (w - fm.stringWidth(emoji)) / 2, y + h * 0.56f);

        float fs = h * 0.15f;
        Font f = TOOL_THEME.display(Font.BOLD, fs);
        g.setFont(f); fm = g.getFontMetrics();
        while (fm.stringWidth(title) > w * 0.9f && fs > 9) { fs -= 1; g.setFont(f = f.deriveFont(fs)); fm = g.getFontMetrics(); }
        g.setColor(TOOL_THEME.INK);
        g.drawString(title, (w - fm.stringWidth(title)) / 2, y + h * 0.83f);
        g.dispose();
    }
}
