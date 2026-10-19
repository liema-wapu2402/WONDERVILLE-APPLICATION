package o2_GAME_CLASSES.G3_Animal_Quiz;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import o2_GAME_CLASSES.G3_Animal_Quiz.G3_AnimalBank.Animal;
import o2_GAME_CLASSES.G3_Animal_Quiz.G3_AnimalBank.Habitat;
import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_LABEL;
import o3_TOOLS_CLASSES.TOOL_POPUP;
import o3_TOOLS_CLASSES.TOOL_ResultBoard;
import o3_TOOLS_CLASSES.TOOL_RoundedPanel;
import o3_TOOLS_CLASSES.TOOL_THEME;

/** ANIMAL QUIZ - EASY: a picture of an animal from the chosen habitat, pick its name from four buttons. */
public class EASY_GAME {

    private final TOOL_GIF_PANEL stage;
    private final Habitat habitat;
    private final List<Animal> pool;
    private final List<Animal> order = new ArrayList<Animal>();
    private final List<TOOL_ResultBoard.Row> results = new ArrayList<TOOL_ResultBoard.Row>();
    private int index;
    private boolean answered, popupOpen;

    private AnimalCard card;
    private TOOL_RoundedPanel chipBg;
    private TOOL_LABEL chip;
    private TOOL_BUTTON[] answers = new TOOL_BUTTON[4];
    private TOOL_BUTTON next, again;
    private TOOL_ResultBoard board;

    public EASY_GAME(TOOL_GIF_PANEL stage, Habitat habitat) {
        this.stage = stage; this.habitat = habitat; this.pool = G3_AnimalBank.easy(habitat);
        build();
        start();
    }

    private void build() {
        chipBg = new TOOL_RoundedPanel(40, TOOL_THEME.RUBY).setOutline(Color.WHITE, 4f);
        stage.place(chipBg, 330, 10, 540, 48);
        chip = new TOOL_LABEL("", 22, Color.WHITE, true);
        stage.place(chip, 342, 12, 516, 44);

        card = new AnimalCard(TOOL_THEME.RUBY);
        stage.place(card, 445, 68, 310, 290);

        for (int i = 0; i < 4; i++) {
            final TOOL_BUTTON b = new TOOL_BUTTON("", TOOL_BUTTON.Icon.NONE, TOOL_BUTTON.Skin.ORANGE);
            b.addActionListener(e -> choose(b));
            answers[i] = b;
            stage.place(b, 300 + (i % 2) * 310, 376 + (i / 2) * 70, 290, 62);
        }

        next = new TOOL_BUTTON("NEXT", TOOL_BUTTON.Icon.NEXT2, TOOL_BUTTON.Skin.HOT);
        next.addActionListener(e -> advance());
        stage.place(next, 912, 556, 230, 82);

        board = new TOOL_ResultBoard(habitat.label.toUpperCase() + " \u2022 EASY RESULTS", "Animal", "Your answer");
        board.setVisible(false);
        stage.place(board, 250, 20, 700, 470);
        again = new TOOL_BUTTON("TRY AGAIN", TOOL_BUTTON.Icon.RETRY, TOOL_BUTTON.Skin.HOT);
        again.addActionListener(e -> start());
        again.setVisible(false);
        stage.place(again, 470, 508, 260, 78);
    }

    private void start() {
        order.clear(); order.addAll(pool); Collections.shuffle(order);
        results.clear(); index = 0;
        setQuizVisible(true);
        board.setVisible(false); again.setVisible(false);
        showQuestion();
    }

    private void setQuizVisible(boolean v) {
        card.setVisible(v); chip.setVisible(v); chipBg.setVisible(v); next.setVisible(v);
        for (TOOL_BUTTON b : answers) b.setVisible(v);
    }

    private void showQuestion() {
        answered = false;
        Animal a = order.get(index);
        card.setImage(a.easyImage());

        List<String> names = new ArrayList<String>();
        for (Animal o : pool) if (o != a) names.add(o.name);
        Collections.shuffle(names);
        List<String> opts = new ArrayList<String>(names.subList(0, 3));
        opts.add(a.name);
        Collections.shuffle(opts);
        for (int i = 0; i < 4; i++) {
            answers[i].setText(opts.get(i));
            answers[i].setSkin(TOOL_BUTTON.Skin.ORANGE);
            answers[i].setEnabled(true);
        }
        chip.setText("Question " + (index + 1) + " / " + order.size() + "  \u2022  What animal is this?");
        chipBg.setFill(TOOL_THEME.RUBY);
        next.setEnabled(false);
        next.setText("NEXT");
        stage.repaint();
    }

    private void choose(TOOL_BUTTON picked) {
        if (answered || popupOpen) return;
        answered = true;
        String correct = order.get(index).name, sel = picked.getText();
        boolean ok = sel.equals(correct);
        results.add(new TOOL_ResultBoard.Row(correct, sel, ok, null));
        for (TOOL_BUTTON b : answers) {
            if (b.getText().equals(correct)) b.setSkin(TOOL_BUTTON.Skin.GREEN);
            else if (b == picked)            b.setSkin(TOOL_BUTTON.Skin.RED);
            else                             b.setEnabled(false);
        }
        chipBg.setFill(ok ? TOOL_THEME.GREEN : TOOL_THEME.RUBY);
        chip.setText(ok ? "Great! It is a " + correct + "!" : "Oops! It is a " + correct + ".");
        popupOpen = true;
        TOOL_POPUP.show(stage, ok ? "/ICONS/CORRECT_ICON.gif" : "/ICONS/INCORRECT_ICON.gif", 509.0 / 286.0, 560, ok ? 2800 : 3300, () -> {
            popupOpen = false;
            next.setEnabled(true);
            next.setText(index + 1 >= order.size() ? "RESULTS" : "NEXT");
        });
    }

    private void advance() {
        if (!answered || popupOpen) return;
        index++;
        if (index < order.size()) { showQuestion(); return; }
        setQuizVisible(false);
        board.setRows(results); board.setVisible(true); again.setVisible(true);
        stage.repaint();
    }
}
