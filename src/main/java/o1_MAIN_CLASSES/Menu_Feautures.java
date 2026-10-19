package o1_MAIN_CLASSES;



import o2_GAME_CLASSES.GAME_1_ColorQuiz;
import o2_GAME_CLASSES.GAME_2_SpellingQuiz;
import o2_GAME_CLASSES.GAME_3_AnimalQuiz;
import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_HIT;
import o3_TOOLS_CLASSES.TOOL_Screen;

/**
 * Visual Aids menu (kind 1) and Games menu (kind 2).
 * The whole card in the artwork is clickable (v1 only reacted on the small label); hovering a card
 * lights up its label button. Visual-aid items are not built yet -> "Coming soon!".
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
            card(stage, "SPELLING GAME",  470, 228, 260, 322, 600, 506, 196, 54, () -> new GAME_2_SpellingQuiz(mainFrame, this).DesignStructure());
            card(stage, "ANIMAL QUIZ",    756, 228, 260, 322, 885, 506, 196, 54, () -> new GAME_3_AnimalQuiz(mainFrame, this).DesignStructure());
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
        TOOL_HIT hit = new TOOL_HIT(pill, action);
        stage.place(hit,  hx, hy, hw, hh);          // placed first => underneath the pill
        stage.place(pill, cx - bw / 2, cy - bh / 2, bw, bh);
    }
}
