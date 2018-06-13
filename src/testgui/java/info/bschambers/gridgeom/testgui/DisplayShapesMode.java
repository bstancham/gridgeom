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
    // private boolean showTriangulation = false;

    // vertex editing
    private boolean vertexMode = false;
    private KbdMode vertexKeys = new KbdMode();

    // sub-shape editing
    private boolean subShapeMode = false;
    private KbdMode subShapeKeys = new KbdMode();

    private TextBlock text = new TextBlock(500, 10);

    public void init(CanvasPanel canvas) {
        super.init(canvas);
        
        getKeys().add('v', () -> "vertex editing mode " + KbdMode.boolStr(vertexMode),
                      () -> setVertexMode(true));

        getKeys().add('S', () -> "sub-shape editing mode " + KbdMode.boolStr(subShapeMode),
                      () -> setVertexMode(true));

        getKeys().add('m', () -> "show multiple " + KbdMode.boolStr(showMultiple),
                      () -> toggleShowMultiple());

        getKeys().add('d', () -> "diagnostics " + KbdMode.boolStr(showDiagnostics),
                      () -> toggleShowDiagnostics());

        getKeys().add('P', "print source code",
                      () -> System.out.println("\n"
                            + slot().shape().getSourceCode()
                            + "\n\n... you can copy & paste this "
                            + "in to your program source code...\n"));

        // TRANSFORMATIONS

        getKeys().add('x', "reflect (x-axis)",
                      () -> slot().wrapper().reflectX());

        getKeys().add('y', "reflect (y-axis)",
                      () -> slot().wrapper().reflectY());

        getKeys().add('z', () -> "rotate 90 degrees",
                      () -> slot().wrapper().rotate90());

        getKeys().add('w', "reverse winding",
                      () -> slot().wrapper().reverseWinding());

        getKeys().add('c', "add containing box",
                      () -> slot().wrapper().addContainingBox());

        // VERTEX EDITING
        
        vertexKeys.add('q', "quit vertex-edit mode", () -> setVertexMode(false));

        vertexKeys.add('r', "reset shape",
                      () -> {
                          slot().wrapper().reset();
                          softUpdate();
                      });

        vertexKeys.add('[', ']',
                      () -> "prev/next vertex (" + (1 + slot().wrapper().vertexIndex()) +
                      " of " + slot().shape().getNumVertices() + ")",
                      () -> previousVertex(),
                      () -> nextVertex());

        vertexKeys.add('d', "delete vertex", () -> deleteVertex());

        vertexKeys.add('a', "add vertex (after)", () -> addVertex());

        // SUB-SHAPE EDITING

        // subShapeKeys.add('q', "quit vertex-edit mode", () -> setSubShapeMode(false));

        // subShapeKeys.add('d', "delete subshape", () -> deleteSubShape());
        
        // subShapeKeys.add('a', "add subshape", () -> addSubShape());

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
            setCurrentVertex(keyCursorX, keyCursorY);
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
        
        if (showGrid)
            getCanvas().paintGrid(g);

        if (showMultiple) {
            for (ShapeSlot ss : getCanvas().getShapeSlots()) {
                paintSlot(g, ss);
            }
        } else {
            paintSlot(g, slot());
        }

        // if (showTriangulation) {
        //     // gfx().triangles(g, currentShape(), offsetX, offsetY);
        //     // gfx().triangles(g, currentShape2(), offset2X, offset2Y);
        //     for (ShapeSlot ss : slots) {
        //         gfx().triangles(g, ss.shape());
        //     }
        // }

        if (showDiagnostics) {
            // paint shape diagnostic info

            text.clear();
            Color textCol = Color.GRAY;
            text.add(textCol, "NAME: " + slot().wrapper().name());
            text.addIfElse(slot().shape().isValid(),
                           Color.GREEN, "VALIDATION: PASSED",
                           Color.RED,   "VALIDATION: FAILED");

            // EACH SUB-SHAPE
            int n = 1;
            for (Shape45 s : slot().shape()) {

                Color col = textCol;
                text.addSeparator(col);
                text.add(col, "OUTLINE " + n++);

                // 45 DEGREE ANGLES
                text.addIfElse(s.is45Compliant(),
                               textCol, "45 DEGREE RULE: passed",
                               Color.RED,  "45 DEGREE RULE: FAILED");
            
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
                text.add(col, str
                         + " --- sum of angles: " + Math.toDegrees(s.getSumAngles())
                         + " degrees (" + s.getSumAngles() + ")");

                // DUPLICATE VERTICES
                int numDuplicates = s.getNumDuplicateVertices();
                text.addIfElse(numDuplicates == 0,
                               textCol, "DUPLICATE VERTICES: " + numDuplicates,
                               Color.RED,  "DUPLICATE VERTICES: " + numDuplicates);

                // EDGE INTERSECTIONS
                numDuplicates = s.getNumEdgeIntersections();
                text.addIfElse(numDuplicates == 0,
                               textCol, "EDGE INTERSECTIONS: " + numDuplicates,
                               Color.RED,  "EDGE INTERSECTIONS: " + numDuplicates);

                text.add(textCol, "NUM SUB-SHAPES: " + s.getNumSubShapes());

            }
        
            text.paint(g);
            
        }

        paintKbdCursor(g);

    }

    private void paintSlot(Graphics g, ShapeSlot ss) {
        g.setColor(ss.wrapper().getColor());
        gfx().shape(g, ss.shape());
    }

    @Override
    public KbdMode getKeys() {
        if (vertexMode)
            return vertexKeys;
        else
            return super.getKeys();
    }

    private void toggleShowMultiple() {
        showMultiple = !showMultiple;
    }
    
    private void toggleShowDiagnostics() {
        showDiagnostics = !showDiagnostics;
    }



    /*------------------------- VERTEX EDITING -------------------------*/

    private void setVertexMode(boolean val) {
        vertexMode = val;
        softUpdate();
    }

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
        slot().wrapper().deleteCurrentVertex();
        previousVertex();
    }
    
    private void addVertex() {
        slot().wrapper().addVertexAfterCurrent();
        nextVertex();
    }

}
