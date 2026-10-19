package o2_GAME_CLASSES.G2_Spelling_Quiz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;

import o3_TOOLS_CLASSES.TOOL_THEME;

/**
 * The play area of the Spelling Quiz: empty letter slots on top, scrambled letter tiles below.
 * Tap a tile (or type the letter) to fill the next slot; tap a filled slot (or Backspace) to take it back.
 * Everything is drawn from the component size, so it scales with the window.
 */
class SpellBoard extends JComponent {

    private static final long serialVersionUID = 1L;
    private static final Color[] PALETTE = { TOOL_THEME.RUBY, TOOL_THEME.SKY, new Color(255, 170, 0),
                                              TOOL_THEME.GREEN, new Color(242, 107, 29), TOOL_THEME.VIOLET };

    private static class Tile { char c; boolean used; Color color; }

    private String word = "";
    private char[] slots = new char[0];
    private int[] slotTile = new int[0];            // tile index sitting in each slot (-1 = empty)
    private final List<Tile> tiles = new ArrayList<Tile>();
    private boolean locked;
    private Color lockColor;                        // green / red after checking
    private String reveal;                          // correct word shown after a wrong answer
    private int hoverTile = -1;
    private final Random rnd = new Random();

    private final List<Rectangle> slotRects = new ArrayList<Rectangle>();
    private final List<Rectangle> tileRects = new ArrayList<Rectangle>();

    SpellBoard() {
        setOpaque(false);
        MouseAdapter m = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int h = hitTile(e.getPoint());
                if (h != hoverTile) { hoverTile = h; repaint(); }
                setCursor(Cursor.getPredefinedCursor(!locked && (h >= 0 || hitSlot(e.getPoint()) >= 0) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
            @Override public void mouseExited(MouseEvent e) { hoverTile = -1; repaint(); }
            @Override public void mousePressed(MouseEvent e) {
                if (locked) return;
                int t = hitTile(e.getPoint());
                if (t >= 0) { place(t); return; }
                int s = hitSlot(e.getPoint());
                if (s >= 0) takeBack(s);
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    // ── model ───────────────────────────────────────────────────────────────
    void setWord(String w, int extraLetters) {
        word = w;
        slots = new char[w.length()];
        slotTile = new int[w.length()];
        java.util.Arrays.fill(slotTile, -1);
        tiles.clear();
        List<Character> letters = new ArrayList<Character>();
        for (char c : w.toCharArray()) letters.add(c);
        for (int i = 0; i < extraLetters; i++) letters.add((char) ('A' + rnd.nextInt(26)));
        Collections.shuffle(letters, rnd);
        for (int i = 0; i < letters.size(); i++) {
            Tile t = new Tile(); t.c = letters.get(i); t.color = PALETTE[i % PALETTE.length]; tiles.add(t);
        }
        locked = false; lockColor = null; reveal = null; hoverTile = -1;
        repaint();
    }

    void type(char c) {
        if (locked) return;
        c = Character.toUpperCase(c);
        for (int i = 0; i < tiles.size(); i++) if (!tiles.get(i).used && tiles.get(i).c == c) { place(i); return; }
    }

    void backspace() {
        if (locked) return;
        for (int s = slots.length - 1; s >= 0; s--) if (slotTile[s] >= 0) { takeBack(s); return; }
    }

    void clear() { if (locked) return; for (int s = 0; s < slots.length; s++) if (slotTile[s] >= 0) takeBack(s); }

    private void place(int tileIdx) {
        Tile t = tiles.get(tileIdx);
        if (t.used) return;
        for (int s = 0; s < slots.length; s++) if (slotTile[s] < 0) {
            slots[s] = t.c; slotTile[s] = tileIdx; t.used = true; repaint(); return;
        }
    }

    private void takeBack(int slot) {
        if (slotTile[slot] < 0) return;
        Tile t = tiles.get(slotTile[slot]);
        t.used = false; slotTile[slot] = -1; slots[slot] = 0; repaint();
    }

    boolean isEmpty() { for (int s : slotTile) if (s >= 0) return false; return true; }
    boolean isFull()  { for (int s : slotTile) if (s < 0) return false; return slots.length > 0; }
    String answer()   { return new String(slots); }

    void lock(boolean correct, String revealWord) {
        locked = true;
        lockColor = correct ? TOOL_THEME.GREEN : TOOL_THEME.RUBY;
        reveal = correct ? null : revealWord;
        repaint();
    }

    // ── geometry ────────────────────────────────────────────────────────────
    private void geometry() {
        slotRects.clear(); tileRects.clear();
        int w = getWidth(), h = getHeight(), n = slots.length;
        if (n == 0) return;
        int slot = (int) Math.min(Math.min(h * 0.24, 84), w / (n * 1.14));
        int gap = Math.max(4, (int) (slot * 0.14));
        int rowW = n * slot + (n - 1) * gap;
        int sx = (w - rowW) / 2;
        for (int i = 0; i < n; i++) slotRects.add(new Rectangle(sx + i * (slot + gap), (int) (h * 0.03), slot, slot));

        int cnt = tiles.size();
        int cols = Math.min(cnt, 7);
        int rows = (cnt + cols - 1) / cols;
        int ts = (int) Math.min(Math.min(h * 0.20, 74), w / (cols * 1.14));
        int tg = Math.max(5, (int) (ts * 0.16));
        int top = Math.max((int) (h * 0.30), slot + (int) (h * 0.10));
        for (int i = 0; i < cnt; i++) {
            int r = i / cols, c = i % cols;
            int inRow = (r == rows - 1) ? cnt - r * cols : cols;
            int rw = inRow * ts + (inRow - 1) * tg;
            tileRects.add(new Rectangle((w - rw) / 2 + c * (ts + tg), top + r * (ts + tg), ts, ts));
        }
    }

    private int hitTile(Point p) { geometry(); for (int i = 0; i < tileRects.size(); i++) if (tileRects.get(i).contains(p) && !tiles.get(i).used) return i; return -1; }
    private int hitSlot(Point p) { geometry(); for (int i = 0; i < slotRects.size(); i++) if (slotRects.get(i).contains(p) && slotTile[i] >= 0) return i; return -1; }

    // ── painting ────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);
        geometry();
        if (slotRects.isEmpty()) { g.dispose(); return; }

        for (int i = 0; i < slotRects.size(); i++) {
            Rectangle r = slotRects.get(i);
            float arc = r.height * 0.3f;
            g.setColor(new Color(0, 0, 0, 30));
            g.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 4, r.width, r.height, arc, arc));
            boolean filled = slotTile[i] >= 0;
            g.setColor(filled ? Color.WHITE : new Color(255, 255, 255, 190));
            g.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, arc, arc));
            Color edge = lockColor != null ? lockColor : filled ? TOOL_THEME.RUBY : TOOL_THEME.MAYA_BLUE;
            g.setColor(edge);
            g.setStroke(filled || lockColor != null ? new BasicStroke(Math.max(3f, r.height * .07f))
                    : new BasicStroke(Math.max(2f, r.height * .05f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 8f, 6f }, 0f));
            g.draw(new RoundRectangle2D.Float(r.x + 1, r.y + 1, r.width - 2, r.height - 2, arc, arc));
            if (filled) {
                g.setFont(TOOL_THEME.display(Font.BOLD, r.height * 0.62f));
                FontMetrics fm = g.getFontMetrics();
                String s = String.valueOf(slots[i]);
                g.setColor(lockColor != null ? TOOL_THEME.darker(lockColor, 0.1f) : TOOL_THEME.INK);
                g.drawString(s, r.x + (r.width - fm.stringWidth(s)) / 2, r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
            }
        }

        if (reveal != null) {
            Rectangle first = slotRects.get(0);
            g.setFont(TOOL_THEME.display(Font.BOLD, Math.max(14f, getHeight() * 0.075f)));
            FontMetrics fm = g.getFontMetrics();
            String s = "Correct spelling:  " + reveal;
            g.setColor(TOOL_THEME.GREEN.darker());
            g.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, first.y + first.height + fm.getHeight() + 2);
        }

        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i); Rectangle r = tileRects.get(i);
            float arc = r.height * 0.3f, d = Math.max(3f, r.height * 0.09f);
            if (t.used) {
                g.setColor(new Color(255, 255, 255, 60));
                g.fill(new RoundRectangle2D.Float(r.x, r.y + d, r.width, r.height - d, arc, arc));
                continue;
            }
            float lift = (i == hoverTile && !locked) ? d * 0.8f : 0f;
            g.setColor(new Color(0, 0, 0, 40));
            g.fill(new RoundRectangle2D.Float(r.x + 1, r.y + d + 3, r.width, r.height - d, arc, arc));
            g.setColor(TOOL_THEME.darker(t.color, 0.3f));
            g.fill(new RoundRectangle2D.Float(r.x, r.y + d - lift, r.width, r.height - d, arc, arc));
            g.setColor(t.color);
            g.fill(new RoundRectangle2D.Float(r.x, r.y - lift, r.width, r.height - d, arc, arc));
            g.setColor(new Color(255, 255, 255, 80));
            g.setStroke(new BasicStroke(Math.max(1.5f, r.height * .03f)));
            g.draw(new RoundRectangle2D.Float(r.x + r.width * .1f, r.y + r.height * .07f - lift, r.width * .8f, (r.height - d) * .8f, arc, arc));
            g.setFont(TOOL_THEME.display(Font.BOLD, r.height * 0.6f));
            FontMetrics fm = g.getFontMetrics();
            String s = String.valueOf(t.c);
            g.setColor(Color.WHITE);
            g.drawString(s, r.x + (r.width - fm.stringWidth(s)) / 2, r.y - lift + ((r.height - d) + fm.getAscent() - fm.getDescent()) / 2);
        }
        g.dispose();
    }
}
