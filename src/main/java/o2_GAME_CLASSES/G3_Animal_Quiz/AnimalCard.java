package o2_GAME_CLASSES.G3_Animal_Quiz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import o3_TOOLS_CLASSES.TOOL_THEME;

/** White rounded card showing one animal picture. With a click action it is a selectable answer cell (Hard mode). */
class AnimalCard extends JComponent {

    private static final long serialVersionUID = 1L;

    enum State { NORMAL, CORRECT, WRONG, DIMMED }

    private Image img;
    private State state = State.NORMAL;
    private boolean over, clickable;
    private final Color ring;

    AnimalCard(Color ring) { this.ring = ring; setOpaque(false); }

    AnimalCard(Color ring, final Runnable onClick) {
        this(ring);
        clickable = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { over = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { over = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (state == State.NORMAL) onClick.run(); }
        });
    }

    void setImage(String path) {
        img = null;
        try { URL u = AnimalCard.class.getResource(path); if (u != null) img = ImageIO.read(u); }
        catch (Exception e) { e.printStackTrace(); }
        state = State.NORMAL;
        repaint();
    }

    void setState(State s) { state = s; repaint(); }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int w = getWidth(), h = getHeight();
        float arc = Math.min(w, h) * 0.14f, b = Math.max(4f, Math.min(w, h) * 0.03f);

        Color edge = state == State.CORRECT ? TOOL_THEME.GREEN : state == State.WRONG ? TOOL_THEME.RUBY
                   : (clickable && over) ? TOOL_THEME.SUN : ring;
        g.setColor(new Color(0, 0, 0, 50));  g.fill(new RoundRectangle2D.Float(3, 7, w - 7, h - 8, arc, arc));
        g.setColor(edge);                    g.fill(new RoundRectangle2D.Float(0, 0, w - 4, h - 5, arc, arc));
        g.setColor(Color.WHITE);             g.fill(new RoundRectangle2D.Float(b, b, w - 4 - 2 * b, h - 5 - 2 * b, arc * .8f, arc * .8f));
        if (img != null) {
            int aw = (int) (w - 4 - 2 * b - Math.min(w, h) * 0.06f), ah = (int) (h - 5 - 2 * b - Math.min(w, h) * 0.06f);
            double s = Math.min(aw / (double) img.getWidth(null), ah / (double) img.getHeight(null));
            int iw = (int) (img.getWidth(null) * s), ih = (int) (img.getHeight(null) * s);
            g.setClip(new RoundRectangle2D.Float(b, b, w - 4 - 2 * b, h - 5 - 2 * b, arc * .8f, arc * .8f));
            g.drawImage(img, (w - 4 - iw) / 2, (h - 5 - ih) / 2, iw, ih, null);
            g.setClip(null);
        }
        if (state == State.DIMMED) { g.setColor(new Color(255, 255, 255, 150)); g.fill(new RoundRectangle2D.Float(0, 0, w - 4, h - 5, arc, arc)); }
        if (state == State.CORRECT || state == State.WRONG) {
            g.setColor(edge); g.setStroke(new BasicStroke(Math.max(4f, b * 1.6f)));
            g.draw(new RoundRectangle2D.Float(b, b, w - 4 - 2 * b, h - 5 - 2 * b, arc * .8f, arc * .8f));
        }
        g.dispose();
    }
}
