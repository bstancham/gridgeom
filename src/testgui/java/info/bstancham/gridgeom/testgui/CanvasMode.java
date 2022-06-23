package info.bstancham.gridgeom.testgui;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import info.bstancham.gridgeom.Pt2D;

/**
 * <p>NOTE: must do init() before use...</p>
 */
public abstract class CanvasMode {

    private String title;
    private KbdMode keys = new KbdMode();
    private CanvasPanel canvas = null;

    protected int keyCursorX = 5;
    protected int keyCursorY = 5;
    
    protected boolean showGrid = true;
    protected boolean showDiagnostics = true;

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

        getKeys().add('g', () -> "show grid " + KbdMode.boolStr(showGrid),
                      () -> toggleShowGrid());

        // set up local key-bindings etc
        initLocal();
    }

    /**
     * <p>Use this to set up local key-bindings etc.</p>
     */
    protected abstract void initLocal();

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
    
    public void paint(Graphics g) {
        if (showGrid)
            getCanvas().paintGrid(g);
        getCanvas().paintCenter(g);
    }

    public void paintKbdCursor(Graphics g) {
        paintKbdCursor(g, 15);
    }

    public void paintKbdCursor(Graphics g, int size) {
        g.setColor(Color.WHITE);
        gfx().crosshairs(g, keyCursorX, keyCursorY, size);
        gfx().centeredCircle(g, keyCursorX, keyCursorY, size);
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
    protected abstract void update();

    public void setKeyCursorPos(Pt2D p) {
        setKeyCursorPos(p.x(), p.y());
    }
    
    public void setKeyCursorPos(int x, int y) {
        keyCursorX = x;
        keyCursorY = y;
    }

    protected void toggleShowGrid() {
        showGrid = !showGrid;
    }
    
    protected void toggleShowDiagnostics() {
        showDiagnostics = !showDiagnostics;
    }
    
}
