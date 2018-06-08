package info.bschambers.gridgeom.testgui;

import java.util.Set;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import info.bschambers.gridgeom.*;

/**
 * <p>TODO: E = edit shape</p>
 */
public class DisplayShapeMode extends CanvasMode {

    private boolean showGrid = true;
    private boolean showOutlines = true;
    // private boolean showTriangulation = false;

    private ShapeSlot[] shapeSlots;
    private int shapeSlotIndex = 0;
    private boolean editMode = false;

    public DisplayShapeMode(int numSlots, ShapeSet[] shapeSets) {
        buildShapeSlots(numSlots, shapeSets);

        addKeyBinding('u', 'i',
                      () -> "prev/next shape-set (" + (1 + slot().shapeSetIndex()) +
                      " of " + slot().numShapeSets() + " --> " + shapeSet().name() + ")",
                      () -> previousShapeSet(),
                      () -> nextShapeSet());

        addKeyBinding('o', 'p',
                      () -> "prev/next shape (" + (1 + shapeSet().index()) +
                      " of " + shapeSet().size() + " --> " + slot().wrapper().name() + ")",
                      () -> previousShape(),
                      () -> nextShape());
    
        if (numSlots > 1)
            addKeyBinding('s',
                          () -> "switch shape-slot (" + (shapeSlotIndex + 1) +
                          " of " + shapeSlots.length + ")",
                          () -> switchShapeSlot());

        addKeyBinding('e', () -> "shape editing mode " + boolStr(editMode),
                      () -> toggleEditMode());

        addKeyBinding('[', ']',
                      () -> "prev/next vertex (" + (1 + slot().wrapper().vertexIndex()) +
                      " of " + slot().shape().getNumVertices() + ")",
                      () -> previousVertex(),
                      () -> nextVertex());

        addKeyBinding('d', () -> "delete vertex", () -> deleteVertex());

        addKeyBinding('a', () -> "add vertex (after)", () -> addVertex());

        addKeyBinding('r', () -> "reset shape",
                      () -> {
                          slot().wrapper().reset();
                          softUpdate();
                      });

        addKeyBinding('x', () -> "reflect (x-axis)",
                      () -> slot().wrapper().reflectX());

        addKeyBinding('y', () -> "reflect (y-axis)",
                      () -> slot().wrapper().reflectY());

        // addKeyBinding('z', () -> "rotate 90 degrees",
        //               () -> { shapeWrapper().rotate();
        //                       softUpdateAndRepaint(); });

        addKeyBinding('P', () -> "print source code",
                      () -> System.out.println("\n"
                            + slot().shape().getSourceCode()
                            + "\n\n... you can copy & paste this source code into your program...\n") );

        // addKeyBinding('S', () -> "save shape to disk",
        //               () -> { System.out.println("... serializing shape object..."); });

        // addKeyBinding('L', () -> "load shapes from disk",
        //               () -> { System.out.println("... attempt to deserialize shape objects..."); });

    }

    protected void softUpdate() {
        if (editMode)
            setCursorPos(slot().wrapper().getCanvasVertex());
        else
            setCursorPos(slot().wrapper().getPosition());
    }
    
    @Override
    public void update() {
        super.update();
        // handle cursor movement
        if (editMode) {
            setCurrentVertex(keyCursorX, keyCursorY);
        } else {
            slot().wrapper().setPosition(keyCursorX, keyCursorY);
        }
    }

    @Override
    public void paint(Graphics g) {

        if (showGrid)
            paintGrid(g);

        if (showOutlines) {
            for (ShapeSlot ss : shapeSlots) {
                g.setColor(ss.wrapper().getColor());
                gfx().shape(g, ss.shape());
            }
        }

        // if (showTriangulation) {
        //     // gfx().triangles(g, currentShape(), offsetX, offsetY);
        //     // gfx().triangles(g, currentShape2(), offset2X, offset2Y);
        //     for (ShapeSlot ss : slots) {
        //         gfx().triangles(g, ss.shape());
        //     }
        // }

        paintKbdCursor(g);

    }



    /*------------------------ SLOTS AND SHAPES ------------------------*/

    private void buildShapeSlots(int numSlots, ShapeSet[] shapeSets) {
        // init
        shapeSlots = new ShapeSlot[numSlots];
        for (int i = 0; i < numSlots; i++) {
            ShapeSet[] shapeSetsCopy = new ShapeSet[shapeSets.length];
            for (int ii = 0; ii < shapeSets.length; ii++)
                shapeSetsCopy[ii] = shapeSets[ii].copy();
            shapeSlots[i] = new ShapeSlot(shapeSetsCopy);
        }
        // transpose each shape-set
        int yStep = 12;
        int y = (numSlots > 1 ?
                 (yStep * numSlots) / 2 :
                 0);
        for (int i = 0; i < numSlots; i++) {
            int yOffset =  y - (i * yStep);
            shapeSlots[i].translate(0, yOffset);
        }
    }
    
    protected ShapeSlot slot() {
        return shapeSlots[shapeSlotIndex];
    }

    protected ShapeSet shapeSet() {
        return slot().shapeSet();
    }

    private void switchShapeSlot() {
        editMode = false;
        shapeSlotIndex++;
        if (shapeSlotIndex >= shapeSlots.length)
            shapeSlotIndex = 0;
        keyCursorX = slot().wrapper().getPosX();
        keyCursorY = slot().wrapper().getPosY();
    }

    private void previousShapeSet() {
        editMode = false;
        slot().previousShapeSet();
    }

    private void nextShapeSet() {
        editMode = false;
        slot().nextShapeSet();
    }

    private void previousShape() {
        editMode = false;
        slot().shapeSet().previousShape();
    }

    private void nextShape() {
        editMode = false;
        slot().shapeSet().nextShape();
    }


    
    /*------------------------- VERTEX EDITING -------------------------*/

    protected void setCurrentVertex(int x, int y) {
        slot().wrapper().setVertex(x, y);        
    }

    private void toggleEditMode() {
        editMode = !editMode;
        softUpdate();
    }

    private void incrVertexIndex(int amount) {
        slot().wrapper().incrVertexIndex(amount);
        softUpdate();
    }
    
    private void previousVertex() { incrVertexIndex(-1); }
    private void nextVertex()     { incrVertexIndex(1); }

    private void deleteVertex() {
        slot().wrapper().deleteCurrentVertex();
        previousVertex();
    }
    
    private void addVertex() {
        slot().wrapper().addVertexAfterCurrent();
        nextVertex();
    }



    /*-------------------------- INNER CLASS ---------------------------*/

    public class ShapeSlot {
        
        private ShapeSet[] shapeSets;
        private int shapeSetIndex = 0;

        public ShapeSlot(ShapeSet[] shapeSets) {
            this.shapeSets = shapeSets;
        }

        public ShapeGroup shape() { return wrapper().shape(); }

        public ShapeWrapper wrapper() { return shapeSet().current(); }

        public ShapeSet shapeSet() { return shapeSets[shapeSetIndex]; }

        public int numShapeSets() { return shapeSets.length; }

        public int shapeSetIndex() { return shapeSetIndex; }

        public void previousShapeSet() {
            shapeSetIndex--;
            if (shapeSetIndex < 0) shapeSetIndex = shapeSets.length - 1;
        }

        public void nextShapeSet() {
            shapeSetIndex++;
            if (shapeSetIndex >= shapeSets.length) shapeSetIndex = 0;
        }

        public void translate(int x, int y) {
            for (ShapeSet ss : shapeSets)
                for (ShapeWrapper sw : ss.all())
                    sw.setPosition(x, y);
        }
                
    }

}
