package info.bschambers.gridgeom.testgui;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import info.bschambers.gridgeom.Pt2D;

/**
 * <p>NOTE: must do init() before use...</p>
 */
public abstract class CanvasMode {

    private String title;
    protected int keyCursorX = 5;
    protected int keyCursorY = 5;
    protected List<KeyBinding> bindings = new ArrayList<KeyBinding>();
    private CanvasPanel canvas = null;
    private int gridSizeX = 40;
    private int gridSizeY = 20;

    public CanvasMode() {
        title = getClass().getSimpleName();
    }

    public void init(CanvasPanel canvas) {
        this.canvas = canvas;
    }

    protected CanvasPanel getCanvas() {
        return canvas;
    }

    public String getTitle() {
        return title;
    }

    protected Gfx gfx() {
        return getCanvas().getGfx();
    }

    public abstract void paint(Graphics g);

    public void paintGrid(Graphics g) {
        g.setColor(Color.BLUE);
        gfx().grid(g, gridSizeX, gridSizeY);
    }

    public void paintKbdCursor(Graphics g) {
        g.setColor(Color.WHITE);
        gfx().crosshairs(g, keyCursorX, keyCursorY, 8);
    }
    
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        for (KeyBinding kb : bindings)
            kb.keyTyped(c);
    }

    /**
     * <p>Handles cursor keys.</p>
     */
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        // System.out.println("CanvasMode.keyPressed: code=" + code);
        if (code == KeyEvent.VK_LEFT) keyCursorX--;
        if (code == KeyEvent.VK_RIGHT) keyCursorX++;
        if (code == KeyEvent.VK_DOWN) keyCursorY--;
        if (code == KeyEvent.VK_UP) keyCursorY++;
    }

    /**
     * <p>Gets called before {@code paint()}. Child classes may override this to
     * do what they need, but must remember to call {@code super.update()}.</p>
     */
    protected void update() {}

    protected void setCursorPos(Pt2D p) {
        setCursorPos(p.x(), p.y());
    }
    
    protected void setCursorPos(int x, int y) {
        keyCursorX = x;
        keyCursorY = y;
    }

    public List<KeyBinding> getKeyBindings() { return bindings; }

    public void addKeyBinding(char c, Supplier<String> description, Runnable action) {
        bindings.add(new KeyBinding(c, description, action));
    }

    public void addKeyBinding(char c1, char c2, Supplier<String> description,
                              Runnable action1, Runnable action2) {
        bindings.add(new KeyBinding.Double(c1, c2, description, action1, action2));
    }

    protected String boolStr(boolean val) {
        return "(" + (val ? "TRUE" : "FALSE" ) + ")";
    }

}
