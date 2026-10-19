package o2_GAME_CLASSES;

import javax.swing.Timer;

import o1_MAIN_CLASSES.MAIN_CLASS_WONDERVILLE_APPLICATION;
import o1_MAIN_CLASSES.Menu_Feautures;
import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_LABEL;
import o3_TOOLS_CLASSES.TOOL_Screen;
import o3_TOOLS_CLASSES.TOOL_THEME;

/** GAME 2 - SPELLING QUIZ.   intro (auto / click)  ->  level select  ->  play screen. */
public class GAME_2_SpellingQuiz {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;
    private final Menu_Feautures backmenu;

    public GAME_2_SpellingQuiz(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame, Menu_Feautures backmenu) {
        this.mainFrame = mainFrame;
        this.backmenu = backmenu;
    }

    public void DesignStructure() { mainFrame.open(new IntroScreen()); }

    private class IntroScreen implements TOOL_Screen {
        public String background()  { return "/G2/G2_BG_COVER_START.gif"; }
        public String loadingText() { return "Loading Spelling Quiz..."; }
        public void build(final TOOL_GIF_PANEL stage) {
            final boolean[] done = { false };
            final Runnable next = () -> { if (!done[0]) { done[0] = true; mainFrame.open(new LevelScreen()); } };
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

    private class LevelScreen implements TOOL_Screen {
        public String background()  { return "/G2/G2_LEVEL.gif"; }
        public String loadingText() { return "Loading..."; }
        public void build(TOOL_GIF_PANEL stage) {
            TOOL_BUTTON easy = new TOOL_BUTTON("EASY", TOOL_BUTTON.Icon.NONE, TOOL_BUTTON.Skin.ORANGE);
            easy.addActionListener(e -> mainFrame.open(new PlayScreen(false)));
            stage.place(easy, 258, 300, 268, 96);
            TOOL_BUTTON hard = new TOOL_BUTTON("HARD", TOOL_BUTTON.Icon.NONE, TOOL_BUTTON.Skin.ORANGE);
            hard.addActionListener(e -> mainFrame.open(new PlayScreen(true)));
            stage.place(hard, 666, 300, 268, 96);
            TOOL_BUTTON exit = new TOOL_BUTTON("EXIT", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
            exit.addActionListener(e -> backmenu.show());
            stage.place(exit, 56, 560, 214, 86);
        }
    }

    private class PlayScreen implements TOOL_Screen {
        private final boolean hard;
        PlayScreen(boolean hard) { this.hard = hard; }
        public String background()  { return "/G2/G2_PANEL.gif"; }
        public String loadingText() { return "Loading..."; }
        public void build(TOOL_GIF_PANEL stage) {
            if (hard) new o2_GAME_CLASSES.G2_Spelling_Quiz.HARD_GAME(stage);
            else      new o2_GAME_CLASSES.G2_Spelling_Quiz.EASY_GAME(stage);
            TOOL_BUTTON back = new TOOL_BUTTON("EXIT", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
            back.addActionListener(e -> mainFrame.open(new LevelScreen()));
            stage.place(back, 56, 560, 214, 86);
        }
    }
}
