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

/** ANIMAL QUIZ - HARD: read an animal's name and tap its picture among the 12 animals of the habitat (10 questions). */
public class HARD_GAME {

    private static final int QUESTIONS = 10;

    private final TOOL_GIF_PANEL stage;
    private final Habitat habitat;
    private final List<Animal> pool;
    private final List<Animal> order = new ArrayList<Animal>();
    private final List<TOOL_ResultBoard.Row> results = new ArrayList<TOOL_ResultBoard.Row>();
    private int index;
    private boolean answered, popupOpen;

    private AnimalCard[] cells;
    private final Animal[] cellAnimal;
    private TOOL_RoundedPanel qPanel;
    private TOOL_LABEL qLabel;
    private TOOL_BUTTON next, again;
    private TOOL_ResultBoard board;

    public HARD_GAME(TOOL_GIF_PANEL stage, Habitat habitat) {
        this.stage = stage; this.habitat = habitat; this.pool = G3_AnimalBank.hard(habitat);
        this.cellAnimal = new Animal[pool.size()];
        build();
        start();
    }

    private void build() {
        qPanel = new TOOL_RoundedPanel(40, TOOL_THEME.MAYA_BLUE).setOutline(Color.WHITE, 5f);
        qPanel.setShadow(true);
        stage.place(qPanel, 240, 8, 720, 76);
        qLabel = new TOOL_LABEL("", 28, TOOL_THEME.INK, true);
        stage.place(qLabel, 260, 12, 680, 68);

        cells = new AnimalCard[pool.size()];
        for (int i = 0; i < cells.length; i++) {
            final int idx = i;
            cells[i] = new AnimalCard(TOOL_THEME.RUBY, () -> choose(idx));
            stage.place(cells[i], 285 + (i % 4) * 160, 98 + (i / 4) * 122, 150, 114);
        }

        next = new TOOL_BUTTON("NEXT", TOOL_BUTTON.Icon.NEXT2, TOOL_BUTTON.Skin.HOT);
        next.addActionListener(e -> advance());
        stage.place(next, 912, 556, 230, 82);

        board = new TOOL_ResultBoard(habitat.label.toUpperCase() + " \u2022 HARD RESULTS", "Animal", "Your answer");
        board.setVisible(false);
        stage.place(board, 200, 10, 800, 520);
        again = new TOOL_BUTTON("TRY AGAIN", TOOL_BUTTON.Icon.RETRY, TOOL_BUTTON.Skin.HOT);
        again.addActionListener(e -> start());
        again.setVisible(false);
        stage.place(again, 470, 544, 260, 78);
    }

    private void start() {
        List<Animal> p = new ArrayList<Animal>(pool);
        Collections.shuffle(p);
        order.clear(); order.addAll(p.subList(0, Math.min(QUESTIONS, p.size())));
        results.clear(); index = 0;
        setQuizVisible(true);
        board.setVisible(false); again.setVisible(false);
        showQuestion();
    }

    private void setQuizVisible(boolean v) {
        qPanel.setVisible(v); qLabel.setVisible(v); next.setVisible(v);
        for (AnimalCard c : cells) c.setVisible(v);
    }

    private void showQuestion() {
        answered = false;
        List<Animal> shuffled = new ArrayList<Animal>(pool);
        Collections.shuffle(shuffled);
        for (int i = 0; i < cells.length; i++) { cellAnimal[i] = shuffled.get(i); cells[i].setImage(shuffled.get(i).hardImage()); }
        qLabel.setText("Question " + (index + 1) + " of " + order.size() + "  \u2022  Tap the " + order.get(index).name.toUpperCase() + "!");
        qLabel.setColor(TOOL_THEME.INK);
        next.setEnabled(false);
        next.setText("NEXT");
        stage.repaint();
    }

    private void choose(int idx) {
        if (answered || popupOpen) return;
        answered = true;
        Animal target = order.get(index), picked = cellAnimal[idx];
        boolean ok = picked == target;
        results.add(new TOOL_ResultBoard.Row(target.name, picked.name, ok, null));
        for (int i = 0; i < cells.length; i++) {
            if (cellAnimal[i] == target) cells[i].setState(AnimalCard.State.CORRECT);
            else if (i == idx)           cells[i].setState(AnimalCard.State.WRONG);
            else                         cells[i].setState(AnimalCard.State.DIMMED);
        }
        qLabel.setText(ok ? "CORRECT! That is the " + target.name + "!" : "INCORRECT! You picked the " + picked.name + ".");
        qLabel.setColor(ok ? new Color(0, 110, 40) : new Color(170, 0, 40));
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
