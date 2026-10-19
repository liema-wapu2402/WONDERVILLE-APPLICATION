package o3_TOOLS_CLASSES;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * WONDERVILLE v2 - reaction popup: dims the screen and plays a GIF (correct / incorrect / oops...)
 * in a rounded frame. Closes on click or after a timeout, then runs onClose.
 */
public class TOOL_POPUP extends JComponent implements TOOL_GIF_PANEL.StageAware {

    private static final long serialVersionUID = 1L;

    private final TOOL_GIF_PANEL stage;
    private final TOOL_GIF_VIEW view;
    private final double aspect;
    private final int stageWidth;
    private final Runnable onClose;
    private final Timer timer;
    private double scale = 1.0;
    private boolean closed;
    private final Rectangle box = new Rectangle();

    private TOOL_POPUP(TOOL_GIF_PANEL stage, String gif, double aspect, int stageWidth, int millis, Runnable onClose) {
        this.stage = stage; this.aspect = aspect; this.stageWidth = stageWidth; this.onClose = onClose;
        setLayout(null);
        setOpaque(false);
        view = new TOOL_GIF_VIEW(gif);
        add(view);
        addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { close(); } });
        timer = new Timer(millis, e -> close());
        timer.setRepeats(false);
    }

    /** gif plays at stageWidth stage-pixels wide (aspect = width/height of the GIF). */
    public static TOOL_POPUP show(TOOL_GIF_PANEL stage, String gif, double aspect, int stageWidth, int millis, Runnable onClose) {
        TOOL_POPUP p = new TOOL_POPUP(stage, gif, aspect, stageWidth, millis, onClose);
        stage.addOverlay(p);
        p.view.play();
        p.timer.start();
        return p;
    }

    public void close() {
        if (closed) return;
        closed = true;
        timer.stop();
        view.stop();
        stage.removeOverlay(this);
        if (onClose != null) onClose.run();
    }

    @Override public void stageScaled(double s) { scale = s; }

    @Override
    public void doLayout() {
        Rectangle st = stage.stageRect();
        int w = (int) Math.round(stageWidth * scale), h = (int) Math.round(w / aspect);
        box.setBounds(st.x + (st.width - w) / 2, st.y + (st.height - h) / 2, w, h);
        view.setBounds(box);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);
        g.setColor(new Color(30, 10, 40, 140));
        g.fillRect(0, 0, getWidth(), getHeight());
        float arc = (float) (40 * scale);
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Float(box.x + 3, box.y + 9, box.width, box.height, arc, arc));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke((float) Math.max(4, 9 * scale)));
        g.draw(new RoundRectangle2D.Float(box.x - 2, box.y - 2, box.width + 4, box.height + 4, arc + 4, arc + 4));
        g.dispose();
    }

    @Override
    protected void paintChildren(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        float arc = (float) (40 * scale);
        g.setClip(new RoundRectangle2D.Float(box.x, box.y, box.width, box.height, arc, arc));
        super.paintChildren(g);
        g.dispose();
    }
}
