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

        Pt2Df ip = l1.getIntersectionPoint(l2);
        if (ip != null) {
            g.setColor(Color.RED);
            gfx().crosshairs(g, ip, 20);
        }
        Pt2Df ip45 = l1.getIntersectionPoint45(l2);
        if (ip45 != null) {
            g.setColor(Color.YELLOW);
            gfx().crosshairs(g, ip45, 14);
        }

        infoBlock.clear();
        
        addLineInfoString(infoBlock, "LINE 1", l1);
        addLineInfoString(infoBlock, "LINE 2", l2);

        infoBlock.addIfElse(ip == null, Color.RED, Color.GREEN,
                            "INTERSECTION-POINT:    " + ip);
        infoBlock.addIfElse(ip45 == null, Color.RED, Color.GREEN,
                            "INTERSECTION-POINT-45: " + ip45);
        
        infoBlock.addSeparator(Color.GRAY);
        boolean test = Line.linesIntersect(l1, l2);
        infoBlock.addIfElse(test, Color.GREEN, Color.RED,
                            "INTERSECTS: " + test);
        test = Line.linesIntersectIgnoreSharedEnds(l1, l2);
        infoBlock.addIfElse(test, Color.GREEN, Color.RED,
                            "INTERSECTS: (ignore shared ends)" + test);
        infoBlock.addSeparator(Color.GRAY);
        test = Line.linesIntersect45(l1, l2);
        infoBlock.addIfElse(test, Color.GREEN, Color.RED,
                            "INTERSECTS-45: " + test);
        test = Line.linesIntersect45IgnoreSharedEnds(l1, l2);
        infoBlock.addIfElse(test, Color.GREEN, Color.RED,
                            "INTERSECTS-45: (ignore shared ends): " + test);
        test = Line.linesIntersect45IgnoreEnds(l1, l2);
        infoBlock.addIfElse(test, Color.GREEN, Color.RED,
                            "INTERSECTS-45: (ignore ends): " + test);
        
        infoBlock.addSeparator(Color.GRAY);
        infoBlock.add(infoColor, "INSIDE LINE...");
        addContainsString(infoBlock, "LINE 1", l1, ip45);
        addContainsString(infoBlock, "LINE 2", l2, ip45);
        infoBlock.paint(g);
    }

    private void addLineInfoString(TextBlock tb, String title, Line l) {
        String angle = " angle=" + (l.isDegenerate() ?
                                    "null" :
                                    Math.toDegrees(Geom2D.lineAngle(l)));
        String str = title + ": start" + l.start() + " end" + l.end() + angle;
        tb.addIfElse(l.isDegenerate(),
                     Color.RED, str,
                     Color.CYAN, str);
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
