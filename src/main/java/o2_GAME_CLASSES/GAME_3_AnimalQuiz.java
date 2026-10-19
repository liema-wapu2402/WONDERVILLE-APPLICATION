package o2_GAME_CLASSES;

import javax.swing.Timer;

import o1_MAIN_CLASSES.MAIN_CLASS_WONDERVILLE_APPLICATION;
import o1_MAIN_CLASSES.Menu_Feautures;
import o2_GAME_CLASSES.G3_Animal_Quiz.EASY_GAME;
import o2_GAME_CLASSES.G3_Animal_Quiz.G3_AnimalBank.Habitat;
import o2_GAME_CLASSES.G3_Animal_Quiz.HARD_GAME;
import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_HIT;
import o3_TOOLS_CLASSES.TOOL_LABEL;
import o3_TOOLS_CLASSES.TOOL_Screen;
import o3_TOOLS_CLASSES.TOOL_THEME;

/** GAME 3 - ANIMAL QUIZ.   intro  ->  habitat (Land / Water / Air)  ->  level  ->  play. */
public class GAME_3_AnimalQuiz {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;
    private final Menu_Feautures backmenu;

    public GAME_3_AnimalQuiz(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame, Menu_Feautures backmenu) {
        this.mainFrame = mainFrame;
        this.backmenu = backmenu;
    }

    public void DesignStructure() { mainFrame.open(new IntroScreen()); }

    // 1. intro
    private class IntroScreen implements TOOL_Screen {
        public String background()  { return "/G3/G3_BG_COVER_START.gif"; }
        public String loadingText() { return "Loading Animal Quiz..."; }
        public void build(final TOOL_GIF_PANEL stage) {
            final boolean[] done = { false };
            final Runnable next = () -> { if (!done[0]) { done[0] = true; mainFrame.open(new HabitatScreen()); } };
            final Timer t = new Timer(3200, e -> next.run());
            t.setRepeats(false);
            t.start();
            stage.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mousePressed(java.awt.event.MouseEvent e) { t.stop(); next.run(); }
            });
            stage.place(new TOOL_LABEL("Click anywhere to start", 26, TOOL_THEME.RUBY, true).setOutline(java.awt.Color.WHITE),
                        350, 600, 500, 44);
        }
    }

    // 2. choose a type of animal
    private class HabitatScreen implements TOOL_Screen {
        public String background()  { return "/G3/G3_ANIMAL_MENU.gif"; }
        public String loadingText() { return "Loading..."; }
        public void build(TOOL_GIF_PANEL stage) {
            card(stage, Habitat.LAND,  176, 200, 254, 326, 300, 482);
            card(stage, Habitat.WATER, 476, 200, 260, 326, 607, 482);
            card(stage, Habitat.AIR,   784, 200, 256, 326, 917, 482);
            TOOL_BUTTON back = new TOOL_BUTTON("BACK", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
            back.addActionListener(e -> backmenu.show());
            stage.place(back, 13, 578, 212, 86);
        }
        private void card(TOOL_GIF_PANEL stage, final Habitat h, int x, int y, int w, int hgt, int cx, int cy) {
            TOOL_BUTTON pill = new TOOL_BUTTON(h.label.toUpperCase());
            Runnable go = () -> mainFrame.open(new LevelScreen(h));
            pill.addActionListener(e -> go.run());
            stage.place(new TOOL_HIT(pill, go), x, y, w, hgt);
            stage.place(pill, cx - 98, cy - 28, 196, 56);
        }
    }

    // 3. level
    private class LevelScreen implements TOOL_Screen {
        private final Habitat h;
        LevelScreen(Habitat h) { this.h = h; }
        public String background()  { return h.levelGif(); }
        public String loadingText() { return "Loading..."; }
        public void build(TOOL_GIF_PANEL stage) {
            TOOL_BUTTON easy = new TOOL_BUTTON("EASY", TOOL_BUTTON.Icon.NONE, TOOL_BUTTON.Skin.ORANGE);
            easy.addActionListener(e -> mainFrame.open(new PlayScreen(h, false)));
            stage.place(easy, 258, 300, 268, 96);
            TOOL_BUTTON hard = new TOOL_BUTTON("HARD", TOOL_BUTTON.Icon.NONE, TOOL_BUTTON.Skin.ORANGE);
            hard.addActionListener(e -> mainFrame.open(new PlayScreen(h, true)));
            stage.place(hard, 666, 300, 268, 96);
            TOOL_BUTTON exit = new TOOL_BUTTON("EXIT", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
            exit.addActionListener(e -> mainFrame.open(new HabitatScreen()));
            stage.place(exit, 56, 560, 214, 86);
        }
    }

    // 4. play
    private class PlayScreen implements TOOL_Screen {
        private final Habitat h; private final boolean hard;
        PlayScreen(Habitat h, boolean hard) { this.h = h; this.hard = hard; }
        public String background()  { return h.panelGif(); }
        public String loadingText() { return "Loading..."; }
        public void build(TOOL_GIF_PANEL stage) {
            if (hard) new HARD_GAME(stage, h); else new EASY_GAME(stage, h);
            TOOL_BUTTON back = new TOOL_BUTTON("EXIT", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
            back.addActionListener(e -> mainFrame.open(new LevelScreen(h)));
            stage.place(back, 56, 560, 214, 86);
        }
    }
}
