package o1_MAIN_CLASSES;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_LoadingDialog;
import o3_TOOLS_CLASSES.TOOL_Screen;

/**
 * WONDERVILLE v2 - main window.
 * Every screen is a TOOL_Screen (GIF background + coded buttons). open(screen) shows the loader,
 * streams the GIF, cross-fades to it and builds the UI. The window is resizable (min 800x450,
 * F11 = maximise / restore) and everything scales from the 1200x675 design size.
 */
public class MAIN_CLASS_WONDERVILLE_APPLICATION extends JFrame {

    private static final long serialVersionUID = 2L;

    public static final String BG_MAIN = "/MAIN_BG_COVER_START.gif";

    public static void main(String[] args) {
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.opengl", "false");

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MAIN_CLASS_WONDERVILLE_APPLICATION().setVisible(true);
            }
        });
    }

    public MAIN_CLASS_WONDERVILLE_APPLICATION() {
        setTitle("WONDERVILLE");
        URL logo = getClass().getResource("/01_MAIN_CLASSES/01_MC_MAIN/LOGO.png");
        if (logo != null) setIconImage(Toolkit.getDefaultToolkit().getImage(logo));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 450));

        // first screen: no loader yet (the window is not visible)
        TOOL_GIF_PANEL bg = TOOL_GIF_PANEL.prepare(BG_MAIN);
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double s = Math.min(1.0, Math.min((usable.width - 40) / 1200.0, (usable.height - 90) / 675.0));
        bg.setPreferredSize(new Dimension((int) (1200 * s), (int) (675 * s)));
        bg.attachTo(this);
        buildMainMenu(bg);
        pack();
        setLocationRelativeTo(null);

        // F11 = maximise / restore
        JComponent root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "full");
        root.getActionMap().put("full", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                setExtendedState((getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0 ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH);
            }
        });
    }

    /** Shows the loader, streams the screen's GIF, swaps screens, then builds the UI. */
    public void open(final TOOL_Screen screen) {
        final MAIN_CLASS_WONDERVILLE_APPLICATION self = this;
        TOOL_LoadingDialog.showUntilReady(this, screen.loadingText(), () -> {
            final TOOL_GIF_PANEL bg = TOOL_GIF_PANEL.prepare(screen.background());      // worker thread
            return () -> {                                                              // EDT
                bg.attachTo(self);
                screen.build(bg);
                bg.revalidate();
                bg.repaint();
            };
        });
    }

    // ── screens ─────────────────────────────────────────────────────────────
    public void showMainMenu() {
        open(new TOOL_Screen() {
            public String background()  { return BG_MAIN; }
            public String loadingText() { return "Going back..."; }
            public void build(TOOL_GIF_PANEL stage) { buildMainMenu(stage); }
        });
    }

    private void buildMainMenu(TOOL_GIF_PANEL stage) {
        String[] labels = { "START", "ABOUT", "CREDITS", "HELP", "EXIT" };
        int[] cy = { 284, 369, 449, 530, 613 };            // centres of the pills painted in the artwork
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            TOOL_BUTTON b = new TOOL_BUTTON(labels[i]);
            b.addActionListener(e -> {
                switch (idx) {
                    case 0: new SUB_CLASS_1_START_MENU(this).show(); break;
                    case 1: new SUB_CLASS_2_ABOUT(this).show();      break;
                    case 2: new SUB_CLASS_3_CREDITS(this).show();    break;
                    case 3: new SUB_CLASS_4_HELP(this).show();       break;
                    default: new SUB_CLASS_5_EXIT(this).show();      break;
                }
            });
            stage.place(b, 468, cy[i] - 42, 264, 86);
        }
    }
}
