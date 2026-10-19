package o2_GAME_CLASSES.G2_Spelling_Quiz;

import java.util.ArrayList;
import java.util.List;

import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;

/** SPELLING QUIZ - EASY: the 7 shapes, letters given exactly (no extra letters). */
public class EASY_GAME extends SPELL_GAME {

    private static List<Item> shapes() {
        String[] w = { "CIRCLE", "DIAMOND", "HEART", "OVAL", "RECTANGLE", "SQUARE", "TRIANGLE" };
        List<Item> l = new ArrayList<Item>();
        for (String s : w) l.add(new Item(s, "/G2/EASY/" + s + ".png"));
        return l;
    }

    public EASY_GAME(TOOL_GIF_PANEL stage) {
        super(stage, "EASY MODE", shapes(), 7, 0);
        begin();
    }
}
