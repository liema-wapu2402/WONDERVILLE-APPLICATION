package o3_TOOLS_CLASSES;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * WONDERVILLE v2 - text label that scales with the stage, word-wraps and auto-shrinks to fit
 * its box. Replaces the fixed-size JLabels + HTML strings of v1 (which clipped on small windows).
 */
public class TOOL_LABEL extends JComponent implements TOOL_GIF_PANEL.StageAware {

    private static final long serialVersionUID = 1L;

    private String text;
    private float basePx;               // font size in stage pixels (1200x675 space)
    private Color color;
    private int style;
    private int align = SwingConstants.CENTER;
    private boolean display = true;     // display (rounded) face vs body face
    private double scale = 1.0;
    private Color outline;

    public TOOL_LABEL(String text, float basePx, Color color, boolean bold) {
        this.text = text;
        this.basePx = basePx;
        this.color = color;
        this.style = bold ? Font.BOLD : Font.PLAIN;
        setOpaque(false);
    }

    public TOOL_LABEL setText(String t)        { this.text = t; repaint(); return this; }
    public String getText()                    { return text; }
    public TOOL_LABEL setColor(Color c)        { this.color = c; repaint(); return this; }
    public TOOL_LABEL setBasePx(float px)      { this.basePx = px; repaint(); return this; }
    public TOOL_LABEL setAlign(int swing)      { this.align = swing; repaint(); return this; }
    public TOOL_LABEL setBodyFace()            { this.display = false; repaint(); return this; }
    public TOOL_LABEL setOutline(Color c)      { this.outline = c; repaint(); return this; }
    public TOOL_LABEL setScale(double s)       { this.scale = s; repaint(); return this; }

    @Override public void stageScaled(double s) { this.scale = s; }

    @Override
    protected void paintComponent(Graphics g0) {
        if (text == null || text.isEmpty() || getWidth() < 4 || getHeight() < 4) return;
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);

        float size = Math.max(9f, (float) (basePx * scale));
        List<String> lines;
        FontMetrics fm;
        while (true) {                                    // shrink until the wrapped text fits the box
            Font f = display ? TOOL_THEME.display(style, size) : TOOL_THEME.body(style, size);
            g.setFont(f);
            fm = g.getFontMetrics();
            lines = wrap(fm, text, getWidth());
            if (lines.size() * fm.getHeight() <= getHeight() || size <= 9f) break;
            size -= 1f;
        }

        int total = lines.size() * fm.getHeight();
        int y = (getHeight() - total) / 2 + fm.getAscent();
        for (String ln : lines) {
            int w = fm.stringWidth(ln);
            int x = align == SwingConstants.LEFT ? 0 : align == SwingConstants.RIGHT ? getWidth() - w : (getWidth() - w) / 2;
            if (outline != null) {
                g.setColor(outline);
                int o = Math.max(1, Math.round(size / 14f));
                for (int dx = -o; dx <= o; dx++) for (int dy = -o; dy <= o; dy++) if (dx != 0 || dy != 0) g.drawString(ln, x + dx, y + dy);
            }
            g.setColor(color);
            g.drawString(ln, x, y);
            y += fm.getHeight();
        }
        g.dispose();
    }

    private static List<String> wrap(FontMetrics fm, String text, int maxW) {
        List<String> out = new ArrayList<String>();
        for (String para : text.split("\n", -1)) {
            StringBuilder cur = new StringBuilder();
            for (String word : para.split(" ")) {
                String trial = cur.length() == 0 ? word : cur + " " + word;
                if (fm.stringWidth(trial) <= maxW || cur.length() == 0) cur = new StringBuilder(trial);
                else { out.add(cur.toString()); cur = new StringBuilder(word); }
            }
            out.add(cur.toString());
        }
        return out;
    }
}
