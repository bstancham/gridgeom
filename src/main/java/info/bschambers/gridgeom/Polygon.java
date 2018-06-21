package info.bschambers.gridgeom;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import static info.bschambers.gridgeom.Geom2D.WindingDir;

/**
 * <p>Immutable data type representing a polygon made up of an ordered
 * collection of {@code int} co-ordinate vertices.</p>
 */
public class Polygon implements Iterable<Pt2D> {

    private Pt2D[] vertices;
    protected WindingDir winding = null;
    private Boolean convex = null;
    private Integer numLeftTurns = null;
    private Integer numRightTurns = null;

    public Polygon(Pt2D ... vertices) {
        this.vertices = vertices;
    }

    public int getNumVertices() { return vertices.length; }

    public Pt2D getVertex(int index) {
        return vertices[index];
    }

    public Pt2D getVertexWrapped(int i) {
        if (i < 0)
            return vertices[vertices.length + i % vertices.length];
        if (i >= vertices.length)
            return vertices[i % vertices.length];
        return vertices[i];
    }

    public boolean containsVertex(Pt2D v) {
        for (Pt2D vv : this)
            if (v.equals(vv))
                return true;
        return false;
    }
    
    public boolean containsVertex(Pt2Df v) {
        for (Pt2D vv : this)
            if (vv.equalsValue(v))
                return true;
        return false;
    }
    


    /*---------------------------- GEOMETRY ----------------------------*/

    /**
     * <p>Makes a new array of all the edges.</p>
     */
    public Line[] getEdges() {
        Line[] edges = new Line[getNumVertices()];
        for (int i = 0; i < getNumVertices(); i++) {
            edges[i] = new Line(vertices[i], getVertexWrapped(i + 1));
        }
        return edges;
    }

    

    /*-------------------------- INTERSECTION --------------------------*/

    public Set<Pt2Df> getIntersectionPoints(Line ln) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (Line e : getEdges()) {
            Pt2Df p = e.getIntersectionPoint(ln);
            if (p != null) {
                if (e.boundingBoxContains(p) &&
                    ln.boundingBoxContains(p))
                    points.add(p);
            }
            
            // // make sure not to miss any intersecting vertices of collinear lines etc
            // if (ln.contains45(e.start()))
            //     points.add(e.start().toFloat());
            // if (ln.contains45(e.end()))
            //     points.add(e.end().toFloat());

        }
        return points;
    }
    
    public boolean intersectsIgnoreSharedVertices(Line ln) {
        Set<Pt2Df> ipts = getIntersectionPoints(ln);
        
        if (ipts.size() == 0)
            return false;

        // check each intersection point against vertices
        for (Pt2Df p : ipts) {
            if (!containsVertex(p))
                return true;
            if (!ln.start().equalsValue(p) &&
                !ln.end().equalsValue(p))
                return true;
        }
        
        return false;
    }
    


    /*------------------ INTERSECTION (45-compliant) -------------------*/

    public Set<Pt2Df> getIntersectionPoints45(Line ln) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (Line e : getEdges()) {
            Pt2Df p = e.getIntersectionPoint45(ln);
            if (p != null) {
                if (e.boundingBoxContains(p) &&
                    ln.boundingBoxContains(p))
                    points.add(p);
            }
            // make sure not to miss any intersecting vertices of collinear lines etc
            if (ln.contains45(e.start()))
                points.add(e.start().toFloat());
            if (ln.contains45(e.end()))
                points.add(e.end().toFloat());
        }
        return points;
    }
    
    public Set<Pt2Df> getIntersectionPoints45(Polygon s) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (Line l1 : getEdges()) {
            for (Line l2 : s.getEdges()) {
                Pt2Df p = l1.getIntersectionPoint45(l2);
                if (p != null) {
                    if (l1.boundingBoxContains(p) &&
                        l2.boundingBoxContains(p))
                        points.add(p);
                }
            }
            // make sure not to miss any intersecting vertices of collinear lines etc
            for (Pt2D p : s)
                if (l1.contains45(p))
                    points.add(p.toFloat());
        }
        return points;
    }
    
    public boolean intersectsIgnoreSharedVertices45(Line ln) {
        Set<Pt2Df> ipts = getIntersectionPoints45(ln);
        
        if (ipts.size() == 0)
            return false;

        // check each intersection point against vertices
        for (Pt2Df p : ipts) {
            if (!containsVertex(p))
                return true;
            if (!ln.start().equalsValue(p) &&
                !ln.end().equalsValue(p))
                return true;
        }
        
        return false;
    }
    
    public boolean intersectsIgnoreSharedVertices45(Polygon poly) {
        Set<Pt2Df> ipts = getIntersectionPoints45(poly);
        
        if (ipts.size() == 0)
            return false;

        // check each intersection point against vertices
        for (Pt2Df p : ipts) {
            if (!containsVertex(p)) return true;
            if (!poly.containsVertex(p)) return true;
        }
        
        return false;
    }

    

    /*--------------------------- DIAGNOSTIC ---------------------------*/

    /**
     * @return True, if shape complies with the 45 degree rule, i.e. every angle
     * is either divisible by 45 degrees or is zero.
     */
    public boolean is45Compliant() {
        for (int i = 0; i < getNumVertices(); i++) {
            double angle = Math.toDegrees(getAngleAtVertex(i));
            if (angle % 45 != 0)
                return false;
        }
        return true;
    }

    /**
     * @return The angle in radians of the corner at vertex index {@code i}.
     * @throws IndexOutOfBoundsException if number of vertices in shape is less
     * than three.
     */
    public double getAngleAtVertex(int i) {
        return Geom2D.angleTurned(getVertexWrapped(i - 1).toFloat(),
                                  getVertexWrapped(i).toFloat(),
                                  getVertexWrapped(i + 1).toFloat());
    }

    public WindingDir getWindingDir() {
        if (winding == null)
            countTurns();
        return winding;
    }

    /**
     * <p>Counts number of left and right turns and uses the results to find the
     * winding direction of the polygon and whether or not it is convex.</p>
     */
    private void countTurns() {
        numLeftTurns = 0;
        numRightTurns = 0;
        for (int i = 0; i < getNumVertices(); i++) {
            int dir = Geom2D.turnDirection(getVertexWrapped(i - 1),
                                           getVertexWrapped(i),
                                           getVertexWrapped(i + 1));
            if (dir < 0) numLeftTurns++;
            if (dir > 0) numRightTurns++;
        }
        // analyse the results        
        winding = WindingDir.INDETERMINATE;
        if (numLeftTurns > numRightTurns)
            winding = WindingDir.CCW;
        if (numRightTurns > numLeftTurns)
            winding = WindingDir.CW;
        convex = numLeftTurns == 0 || numRightTurns == 0;
    }
    
    public boolean isCWWinding() {
        return getWindingDir() == WindingDir.CW;
    }
    
    public boolean isCCWWinding() {
        return getWindingDir() == WindingDir.CCW;
    }

    public boolean isConvex() {
        if (convex == null)
            countTurns();
        return convex;
    }

    public int getNumDuplicateVertices() {
        int n = 0;
        for (Pt2D v1 : this)
            for (Pt2D v2 : this)
                if (v1 != v2)
                    if (v1.equals(v2))
                        n++;
        return n;
    }



    /*--------------------------- ITERATION ----------------------------*/

    public Iterator<Pt2D> iterator() {
        return new Pt2DIterator();
    }

    private class Pt2DIterator implements Iterator<Pt2D> {
        private int i = 0;
        @Override
        public Pt2D next() {
            return vertices[i++];
        }
        @Override
        public boolean hasNext() {
            return i < vertices.length;
        }
    }

    

    /*-------------- TRANSFORMATIONS (return new Polygon) --------------*/

    public Polygon shift(int x, int y) {
        Pt2D[] newVertices = new Pt2D[vertices.length];
        for (int i = 0; i < vertices.length; i++)
            newVertices[i] = vertices[i].transpose(x, y);
        return new Polygon(newVertices);
    }

    public Polygon reverseWinding() {
        Pt2D[] newVertices = new Pt2D[vertices.length];
        for (int i = 0; i < vertices.length; i++)
            newVertices[i] = vertices[vertices.length - 1 - i];
        return new Polygon(newVertices);
    }

    public Polygon reflectX(int center) {
        Pt2D[] newVertices = new Pt2D[vertices.length];
        for (int i = 0; i < vertices.length; i++)
            newVertices[i] = vertices[i].reflectX(center);
        return new Polygon(newVertices);
    }

    public Polygon reflectY(int center) {
        Pt2D[] newVertices = new Pt2D[vertices.length];
        for (int i = 0; i < vertices.length; i++)
            newVertices[i] = vertices[i].reflectY(center);
        return new Polygon(newVertices);
    }

    public Polygon rotate90(int centerX, int centerY) {
        Pt2D[] newVertices = new Pt2D[vertices.length];
        for (int i = 0; i < vertices.length; i++)
            newVertices[i] = vertices[i].rotate90(centerX, centerY);
        return new Polygon(newVertices);
    }

    public Polygon rotateVertexOrder(int amt) {
        Pt2D[] newVertices = new Pt2D[vertices.length];
        for (int i = 0; i < vertices.length; i++)
            newVertices[i] = getVertexWrapped(i + amt);
        return new Polygon(newVertices);
    }



    /*------------------------- TRIANGULATION --------------------------*/

    /**
     * <p>NOTE: Only works for polygon with CCW winding!</p>
     */
    public static Triangle[] triangulate(Polygon poly) {
        if (poly.isConvex())
            return triangulateConvex(poly);
        else
            return triangulateEarClipping(poly);
    }

    /**
     * <p>Fast method, but only works for convex polygons no zero-degree
     * angles.</p>
     */
    public static Triangle[] triangulateConvex(Polygon poly) {
        Triangle[] tris = new Triangle[poly.getNumVertices() - 2];
        System.out.println("Polygon.triangulateConvex() --> "
                           + tris.length + " triangles");
        for (int i = 0; i < tris.length; i++) {
            tris[i] = new Triangle(poly.getVertex(0),
                                   poly.getVertex(i + 1),
                                   poly.getVertex(i + 2));
        }
        return tris;
    }

    /**
     * <p>Works for any valid polygon with CCW winding.</p>
     */
    public static Triangle[] triangulateEarClipping(Polygon poly) {
        return new EarClippingTriangulator(poly).getTriangles();
    }

    private static class EarClippingTriangulator {
        
        private boolean[] used;
        private int a = 0;
        private int b = 0;
        private int c = 0;
        
        private boolean kill = false;
        private int totalCount = 0;

        private List<Triangle> tris = new ArrayList<>();
        
        public EarClippingTriangulator(Polygon poly) {
            used = new boolean[poly.getNumVertices()];
            
            while (numRemaining() >= 3
                   && !kill) {

                System.out.format("a=%s, b=%s, c=%s --- %s triangles --- remaining: %s\n",
                                  a, b, c, tris.size(), remainString());

                // get next triangle
                b = nextIndex(a);
                c = nextIndex(b);
                Triangle t = new Triangle(poly.getVertex(a),
                                          poly.getVertex(b),
                                          poly.getVertex(c));
                
                if (!triangleIntersectsShape(poly) &&
                    t.isCCWWinding()) {
                    tris.add(t);
                    used[b] = true;
                } else {
                    // move start point to next index
                    a = b;
                }
            }

            System.out.println("made " + tris.size() + " triangles");
            // triangles = tris.toArray(new Triangle[tris.size()]);
        }

        public Triangle[] getTriangles() {
            return tris.toArray(new Triangle[tris.size()]);
        }
        
        private String remainString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < used.length; i++) {
                if (used[i])
                    sb.append(". |");
                else
                    sb.append(i + " |");
            }
            return sb.toString();
        }

        private int numRemaining() {
            int count = 0;
            for (Boolean bool : used)
                if (!bool)
                    count++;
            return count;
        }

        private int nextIndex(int i) {
            i++;
            totalCount++;
            if (i >= used.length) {
                i = 0;
            }
            while (used[i]) {
                i++;
                if (i >= used.length) {
                    i = 0;
                }
            }

            // prevent infinite loop during testing
            if (totalCount > used.length * 5)
                kill = true;
            
            return i;
        }

        private boolean triangleIntersectsShape(Polygon poly) {
            return edgeIntersectsShape(poly, a, b) ||
                   edgeIntersectsShape(poly, b, c) ||
                   edgeIntersectsShape(poly, c, a);
        }

        private boolean edgeIntersectsShape(Polygon poly, int i1, int i2) {

            if (contingentIndices(poly, i1, i2))
                return false;

            Line ln = new Line(poly.getVertex(i1),
                               poly.getVertex(i2));
            if (poly.intersectsIgnoreSharedVertices(ln))
                return true;
                
            return false;
        }

        private boolean contingentIndices(Polygon poly, int i1, int i2) {
            if (i1 < i2 && i1 == i2 - 1)
                return true;
           if (i1 == poly.getNumVertices() - 1 &&
                i2 == 0)
                return true;
            return false;
        }
    }

}
