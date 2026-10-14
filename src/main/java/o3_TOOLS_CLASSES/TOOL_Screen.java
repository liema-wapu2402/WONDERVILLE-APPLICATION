package o3_TOOLS_CLASSES;

/**
 * WONDERVILLE v2 - one full-window screen = one animated GIF background + the buttons/cards on top.
 * MAIN_CLASS_WONDERVILLE_APPLICATION.open(screen) shows the loader, streams the GIF, swaps the screen
 * (with a cross-fade) and then calls build() on the Event Dispatch Thread.
 */
public interface TOOL_Screen {
    /** Classpath path of the background GIF, e.g. "/SUB1_BG_COVER_START.gif". */
    String background();

    /** Text under the bouncing dots while the screen loads. */
    String loadingText();

    /** Add components with stage.place(component, x, y, w, h) using 1200x675 coordinates. */
    void build(TOOL_GIF_PANEL stage);
}
