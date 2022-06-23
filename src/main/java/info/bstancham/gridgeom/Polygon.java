package info.bstancham.gridgeom;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import static info.bstancham.gridgeom.Geom2D.WindingDir;

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
    private Line[] polyEdges = null;

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

    /**
     * <p>Makes a new array of all the edges.</p>
     */
    public int getNumEdges() {
        if (polyEdges == null)
            buildEdges();
        return polyEdges.length;
    }

    /**
     * @return The edge at index {@code i}.
     */
    public Line getEdge(int i) {
        if (polyEdges == null)
            buildEdges();
        return polyEdges[i];
    }

    private void buildEdges() {
        polyEdges = new Line[getNumVertices()];
        for (int i = 0; i < getNumVertices(); i++)
            polyEdges[i] = new Line(vertices[i], getVertexWrapped(i + 1));
    }

    public boolean hasVertex(Pt2D v) {
        for (Pt2D vv : this)
            if (v.equals(vv))
                return true;
        return false;
    }

    public boolean hasVertex(Pt2Df v) {
        for (Pt2D vv : this)
            if (vv.equalsValue(v))
                return true;
        return false;
    }



    /*---------------- INTERSECTION (non-45 compliant) -----------------*/

    public Set<Pt2Df> getIntersectionPoints(Line ln) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (int i = 0; i < getNumEdges(); i++) {
            Line edge = getEdge(i);
            Pt2Df p = edge.getIntersectionPoint(ln);
            if (p != null) {
                if (edge.boundingBoxContains(p) &&
                    ln.boundingBoxContains(p))
                    points.add(p);
            }
        }
        return points;
    }

    public Set<Pt2Df> getIntersectionPointsIncludeParallel(Line ln) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (int i = 0; i < getNumEdges(); i++) {
            Line edge = getEdge(i);
            Pt2Df p = edge.getIntersectionPoint(ln);
            if (p != null) {
                if (edge.boundingBoxContains(p) &&
                    ln.boundingBoxContains(p))
                    points.add(p);
            }
            // make sure not to miss any intersecting vertices of collinear lines etc
            if (ln.contains(edge.start()))
                points.add(edge.start().toFloat());
            if (ln.contains(edge.end()))
                points.add(edge.end().toFloat());
        }
        return points;
    }

    public boolean intersectsIgnoreSharedVertices(Line ln) {
        Set<Pt2Df> ipts = getIntersectionPoints(ln);

        if (ipts.size() == 0)
            return false;

        // check each intersection point against vertices
        for (Pt2Df p : ipts) {
            if (!hasVertex(p))
                return true;
            if (!ln.start().equalsValue(p) &&
                !ln.end().equalsValue(p))
                return true;
        }

        return false;
    }

    public boolean intersectsIgnoreSharedVerticesIncludeParallel(Line ln) {
        Set<Pt2Df> ipts = getIntersectionPointsIncludeParallel(ln);

        if (ipts.size() == 0)
            return false;

        // check each intersection point against vertices
        for (Pt2Df p : ipts) {

            boolean pContains = hasVertex(p);
            boolean ls = ln.start().equalsValue(p);
            boolean le = ln.end().equalsValue(p);

            if (!pContains) return true;
            if (!ls && !le) return true;

            if (pContains && !(ls || le)) return true;
            if (!pContains && (ls || le)) return true;

        }

        return false;
    }



    /*------------------ INTERSECTION (45-compliant) -------------------*/

    public Set<Pt2Df> getIntersectionPoints45(Line ln) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (int i = 0; i < getNumEdges(); i++) {
            Line edge = getEdge(i);
            Pt2Df p = edge.getIntersectionPoint45(ln);
            if (p != null) {
                if (edge.boundingBoxContains(p) &&
                    ln.boundingBoxContains(p))
                    points.add(p);
            }
        }
        return points;
    }

    public Set<Pt2Df> getIntersectionPointsIncludeParallel45(Line ln) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (int i = 0; i < getNumEdges(); i++) {
            Line edge = getEdge(i);
            Pt2Df p = edge.getIntersectionPoint45(ln);
            if (p != null) {
                if (edge.boundingBoxContains(p) &&
                    ln.boundingBoxContains(p))
                    points.add(p);
            }
            // make sure not to miss any intersecting vertices of collinear lines etc
            if (ln.contains45(edge.start()))
                points.add(edge.start().toFloat());
            if (ln.contains45(edge.end()))
                points.add(edge.end().toFloat());
        }
        return points;
    }

    public Set<Pt2Df> getIntersectionPoints45(Polygon s) {
        Set<Pt2Df> points = new HashSet<>();
        // intersect all lines
        for (int i = 0; i < getNumEdges(); i++) {
            Line e1 = getEdge(i);
            for (int j = 0; j < s.getNumEdges(); j++) {
                Line e2 = s.getEdge(j);
                Pt2Df p = e1.getIntersectionPoint45(e2);
                if (p != null) {
                    if (e1.boundingBoxContains(p) &&
                        e2.boundingBoxContains(p))
                        points.add(p);
                }
            }
            // make sure not to miss any intersecting vertices of collinear lines etc
            for (Pt2D p : s)
                if (e1.contains45(p))
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
            if (!hasVertex(p))
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
            if (!hasVertex(p)) return true;
            if (!poly.hasVertex(p)) return true;
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
        return Geom2D.angleTurned(getVertexWrapped(i - 1),
                                  getVertexWrapped(i),
                                  getVertexWrapped(i + 1));
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
        int numZero = 0;
        for (int i = 0; i < getNumVertices(); i++) {
            int dir = Geom2D.turnDirection(getVertexWrapped(i - 1),
                                           getVertexWrapped(i),
                                           getVertexWrapped(i + 1));
            if (dir < 0)
                numLeftTurns++;
            else if (dir > 0)
                numRightTurns++;
            else
                numZero++;
        }
        // analyse results
        winding = WindingDir.INDETERMINATE;
        if (numLeftTurns > numRightTurns)
            winding = WindingDir.CCW;
        if (numRightTurns > numLeftTurns)
            winding = WindingDir.CW;
        convex = numZero == 0 &&
            (numLeftTurns == 0 || numRightTurns == 0);
    }

    public boolean isCWWinding() {
        return getWindingDir() == WindingDir.CW;
    }

    public boolean isCCWWinding() {
        return getWindingDir() == WindingDir.CCW;
    }

    /**
     * <p>Polygon is considered convex if the angle at every vertex turns in the
     * same direction, i.e. either all turns are clockwise, or all turns are
     * counter-clockwise.</p>
     *
     * <p>Note that if any zero-degree angles are present the polygon is
     * considered <strong>not</strong> convex. This is consistent with what may
     * be safely be used with {@link Polygon#triangulatConvex
     * triangulateConvex}.</p>
     */
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

        private Polygon poly;
        private boolean[] used;
        private int numRemaining;
        private int a = 0;
        private int b = 0;
        private int c = 0;

        private int countSinceLast = 0;
        private boolean failed = false;

        private List<Triangle> tris = new ArrayList<>();

        public EarClippingTriangulator(Polygon p) {
            poly = p;
            used = new boolean[poly.getNumVertices()];
            numRemaining = used.length;

            // reduce vertices until only 3 remain (the final triangle)
            while (numRemaining >= 3
                   && !failed) {

                // get next triangle
                b = nextIndex(a);
                c = nextIndex(b);
                Triangle t = new Triangle(poly.getVertex(a),
                                          poly.getVertex(b),
                                          poly.getVertex(c));

                if (!triangleIntersects() &&
                    t.isCCWWinding() &&
                    angleIsInside()) {
                    // tests passed: add triangle and eliminate vertex b
                    tris.add(t);
                    used[b] = true;
                    numRemaining--;
                    countSinceLast = 0;
                } else {
                    // failed: move start point to next index
                    a = b;
                }

                System.out.format("a=%s, b=%s, c=%s --- %s triangles --- remaining: %s\n",
                                  a, b, c, tris.size(), remainString());
            }

            System.out.println("made " + tris.size() + " triangles... "
                               + (failed ? "FAILED!" : "SUCCEEDED"));
        }

        public Triangle[] getTriangles() {
            return tris.toArray(new Triangle[tris.size()]);
        }

        private int nextIndex(int i) {
            i++;
            countSinceLast++;
            if (i >= used.length) {
                i = 0;
            }
            while (used[i]) {
                i++;
                if (i >= used.length) {
                    i = 0;
                }
            }
            // prevent infinite loop on failure
            if (countSinceLast > used.length * 5)
                failed = true;

            return i;
        }

        private boolean triangleIntersects() {
            return edgeIntersects(a, b) ||
                   edgeIntersects(b, c) ||
                   edgeIntersects(c, a);
        }

        private boolean edgeIntersects(int i1, int i2) {
            if (contingentIndices(i1, i2))
                return false;
            if (poly.intersectsIgnoreSharedVerticesIncludeParallel(new Line(poly.getVertex(i1),
                                                                            poly.getVertex(i2))))
                return true;
            return false;
        }

        private boolean contingentIndices(int i1, int i2) {
            if (i1 < i2 && i1 == i2 - 1)
                return true;
           if (i1 == poly.getNumVertices() - 1 &&
                i2 == 0)
                return true;
            return false;
        }

        /**
         * <p>Checks that the third edge of the current triangle is inside the
         * outline of the polygon by checking that the angle-turned is greater
         * than the angle-turned of the outline at that point.</p>
         */
        private boolean angleIsInside() {
            if (numRemaining < 4)
                return true;
            int d = nextIndex(c);
            return Geom2D.angleTurned(poly.getVertex(b),
                                      poly.getVertex(c),
                                      poly.getVertex(a))
                 > Geom2D.angleTurned(poly.getVertex(b),
                                      poly.getVertex(c),
                                      poly.getVertex(d));
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

    }

}
