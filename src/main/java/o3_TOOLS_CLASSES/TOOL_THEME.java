package o3_TOOLS_CLASSES;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

/**
 * WONDERVILLE v2 - single source of truth for the theme.
 * Colors are taken from the artwork (ruby / maya blue / sunshine yellow / candy orange)
 * and from the original button images (orange pill + light pill with a pale-blue rim).
 */
public final class TOOL_THEME {

    private TOOL_THEME() {}

    // ── Brand colours (from the cover artwork) ──────────────────────────────
    public static final Color RUBY       = new Color(239,  24, 100);   // #EF1864
    public static final Color MAYA_BLUE  = new Color(121, 201, 250);   // #79C9FA
    public static final Color SKY        = new Color( 77, 198, 232);   // #4DC6E8
    public static final Color SUN        = new Color(255, 204,   0);   // #FFCC00
    public static final Color GREEN      = new Color( 80, 200, 120);   // #50C878
    public static final Color VIOLET     = new Color(148,   0, 211);

    // ── Button colours (sampled from BTN_*.png) ─────────────────────────────
    public static final Color ORANGE_TOP  = new Color(254, 201, 107);  // pill highlight
    public static final Color ORANGE      = new Color(251, 183, 102);  // pill body
    public static final Color ORANGE_RIM  = new Color(243, 131,  86);  // pill bottom rim
    public static final Color ORANGE_TEXT = new Color(242, 107,  29);  // text on light pill
    public static final Color LIGHT_TOP   = new Color(244, 246, 251);
    public static final Color LIGHT       = new Color(233, 235, 243);  // light pill body
    public static final Color LIGHT_RIM   = new Color(170, 217, 234);  // pale blue rim

    // ── Surfaces / text ─────────────────────────────────────────────────────
    public static final Color CREAM    = new Color(255, 249, 235);
    public static final Color INK      = new Color( 52,  34,  58);     // body text
    public static final Color INK_SOFT = new Color(110,  92, 120);
    public static final Color WOOD     = new Color(205, 118,  52);     // signboard brown from the art

    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    public static Color mix(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
            Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t),
            Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    public static Color darker(Color c, float f) {
        return new Color(Math.round(c.getRed() * (1 - f)), Math.round(c.getGreen() * (1 - f)),
                         Math.round(c.getBlue() * (1 - f)), c.getAlpha());
    }

    // ── Fonts ───────────────────────────────────────────────────────────────
    // The button artwork uses a rounded, hand-drawn display face. We pick the first one
    // installed on the machine and fall back to Comic Sans (already used by v1) / SansSerif.
    private static final String[] DISPLAY_CHOICES = {
        "Fredoka One", "Fredoka", "Baloo 2", "Baloo", "Lilita One", "Chewy",
        "Comic Sans MS", "Arial Rounded MT Bold", "Segoe UI Black", "SansSerif"
    };
    private static final String[] BODY_CHOICES = {
        "Comic Sans MS", "Segoe UI", "Arial", "SansSerif"
    };
    private static String displayFamily, bodyFamily;

    private static String pick(String[] choices) {
        Set<String> have = new HashSet<String>();
        for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) have.add(s);
        for (String c : choices) if (have.contains(c)) return c;
        return Font.SANS_SERIF;
    }

    /** Rounded display face for buttons / titles. */
    public static synchronized Font display(int style, float size) {
        if (displayFamily == null) displayFamily = pick(DISPLAY_CHOICES);
        return new Font(displayFamily, style, 12).deriveFont(size);
    }

    /** Readable face for paragraphs. */
    public static synchronized Font body(int style, float size) {
        if (bodyFamily == null) bodyFamily = pick(BODY_CHOICES);
        return new Font(bodyFamily, style, 12).deriveFont(size);
    }

    public static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
