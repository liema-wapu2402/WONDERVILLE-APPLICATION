package o1_MAIN_CLASSES;

import java.awt.Color;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_CARD;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_OVERLAY;
import o3_TOOLS_CLASSES.TOOL_Screen;
import o3_TOOLS_CLASSES.TOOL_THEME;

/** HELP - one card per topic; each opens its how-to picture in an in-window card. */
public class SUB_CLASS_4_HELP implements TOOL_Screen {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;

    // { label, emoji, image, accent }
    private static final Object[][] CARDS = {
        { "Colors",    "\uD83C\uDFA8", "/01_MAIN_CLASSES/04_SC_HELP/COLOR.png",     TOOL_THEME.RUBY  },
        { "Spelling",  "\u270F",       "/01_MAIN_CLASSES/04_SC_HELP/SPELLING.png",  TOOL_THEME.SUN   },
        { "Animals",   "\uD83D\uDC3E", "/01_MAIN_CLASSES/04_SC_HELP/ANIMAL.png",    TOOL_THEME.SKY   },
        { "Education", "\uD83D\uDCDA", "/01_MAIN_CLASSES/04_SC_HELP/EDUCATION.png", TOOL_THEME.GREEN },
        { "Rhymes",    "\uD83C\uDFB5", "/01_MAIN_CLASSES/04_SC_HELP/RHYME.png",     new Color(255, 165, 77) },
    };

    public SUB_CLASS_4_HELP(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame) { this.mainFrame = mainFrame; }

    public void show() { mainFrame.open(this); }

    public String background()  { return "/SUB4_BG_COVER_HELP.gif"; }
    public String loadingText() { return "Loading Help..."; }

    public void build(final TOOL_GIF_PANEL stage) {
        int cw = 256, ch = 172, gap = 22;
        int x3 = 600 - (3 * cw + 2 * gap) / 2, x2 = 600 - (2 * cw + gap) / 2;
        int[][] pos = {
            { x3, 150 }, { x3 + cw + gap, 150 }, { x3 + 2 * (cw + gap), 150 },
            { x2, 150 + ch + 20 }, { x2 + cw + gap, 150 + ch + 20 },
        };
        for (int i = 0; i < CARDS.length; i++) {
            final Object[] c = CARDS[i];
            stage.place(new TOOL_CARD((String) c[0], (String) c[1], (Color) c[3], "Tap to view",
                () -> TOOL_OVERLAY.showImage(stage, (String) c[0], (String) c[1], (Color) c[3], (String) c[2])),
                pos[i][0], pos[i][1], cw, ch);
        }
        TOOL_BUTTON back = new TOOL_BUTTON("BACK", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
        back.addActionListener(e -> mainFrame.showMainMenu());
        stage.place(back, 66, 546, 244, 92);
    }
}
