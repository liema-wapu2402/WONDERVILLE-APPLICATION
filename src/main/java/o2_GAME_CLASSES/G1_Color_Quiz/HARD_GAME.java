package o2_GAME_CLASSES.G1_Color_Quiz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_GIF_VIEW;
import o3_TOOLS_CLASSES.TOOL_LABEL;
import o3_TOOLS_CLASSES.TOOL_RoundedPanel;
import o3_TOOLS_CLASSES.TOOL_THEME;

/** COLOR QUIZ - HARD: 10 questions, find the named colour in a 4x4 grid of 16 swatches. */
public class HARD_GAME {

    private static final int QUESTIONS = 10;

    private final TOOL_GIF_PANEL stage;
    private final List<String> targets = new ArrayList<String>();
    private final List<G1_ResultBoard.Row> results = new ArrayList<G1_ResultBoard.Row>();
    private int index;
    private boolean answered;
    private String target;

    private Swatch[] swatches;
    private JComponent gridFrame;
    private TOOL_GIF_VIEW feedback;
    private TOOL_RoundedPanel qPanel;
    private TOOL_LABEL qLabel, chip;
    private TOOL_BUTTON next, again;
    private G1_ResultBoard board;

    public HARD_GAME(TOOL_GIF_PANEL stage) {
        this.stage = stage;
        build();
        start();
    }

    private void build() {
        gridFrame = new JComponent() {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                TOOL_THEME.smooth(g);
                int w = getWidth(), h = getHeight(); float arc = h * 0.08f;
                g.setColor(new Color(0, 0, 0, 50)); g.fill(new RoundRectangle2D.Float(4, 8, w - 8, h - 8, arc, arc));
                g.setColor(TOOL_THEME.RUBY);        g.fill(new RoundRectangle2D.Float(0, 0, w - 4, h - 6, arc, arc));
                g.setColor(Color.WHITE);            g.fill(new RoundRectangle2D.Float(6, 6, w - 16, h - 18, arc * .8f, arc * .8f));
                g.dispose();
            }
        };
        stage.place(gridFrame, 250, 18, 700, 330);

        List<String> names = new ArrayList<String>();
        Collections.addAll(names, G1_ColorBank.HARD_NAMES);
        swatches = new Swatch[16];
        for (int i = 0; i < 16; i++) {
            swatches[i] = new Swatch();
            stage.place(swatches[i], 270 + (i % 4) * 171, 34 + (i / 4) * 76, 160, 68);
        }

        feedback = new TOOL_GIF_VIEW("/ICONS/CORRECT_ICON_HARD.gif");
        feedback.setVisible(false);
        stage.place(feedback, 262, 26, 676, 314);

        qPanel = new TOOL_RoundedPanel(40, TOOL_THEME.MAYA_BLUE).setOutline(Color.WHITE, 5f);
        qPanel.setShadow(true);
        stage.place(qPanel, 250, 362, 700, 100);
        qLabel = new TOOL_LABEL("", 28, TOOL_THEME.INK, true);
        stage.place(qLabel, 275, 368, 650, 88);

        chip = new TOOL_LABEL("", 22, Color.WHITE, true);
        stage.place(chip, 880, 330, 270, 0 + 36);
        chip.setVisible(false);          // question number is part of the question text in hard mode

        next = new TOOL_BUTTON("NEXT", TOOL_BUTTON.Icon.NEXT2, TOOL_BUTTON.Skin.HOT);
        next.addActionListener(e -> advance());
        stage.place(next, 912, 556, 230, 82);

        board = new G1_ResultBoard("HARD MODE RESULTS");
        board.setVisible(false);
        stage.place(board, 200, 10, 800, 520);

        again = new TOOL_BUTTON("TRY AGAIN", TOOL_BUTTON.Icon.RETRY, TOOL_BUTTON.Skin.HOT);
        again.addActionListener(e -> start());
        again.setVisible(false);
        stage.place(again, 470, 544, 260, 84);
    }

    private void start() {
        targets.clear();
        List<String> pool = new ArrayList<String>();
        Collections.addAll(pool, G1_ColorBank.HARD_NAMES);
        Collections.shuffle(pool);
        targets.addAll(pool.subList(0, QUESTIONS));
        results.clear();
        index = 0;
        setQuizVisible(true);
        board.setVisible(false);
        again.setVisible(false);
        showQuestion();
    }

    private void setQuizVisible(boolean v) {
        gridFrame.setVisible(v); qPanel.setVisible(v); qLabel.setVisible(v); next.setVisible(v);
        for (Swatch s : swatches) s.setVisible(v);
    }

    private void showQuestion() {
        answered = false;
        feedback.stop(); feedback.setVisible(false);
        target = targets.get(index);

        List<String> names = new ArrayList<String>();
        Collections.addAll(names, G1_ColorBank.HARD_NAMES);
        Collections.shuffle(names);
        for (int i = 0; i < 16; i++) swatches[i].set(names.get(i));

        qLabel.setText("Question " + (index + 1) + " of " + QUESTIONS + "\nFind and click the color: " + target.toUpperCase());
        qLabel.setColor(TOOL_THEME.INK);
        next.setEnabled(false);
        next.setText(index + 1 >= QUESTIONS ? "RESULTS" : "NEXT");
    }

    private void choose(Swatch s) {
        if (answered) return;
        answered = true;
        boolean ok = s.name.equals(target);
        results.add(new G1_ResultBoard.Row(target, s.name, ok));
        qLabel.setText(ok ? "CORRECT! That is " + target + "!"
                          : "INCORRECT! You picked " + s.name + ".\nThe correct answer is " + target + ".");
        qLabel.setColor(ok ? new Color(0, 110, 40) : new Color(170, 0, 40));

        stage.remove(feedback);
        feedback = new TOOL_GIF_VIEW(ok ? "/ICONS/CORRECT_ICON_HARD.gif" : "/ICONS/INCORRECT_ICON_HARD.gif");
        stage.place(feedback, 262, 26, 676, 314);
        stage.setComponentZOrder(feedback, 0);
        feedback.play();
        next.setEnabled(true);
    }

    private void advance() {
        if (!answered) return;
        index++;
        if (index < QUESTIONS) { showQuestion(); return; }
        feedback.stop(); feedback.setVisible(false);
        setQuizVisible(false);
        board.setRows(results);
        board.setVisible(true);
        again.setVisible(true);
        stage.repaint();
    }

    // ── one colour swatch ───────────────────────────────────────────────────
    private class Swatch extends JComponent {
        private static final long serialVersionUID = 1L;
        String name = "Red";
        boolean over;
        Swatch() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { over = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { if (isEnabled()) choose(Swatch.this); }
            });
        }
        void set(String n) { name = n; setEnabled(true); repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            TOOL_THEME.smooth(g);
            int w = getWidth(), h = getHeight(); float arc = h * 0.35f;
            boolean on = over && !answered;
            g.setColor(new Color(0, 0, 0, 40)); g.fill(new RoundRectangle2D.Float(2, 4, w - 4, h - 4, arc, arc));
            g.setColor(G1_ColorBank.of(name));  g.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 4, arc, arc));
            g.setColor(new Color(255, 255, 255, 60)); g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(5, 5, w - 12, (h - 4) * .45f, arc * .6f, arc * .6f));
            g.setColor(on ? TOOL_THEME.SUN : new Color(0, 0, 0, 70));
            g.setStroke(new BasicStroke(on ? Math.max(4f, h * .07f) : 1.5f));
            g.draw(new RoundRectangle2D.Float(1, 1, w - 4, h - 6, arc, arc));
            g.dispose();
        }
    }
}
