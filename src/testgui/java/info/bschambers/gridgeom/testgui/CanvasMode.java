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
    private KbdMode keys = new KbdMode();
    private CanvasPanel canvas = null;

    protected int keyCursorX = 5;
    protected int keyCursorY = 5;

    public CanvasMode() {
        title = getClass().getSimpleName();
    }

    public void init(CanvasPanel canvas) {
        this.canvas = canvas;
        // set up default cursor key actions
        keys.setCursorKeys("move",
                           () -> keyCursorY++,
                           () -> keyCursorY--,
                           () -> keyCursorX--,
                           () -> keyCursorX++);
    }

    protected CanvasPanel getCanvas() {
        return canvas;
    }

    protected ShapeSlot slot() {
        return getCanvas().slot();
    }

    public String getTitle() {
        return title;
    }

    protected Gfx gfx() {
        return getCanvas().gfx();
    }

    public abstract void paint(Graphics g);

    public void paintKbdCursor(Graphics g) {
        g.setColor(Color.WHITE);
        gfx().crosshairs(g, keyCursorX, keyCursorY, 8);
    }

    public KbdMode getKeys() {
        return keys;
    }
    
    public boolean keyTyped(KeyEvent e) {
        return getKeys().keyTyped(e);
    }

    /**
     * <p>Handles cursor keys.</p>
     */
    public boolean keyPressed(KeyEvent e) {
        return getKeys().keyPressed(e);
    }

    public List<KeyBinding> getKeyBindings() {
        return getKeys().getKeyBindings();
    }
    
    /**
     * <p>Gets called before {@code paint()}. Child classes may override this to
     * do what they need, but must remember to call {@code super.update()}.</p>
     */
    protected void update() {}

    public void setKeyCursorPos(Pt2D p) {
        setKeyCursorPos(p.x(), p.y());
    }
    
    public void setKeyCursorPos(int x, int y) {
        keyCursorX = x;
        keyCursorY = y;
    }

}
