package info.bschambers.gridgeom.testgui;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.awt.Graphics;
import java.awt.Color;
import info.bschambers.gridgeom.*;

/**
 * <p>Test intersection between {@code Line} and {@code Polygon}, {@code
 * Shape45} and {@code ShapeGroup}.</p>
 */
public class ShapeAndLineMode extends CanvasMode {
    
    private Pt2D start = new Pt2D(1, 5);
    private Pt2D end   = new Pt2D(10, 5);
    private boolean startEnd = false;
    private Line currentLine = new Line(start, end);

    private boolean includeParallel = true;
    
    private Color iptCol = Color.YELLOW;

    private TextBlock text45 = new TextBlock(500, 10);
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
        
        getKeys().add('s', () -> "switch ends",
                      () -> switchEnds());
        
        getKeys().add('a', () -> "include parallel: "
                      + KbdMode.boolStr(includeParallel),
                      () -> toggleIncludeParallel());
    }

    private ShapeGroup shape() {
        return slot().shape();
    }

    private Line line() {
        return currentLine;
    }

    private void switchEnds() {
        startEnd = !startEnd;
        if (startEnd) {
            keyCursorX = start.x();
            keyCursorY = start.y();
        } else {
            keyCursorX = end.x();
            keyCursorY = end.y();
        }
    }

    private void toggleIncludeParallel() {
        includeParallel = !includeParallel;
    }

    @Override
    public void update() {
        
        // line position
        if (startEnd)
            start = new Pt2D(keyCursorX, keyCursorY);
        else
            end = new Pt2D(keyCursorX, keyCursorY);
        currentLine = new Line(start, end);

        // shape color based on validation
        if (shape().isValid())
            slot().wrapper().setColor(Color.GRAY);
        else
            slot().wrapper().setColor(Color.RED);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        text.clear();
        text45.clear();

        // SHAPE

        g.setColor(slot().wrapper().getColor());
        gfx().shape(g, shape());
        
        // LINE
        
        if (!line().isDegenerate()) {

            g.setColor((line().is45Compliant() ? Color.CYAN : Color.MAGENTA));
            gfx().arrow(g, line());

            g.setColor(Color.RED);
            Collection<Pt2Df> ipts = null;
            ipts = getPoints((includeParallel ?
                              Polygon::getIntersectionPointsIncludeParallel :
                              Polygon::getIntersectionPoints));
            text.add(ipts.size() + " INTERSECTIONS");
            int count = 1;
            for (Pt2Df p : ipts) {
                gfx().crosshairs(g, p, 36);
                text.add(Color.GRAY, count++ + ": " + p);
            }
            
            g.setColor(Color.YELLOW);
            ipts = getPoints((includeParallel ?
                              Polygon::getIntersectionPointsIncludeParallel45 :
                              Polygon::getIntersectionPoints45));
            text45.add(ipts.size() + " INTERSECTIONS (45)");
            count = 1;
            for (Pt2Df p : ipts) {
                gfx().crosshairs(g, p, 20);
                text45.add(Color.GRAY, count++ + ": " + p);
            }
            
        }

        // INFO ETC

        if (showDiagnostics) {
            text45.paint(g);
            text.paint(g);
        }

        paintKbdCursor(g);
    }

    /**
     * <p>Pass method reference to Polygon/Line intersection method...</p>
     */
    private Set<Pt2Df> getPoints(BiFunction<Polygon, Line, Set<Pt2Df>> func) {
        Set<Pt2Df> points = new HashSet<>();
        for (Shape45 s : shape())
            for (Pt2Df p : func.apply(s.getOutline(), line()))
                points.add(p);
        return points;
    }

}
