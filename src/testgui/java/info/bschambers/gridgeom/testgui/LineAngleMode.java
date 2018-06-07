package info.bschambers.gridgeom.testgui;

import java.awt.Graphics;
import java.awt.Color;
import info.bschambers.gridgeom.*;

public class LineAngleMode extends CanvasMode {

    private TextBlock text = new TextBlock(200, 10);

    private Pt2D[] points = new Pt2D[] {
        new Pt2D(4, 4),
        new Pt2D(10, 4),
        new Pt2D(16, 10)
    };

    private int endIndex = 2;

    private Color angleBetweenColor = new Color(48, 175, 48);
    private Color lineLengthColor =   new Color(48, 175, 191);
    
    public LineAngleMode() {
        bindings.add(new KeyBinding('s', "switch ends", () -> switchEnds()));
        setKeyCursorToCurrentPoint();
    }

    private void switchEnds() {
        endIndex--;
        if (endIndex < 0)
            endIndex = 2;
        setKeyCursorToCurrentPoint();
    }

    public void setKeyCursorToCurrentPoint() {
        keyCursorX = points[endIndex].x();
        keyCursorY = points[endIndex].y();
    }

    @Override
    public void update() {
        points[endIndex] = new Pt2D(keyCursorX, keyCursorY);
    }

    @Override
    public void paint(Graphics g) {
        text.clear();
        
        Line line1 = new Line(points[0], points[1]);
        Line line2 = new Line(points[1], points[2]);
        paintGrid(g);
        
        // draw lines and basic info
        g.setColor(Color.CYAN);
        gfx().line(g, line1);
        if (!line2.isDegenerate())
            gfx().arrow(g, line2);
        
        text.add(Color.CYAN,
                 coordsString("(start)", points[0]),
                 coordsString("  (mid)", points[1]),
                 coordsString("  (end)", points[2]));
        
        text.add(lineLengthColor,
                 lineLengthString("LINE 1", line1),
                 lineLengthString("LINE 2", line2));

        text.add(Color.PINK,
                 lineAngleString("LINE 1", line1),
                 lineAngleString("LINE 2", line2));
        

        
        if (!line1.isDegenerate() && !line2.isDegenerate()) {

            // paint angles
            double angleBetween = Geom2D.angle(points[0], points[1], points[2]);
            double angleTurned = Geom2D.angleTurned(points[0], points[1], points[2]);

            // absolute angle
            gfx().drawAbsoluteAngle(g, line1);
            gfx().drawAbsoluteAngle(g, line2);
            // angle-between
            g.setColor(angleBetweenColor);
            gfx().drawAngle(g, points[1], 45,
                            (int) -Math.toDegrees(line1.angle()) - 90,
                            (int) -Math.toDegrees(angleBetween));
            // angle turned
            gfx().drawAngleTurned(g, points[0], points[1], points[2]);
        
            text.add((angleTurned < 0 ? Color.RED : Color.GREEN),
                     "ANGLE TURNED (degrees): " + Math.toDegrees(angleTurned),
                     "ANGLE TURNED (radians): " + angleTurned);
        
            text.add(angleBetweenColor,
                     "ANGLE BETWEEN (degrees): " + Math.toDegrees(angleBetween),
                     "ANGLE BETWEEN (radians): " + angleBetween);
        }
        
        text.paint(g);
        paintKbdCursor(g);
        
    }
    
    private String coordsString(String title, Pt2D p) {
        return String.format("%s co-ords: %4d, %4d", title, p.x(), p.y());
    }

    private String lineAngleString(String title, Line ln) {
        String angleStr = null;
        if (ln.isDegenerate())
            angleStr = "DEGENERATE LINE";
        else
            angleStr = String.format("ANGLE = (degree) %f, (radians) %f",
                                     Math.toDegrees(ln.angle()), ln.angle());
        return String.format("%s: slope=%f intercept=%f --- %s",
                             title, ln.slope(), ln.intercept(), angleStr);
    }

    private String lineLengthString(String title, Line ln) {
        return String.format("%s: xDist=%4d yDist=%4d --- LENGTH = %.4f",
                             title, ln.distX(), ln.distY(), ln.length());
    }
    
}
