package info.bschambers.gridgeom.testgui;

import java.util.Set;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import info.bschambers.gridgeom.*;

/**
 */
public class DisplayShapesMode extends CanvasMode {

    private boolean showGrid = true;
    private boolean showMultiple = true;
    private boolean showDiagnostics = true;
    private boolean showTriangulation = false;

    // vertex editing
    private boolean vertexMode = false;
    private KbdMode vertexKeys = new KbdMode();

    // sub-shape editing
    private boolean subShapeMode = false;
    private KbdMode subShapeKeys = new KbdMode();

    private TextBlock text = new TextBlock(500, 10);
    private Color textCol = Color.GRAY;

    public void init(CanvasPanel canvas) {
        super.init(canvas);
        
        getKeys().add('g', () -> "show grid " + KbdMode.boolStr(showGrid),
                      () -> toggleShowGrid());

        getKeys().add('m', () -> "show multiple " + KbdMode.boolStr(showMultiple),
                      () -> toggleShowMultiple());

        getKeys().add('t', () -> "show triangulation " + KbdMode.boolStr(showTriangulation),
                      () -> toggleShowTriangulation());

        getKeys().add('d', () -> "diagnostics " + KbdMode.boolStr(showDiagnostics),
                      () -> toggleShowDiagnostics());

        getKeys().add('P', "print source code",
                      () -> slot().wrapper().printSourceCode());

        getKeys().add('v', () -> "vertex editing mode " + KbdMode.boolStr(vertexMode),
                      () -> switchToVertexMode());

        getKeys().add('s', () -> "sub-shape editing mode " + KbdMode.boolStr(subShapeMode),
                      () -> switchToSubShapeMode());

        getKeys().add('R', "reset shape",
                      () -> {
                          slot().wrapper().reset();
                          softUpdate();
                      });

        // TRANSFORMATIONS

        getKeys().add('x', "reflect (x-axis)",
                      () -> slot().wrapper().reflectX());

        getKeys().add('y', "reflect (y-axis)",
                      () -> slot().wrapper().reflectY());

        getKeys().add('z', () -> "rotate 90 degrees",
                      () -> slot().wrapper().rotate90());

        getKeys().add('w', "reverse winding",
                      () -> slot().wrapper().reverseWinding());

        // VERTEX EDITING
        
        vertexKeys.add('s', () -> "sub-shape editing mode " + KbdMode.boolStr(subShapeMode),
                       () -> switchToSubShapeMode());

        vertexKeys.add('q', "quit vertex-edit mode", () -> switchToBaseMode());

        vertexKeys.setCursorKeys("move vertex",
                                 () -> slot().wrapper().shiftVertex(0, 1),
                                 () -> slot().wrapper().shiftVertex(0, -1),
                                 () -> slot().wrapper().shiftVertex(-1, 0),
                                 () -> slot().wrapper().shiftVertex(1, 0));

        vertexKeys.add('[', ']',
                      () -> "prev/next vertex (" + (1 + slot().wrapper().vertexIndex()) +
                      " of " + slot().shape().getNumVertices() + ")",
                      () -> previousVertex(),
                      () -> nextVertex());

        vertexKeys.add('d', "delete vertex", () -> deleteVertex());

        vertexKeys.add('a', "add vertex (after)", () -> addVertex());

        vertexKeys.add('{', '}',
                       () -> "rotate vertices left/right",
                       () -> rotateVerticesLeft(),
                       () -> rotateVerticesRight());

        // SUB-SHAPE EDITING

        subShapeKeys.add('v', () -> "vertex editing mode " + KbdMode.boolStr(vertexMode),
                         () -> switchToVertexMode());

        subShapeKeys.add('q', "quit sub-shape mode", () -> switchToBaseMode());

        subShapeKeys.setCursorKeys("move sub-shape",
                                   () -> slot().wrapper().shiftSubShape(0, 1),
                                   () -> slot().wrapper().shiftSubShape(0, -1),
                                   () -> slot().wrapper().shiftSubShape(-1, 0),
                                   () -> slot().wrapper().shiftSubShape(1, 0));
        
        subShapeKeys.add('[', ']',
                         () -> "prev/next sub-shape (" + (1 + slot().wrapper().subShapeIndex()) +
                         " of " + slot().shape().getNumShapesRecursive() + ")",
                         () -> previousSubShape(),
                         () -> nextSubShape());

        subShapeKeys.add('d', "delete subshape", () -> deleteSubShape());
        
        subShapeKeys.add('a', "add subshape", () -> addSubShape());

        subShapeKeys.add('A', "add subshape at same level",
                         () -> addSubShapeAtSameLevel());

        subShapeKeys.add('c', "add containing box",
                         () -> slot().wrapper().addContainingBox());

        subShapeKeys.add('w', "reverse winding (sub-shape)",
                         () -> slot().wrapper().reverseSubShapeWinding());

    }

    protected void softUpdate() {
        if (vertexMode)
            setKeyCursorPos(slot().wrapper().getCanvasVertex());
        else
            setKeyCursorPos(slot().wrapper().getPosition());
    }
    
    @Override
    public void update() {
        super.update();
        
        // handle cursor movement
        if (vertexMode) {
            setKeyCursorPos(slot().wrapper().getCanvasVertex());
            // setCurrentVertex(keyCursorX, keyCursorY);
        } else {
            slot().wrapper().setPosition(keyCursorX, keyCursorY);
        }

        // colour code for shape-validation
        for (ShapeSlot ss : getCanvas().getShapeSlots()) {
            if (showDiagnostics) {
                if (ss.shape().isValid())
                    ss.wrapper().setColor(Color.GREEN);
                else
                    ss.wrapper().setColor(Color.RED);
            } else {
                ss.wrapper().setColor(Color.GRAY);
            }
        }
        
    }

    @Override
    public void paint(Graphics g) {

        // hilight sub-shape for vertex editing
        if (vertexMode) {
            g.setColor(Color.DARK_GRAY);
            Shape45 sub = slot().wrapper().getShapeForVertexIndex();
            gfx().fillBox(g, sub.getBoundingBox());
        }

        if (showGrid)
            getCanvas().paintGrid(g);

        if (showMultiple) {
            for (ShapeSlot ss : getCanvas().getShapeSlots()) {
                paintSlot(g, ss);
            }
        } else {
            paintSlot(g, slot());
        }

        // hilight shape for sub-shape editing
        if (subShapeMode) {
            g.setColor(Color.WHITE);
            gfx().shape(g, slot().wrapper().getSubShape());
        }

        if (showDiagnostics) {
            // paint shape diagnostic info

            text.clear();
            text.add(textCol, "NAME: " + slot().wrapper().name());
            text.addIfElse(slot().shape().isValid(),
                           Color.GREEN, "VALIDATION: PASSED",
                           Color.RED,   "VALIDATION: FAILED");
            text.add(textCol, "nested depth: " + slot().shape().getNestedDepth());

            // EACH SUB-SHAPE
            int n = 1;
            for (Shape45 s : slot().shape()) {
                
                Color col = textCol;
                text.addSeparator(col);
                // text.add(col, "OUTLINE " + n++);

                addDiagnosticText(text, s, "OUTLINE " + n++, "");
            }
            text.paint(g);
        }

        paintKbdCursor(g);
    }

    private void paintSlot(Graphics g, ShapeSlot ss) {

        if (showTriangulation) {
            gfx().numberedTriangles(g, ss.shape(), Color.LIGHT_GRAY, Color.RED);
        }

        if (subShapeMode) {
            gfx().shapeNestedDepthFade(g, ss.shape(), ss.wrapper().getColor());
        } else {
            // standard view
            g.setColor(ss.wrapper().getColor());
            gfx().shape(g, ss.shape());
        }
    }

    private void addDiagnosticText(TextBlock text, Shape45 s, String label, String pad) {
        Color col = textCol;

        // TITLE
        text.add(col, pad + label);
        
        // 45 DEGREE ANGLES
        text.addIfElse(s.is45Compliant(),
                       textCol, pad + "45 DEGREE RULE: passed",
                       Color.RED,  pad + "45 DEGREE RULE: FAILED");
            
        // WINDING DIRECTION
        String str = "WINDING: ";
        if (s.isCCWWinding()) {
            str += "CCW";
        } else if (s.isCWWinding()) {
            str += "CW";
            col = Color.PINK;
        } else {
            str += "unknown";
            col = Color.RED;
        }
        text.add(col, pad + str
                 + " --- sum of angles: " + Math.toDegrees(s.getSumAngles())
                 + " degrees (" + s.getSumAngles() + ")");

        // DUPLICATE VERTICES
        int numDuplicates = s.getNumDuplicateVertices();
        text.addIfElse(numDuplicates == 0,
                       textCol, pad + "DUPLICATE VERTICES: " + numDuplicates,
                       Color.RED, pad + "DUPLICATE VERTICES: " + numDuplicates);

        // EDGE INTERSECTIONS
        numDuplicates = s.getNumEdgeIntersections();
        text.addIfElse(numDuplicates == 0,
                       textCol, pad + "EDGE INTERSECTIONS: " + numDuplicates,
                       Color.RED,  pad + "EDGE INTERSECTIONS: " + numDuplicates);

        text.add(textCol, pad + "NUM SUB-SHAPES: " + s.getNumSubShapes());

        int n = 1;
        for (Shape45 sub : s.getSubShapes())
            addDiagnosticText(text, sub, label + " SUB-SHAPE " + n++, pad + "  ");
    }

    @Override
    public KbdMode getKeys() {
        if (vertexMode)
            return vertexKeys;
        else if (subShapeMode)
            return subShapeKeys;
        else
            return super.getKeys();
    }

    private void toggleShowGrid() {
        showGrid = !showGrid;
    }
    
    private void toggleShowMultiple() {
        showMultiple = !showMultiple;
    }
    
    private void toggleShowTriangulation() {
        showTriangulation = !showTriangulation;
    }
    
    private void toggleShowDiagnostics() {
        showDiagnostics = !showDiagnostics;
    }

    

    /*------------------------- MODE SWITCHING -------------------------*/

    private void switchToBaseMode() {
        vertexMode = false;
        subShapeMode = false;
        softUpdate();
    }
    
    private void switchToVertexMode() {
        vertexMode = true;
        subShapeMode = false;
        softUpdate();
    }
    
    private void switchToSubShapeMode() {
        vertexMode = false;
        subShapeMode = true;
        softUpdate();
    }


    
    


    /*------------------------- VERTEX EDITING -------------------------*/

    protected void setCurrentVertex(int x, int y) {
        slot().wrapper().setVertex(x, y);        
    }

    private void incrVertexIndex(int amount) {
        slot().wrapper().incrVertexIndex(amount);
        softUpdate();
    }
    
    private void previousVertex() { incrVertexIndex(-1); }
    private void nextVertex()     { incrVertexIndex(1); }

    private void deleteVertex() {
        if (slot().wrapper().deleteCurrentVertex())
            previousVertex();
    }
    
    private void addVertex() {
        slot().wrapper().addVertexAfterCurrent();
        nextVertex();
    }

    private void rotateVerticesLeft() {
        slot().wrapper().rotateVertices(-1);
    }

    private void rotateVerticesRight() {
        slot().wrapper().rotateVertices(1);
    }



    /*----------------------- SUB-SHAPE EDITING ------------------------*/

    private void incrSubShapeIndex(int amount) {
        slot().wrapper().incrSubShapeIndex(amount);
        softUpdate();
    }
    
    private void previousSubShape() { incrSubShapeIndex(-1); }
    private void nextSubShape()     { incrSubShapeIndex(1); }

    private void deleteSubShape() {
        slot().wrapper().deleteCurrentSubShape();
        previousSubShape();
    }
    
    private void addSubShape() {
        slot().wrapper().addSubShapeOfCurrent();
        nextSubShape();
    }

    private void addSubShapeAtSameLevel() {
        slot().wrapper().addSubShapeAtCurrentLevel();
        nextSubShape();
    }

}
