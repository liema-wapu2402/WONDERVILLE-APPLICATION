package o3_TOOLS_CLASSES;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/**
 * Transparent click area laid over a card that is painted in the GIF artwork.
 * Hovering draws a sun-yellow outline and lights up the card's label button; clicking runs the action.
 */
public class TOOL_HIT extends JComponent {

    private static final long serialVersionUID = 1L;
    private boolean over;

    public TOOL_HIT(final TOOL_BUTTON pill, final Runnable action) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { over = true;  if (pill != null) pill.getModel().setRollover(true);  repaint(); }
            @Override public void mouseExited (MouseEvent e) { over = false; if (pill != null) pill.getModel().setRollover(false); repaint(); }
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
        });
    }

    @Override protected void paintComponent(Graphics g0) {
        if (!over) return;
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);
        g.setColor(TOOL_THEME.SUN);
        g.setStroke(new BasicStroke(Math.max(3f, getHeight() * 0.018f)));
        float a = getWidth() * 0.12f;
        g.draw(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, a, a));
        g.dispose();
    }
}
