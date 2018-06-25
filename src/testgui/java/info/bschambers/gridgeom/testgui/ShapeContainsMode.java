package info.bschambers.gridgeom.testgui;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.awt.Graphics;
import java.awt.Color;
import info.bschambers.gridgeom.*;

public class ShapeContainsMode extends CanvasMode {
    
    // private Pt2D start = new Pt2D(1, 5);
    // private Pt2D end   = new Pt2D(10, 5);
    // private boolean startEnd = false;
    // private Line currentLine = new Line(start, end);

    // private boolean includeParallel = true;
    
    // private Color iptCol = Color.YELLOW;

    // private TextBlock text45 = new TextBlock(500, 10);
    private TextBlock text = new TextBlock(700, 10);
    private Color textCol = Color.GRAY;

    @Override
    protected void initLocal() {
        
        getKeys().add('u', 'i',
                      () -> "prev/next shape-set (" + (1 + slot().shapeSetIndex())
                      + " of " + slot().numShapeSets()
                      + " --> " + slot().shapeSet().name() + ")",
                      () -> getCanvas().previousShapeSet(),
                      () -> getCanvas().nextShapeSet());

        getKeys().add('o', 'p',
                      () -> "prev/next shape (" + (1 + slot().shapeSet().index())
                      + " of " + slot().shapeSet().size()
                      + " --> " + slot().wrapper().name() + ")",
                      () -> getCanvas().previousShape(),
                      () -> getCanvas().nextShape());
        
        // getKeys().add('s', () -> "switch ends",
        //               () -> switchEnds());
        
        // getKeys().add('a', () -> "include parallel: "
        //               + KbdMode.boolStr(includeParallel),
        //               () -> toggleIncludeParallel());
    }

    // private ShapeGroup shape() {
    //     return slot().shape();
    // }

    // private Line line() {
    //     return currentLine;
    // }

    // private void switchEnds() {
    //     startEnd = !startEnd;
    //     if (startEnd) {
    //         keyCursorX = start.x();
    //         keyCursorY = start.y();
    //     } else {
    //         keyCursorX = end.x();
    //         keyCursorY = end.y();
    //     }
    // }

    // private void toggleIncludeParallel() {
    //     includeParallel = !includeParallel;
    // }

    @Override
    public void update() {
        
        // // line position
        // if (startEnd)
        //     start = new Pt2D(keyCursorX, keyCursorY);
        // else
        //     end = new Pt2D(keyCursorX, keyCursorY);
        // currentLine = new Line(start, end);

        // // shape color based on validation
        // if (shape().isValid())
        //     slot().wrapper().setColor(Color.GRAY);
        // else
        //     slot().wrapper().setColor(Color.RED);

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        text.clear();

        // SHAPE

        g.setColor(getFillColor());
        gfx().fillShape(g, slot().shape());
        g.setColor(Color.WHITE);
        gfx().shape(g, slot().shape());
        


        // INFO ETC

        if (showDiagnostics) {
            text.paint(g);
        }

        paintKbdCursor(g);
    }

    private Color getFillColor() {
        // if inside return green
        if (slot().shape().contains(new Pt2D(keyCursorX, keyCursorY)))
            return Color.GREEN;
        else
            return Color.RED;
    }

}
