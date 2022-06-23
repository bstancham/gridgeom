package info.bstancham.gridgeom.testgui;

import java.util.Iterator;
import java.awt.Graphics;
import java.awt.Color;
import info.bstancham.gridgeom.*;

public class Gfx {

    private int scaling = 10;
    private int centerX = 0;
    private int centerY = 0;
    private CanvasPanel panel;

    private Color[] digraphConnectionColors = new Color[] {
        new Color(100, 100, 0  ),
        new Color(200, 100, 0  ),
        new Color(100, 200, 0  ),
        new Color(200, 200, 0  ),
        new Color(200, 100, 100),
        new Color(100, 200, 100),
        new Color(200, 200, 100),

        new Color(100, 0  , 100),
        new Color(200, 0  , 100),
        new Color(100, 0  , 200),
        new Color(200, 0  , 200),
        new Color(200, 100, 100),
        new Color(100, 100, 200),
        new Color(200, 100, 200),

        new Color(0  , 100, 100),
        new Color(0  , 200, 100),
        new Color(0  , 100, 200),
        new Color(0  , 200, 200),
        new Color(100, 200, 100),
        new Color(100, 100, 200),
        new Color(100, 200, 200),
    };

    public Gfx(CanvasPanel panel) {
        this.panel = panel;
    }

    private int getScaling() {
        return scaling;
    }

    private int getCenterX() {
        return centerX;
    }

    private int getCenterY() {
        return panel.getHeight() - centerY;
    }

    public int getX(int x) {
        return x * scaling + getCenterX();
    }

    public int getY(int y) {
        return getCenterY() - (y * scaling);
    }

    public int getX(float x) {
        return (int) (x * scaling) + getCenterX();
    }

    public int getY(float y) {
        return (int) (getCenterY() - (y * scaling));
    }

    public Pt2D getPoint(Pt2D p) {
        return new Pt2D(getX(p.x()), getY(p.y()));
    }

    public void setScaling(int val) {
        scaling = val;
    }

    public void incrementScaling(int amt) {
        scaling += amt;
        if (scaling < 1) scaling = 1;
    }

    public void shiftCenter(int x, int y) {
        centerX += x;
        centerY += y;
    }

    /**
     * <p>Paints horizontal and vertical which cross at the current center.</p>
     */
    public void centerLines(Graphics g) {
        g.drawLine(0, getCenterY(), panel.getWidth(), getCenterY());
        g.drawLine(getCenterX(), 0, getCenterX(), panel.getHeight());
    }

    /**
     * <p>Paints a grid, which fills the whole canvas, and is aligned with the
     * current center point.</p>
     */
    public void grid(Graphics g) {
        // vertical lines
        int x = getCenterX() % getScaling();
        while (x < panel.getWidth()) {
            g.drawLine(x, 0, x, panel.getHeight());
            x += getScaling();
        }
        // horizontal lines
        int y = getCenterY() % getScaling();
        while (y < panel.getHeight()) {
            g.drawLine(0, y, panel.getWidth(), y);
            y += getScaling();
        }
    }

    

    /*------------------------- SIMPLE SHAPES --------------------------*/

    public void line(Graphics g, Line l) {
        line(g, l.start(), l.end(), 0, 0);
    }
    
    public void line(Graphics g, Pt2D start, Pt2D end, int x, int y) {
        line(g,
             x + start.x(),
             y + start.y(),
             x + end.x(),
             y + end.y());
    }
    
    public void line(Graphics g, int x1, int y1, int x2, int y2) {
        g.drawLine(getX(x1), getY(y1), getX(x2), getY(y2));
    }

    public void arrow(Graphics g, Line l) {
        arrow(g, l.toFloat());
    }
    
    public void arrow(Graphics g, Linef l) {
        arrow(g, l.start(), l.end());
    }
    
    public void arrow(Graphics g, Pt2D start, Pt2D end) {
        arrow(g, start.x(), start.y(), end.x(), end.y());
    }
    
    public void arrow(Graphics g, Pt2Df start, Pt2Df end) {
        arrow(g, start.x(), start.y(), end.x(), end.y());
    }
    
    // public void arrow(Graphics g, int x1, int y1, int x2, int y2) {
    public void arrow(Graphics g, float x1, float y1, float x2, float y2) {
        g.drawLine(getX(x1), getY(y1), getX(x2), getY(y2));
        // arrowhead
        double radius = 10;
        double lineAngle = Geom2D.lineAngle(x1, y1, x2, y2);
        double headAngle = Math.toRadians(25);
        double h1Angle = lineAngle + headAngle;
        double h2Angle = lineAngle - headAngle;
        int x = getX(x2);
        int y = getY(y2);
        g.drawLine(x, y,
                   x + (int) (Geom2D.circlePointX(-h1Angle, radius)),
                   y + (int) (Geom2D.circlePointY(-h1Angle, radius)));
        g.drawLine(x, y,
                   x + (int) (Geom2D.circlePointX(-h2Angle, radius)),
                   y + (int) (Geom2D.circlePointY(-h2Angle, radius)));
    }

    public void rect(Graphics g, int x, int y, int sizeX, int sizeY) {
        g.drawRect(getX(x), getY(y + sizeY), sizeX * scaling, sizeY * scaling);
    }

    public void fillRect(Graphics g, int x, int y, int sizeX, int sizeY) {
        g.fillRect(getX(x), getY(y + sizeY), sizeX * scaling, sizeY * scaling);
    }

    public void box(Graphics g, Box2D b) {
        rect(g, b.lowX, b.lowY, b.sizeX, b.sizeY);
    }
    
    public void fillBox(Graphics g, Box2D b) {
        fillRect(g, b.lowX, b.lowY, b.sizeX, b.sizeY);
    }

    public void centeredCircle(Graphics g, int x, int y, int size) {
        int posX = getX(x) - (size / 2);
        int posY = getY(y) - (size / 2);
        g.drawOval(posX, posY, size, size);
    }
    
    public void crosshairs(Graphics g, Pt2Df p, int size) {
        crosshairs(g, p.x(), p.y(), size);
    }
    
    public void crosshairs(Graphics g, float x, float y, int size) {
        int half = size / 2;
        g.drawLine(getX(x) - half, getY(y),        getX(x) + half, getY(y));
        g.drawLine(getX(x),        getY(y) - half, getX(x),        getY(y) + half);
    }
    
    public void crosshairs(Graphics g, int x, int y, int size) {
        int half = size / 2;
        g.drawLine(getX(x) - half, getY(y),        getX(x) + half, getY(y));
        g.drawLine(getX(x),        getY(y) - half, getX(x),        getY(y) + half);
    }



    /*------------------------- COMPLEX SHAPES -------------------------*/

    public void shape(Graphics g, ShapeGroup shape) {
        shape(g, shape, 0, 0);
    }
    
    public void shape(Graphics g, ShapeGroup shape, int x, int y) {
        for (Shape45 s : shape)
            shape(g, s, x, y);
    }

    public void shape(Graphics g, Shape45 s) {
        shape(g, s, 0, 0);
    }
    
    public void shape(Graphics g, Shape45 s, int x, int y) {
        polygon(g, s.getOutline(), x, y);
        for (int i = 0; i < s.getNumSubShapes(); i++)
            shape(g, s.getSubShape(i), x, y);
    }
    
    public void fillShape(Graphics g, ShapeGroup sg) {
        for (int i = 0; i < sg.getNumTriangles(); i++) {
            fillPolygon(g, sg.getTriangle(i));
        }
    }

    public void polygon(Graphics g, Polygon poly) {
        polygon(g, poly, 0, 0);
    }
    
    public void polygon(Graphics g, Polygon poly, int x, int y) {
        Pt2D lastVertex = null;
        for (Pt2D vertex : poly) {
            if (lastVertex != null) {
                Pt2D v1 = lastVertex.transpose(x, y);
                Pt2D v2 = vertex.transpose(x, y);
                if (!v1.equals(v2))
                    arrow(g, v1, v2);
            }
            lastVertex = vertex;
        }
        arrow(g, lastVertex.transpose(x, y), poly.getVertex(0).transpose(x, y));
    }

    public void fillPolygon(Graphics g, Polygon poly) {
        int[] xs = new int[poly.getNumVertices()];
        int[] ys = new int[poly.getNumVertices()];
        for (int i = 0; i < poly.getNumVertices(); i++) {
            xs[i] = getX(poly.getVertex(i).x());
            ys[i] = getY(poly.getVertex(i).y());
        }
        g.fillPolygon(xs, ys, poly.getNumVertices());
    }

    public void shapeNestedDepthFade(Graphics g, ShapeGroup sg, Color c) {
        for (Shape45 s : sg)
            shapeNestedDepthFade(g, s, c);
    }
    
    public void shapeNestedDepthFade(Graphics g, Shape45 s, Color c) {
        // paint outline
        g.setColor(c);
        polygon(g, s.getOutline());
        // get color for sub-shapes
        int depth = s.getNestedDepth();
        Color subColor = Gfx.relativeBrightness(c, 1.0 - (1.0 / depth));
        // paint sub-shapes
        for (Shape45 sub : s.getSubShapes())
            shapeNestedDepthFade(g, sub, subColor);
    }

    public void triangleNumbers(Graphics g, ShapeGroup sg) {
        // adjustment for text size
        int fontSize = g.getFont().getSize();
        int xAdjust = fontSize / 2;
        int yAdjust = fontSize / 2;
        
        int count = 1;
        Iterator<Triangle> iter = sg.triangleIterator();
        while (iter.hasNext()) {
            // get centroid
            Pt2Df centroid = iter.next().centroid();
            int x = getX(centroid.x());
            int y = getY(centroid.y());
            // paint number
            g.drawString("" + count, x - xAdjust, y + yAdjust);
            count++;
        }
    }

    public void digraph(Graphics g, Digraph2D graph) {
        digraph(g, graph, Color.MAGENTA, digraphConnectionColors);
    }
    
    public void digraph(Graphics g, Digraph2D graph,
                        Color nodeColor, Color[] connectionColors) {
        // connections
        for (int i = 0; i < graph.getNumNodes(); i++) {
            Digraph2D.Node n = graph.getNode(i);
            for (Digraph2D.Connection c : n.getConnectionsForward()) {
                // we want to shift line to the side, so that bi-directional
                // connections will show without overlapping...
                // ... also, want to visualise connections with multiple
                // shape-ids, so we will shift the line one step further to the
                // side and re-draw it each time
                Linef line = c.getLine();
                if (line.isDegenerate()) {
                    System.out.println("WARNING! Degenerate line in Gfx.digraph()");
                } else {
                    double shiftRadius = 0.2;
                    // set ends slightly short so that arrowheads don't look muddled
                    Pt2Df startShift = Geom2D.circlePoint(line.angle(),
                                                          shiftRadius).toFloat();
                    Pt2Df endShift = Geom2D.circlePoint(line.angle() + Geom2D.HALF_TURN,
                                                        shiftRadius).toFloat();
                    // to shift sideways
                    Pt2Df multiShift = Geom2D.circlePoint(line.angle() + Geom2D.QUARTER_TURN,
                                                          shiftRadius).toFloat();
                    Linef newLine = new Linef(line.start().sum(startShift),
                                              line.end().sum(endShift));
                    // shift and repaint arrow in different color for each shape-id
                    for (Integer id : c.getIDs()) {
                        g.setColor(connectionColors[id % connectionColors.length]);
                        newLine = newLine.shift(multiShift);
                        arrow(g, newLine);
                    }
                }
            }
        }
        
        // nodes
        for (int i = 0; i < graph.getNumNodes(); i++) {
            Digraph2D.Node n = graph.getNode(i);
            Pt2Df p = n.getPoint();
            g.setColor(nodeColor);
            crosshairs(g, p, 16);
            g.setColor(Color.CYAN);
            g.drawString(n.getNumConnectionsForward() + "",
                         getX(p.x()) + 3,
                         getY(p.y()) - 3);
        }
    }



    /*----------------------------- ANGLES -----------------------------*/

    public void drawAngleTurned(Graphics g, Pt2D a, Pt2D b, Pt2D c) {
        double refAngle = Geom2D.lineAngle(a.toFloat(), b.toFloat());
        double angleTurned = Geom2D.angleTurned(a, b, c);
        g.setColor((angleTurned > 0 ? Color.GREEN : Color.RED));
        // continuation line
        Pt2D cont = Geom2D.circlePoint(-(refAngle + Geom2D.HALF_TURN), 40).toInt();
        g.drawLine(getX(b.x()),
                   getY(b.y()),
                   getX(b.x()) + cont.x(),
                   getY(b.y()) + cont.y());
        // arc
        drawAngle(g, b, 30,
                  (int) -Math.toDegrees(refAngle) + 90,
                  (int) Math.toDegrees(angleTurned));
    }

    public void drawAngle(Graphics g, Pt2D p, int radius, int startAngle, int angle) {
        if (angle % 90 == 0) {
            int rAngle = 0;
            while (rAngle < Math.abs(angle)) {
                int adjusted = (angle > 0 ? startAngle + 90 + rAngle
                                           : startAngle - rAngle);
                drawRightAngle(g, p, radius, adjusted);
                rAngle += 90;
            }
        } else {
            g.drawArc(getX(p.x()) - radius,
                      getY(p.y()) - radius,
                      radius * 2, radius * 2, startAngle, angle);
        }
    }

    /**
     * Draw right angle as square.
     *
     * @TODO: fix!
     */
    public void drawRightAngle(Graphics g, Pt2D p, int radius, int startAngle) {
        // System.out.println("right angle: starting at " + startAngle);
        Pt2D centre = new Pt2D(getX(p.x()), getY(p.y()));
        Pt2D p1 = Geom2D.circlePoint(Math.toRadians(startAngle), radius).toInt();
        Pt2D p2 = Geom2D.circlePoint(Math.toRadians(startAngle + 90), radius).toInt();
        Pt2D p3 = p1.sum(p2);
        Pt2D a = centre.sum(p1);
        Pt2D b = centre.sum(p2);
        Pt2D c = centre.sum(p3);
        g.drawLine(a.x(), a.y(), c.x(), c.y());
        g.drawLine(b.x(), b.y(), c.x(), c.y());
    }

    public void drawAbsoluteAngle(Graphics g, Line ln) {
        g.setColor(Color.PINK);
        drawAngle(g, ln.start(), 60, 90, (int) -Math.toDegrees(ln.angle()));
        Pt2D endPt = getPoint(ln.start());
        g.drawLine(endPt.x(), endPt.y(),
                   endPt.x(), endPt.y() - 70);
    }


    
    /*----------------------------- COLOR ------------------------------*/
    
    /**
     * @return A new random color generated using RGB values between 0
     * and 255.
     */
    public static Color randomColor() {
        return new Color((int) (Math.random() * 255),
                         (int) (Math.random() * 255),
                         (int) (Math.random() * 255));
    }
    
    /**
     * @param c The input color.
     * @return A copy of the input color, with the RGB values inverted.
     */
    public static Color invertColor(Color c) {
        return new Color(255 - c.getRed(),
                         255 - c.getGreen(),
                         255 - c.getBlue());
    }

    /**
     * @param c The input color.
     * @param amount The amount to adjust brightness by. Value of 1.0
     * will be just the same as the input color. Value may be larger
     * than 1.0.
     * @return A new color based on the input color, but with relative
     * brightness adjusted.
     */ 
    public static Color relativeBrightness(Color c, double amount) {
        int rVal = (int)(c.getRed() * amount);   if (rVal > 255) { rVal = 255; }
        int gVal = (int)(c.getGreen() * amount); if (gVal > 255) { gVal = 255; }
        int bVal = (int)(c.getBlue() * amount);  if (bVal > 255) { bVal = 255; }
        return new Color(rVal, gVal, bVal);
    }

}
