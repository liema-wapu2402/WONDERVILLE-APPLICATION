package o1_MAIN_CLASSES;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_Screen;

/** CREDITS - the credit card is part of the GIF artwork, so only the Back button is needed. */
public class SUB_CLASS_3_CREDITS implements TOOL_Screen {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;

    public SUB_CLASS_3_CREDITS(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame) { this.mainFrame = mainFrame; }

    public void show() { mainFrame.open(this); }

    public String background()  { return "/SUB3_BG_COVER_CREDIT.gif"; }
    public String loadingText() { return "Loading Credits..."; }

    public void build(TOOL_GIF_PANEL stage) {
        TOOL_BUTTON back = new TOOL_BUTTON("BACK", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
        back.addActionListener(e -> mainFrame.showMainMenu());
        stage.place(back, 66, 546, 244, 92);
    }
}
