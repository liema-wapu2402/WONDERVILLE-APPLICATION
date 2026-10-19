package o2_GAME_CLASSES.G2_Spelling_Quiz;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_LABEL;
import o3_TOOLS_CLASSES.TOOL_POPUP;
import o3_TOOLS_CLASSES.TOOL_ResultBoard;
import o3_TOOLS_CLASSES.TOOL_RoundedPanel;
import o3_TOOLS_CLASSES.TOOL_THEME;

/**
 * SPELLING QUIZ engine shared by EASY (shapes) and HARD (fruits and vegetables).
 * A picture is shown, the player rebuilds the word from scrambled letter tiles (tap or type),
 * presses SUBMIT, and the reaction GIFs in /G2/ICONS answer: EMPTY, LETTERS (not all filled), CORRECT, INCORRECT.
 */
abstract class SPELL_GAME {

    /** One word: the text to spell and the picture file. */
    static final class Item {
        final String word, image;
        Item(String word, String image) { this.word = word; this.image = image; }
    }

    private final TOOL_GIF_PANEL stage;
    private final List<Item> pool;
    private final int questions, extraLetters;
    private final String title;

    private final List<Item> order = new ArrayList<Item>();
    private final List<TOOL_ResultBoard.Row> results = new ArrayList<TOOL_ResultBoard.Row>();
    private int index;
    private boolean answered, popupOpen;

    private PicCard pic;
    private SpellBoard board;
    private TOOL_LABEL hint, chip;
    private TOOL_RoundedPanel chipBg;
    private TOOL_BUTTON clear, main, again;
    private TOOL_ResultBoard resultBoard;

    SPELL_GAME(TOOL_GIF_PANEL stage, String title, List<Item> pool, int questions, int extraLetters) {
        this.stage = stage; this.title = title; this.pool = pool; this.questions = Math.min(questions, pool.size());
        this.extraLetters = extraLetters;
    }

    /** call from the subclass constructor */
    final void begin() { build(); bindKeys(); start(); }

    // ── UI ──────────────────────────────────────────────────────────────────
    private void build() {
        pic = new PicCard();
        stage.place(pic, 110, 70, 350, 350);

        chipBg = new TOOL_RoundedPanel(40, TOOL_THEME.RUBY).setOutline(Color.WHITE, 4f);
        stage.place(chipBg, 900, 24, 250, 48);
        chip = new TOOL_LABEL("", 22, Color.WHITE, true);
        stage.place(chip, 910, 26, 230, 44);

        hint = new TOOL_LABEL("Spell the word!", 34, TOOL_THEME.RUBY, true).setOutline(Color.WHITE);
        stage.place(hint, 500, 50, 560, 54);

        board = new SpellBoard();
        stage.place(board, 500, 108, 560, 330);

        clear = new TOOL_BUTTON("CLEAR", TOOL_BUTTON.Icon.RETRY, TOOL_BUTTON.Skin.LIGHT);
        clear.addActionListener(e -> { if (!popupOpen) board.clear(); });
        stage.place(clear, 500, 446, 240, 78);

        main = new TOOL_BUTTON("SUBMIT", TOOL_BUTTON.Icon.CHECK, TOOL_BUTTON.Skin.HOT);
        main.addActionListener(e -> onMain());
        stage.place(main, 780, 446, 280, 78);

        resultBoard = new TOOL_ResultBoard(title + " RESULTS", "Word", "Your answer");
        resultBoard.setVisible(false);
        stage.place(resultBoard, 250, 20, 700, 470);

        again = new TOOL_BUTTON("TRY AGAIN", TOOL_BUTTON.Icon.RETRY, TOOL_BUTTON.Skin.HOT);
        again.addActionListener(e -> start());
        again.setVisible(false);
        stage.place(again, 470, 508, 260, 78);
    }

    private void bindKeys() {
        javax.swing.InputMap im = stage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = stage.getActionMap();
        for (char c = 'A'; c <= 'Z'; c++) {
            final char ch = c;
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A + (c - 'A'), 0), "type" + c);
            am.put("type" + c, new AbstractAction() {
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) { if (!popupOpen && !answered) board.type(ch); }
            });
        }
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "back");
        am.put("back", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) { if (!popupOpen) board.backspace(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        am.put("enter", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) { if (!popupOpen && main.isVisible()) onMain(); }
        });
    }

    // ── flow ────────────────────────────────────────────────────────────────
    private void start() {
        order.clear();
        List<Item> p = new ArrayList<Item>(pool);
        Collections.shuffle(p);
        order.addAll(p.subList(0, questions));
        results.clear();
        index = 0;
        setQuizVisible(true);
        resultBoard.setVisible(false);
        again.setVisible(false);
        showQuestion();
    }

    private void setQuizVisible(boolean v) {
        pic.setVisible(v); board.setVisible(v); hint.setVisible(v); chip.setVisible(v); chipBg.setVisible(v);
        clear.setVisible(v); main.setVisible(v);
    }

    private void showQuestion() {
        answered = false;
        Item it = order.get(index);
        pic.set(it.image);
        board.setWord(it.word, extraLetters);
        chip.setText("Word " + (index + 1) + " / " + order.size());
        chipBg.setFill(TOOL_THEME.RUBY);
        hint.setText("Spell the word!");
        hint.setColor(TOOL_THEME.RUBY);
        clear.setEnabled(true);
        main.setText("SUBMIT");
        main.setIconType(TOOL_BUTTON.Icon.CHECK);
        stage.repaint();
    }

    private void onMain() {
        if (popupOpen) return;
        if (answered) { advance(); return; }

        if (board.isEmpty()) { popup("/G2/ICONS/EMPTY.gif", 2600, null); return; }
        if (!board.isFull()) { popup("/G2/ICONS/LETTERS.gif", 3000, null); return; }

        Item it = order.get(index);
        String ans = board.answer();
        boolean ok = ans.equals(it.word);
        answered = true;
        results.add(new TOOL_ResultBoard.Row(it.word, ans, ok, null));
        board.lock(ok, it.word);
        clear.setEnabled(false);
        chipBg.setFill(ok ? TOOL_THEME.GREEN : TOOL_THEME.RUBY);
        hint.setText(ok ? "Correct!" : "Not quite!");
        hint.setColor(ok ? new Color(0, 130, 60) : TOOL_THEME.RUBY);
        final boolean last = index + 1 >= order.size();
        popup(ok ? "/G2/ICONS/CORRECT.gif" : "/G2/ICONS/INCORRECT.gif", ok ? 3600 : 3300, () -> {
            main.setText(last ? "RESULTS" : "NEXT");
            main.setIconType(TOOL_BUTTON.Icon.NEXT2);
        });
    }

    private void popup(String gif, int millis, final Runnable after) {
        popupOpen = true;
        TOOL_POPUP.show(stage, gif, 300.0 / 250.0, 480, millis, () -> {
            popupOpen = false;
            if (after != null) after.run();
        });
    }

    private void advance() {
        index++;
        if (index < order.size()) { showQuestion(); return; }
        setQuizVisible(false);
        resultBoard.setRows(results);
        resultBoard.setVisible(true);
        again.setVisible(true);
        stage.repaint();
    }

    // ── picture card ────────────────────────────────────────────────────────
    private static class PicCard extends JComponent {
        private static final long serialVersionUID = 1L;
        private Image img;
        void set(String path) {
            img = null;
            try { URL u = SPELL_GAME.class.getResource(path); if (u != null) img = ImageIO.read(u); }
            catch (Exception e) { e.printStackTrace(); }
            repaint();
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            TOOL_THEME.smooth(g);
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int w = getWidth(), h = getHeight(); float arc = h * 0.12f, b = Math.max(5f, h * 0.03f);
            g.setColor(new Color(0, 0, 0, 50));    g.fill(new RoundRectangle2D.Float(4, 8, w - 8, h - 8, arc, arc));
            g.setColor(TOOL_THEME.RUBY);           g.fill(new RoundRectangle2D.Float(0, 0, w - 4, h - 6, arc, arc));
            g.setColor(Color.WHITE);               g.fill(new RoundRectangle2D.Float(b, b, w - 4 - 2 * b, h - 6 - 2 * b, arc * .8f, arc * .8f));
            if (img != null) {
                g.setClip(new RoundRectangle2D.Float(b, b, w - 4 - 2 * b, h - 6 - 2 * b, arc * .8f, arc * .8f));
                g.drawImage(img, (int) b, (int) b, w - 4 - 2 * (int) b, h - 6 - 2 * (int) b, null);
            }
            g.dispose();
        }
    }
}
