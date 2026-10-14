package o1_MAIN_CLASSES;

import java.awt.Color;

import o3_TOOLS_CLASSES.TOOL_BUTTON;
import o3_TOOLS_CLASSES.TOOL_CARD;
import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;
import o3_TOOLS_CLASSES.TOOL_OVERLAY;
import o3_TOOLS_CLASSES.TOOL_Screen;
import o3_TOOLS_CLASSES.TOOL_THEME;

/** ABOUT - five tap-to-read cards (3 + 2). Text opens in an in-window card. */
public class SUB_CLASS_2_ABOUT implements TOOL_Screen {

    private final MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame;

    private static final Color C_ORANGE = new Color(255, 165, 77);

    // { label, emoji, accent, html-body }
    private static final Object[][] CARDS = {
        { "About Wonderville", "\uD83C\uDFE1", TOOL_THEME.RUBY,
            "Welcome to <b>Wonderville!</b><br><br>"
          + "Wonderville is an innovative application designed to enhance children's learning "
          + "through interactive visual aids and engaging games.<br><br>"
          + "Wonderville aims to make learning fun and effective for children, while also "
          + "providing tools for parents and educators to support their teaching efforts." },
        { "Key Features", "\u2B50", TOOL_THEME.SUN,
            "<b>What can you do in Wonderville?</b><br><br>"
          + "&#9679;&nbsp;<b>Color Quiz</b> &mdash; Enhance color recognition skills<br><br>"
          + "&#9679;&nbsp;<b>Spelling Game</b> &mdash; Improve spelling one word at a time<br><br>"
          + "&#9679;&nbsp;<b>Animal Quiz</b> &mdash; Learn about different animals<br><br>"
          + "&#9679;&nbsp;<b>Educational Videos</b> &mdash; Visual learning through fun videos<br><br>"
          + "&#9679;&nbsp;<b>Rhyme Songs</b> &mdash; Fun and educational rhymes for kids" },
        { "Our Mission", "\uD83C\uDFAF", TOOL_THEME.SKY,
            "<b>What drives us?</b><br><br>"
          + "At Wonderville, our mission is to provide an intuitive and seamless learning "
          + "experience that helps children achieve their educational goals efficiently "
          + "and enjoyably.<br><br>"
          + "We believe in fostering a love for learning through interactive and engaging "
          + "methods that inspire curiosity and creativity in every child." },
        { "The Developer", "\uD83D\uDCBB", C_ORANGE,
            "<b>Who built Wonderville?</b><br><br>"
          + "Wonderville was created by <b>Amiel Andrei C. Reyes</b>, a passionate "
          + "21-year-old Computer Engineering student at Southern Luzon State University.<br><br>"
          + "Amiel developed Wonderville to address common challenges in children's education "
          + "by making learning more interactive, fun, and accessible to everyone." },
        { "Contact Us", "\uD83D\uDCE7", TOOL_THEME.GREEN,
            "<b>We'd love to hear from you!</b><br><br>"
          + "&#128231;&nbsp;<b>Email:</b>&nbsp;&nbsp;aacreyes@slsu.edu.ph<br><br>"
          + "&#128100;&nbsp;<b>Facebook:</b>&nbsp;&nbsp;facebook.com/wonderville<br><br>"
          + "&#128279;&nbsp;<b>LinkedIn:</b>&nbsp;&nbsp;Amiel Andrei Reyes<br><br>"
          + "&#128025;&nbsp;<b>GitHub:</b>&nbsp;&nbsp;github.com/liema-wapu2402<br><br>"
          + "Thank you for choosing Wonderville! We hope you enjoy using the app "
          + "as much as we enjoyed creating it." },
    };

    public SUB_CLASS_2_ABOUT(MAIN_CLASS_WONDERVILLE_APPLICATION mainFrame) { this.mainFrame = mainFrame; }

    public void show() { mainFrame.open(this); }

    public String background()  { return "/SUB2_BG_COVER_ABOUT.gif"; }
    public String loadingText() { return "Loading About..."; }

    public void build(final TOOL_GIF_PANEL stage) {
        int cw = 256, ch = 172, gap = 22;
        int x3 = 600 - (3 * cw + 2 * gap) / 2, x2 = 600 - (2 * cw + gap) / 2;
        int[][] pos = {                                   // 3 on top, 2 centred below
            { x3, 150 }, { x3 + cw + gap, 150 }, { x3 + 2 * (cw + gap), 150 },
            { x2, 150 + ch + 20 }, { x2 + cw + gap, 150 + ch + 20 },
        };
        for (int i = 0; i < CARDS.length; i++) {
            final Object[] c = CARDS[i];
            stage.place(new TOOL_CARD((String) c[0], (String) c[1], (Color) c[2], "Tap to read",
                () -> TOOL_OVERLAY.showText(stage, (String) c[0], (String) c[1], (Color) c[2], (String) c[3])),
                pos[i][0], pos[i][1], cw, ch);
        }
        TOOL_BUTTON back = new TOOL_BUTTON("BACK", TOOL_BUTTON.Icon.BACK, TOOL_BUTTON.Skin.ORANGE);
        back.addActionListener(e -> mainFrame.showMainMenu());
        stage.place(back, 66, 546, 244, 92);
    }
}
