package o3_TOOLS_CLASSES;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * WONDERVILLE v2 pill button - 100% drawn in code (no PNGs).
 *
 * Look (copied from the button artwork):
 *   - candy-orange pill with a darker orange bottom rim, pink label     (ORANGE)
 *   - light grey-blue pill with a pale-blue bottom rim, orange label    (LIGHT)
 *   - hot-orange pill with white label + yellow chevrons (primary)      (HOT)
 * A button rests in one skin and eases into the "opposite" skin on hover,
 * sinks 2-3 px when pressed and scales its label/icon with its own height,
 * so it stays crisp at any window size.
 */
public class TOOL_BUTTON extends JButton {

    private static final long serialVersionUID = 1L;

    public enum Skin {
        LIGHT (TOOL_THEME.LIGHT_TOP,  TOOL_THEME.LIGHT,  TOOL_THEME.LIGHT_RIM,  TOOL_THEME.ORANGE_TEXT, TOOL_THEME.RUBY),
        ORANGE(TOOL_THEME.ORANGE_TOP, TOOL_THEME.ORANGE, TOOL_THEME.ORANGE_RIM, TOOL_THEME.RUBY,        TOOL_THEME.RUBY),
        HOT   (new Color(247, 156, 88), new Color(241, 130, 68), new Color(236, 78, 77), Color.WHITE,   TOOL_THEME.SUN),
        GREEN (new Color(120, 220, 150), new Color( 70, 190, 110), new Color( 40, 140,  80), Color.WHITE, Color.WHITE),
        RED   (new Color(255, 120, 130), new Color(235,  70,  90), new Color(180,  40,  60), Color.WHITE, Color.WHITE);

        final Color top, body, rim, text, icon;
        Skin(Color top, Color body, Color rim, Color text, Color icon) {
            this.top = top; this.body = body; this.rim = rim; this.text = text; this.icon = icon;
        }
    }

    public enum Icon { NONE, BACK, NEXT, NEXT2, PREV, RETRY, CHECK }

    private Skin rest;
    private Skin hover;
    private Icon icon;
    private float t = 0f;            // 0 = resting skin, 1 = hover skin
    private float press = 0f;        // 0..1 sink
    private boolean keyboardFocus;
    private final Timer anim;

    public TOOL_BUTTON(String text) { this(text, Icon.NONE, Skin.LIGHT); }

    public TOOL_BUTTON(String text, Icon icon) { this(text, icon, Skin.LIGHT); }

    public TOOL_BUTTON(String text, Icon icon, Skin rest) {
        super(text);
        this.icon  = icon;
        setSkin(rest);

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setBorder(null);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setFocusable(true);
        setMargin(new Insets(0, 0, 0, 0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(TOOL_THEME.display(Font.BOLD, 20f));

        anim = new Timer(16, e -> step());
        getModel().addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) { if (!anim.isRunning()) anim.start(); }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { keyboardFocus = false; }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                keyboardFocus = !getModel().isPressed(); repaint();
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) { keyboardFocus = false; repaint(); }
        });
    }

    public TOOL_BUTTON setSkin(Skin rest) {
        this.rest  = rest;
        this.hover = (rest == Skin.GREEN || rest == Skin.RED) ? rest
                   : (rest == Skin.LIGHT) ? Skin.ORANGE : Skin.LIGHT;
        repaint();
        return this;
    }

    public TOOL_BUTTON setIconType(Icon icon) { this.icon = icon; repaint(); return this; }

    // ── animation ───────────────────────────────────────────────────────────
    private void step() {
        boolean over    = isEnabled() && (getModel().isRollover() || getModel().isArmed());
        boolean pressed = isEnabled() && getModel().isArmed();
        float tt = over ? 1f : 0f, pp = pressed ? 1f : 0f;
        t     += (tt - t) * 0.35f;
        press += (pp - press) * 0.5f;
        if (Math.abs(tt - t) < 0.02f) t = tt;
        if (Math.abs(pp - press) < 0.02f) press = pp;
        repaint();
        if (t == tt && press == pp) anim.stop();
    }

    @Override public void removeNotify() { anim.stop(); super.removeNotify(); }

    // Only the pill itself is clickable, not the transparent corners.
    @Override public boolean contains(int x, int y) {
        return new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()).contains(x, y);
    }

    @Override public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int h = 52;
        int w = fm.stringWidth(getText() == null ? "" : getText()) + h + (icon != Icon.NONE ? h / 2 : 0);
        return new Dimension(Math.max(w, 120), h);
    }

    // ── painting ────────────────────────────────────────────────────────────
    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);

        int w = getWidth(), h = getHeight();
        boolean on = isEnabled();
        float depth = Math.max(3f, h * 0.075f);               // thickness of the bottom rim
        float shadow = Math.max(2f, h * 0.05f);               // soft drop shadow
        float sink = depth * 0.6f * press;                    // pressed look
        float ph = h - depth - shadow;                        // pill face height
        float x = 1f, pw = w - 2f;
        float y = sink;

        Color top  = TOOL_THEME.mix(rest.top,  hover.top,  t);
        Color body = TOOL_THEME.mix(rest.body, hover.body, t);
        Color rim  = TOOL_THEME.mix(rest.rim,  hover.rim,  t);
        Color txt  = TOOL_THEME.mix(rest.text, hover.text, t);
        Color ico  = TOOL_THEME.mix(rest.icon, hover.icon, t);
        if (!on) {
            Color gray = new Color(190, 190, 198);
            top = TOOL_THEME.mix(top, gray, .6f); body = TOOL_THEME.mix(body, gray, .6f);
            rim = TOOL_THEME.mix(rim, gray, .6f); txt  = new Color(150, 150, 160); ico = txt;
        }

        // drop shadow
        g.setColor(new Color(0, 0, 0, 38));
        g.fill(new RoundRectangle2D.Float(x + 1, y + depth + shadow, pw - 2, ph, ph, ph));

        // rim (bottom edge)
        g.setColor(rim);
        g.fill(new RoundRectangle2D.Float(x, y + depth, pw, ph, ph, ph));

        // face
        RoundRectangle2D face = new RoundRectangle2D.Float(x, y, pw, ph, ph, ph);
        g.setPaint(new GradientPaint(0, y, top, 0, y + ph, body));
        g.fill(face);

        // glossy highlight line along the upper edge
        g.setColor(new Color(255, 255, 255, on ? 95 : 40));
        g.setStroke(new BasicStroke(Math.max(1.2f, h * 0.025f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new RoundRectangle2D.Float(x + ph * 0.12f, y + ph * 0.07f, pw - ph * 0.24f, ph * 0.86f, ph * 0.86f, ph * 0.86f));

        // thin darker outline for definition on busy backgrounds
        g.setColor(new Color(rim.getRed(), rim.getGreen(), rim.getBlue(), 110));
        g.setStroke(new BasicStroke(1f));
        g.draw(face);

        // ── label + icon, centred as one group ──
        String text = getText() == null ? "" : getText();
        float fs = Math.max(10f, ph * 0.46f);
        Font f = getFont().deriveFont(Font.BOLD, fs);
        FontMetrics fm = g.getFontMetrics(f);
        float icoSize = icon == Icon.NONE ? 0 : ph * 0.46f;
        float gap     = icon == Icon.NONE || text.isEmpty() ? 0 : ph * 0.16f;
        float avail   = pw - ph * 0.6f;
        while (fs > 9f && fm.stringWidth(text) + icoSize + gap > avail) {   // shrink to fit
            fs -= 1f; f = f.deriveFont(fs); fm = g.getFontMetrics(f);
        }
        float tw = fm.stringWidth(text);
        float groupW = icoSize + gap + tw;
        float gx = x + (pw - groupW) / 2f;
        float cy = y + ph / 2f;

        boolean iconLeft = icon == Icon.BACK || icon == Icon.PREV || icon == Icon.RETRY || icon == Icon.CHECK;
        float tx = iconLeft ? gx + icoSize + gap : gx;
        float ix = iconLeft ? gx : gx + tw + gap;

        g.setFont(f);
        g.setColor(txt);
        g.drawString(text, tx, cy + (fm.getAscent() - fm.getDescent()) / 2f);

        if (icon != Icon.NONE) drawIcon(g, icon, ix, cy, icoSize, ico);

        // keyboard focus ring
        if (keyboardFocus && on) {
            g.setColor(TOOL_THEME.RUBY);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 4f}, 0f));
            g.draw(new RoundRectangle2D.Float(x - 1, y - 1, pw + 2, ph + depth + 2, ph + 4, ph + 4));
        }
        g.dispose();
    }

    private static void drawIcon(Graphics2D g, Icon icon, float x, float cy, float s, Color c) {
        g.setColor(c);
        switch (icon) {
            case BACK:
            case PREV: {                      // solid arrow pointing left
                Path2D p = new Path2D.Float();
                p.moveTo(x,            cy);
                p.lineTo(x + s * 0.50f, cy - s * 0.52f);
                p.lineTo(x + s * 0.50f, cy - s * 0.22f);
                p.lineTo(x + s,         cy - s * 0.22f);
                p.lineTo(x + s,         cy + s * 0.22f);
                p.lineTo(x + s * 0.50f, cy + s * 0.22f);
                p.lineTo(x + s * 0.50f, cy + s * 0.52f);
                p.closePath();
                g.fill(p);
                break;
            }
            case NEXT: {                      // solid arrow pointing right
                Path2D p = new Path2D.Float();
                p.moveTo(x + s,        cy);
                p.lineTo(x + s * 0.50f, cy - s * 0.52f);
                p.lineTo(x + s * 0.50f, cy - s * 0.22f);
                p.lineTo(x,             cy - s * 0.22f);
                p.lineTo(x,             cy + s * 0.22f);
                p.lineTo(x + s * 0.50f, cy + s * 0.22f);
                p.lineTo(x + s * 0.50f, cy + s * 0.52f);
                p.closePath();
                g.fill(p);
                break;
            }
            case NEXT2: {                     // double chevron  >>
                g.setStroke(new BasicStroke(Math.max(2.5f, s * 0.26f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 2; i++) {
                    float ox = x + i * s * 0.42f;
                    Path2D p = new Path2D.Float();
                    p.moveTo(ox + s * 0.10f, cy - s * 0.48f);
                    p.lineTo(ox + s * 0.50f, cy);
                    p.lineTo(ox + s * 0.10f, cy + s * 0.48f);
                    g.draw(p);
                }
                break;
            }
            case RETRY: {                     // circular arrow
                g.setStroke(new BasicStroke(Math.max(2f, s * 0.2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                float r = s * 0.42f, ccx = x + s / 2f;
                g.draw(new Arc2D.Float(ccx - r, cy - r, r * 2, r * 2, 40, 280, Arc2D.OPEN));
                Path2D p = new Path2D.Float();
                float ax = ccx + (float) (r * Math.cos(Math.toRadians(40)));
                float ay = cy  - (float) (r * Math.sin(Math.toRadians(40)));
                p.moveTo(ax + s * 0.28f, ay - s * 0.02f);
                p.lineTo(ax - s * 0.02f, ay - s * 0.10f);
                p.lineTo(ax + s * 0.04f, ay + s * 0.28f);
                p.closePath();
                g.fill(p);
                break;
            }
            case CHECK: {
                g.setStroke(new BasicStroke(Math.max(2.5f, s * 0.24f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D p = new Path2D.Float();
                p.moveTo(x + s * 0.08f, cy + s * 0.02f);
                p.lineTo(x + s * 0.38f, cy + s * 0.34f);
                p.lineTo(x + s * 0.92f, cy - s * 0.34f);
                g.draw(p);
                break;
            }
            default: break;
        }
    }
}
