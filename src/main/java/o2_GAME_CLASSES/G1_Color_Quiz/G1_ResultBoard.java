package o2_GAME_CLASSES.G1_Color_Quiz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import o3_TOOLS_CLASSES.TOOL_THEME;

/**
 * Score card shown at the end of a Color Quiz round (Easy + Hard).
 * Replaces the HTML <table> in a JLabel from v1; everything is sized from the component height.
 */
public class G1_ResultBoard extends JComponent {

    private static final long serialVersionUID = 1L;

    public static final class Row {
        final String target, picked; final boolean ok;
        public Row(String target, String picked, boolean ok) { this.target = target; this.picked = picked; this.ok = ok; }
    }

    private final String title;
    private final List<Row> rows = new ArrayList<Row>();

    public G1_ResultBoard(String title) { this.title = title; setOpaque(false); }

    public void setRows(List<Row> r) { rows.clear(); rows.addAll(r); repaint(); }

    private int score() { int n = 0; for (Row r : rows) if (r.ok) n++; return n; }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);
        int W = getWidth(), H = getHeight();
        float arc = H * 0.08f;

        g.setColor(new Color(0, 0, 0, 50));
        g.fill(new RoundRectangle2D.Float(4, 8, W - 8, H - 8, arc, arc));
        g.setColor(TOOL_THEME.RUBY);   g.fill(new RoundRectangle2D.Float(0, 0, W - 4, H - 6, arc, arc));
        g.setColor(TOOL_THEME.MAYA_BLUE); g.fill(new RoundRectangle2D.Float(7, 7, W - 18, H - 20, arc * .8f, arc * .8f));
        g.setColor(Color.WHITE);       g.fill(new RoundRectangle2D.Float(13, 13, W - 30, H - 32, arc * .7f, arc * .7f));

        int n = Math.max(1, rows.size());
        float unit = (H - 40f) / (n + 3.2f);                     // title(1.4) + header(0.8) + rows + score(1)
        float x0 = 28, x1 = W - 28 - 4;
        float y = 22;

        // title bar
        g.setPaint(new GradientPaint(0, y, TOOL_THEME.RUBY, W, y, TOOL_THEME.darker(TOOL_THEME.RUBY, .15f)));
        g.fill(new RoundRectangle2D.Float(x0, y, x1 - x0, unit * 1.3f, unit * .6f, unit * .6f));
        g.setFont(TOOL_THEME.display(Font.BOLD, unit * .72f));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, x0 + (x1 - x0 - fm.stringWidth(title)) / 2f, y + unit * .65f + (fm.getAscent() - fm.getDescent()) / 2f);
        y += unit * 1.45f;

        // columns
        float cw = x1 - x0;
        float[] cx = { x0, x0 + cw * .09f, x0 + cw * .34f, x0 + cw * .62f };
        String[] head = { "#", "Result", "Target color", "Your answer" };
        g.setFont(TOOL_THEME.display(Font.BOLD, unit * .48f)); fm = g.getFontMetrics();
        g.setColor(TOOL_THEME.INK_SOFT);
        for (int i = 0; i < head.length; i++) g.drawString(head[i], cx[i] + unit * .3f, y + unit * .6f);
        y += unit * .8f;

        // rows
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            g.setColor(r.ok ? new Color(214, 245, 222) : new Color(253, 226, 230));
            g.fill(new RoundRectangle2D.Float(x0, y + unit * .06f, cw, unit * .88f, unit * .5f, unit * .5f));

            g.setFont(TOOL_THEME.display(Font.BOLD, unit * .5f)); fm = g.getFontMetrics();
            float by = y + unit * .5f + (fm.getAscent() - fm.getDescent()) / 2f;
            g.setColor(TOOL_THEME.INK);
            g.drawString(String.valueOf(i + 1), cx[0] + unit * .3f, by);

            // result chip
            g.setColor(r.ok ? TOOL_THEME.GREEN : TOOL_THEME.RUBY);
            g.fill(new RoundRectangle2D.Float(cx[1] + unit * .1f, y + unit * .16f, unit * 1.0f, unit * .68f, unit, unit));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(Math.max(2f, unit * .1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float mx = cx[1] + unit * .6f, my = y + unit * .5f, d = unit * .17f;
            if (r.ok) { g.drawLine((int) (mx - d), (int) my, (int) (mx - d * .3f), (int) (my + d)); g.drawLine((int) (mx - d * .3f), (int) (my + d), (int) (mx + d * 1.2f), (int) (my - d)); }
            else      { g.drawLine((int) (mx - d), (int) (my - d), (int) (mx + d), (int) (my + d)); g.drawLine((int) (mx + d), (int) (my - d), (int) (mx - d), (int) (my + d)); }

            // target colour swatch + name
            g.setColor(G1_ColorBank.of(r.target));
            g.fill(new RoundRectangle2D.Float(cx[2] + unit * .1f, y + unit * .2f, unit * .9f, unit * .6f, unit * .3f, unit * .3f));
            g.setColor(new Color(0, 0, 0, 90)); g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Float(cx[2] + unit * .1f, y + unit * .2f, unit * .9f, unit * .6f, unit * .3f, unit * .3f));
            g.setFont(TOOL_THEME.display(Font.BOLD, unit * .5f));
            g.setColor(TOOL_THEME.INK);
            g.drawString(r.target, cx[2] + unit * 1.25f, by);

            g.setColor(r.ok ? new Color(120, 120, 130) : TOOL_THEME.RUBY);
            g.drawString(r.ok ? "\u2014" : r.picked, cx[3] + unit * .3f, by);
            y += unit;
        }

        // score footer
        int sc = score(), total = rows.size();
        String verdict = sc >= total * 0.8 ? "Excellent!" : sc >= total * 0.5 ? "Good job!" : "Keep practicing!";
        y += unit * .1f;
        g.setPaint(new GradientPaint(0, y, TOOL_THEME.SUN, W, y, TOOL_THEME.ORANGE));
        g.fill(new RoundRectangle2D.Float(x0, y, cw, unit * 1.0f, unit * .6f, unit * .6f));
        g.setFont(TOOL_THEME.display(Font.BOLD, unit * .62f)); fm = g.getFontMetrics();
        String s = "Score: " + sc + " / " + total + "   \u2022   " + verdict;
        g.setColor(TOOL_THEME.INK);
        g.drawString(s, x0 + (cw - fm.stringWidth(s)) / 2f, y + unit * .5f + (fm.getAscent() - fm.getDescent()) / 2f);
        g.dispose();
    }
}
