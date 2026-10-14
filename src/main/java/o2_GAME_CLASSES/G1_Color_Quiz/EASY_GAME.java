package o2_GAME_CLASSES.G1_Color_Quiz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

/**
 * COLOR QUIZ - EASY: 6 questions, a big colour card and six name buttons.
 * Builds its widgets straight onto the stage (1200x675 coordinates) so it scales with the window.
 */
public class EASY_GAME {

    private final TOOL_GIF_PANEL stage;
    private final List<String> order = new ArrayList<String>();     // question order
    private final List<G1_ResultBoard.Row> results = new ArrayList<G1_ResultBoard.Row>();
    private int index;
    private boolean answered;

    private ColorCard card;
    private TOOL_GIF_VIEW feedback;
    private TOOL_RoundedPanel qPanel;
    private TOOL_LABEL qLabel;
    private TOOL_BUTTON[] answers;
    private TOOL_BUTTON next, again;
    private G1_ResultBoard board;
    private TOOL_LABEL chip;
    private TOOL_RoundedPanel chipBg;

    public EASY_GAME(TOOL_GIF_PANEL stage) {
        this.stage = stage;
        build();
        start();
    }

    // ── UI ──────────────────────────────────────────────────────────────────
    private void build() {
        card = new ColorCard();
        stage.place(card, 330, 22, 540, 236);

        feedback = new TOOL_GIF_VIEW("/ICONS/CORRECT_ICON.gif");
        feedback.setVisible(false);
        stage.place(feedback, 345, 32, 510, 216);

        chipBg = new TOOL_RoundedPanel(40, TOOL_THEME.RUBY).setOutline(Color.WHITE, 4f);
        stage.place(chipBg, 900, 28, 250, 48);
        chip = new TOOL_LABEL("", 22, Color.WHITE, true);
        stage.place(chip, 910, 30, 230, 44);

        qPanel = new TOOL_RoundedPanel(40, TOOL_THEME.MAYA_BLUE).setOutline(Color.WHITE, 5f);
        qPanel.setShadow(true);
        stage.place(qPanel, 250, 270, 700, 82);
        qLabel = new TOOL_LABEL("", 28, TOOL_THEME.INK, true);
        stage.place(qLabel, 275, 274, 650, 72);

        answers = new TOOL_BUTTON[G1_ColorBank.EASY_NAMES.length];
        for (int i = 0; i < answers.length; i++) {
            final TOOL_BUTTON b = new TOOL_BUTTON("", TOOL_BUTTON.Icon.NONE, TOOL_BUTTON.Skin.ORANGE);
            b.addActionListener(e -> choose(b));
            answers[i] = b;
            stage.place(b, 250 + (i % 3) * 240, 364 + (i / 3) * 70, 220, 62);
        }

        next = new TOOL_BUTTON("NEXT", TOOL_BUTTON.Icon.NEXT2, TOOL_BUTTON.Skin.HOT);
        next.addActionListener(e -> advance());
        stage.place(next, 912, 556, 230, 82);

        board = new G1_ResultBoard("EASY MODE RESULTS");
        board.setVisible(false);
        stage.place(board, 250, 24, 700, 470);

        again = new TOOL_BUTTON("TRY AGAIN", TOOL_BUTTON.Icon.RETRY, TOOL_BUTTON.Skin.HOT);
        again.addActionListener(e -> start());
        again.setVisible(false);
        stage.place(again, 470, 508, 260, 84);
    }

    // ── game flow ───────────────────────────────────────────────────────────
    private void start() {
        order.clear();
        Collections.addAll(order, G1_ColorBank.EASY_NAMES);
        Collections.shuffle(order);
        results.clear();
        index = 0;
        setQuizVisible(true);
        board.setVisible(false);
        again.setVisible(false);
        showQuestion();
    }

    private void setQuizVisible(boolean v) {
        card.setVisible(v); qPanel.setVisible(v); qLabel.setVisible(v); chip.setVisible(v); chipBg.setVisible(v); next.setVisible(v);
        for (TOOL_BUTTON b : answers) b.setVisible(v);
    }

    private void showQuestion() {
        answered = false;
        feedback.stop(); feedback.setVisible(false);
        card.color = G1_ColorBank.of(order.get(index));

        List<String> names = new ArrayList<String>();
        Collections.addAll(names, G1_ColorBank.EASY_NAMES);
        Collections.shuffle(names);
        for (int i = 0; i < answers.length; i++) {
            answers[i].setText(names.get(i));
            answers[i].setSkin(TOOL_BUTTON.Skin.ORANGE);
            answers[i].setEnabled(true);
        }
        chip.setText("Question " + (index + 1) + " / " + order.size());
        qLabel.setText("What color is this? Choose from the buttons below.");
        next.setEnabled(false);
        card.repaint();
    }

    private void choose(TOOL_BUTTON picked) {
        if (answered) return;
        answered = true;
        String correct = order.get(index), sel = picked.getText();
        boolean ok = sel.equals(correct);
        results.add(new G1_ResultBoard.Row(correct, sel, ok));

        for (TOOL_BUTTON b : answers) {
            if (b.getText().equals(correct)) b.setSkin(TOOL_BUTTON.Skin.GREEN);
            else if (b == picked)            b.setSkin(TOOL_BUTTON.Skin.RED);
            else                             b.setEnabled(false);
        }
        qLabel.setText(ok ? "GREAT! You got the correct answer.\nIt is the color " + correct + "."
                          : "SORRY! That is not right.\nIt is the color " + correct + ".");
        feedback = replaceFeedback(ok ? "/ICONS/CORRECT_ICON.gif" : "/ICONS/INCORRECT_ICON.gif");
        feedback.play();
        next.setEnabled(true);
        next.setText(index + 1 >= order.size() ? "RESULTS" : "NEXT");
    }

    private TOOL_GIF_VIEW replaceFeedback(String path) {
        stage.remove(feedback);
        TOOL_GIF_VIEW v = new TOOL_GIF_VIEW(path);
        stage.place(v, 345, 32, 510, 216);
        stage.setComponentZOrder(v, 0);
        return v;
    }

    private void advance() {
        if (!answered) return;
        index++;
        if (index < order.size()) { showQuestion(); return; }
        feedback.stop(); feedback.setVisible(false);
        setQuizVisible(false);
        board.setRows(results);
        board.setVisible(true);
        again.setVisible(true);
        stage.repaint();
    }

    // ── the big colour card ─────────────────────────────────────────────────
    private static class ColorCard extends JComponent {
        private static final long serialVersionUID = 1L;
        Color color = Color.ORANGE;
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            TOOL_THEME.smooth(g);
            int w = getWidth(), h = getHeight();
            float arc = h * 0.16f, b = Math.max(6f, h * 0.045f);
            g.setColor(new Color(0, 0, 0, 50));
            g.fill(new RoundRectangle2D.Float(4, 8, w - 8, h - 8, arc, arc));
            g.setColor(TOOL_THEME.RUBY);       g.fill(new RoundRectangle2D.Float(0, 0, w - 4, h - 6, arc, arc));
            g.setColor(TOOL_THEME.MAYA_BLUE);  g.fill(new RoundRectangle2D.Float(b, b, w - 4 - 2 * b, h - 6 - 2 * b, arc * .8f, arc * .8f));
            g.setColor(Color.WHITE);           g.fill(new RoundRectangle2D.Float(2 * b, 2 * b, w - 4 - 4 * b, h - 6 - 4 * b, arc * .6f, arc * .6f));
            g.setColor(color);                 g.fill(new RoundRectangle2D.Float(3 * b, 3 * b, w - 4 - 6 * b, h - 6 - 6 * b, arc * .5f, arc * .5f));
            g.setColor(new Color(255, 255, 255, 70)); g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(3 * b + 6, 3 * b + 6, w - 4 - 6 * b - 12, (h - 6 - 6 * b) * .45f, arc * .4f, arc * .4f));
            g.dispose();
        }
    }
}
