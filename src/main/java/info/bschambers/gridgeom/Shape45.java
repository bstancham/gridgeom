package info.bschambers.gridgeom;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

/**
 * <p>NOTE: A shape with non-45 angles may be constructed! Use {@link isValid}
 * to test shape before using it. If angles are not ALL divisible by 45 degrees,
 * then many of the other methods will be unreliable..</p>
 *
 * <p>Rules:</p>
 * <ul>
 * <li>all angles divisible by 45 degrees</li>
 * <li>counter-clockwise winding for outline</li>
 * <li>no intersecting edges</li>
 * <li>no shared edges or edge sections</li>
 * <li>holes may have shared vertices with outline, but not edges</li>
 * <li>holes have clockwise winding</li>
 * </ul>
 */
public class Shape45 extends AbstractShape {

    private Triangle[] triangles = null;
    private Shape45[] subShapes = null;
    private Integer totalNumVertices = null;

    public Shape45(Pt2D ... vertices) {
        this(new Shape45[0], vertices);
    }

    public Shape45(Shape45 sub, Pt2D ... vertices) {
        this(new Shape45[] { sub }, vertices);
    }
    
    public Shape45(Shape45[] subShapes, Pt2D ... vertices) {
        super(vertices);
        this.subShapes = subShapes;
    }

    public int getNumSubShapes() {
        return subShapes.length;
    }

    public Shape45 getSubShape(int index) {
        return subShapes[index];
    }

    /**
     * @return Total combined number of vertices of outline and all sub-shapes.
     */
    public int getTotalNumVertices() {
        if (totalNumVertices == null) {
            totalNumVertices = getNumVertices();
            for (Shape45 sub : subShapes)
                totalNumVertices += sub.getTotalNumVertices();
        }
        return totalNumVertices;
    }

    /**
     * <p>Get vertex in shape-outline or sub-shape.</p>
     *
     * <p>Vertices are given in order of depth-first search, i.e:</p>
     *
     * <ul>
     * <li>outline</li>
     * <li>subshape 1</li>
     * <li>subshape 1 subshape 1 ... etc</li>
     * <li>... etc</li>
     * </ul>
     *
     * @return vertex at index, or {@code null} if index is out of range.
     */
    public Pt2D getVertex(int index) {
        
        if (index < getNumVertices())
            return super.getVertex(index);
        
        int count = getNumVertices();
        
        for (int i = 0; i < subShapes.length; i++) {
            if (index - count < subShapes[i].getTotalNumVertices())
                return subShapes[i].getVertex(index - count);
            count += subShapes[i].getTotalNumVertices();
        }

        // index is out of range
        return null;
    }
    
    /**
     * <p>Returns true if shape is valid. False otherwise.</p>
     *
     * <p>Note that if the shape is not valid, many other methods cannot be
     * relied on to work properly.</p>
     *
     * @return True, if shape is valid. False otherwise.
     */
    public boolean isValid() {
        // OUTLINE: AT LEAST THREE VERTICES
        if (getNumVertices() < 3) return false;
        // OUTLINE: ALL ANGLES MUST BE DIVISIBLE BY 45 DEGREES
        if (!is45Compliant()) return false;
        // OUTLINE: WINDING DIRECTION MUST BE CCW
        if (!isCCWWinding()) return false;
        // OUTLINE: NO DUPLICATE VERTICES
        if (getNumDuplicateVertices() != 0) return false;
        // OUTLINE: NO INTERSECTING EDGES
        if (getNumEdgeIntersections() !=0) return false;

        // SUB-SHAPE: ALL EDGES MUST BE INSIDE OUTLINE

        // SUB-SHAPE MAY NOT INTERSECT
        
        return true;
    }


    
    /*--------------------------- DIAGNOSTIC ---------------------------*/

    /**
     * <p>WARNING: Not guaranteed to work unless shape is 45-compliant.</p>
     */
    public int getNumEdgeIntersections() {
        int num = 0;
        Line[] edges = getEdges();
        for (Line edge1 : edges) {
            for (Line edge2 : edges) {
                if (edge1 != edge2) {
                    if (Line.linesIntersect45(edge1, edge2))
                        num++;
                }
            }
        }
        return num;
    }
    
    /**
     * @return True, if shape complies with the 45 degree rule, i.e. all angles
     * are divisible by 45 degrees, and no angles are zero degrees.
     */
    public boolean is45Compliant() {
        for (int i = 0; i < getNumVertices(); i++) {
            double angle = Math.toDegrees(getAngleAtVertex(i));
            if (angle == 0)
                return false;
            if (angle % 45 != 0)
                return false;
        }
        return true;
    }



    /*------------------------- TRIANGULATION --------------------------*/

    public int getNumTriangles() {
        if (triangles == null)
            triangulate();
        return triangles.length;
    }

    public Triangle getTriangle(int index) {
        if (triangles == null)
            triangulate();
        return triangles[index];
    }
    
    private void triangulate() {
        triangulateConvexSimple();
    }
    
    private void triangulateConvexSimple() {
        triangles = new Triangle[getNumVertices() - 2];
        for (int i = 0; i < triangles.length; i++) {
            triangles[i] = new Triangle(getVertex(0),
                                        getVertex(i + 1),
                                        getVertex(i + 2));
        }
    }
    


    /*------------- TRANSFORMATIONS (return a new Shape45) -------------*/

    /**
     * @return A new {@code Shape45} which is an identical copy of this one but
     * shifted by {@code x/y} units.
     */
    public Shape45 transpose(int x, int y) {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].transpose(x, y);
        Pt2D[] newVerts = transposeVertices(x, y);
        return new Shape45(newSubs, newVerts);
    }

    /**
     * @return A new {@code Shape45} which is an identical copy of this one,
     * except with the vertex at index {@code i} set to the given {@code x/y}
     * co-ordinates.
     */
    public Shape45 setVertex(int index, int x, int y) {
        
        // OUTLINE
        Pt2D[] newVertices = new Pt2D[getNumVertices()];
        for (int i = 0; i < getNumVertices(); i++) {
            if (i == index)
                newVertices[i] = new Pt2D(x, y);
            else
                newVertices[i] = super.getVertex(i);
        }

        // SUB-SHAPES
        int count = getNumVertices();
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (index - count < subShapes[i].getTotalNumVertices()) {
                newSubs[i] = subShapes[i].setVertex(index - count, x, y);
            } else {
                newSubs[i] = subShapes[i];
            }                
            count += subShapes[i].getTotalNumVertices();
        }

        return new Shape45(newSubs, newVertices);
    }

    public List<Shape45> subtract(Shape45 s) {

        // get edges
        List<Line> homeLines = new ArrayList<>(); //getEdges());
        for (Line edge : getEdges())
            homeLines.add(edge);
        List<Line> awayLines = new ArrayList<>(); //s.getEdges());
        for (Line edge : s.getEdges())
            awayLines.add(edge);
        
        // get intersection points
        Set<IPt> iPoints = new HashSet<>();
        for (int hi = 0; hi < homeLines.size(); hi++) {
            Line l1 = homeLines.get(hi);
            // collect all intersection for current line, so we can sort them
            // before adding to the main set
            List<IPt> linePoints = new ArrayList<>();
            for (int ai = 0; ai < awayLines.size(); ai++) {
                Line l2 = awayLines.get(ai);
                Pt2Df p = l1.getIntersectionPoint(l2);
                if (p != null) {
                    if (l1.boundingBoxContains(p) &&
                        l2.boundingBoxContains(p))
                        iPoints.add(new IPt(p, hi, ai));
                } else {
                    // line has failed to make an intersection point...
                    // ... probably means that it's paralell...
                    // ... so, check that neither end is situated on home-line
                    if (l1.contains(l2.start())) {
                        iPoints.add(new IPt(l2.start().toFloat(), hi, ai));
                    }
                }
            }
            // sort intersections closest to start point of home-line first
            Collections.sort(linePoints, new PtDistComparator(l1.start()));
            iPoints.addAll(linePoints);
        }
        
        // if no intersection points? return early

        // split all lines

        // if no splits made? return early



        // traverse outline...

        // at junction?


        
        List<Shape45> output = new ArrayList<>();
        return output;
        
    }

    /**
     * <p>Stores information about an intersection point.</p>
     */
    private class IPt {

        Pt2Df p;
        int homeIndex;
        int awayIndex;

        public IPt(Pt2Df p, int homeIndex, int awayIndex) {
            this.p = p;
            this.homeIndex = homeIndex;
            this.awayIndex = awayIndex;
        }

        public Pt2Df getPoint() { return p; }

        @Override
        public boolean equals(Object obj) {
        
            // some safe optimizations
            if (obj == this) return true;
            if (obj == null) return false;
            if (obj.getClass() != this.getClass()) return false;

            // cast guaranteed to succeed here
            IPt that = (IPt) obj;

            // test for equality in all significant fields
            if (!that.p.equals(p)) return false;
            if (that.homeIndex != homeIndex) return false;
            if (that.awayIndex != awayIndex) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return (int) p.hashCode() + homeIndex + awayIndex;
        }
        
    }

    public class PtDistComparator implements Comparator<IPt> {

        private Pt2D homePt;

        public PtDistComparator(Pt2D homePt) {
            this.homePt = homePt;
        }
        
        /**
         * @return negative num = less than, zero = equal, positive num =
         * greater than.  Returns a negative integer, zero, or a positive
         * integer as the first argument is less than, equal to, or greater than
         * the second.
         */
        @Override
        public int compare(IPt p1, IPt p2) {
            double dist1 = Geom2D.distSquared(p1.getPoint(), homePt.toFloat());
            double dist2 = Geom2D.distSquared(p2.getPoint(), homePt.toFloat());
            if (dist1 < dist2) return -1;
            if (dist1 > dist2) return 1;
            else return 0;
        }
    }

}
