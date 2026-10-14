package o3_TOOLS_CLASSES;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.html.HTMLEditorKit;

/**
 * WONDERVILLE v2 - modal info card that lives INSIDE the window (v1 opened two extra undecorated
 * JDialogs that did not follow the main window when it was moved or resized).
 * Text mode (About) or image mode (Help). Click outside, press ESC or hit "Close" to dismiss.
 */
public class TOOL_OVERLAY extends JComponent implements TOOL_GIF_PANEL.StageAware {

    private static final long serialVersionUID = 1L;

    private final TOOL_GIF_PANEL stage;
    private final String title, emoji;
    private final Color accent;
    private final String html;           // text mode
    private final Image image;           // image mode
    private double scale = 1.0;

    private final TOOL_BUTTON closeBtn;
    private JScrollPane scroll;
    private JEditorPane pane;
    private Rectangle card = new Rectangle();

    private TOOL_OVERLAY(TOOL_GIF_PANEL stage, String title, String emoji, Color accent, String html, Image image) {
        this.stage = stage; this.title = title; this.emoji = emoji; this.accent = accent;
        this.html = html; this.image = image;
        setLayout(null);
        setOpaque(false);
        setFocusable(true);

        closeBtn = new TOOL_BUTTON("Close", TOOL_BUTTON.Icon.CHECK, TOOL_BUTTON.Skin.HOT);
        closeBtn.addActionListener(e -> dismiss());
        add(closeBtn);

        if (html != null) {
            pane = new JEditorPane();
            pane.setEditorKit(new HTMLEditorKit());
            pane.setContentType("text/html");
            pane.setEditable(false);
            pane.setOpaque(false);
            pane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            scroll = new JScrollPane(pane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(18);
            add(scroll);
        }

        // click on the veil (outside the card) closes
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (!card.contains(e.getPoint())) dismiss(); }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getActionMap().put("close", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { dismiss(); }
        });
    }

    public static void showText(TOOL_GIF_PANEL stage, String title, String emoji, Color accent, String html) {
        stage.addOverlay(new TOOL_OVERLAY(stage, title, emoji, accent, html, null));
    }

    public static void showImage(TOOL_GIF_PANEL stage, String title, String emoji, Color accent, String imagePath) {
        URL u = TOOL_OVERLAY.class.getResource(imagePath);
        Image img = u == null ? null : java.awt.Toolkit.getDefaultToolkit().getImage(u);
        if (img != null) new javax.swing.ImageIcon(img);     // blocks until loaded (PNG - small, EDT-safe)
        stage.addOverlay(new TOOL_OVERLAY(stage, title, emoji, accent, null, img));
    }

    public void dismiss() { stage.removeOverlay(this); }

    @Override public void stageScaled(double s) { this.scale = s; }

    // ── layout ──────────────────────────────────────────────────────────────
    @Override
    public void doLayout() {
        Rectangle st = stage.stageRect();
        int pad = (int) Math.round(22 * scale);
        int headerH = (int) Math.round(78 * scale);
        int footerH = (int) Math.round(66 * scale);

        int cw, ch;
        if (image != null) {                                         // image mode: card hugs a 16:9 picture
            int maxW = (int) (st.width * 0.86), maxH = (int) (st.height * 0.92);
            int imgH = Math.min(maxH - headerH - footerH - pad * 2, (int) ((maxW - pad * 2) * 9.0 / 16.0));
            int imgW = (int) (imgH * 16.0 / 9.0);
            cw = imgW + pad * 2;
            ch = imgH + headerH + footerH + pad;
        } else {
            cw = (int) (st.width * 0.74);
            ch = (int) (st.height * 0.82);
        }
        card = new Rectangle(st.x + (st.width - cw) / 2, st.y + (st.height - ch) / 2, cw, ch);

        int btnW = (int) Math.round(190 * scale), btnH = (int) Math.round(58 * scale);
        closeBtn.setBounds(card.x + (cw - btnW) / 2, card.y + ch - footerH + (footerH - btnH) / 2 - (int) (4 * scale), btnW, btnH);

        if (scroll != null) {
            scroll.setBounds(card.x + pad + (int) (14 * scale), card.y + headerH + pad / 2,
                             cw - pad * 2 - (int) (28 * scale), ch - headerH - footerH - pad);
            int px = Math.max(12, (int) Math.round(22 * scale));
            String css = "<html><body style=\"font-family:'" + TOOL_THEME.body(Font.PLAIN, 12).getFamily()
                       + "',Arial,sans-serif; font-size:" + px + "px; color:#34223A;\">" + html + "</body></html>";
            if (!css.equals(pane.getText())) { pane.setText(css); pane.setCaretPosition(0); }
        }
    }

    // ── painting ────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        TOOL_THEME.smooth(g);
        int W = getWidth(), H = getHeight();

        // veil tinted with the card's accent colour
        g.setPaint(new GradientPaint(0, 0, TOOL_THEME.alpha(accent, 170),
                                     W, H, TOOL_THEME.alpha(TOOL_THEME.darker(accent, 0.55f), 215)));
        g.fillRect(0, 0, W, H);

        int arc = (int) (46 * scale);
        // card shadow + body
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Float(card.x + 3, card.y + 9, card.width, card.height, arc, arc));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(card.x, card.y, card.width, card.height, arc, arc));
        g.setColor(accent);
        g.setStroke(new BasicStroke(Math.max(3f, (float) (6 * scale))));
        g.draw(new RoundRectangle2D.Float(card.x + 3, card.y + 3, card.width - 6, card.height - 6, arc - 6, arc - 6));

        // header: emoji badge + title + accent underline
        int pad = (int) Math.round(22 * scale);
        int badge = (int) Math.round(58 * scale);
        int hx = card.x + pad + (int) (8 * scale), hy = card.y + pad - (int) (2 * scale);
        g.setColor(TOOL_THEME.alpha(accent, 60));
        g.fill(new RoundRectangle2D.Float(hx, hy, badge, badge, badge * 0.36f, badge * 0.36f));
        if (emoji != null) {
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, Math.max(12, (int) (badge * 0.62))));
            FontMetrics em = g.getFontMetrics();
            g.setColor(TOOL_THEME.INK);
            g.drawString(emoji, hx + (badge - em.stringWidth(emoji)) / 2, hy + (badge + em.getAscent() - em.getDescent()) / 2);
        }
        g.setFont(TOOL_THEME.display(Font.BOLD, (float) Math.max(16, 40 * scale)));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(TOOL_THEME.darker(accent, 0.12f));
        int tx = hx + badge + (int) (16 * scale);
        g.drawString(title, tx, hy + (badge + fm.getAscent() - fm.getDescent()) / 2);
        int ly = hy + badge + (int) (10 * scale);
        g.setPaint(new GradientPaint(hx, 0, accent, card.x + card.width - pad, 0, TOOL_THEME.alpha(accent, 0)));
        g.fillRoundRect(hx, ly, card.width - pad * 2 - (int) (16 * scale), Math.max(3, (int) (5 * scale)), 6, 6);

        // image mode
        if (image != null) {
            int headerH = (int) Math.round(78 * scale), footerH = (int) Math.round(66 * scale);
            Rectangle area = new Rectangle(card.x + pad, card.y + headerH + pad / 2, card.width - pad * 2, card.height - headerH - footerH - pad);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setClip(new RoundRectangle2D.Float(area.x, area.y, area.width, area.height, 28 * (float) scale, 28 * (float) scale));
            g.drawImage(image, area.x, area.y, area.width, area.height, this);
            g.setClip(null);
            g.setColor(TOOL_THEME.alpha(accent, 200));
            g.setStroke(new BasicStroke(Math.max(2f, (float) (4 * scale))));
            g.draw(new RoundRectangle2D.Float(area.x, area.y, area.width, area.height, 28 * (float) scale, 28 * (float) scale));
        } else if (html != null && scroll != null) {
            g.setColor(TOOL_THEME.alpha(accent, 26));
            Rectangle b = scroll.getBounds();
            g.fill(new RoundRectangle2D.Float(b.x - 8, b.y - 4, b.width + 16, b.height + 8, 30 * (float) scale, 30 * (float) scale));
        }

        // footer tag line
        g.setFont(TOOL_THEME.body(Font.ITALIC, (float) Math.max(10, 13 * scale)));
        g.setColor(new Color(170, 160, 175));
        String tag = "Wonderville  \u2022  Learning made fun!";
        FontMetrics ff = g.getFontMetrics();
        g.drawString(tag, card.x + card.width - pad - ff.stringWidth(tag) - 10, card.y + card.height - (int) (14 * scale));
        g.dispose();
    }
}
