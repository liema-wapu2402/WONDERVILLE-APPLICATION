package o1_MAIN_CLASSES;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_Screen;

/** EXIT - "Are you sure?" YES quits, NO returns to the main menu. */
public class SUB_CLASS_5_EXIT implements TOOL_Screen {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;

    public SUB_CLASS_5_EXIT(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame) { this.mainFrame = mainFrame; }

    public void show() { mainFrame.open(this); }

    public String background()  { return "/SUB5_BG_COVER_EXIT.gif"; }
    public String loadingText() { return "Loading..."; }

    public void build(TOOL_GIF_PANEL stage) {
        TOOL_BUTTON yes = new TOOL_BUTTON("YES");
        yes.addActionListener(e -> System.exit(0));
        stage.place(yes, 296, 383, 296, 90);

        TOOL_BUTTON no = new TOOL_BUTTON("NO");
        no.addActionListener(e -> mainFrame.showMainMenu());
        stage.place(no, 698, 383, 296, 90);
    }
}
