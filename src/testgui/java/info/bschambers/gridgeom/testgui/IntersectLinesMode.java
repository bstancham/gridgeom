package info.bschambers.gridgeom.testgui;

import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import info.bschambers.gridgeom.*;

public class IntersectLinesMode extends CanvasMode {

    private TextBlock infoBlock = new TextBlock(300, 10);
    private Color infoColor = Color.GRAY;

    private enum End {
        Line1Start, 
        Line1End, 
        Line2Start, 
        Line2End, 
    };

    private End editPoint = End.Line1End;

    private Pt2D start1 = new Pt2D(1, 5);
    private Pt2D end1   = new Pt2D(10, 5);
    private Pt2D start2 = new Pt2D(4, 3);
    private Pt2D end2   = new Pt2D(4, 8);

    public IntersectLinesMode() {
        getKeys().add('a', "edit line 1 start",
                 () -> editEnd(End.Line1Start));
        getKeys().add('s', "edit line 1 end",
                 () -> editEnd(End.Line1End));
        getKeys().add('d', "edit line 2 start",
                 () -> editEnd(End.Line2Start));
        getKeys().add('f', "edit line 2 end",
                 () -> editEnd(End.Line2End));
    }

    private void editEnd(End e) {
        editPoint = e;
        if        (e == End.Line1Start) {
            keyCursorX = start1.x();
            keyCursorY = start1.y();
        } else if (e == End.Line1End) {
            keyCursorX = end1.x();
            keyCursorY = end1.y();
        } else if (e == End.Line2Start) {
            keyCursorX = start2.x();
            keyCursorY = start2.y();
        } else if (e == End.Line2End) {
            keyCursorX = end2.x();
            keyCursorY = end2.y();
        }
    }

    @Override
    public void update() {
        // update end
        if        (editPoint == End.Line1Start) {
            start1 = new Pt2D(keyCursorX, keyCursorY);
        } else if (editPoint == End.Line1End) {
            end1 = new Pt2D(keyCursorX, keyCursorY);
        } else if (editPoint == End.Line2Start) {
            start2 = new Pt2D(keyCursorX, keyCursorY);
        } else if (editPoint == End.Line2End) {
            end2 = new Pt2D(keyCursorX, keyCursorY);
        }
    }

    @Override
    public void paint(Graphics g) {

        getCanvas().paintGrid(g);
        
        Line l1 = new Line(start1, end1);
        Line l2 = new Line(start2, end2);

        if (!l1.isDegenerate()) {
            g.setColor(getAngleColour(l1));
            gfx().arrow(g, l1);
        }
        if (!l2.isDegenerate()) {
            g.setColor(getAngleColour(l2));
            gfx().arrow(g, l2);
        }
        
        paintKbdCursor(g);

        Pt2Df iPt = l1.getIntersectionPoint(l2);
        if (iPt != null) {
            g.setColor(Color.YELLOW);
            gfx().crosshairs(g, iPt, 14);
        }

        infoBlock.clear();
        infoBlock.addIfElse(iPt == null,
                            Color.RED, "INTERSECTION POINT: " + iPt,
                            Color.GREEN, "INTERSECTION POINT: " + iPt);
        addLineInfoString(infoBlock, "LINE 1", l1);
        addLineInfoString(infoBlock, "LINE 2", l2);
        infoBlock.add(infoColor, "INSIDE LINE...");
        addContainsString(infoBlock, "LINE 1", l1, iPt);
        addContainsString(infoBlock, "LINE 2", l2, iPt);
        infoBlock.paint(g);
    }

    private void addLineInfoString(TextBlock tb, String title, Line l) {
        String angle = " angle=" + (l.isDegenerate() ?
                                    "null" :
                                    Math.toDegrees(Geom2D.lineAngle(l)));
        String str = title + ": start" + l.start() + " end" + l.end() + angle;
        tb.addIfElse(l.isDegenerate(),
                     Color.RED, str,
                     infoColor, str);
    }

    private void addContainsString(TextBlock tb, String title, Line l, Pt2Df p) {
        String str = "... " + title + ": ";
        Color c = null;
        if (p == null) {
            str += "null";
            c = Color.RED;
        } else {
            boolean contains = l.boundingBoxContains(p);
            str += contains;
            c = (contains ? Color.GREEN : Color.ORANGE);
        }
        tb.add(c, str);
    }

    private Color getAngleColour(Line l) {
        if (l.is45Compliant())
            return Color.CYAN;
        else
            return Color.RED;
    }

}
