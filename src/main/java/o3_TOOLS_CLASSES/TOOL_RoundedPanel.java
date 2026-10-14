package o3_TOOLS_CLASSES;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

/**
 * Reusable rounded-corner panel with a settable fill colour.
 * v2: optional outline and soft drop shadow so cards look like the artwork (white edge, candy colours).
 */
public class TOOL_RoundedPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private int radius;
    private Color fill;
    private Color outline;
    private float outlineWidth = 0f;
    private boolean shadow;

    public TOOL_RoundedPanel(int radius, Color fill) {
        this.radius = radius;
        this.fill = fill;
        setOpaque(false);
    }

    public TOOL_RoundedPanel setOutline(Color c, float width) { this.outline = c; this.outlineWidth = width; repaint(); return this; }
    public TOOL_RoundedPanel setShadow(boolean on)            { this.shadow = on; repaint(); return this; }

    public void setFill(Color c)  { this.fill = c; }
    public Color getFill()        { return fill; }
    public void setRadius(int r)  { this.radius = r; }
    public int getRadius()        { return radius; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        TOOL_THEME.smooth(g2);
        int w = getWidth() - 1, h = getHeight() - 1;
        int sh = shadow ? Math.max(3, h / 60) : 0;
        if (shadow) {
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Float(2, sh + 1, w - 3, h - sh - 1, radius, radius));
        }
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h - sh, radius, radius));
        if (outline != null && outlineWidth > 0f) {
            g2.setColor(outline);
            g2.setStroke(new BasicStroke(outlineWidth));
            float o = outlineWidth / 2f;
            g2.draw(new RoundRectangle2D.Float(o, o, w - outlineWidth, h - sh - outlineWidth, radius, radius));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
