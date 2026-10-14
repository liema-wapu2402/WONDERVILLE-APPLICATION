package o2_GAME_CLASSES.G1_Color_Quiz;

import java.awt.Color;

/** Colour names + values shared by the Easy and Hard Color Quiz. */
public final class G1_ColorBank {
    private G1_ColorBank() {}

    public static final String[] EASY_NAMES = { "Red", "Green", "Blue", "Yellow", "Orange", "Violet" };

    public static final String[] HARD_NAMES = {
        "Red", "Blue", "Green", "Yellow", "Black", "White", "Gray", "Cyan",
        "Magenta", "Pink", "Orange", "Violet", "Turquoise", "Maroon", "Emerald", "Mustard"
    };

    public static Color of(String name) {
        switch (name) {
            case "Red":       return Color.RED;
            case "Blue":      return Color.BLUE;
            case "Green":     return Color.GREEN;
            case "Yellow":    return Color.YELLOW;
            case "Black":     return Color.BLACK;
            case "White":     return Color.WHITE;
            case "Gray":      return Color.GRAY;
            case "Cyan":      return Color.CYAN;
            case "Magenta":   return Color.MAGENTA;
            case "Pink":      return Color.PINK;
            case "Orange":    return Color.ORANGE;
            case "Violet":    return new Color(138, 43, 226);
            case "Turquoise": return new Color(64, 224, 208);
            case "Maroon":    return new Color(128, 0, 0);
            case "Emerald":   return new Color(0, 201, 87);
            case "Mustard":   return new Color(255, 219, 88);
            default:          return Color.DARK_GRAY;
        }
    }
}
