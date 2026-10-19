package o2_GAME_CLASSES.G2_Spelling_Quiz;

import java.util.ArrayList;
import java.util.List;

import o3_TOOLS_CLASSES.TOOL_GIF_PANEL;

/** SPELLING QUIZ - HARD: 10 of 12 fruits and vegetables, with 4 extra decoy letters among the tiles. */
public class HARD_GAME extends SPELL_GAME {

    private static List<Item> foods() {
        // { word to spell, picture file }  (the picture for BROCCOLI is stored as BROCOLLI.png)
        String[][] w = { {"APPLE","APPLE"}, {"AVOCADO","AVOCADO"}, {"BROCCOLI","BROCOLLI"}, {"EGGPLANT","EGGPLANT"},
                         {"GARLIC","GARLIC"}, {"GINGER","GINGER"}, {"ONION","ONION"}, {"ORANGE","ORANGE"},
                         {"PEAR","PEAR"}, {"PINEAPPLE","PINEAPPLE"}, {"STRAWBERRY","STRAWBERRY"}, {"TOMATO","TOMATO"} };
        List<Item> l = new ArrayList<Item>();
        for (String[] s : w) l.add(new Item(s[0], "/G2/HARD/" + s[1] + ".png"));
        return l;
    }

    public HARD_GAME(TOOL_GIF_PANEL stage) {
        super(stage, "HARD MODE", foods(), 10, 4);
        begin();
    }
}
