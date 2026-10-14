package o1_MAIN_CLASSES;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import o2_GAME_CLASSES.GAME_1_ColorQuiz;
import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_Screen;
import o3_TOOLS_CLASSES.TOOL_THEME;

/**
 * Visual Aids menu (kind 1) and Games menu (kind 2).
 * The whole card in the artwork is clickable (v1 only reacted on the small label); hovering a card
 * lights up its label button. Spelling / Animal / Visual-aid items are not built yet -> "Coming soon!".
 */
public class Menu_Feautures implements TOOL_Screen {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;
    private final int kind;

    public Menu_Feautures(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame, int kind) {
        this.mainFrame = mainFrame;
        this.kind = kind;
    }

    public void show() { mainFrame.open(this); }

    public String background() {
        return kind == 1 ? "/01_MAIN_CLASSES/01_SC_START_MENU/VISUAL_BG_COVER_START.gif"
                         : "/01_MAIN_CLASSES/01_SC_START_MENU/GAME_BG_COVER_START.gif";
    }
    public String loadingText() { return "Loading..."; }

    public void build(final TOOL_GIF_PANEL stage) {
        if (kind == 1) {
            card(stage, "RHYME SONGS",        314, 228, 280, 346, 452, 529, 224, 60, () -> stage.showToast("Rhyme Songs - coming soon!"));
            card(stage, "EDUCATIONAL VIDEOS", 626, 228, 280, 346, 765, 529, 244, 60, () -> stage.showToast("Educational Videos - coming soon!"));
        } else {
            card(stage, "COLOR QUIZ",     165, 228, 260, 322, 290, 506, 196, 54, () -> new GAME_1_ColorQuiz(mainFrame, this).DesignStructure());
            card(stage, "SPELLING GAME",  470, 228, 260, 322, 600, 506, 196, 54, () -> stage.showToast("Spelling Game - coming soon!"));
            card(stage, "ANIMAL QUIZ",    756, 228, 260, 322, 885, 506, 196, 54, () -> stage.showToast("Animal Quiz - coming soon!"));
        }
        TOOL_BUTTON back = new TOOL_BUTTON("BACK", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
        back.addActionListener(e -> new SUB_CLASS_1_START_MENU(mainFrame).show());
        stage.place(back, 13, 578, 212, 86);
    }

    /** card hit-area (hx,hy,hw,hh) + label button centred on (cx,cy). */
    private void card(TOOL_GIF_PANEL stage, String label, int hx, int hy, int hw, int hh,
                      int cx, int cy, int bw, int bh, final Runnable action) {
        final TOOL_BUTTON pill = new TOOL_BUTTON(label);
        pill.addActionListener(e -> action.run());
        Hit hit = new Hit(pill, action);
        stage.place(hit,  hx, hy, hw, hh);          // placed first => underneath the pill
        stage.place(pill, cx - bw / 2, cy - bh / 2, bw, bh);
    }

    private static class Hit extends JComponent {
        private static final long serialVersionUID = 1L;
        private boolean over;
        Hit(final TOOL_BUTTON pill, final Runnable action) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true;  pill.getModel().setRollover(true);  repaint(); }
                @Override public void mouseExited (MouseEvent e) { over = false; pill.getModel().setRollover(false); repaint(); }
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
}
