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
    private Double sumAnglesDegrees = null;

    public Polygon(Pt2D ... vertices) {
        this.vertices = vertices;
    }

    public int getNumVertices() { return vertices.length; }

    public Pt2D getVertex(int index) {
        return vertices[index];
    }

    public Pt2D getVertexWrapped(int index) {
        return vertices[wrapIndex(index)];
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
            edges[i] = new Line(vertices[i], vertices[wrapIndex(i + 1)]);
        }
        return edges;
    }

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
        for (int i = 0; i < vertices.length; i++) {
            newVertices[i] = vertices[wrapIndex(i + amt)];
        }
        return new Polygon(newVertices);
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
    
    // public boolean intersects45IgnoreSharedVertices(Shape45 s) {
    public boolean intersects45IgnoreSharedVertices(Polygon poly) {
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
    
    public boolean intersects45IgnoreSharedVertices(Line ln) {
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
     * @throws IndexOutOfBoundsException if number of vertices in shape is less
     * than three.
     */
    public double getAngleAtVertex(int index) {
        return Geom2D.angleTurned(vertices[wrapIndex(index - 1)].toFloat(),
                                  vertices[wrapIndex(index)].toFloat(),
                                  vertices[wrapIndex(index + 1)].toFloat());
    }
    
    /**
     * @return The sum of the angles of the outline, in radians.
     */
    public double getSumAngles() {
        if (sumAnglesDegrees == null) {
            sumAnglesDegrees = 0.0;
            for (int i = 0; i < getNumVertices(); i++)
                sumAnglesDegrees += getAngleAtVertex(i);
        }
        return sumAnglesDegrees;
    }

    public WindingDir getWindingDir() {
        if (isCWWinding())
            return WindingDir.CW;
        if (isCCWWinding())
            return WindingDir.CCW;
        return WindingDir.INDETERMINATE;
    }
    
    public boolean isCWWinding() {
        return getSumAngles() == -(Math.PI * 2);
    }
    
    public boolean isCCWWinding() {
        return getSumAngles() == Math.PI * 2;
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

    

    /*-------------------- PRIVATE UTILITY METHODS ---------------------*/

    private int wrapIndex(int i) {
        if (i < 0)
            return vertices.length + i % vertices.length;
        if (i >= vertices.length)
            return i % vertices.length;
        return i;
    }

}
