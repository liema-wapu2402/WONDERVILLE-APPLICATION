package o1_MAIN_CLASSES;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_Screen;

/** START -> "Choose a learning": Visual Aids / Games / Back. */
public class SUB_CLASS_1_START_MENU implements TOOL_Screen {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;

    public SUB_CLASS_1_START_MENU(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame) { this.mainFrame = mainFrame; }

    public void show() { mainFrame.open(this); }

    public String background()  { return "/SUB1_BG_COVER_START.gif"; }
    public String loadingText() { return "Loading Menu..."; }

    public void build(TOOL_GIF_PANEL stage) {
        TOOL_BUTTON va = new TOOL_BUTTON("VISUAL AIDS");
        va.addActionListener(e -> new Menu_Feautures(mainFrame, 1).show());
        stage.place(va, 312, 344, 296, 90);

        TOOL_BUTTON games = new TOOL_BUTTON("GAMES");
        games.addActionListener(e -> new Menu_Feautures(mainFrame, 2).show());
        stage.place(games, 648, 344, 296, 90);

        TOOL_BUTTON back = new TOOL_BUTTON("BACK", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
        back.addActionListener(e -> mainFrame.showMainMenu());
        stage.place(back, 66, 546, 244, 92);
    }
}
