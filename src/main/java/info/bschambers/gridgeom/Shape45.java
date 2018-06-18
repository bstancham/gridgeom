package info.bschambers.gridgeom;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import static info.bschambers.gridgeom.Geom2D.WindingDir;

/**
 * <p>Immutable data type representing a two-dimensional shape made up of an ordered
 * collection of integer co-ordinate vertices.</p>
 *
 * <p>Shape45 assumes that all corners have angles divisible by 45 degrees and
 * relies on this for some methods to work properly.</p>
 *
 * <p>NOTE: A shape with non-45 angles may be constructed! Use {@link isValid}
 * to test shape before using it. If angles are not ALL divisible by 45 degrees,
 * then many of the other methods will be unreliable..</p>
 *
 * <p>Shape45 may have nested sub-shapes.</p>
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

    private WindingDir expectedWinding = WindingDir.CCW;
    private Shape45[] subShapes;
    private Triangle[] triangles = null;
    private Box2D boundingBox = null;
    private Integer totalNumVertices = null;
    private int nestedDepth;
    private Boolean valid = null;

    public Shape45(Pt2D ... vertices) {
        this(new Shape45[0], vertices);
    }

    public Shape45(Shape45 sub, Pt2D ... vertices) {
        this(new Shape45[] { sub }, vertices);
    }
    
    public Shape45(Shape45[] subShapes, Pt2D ... vertices) {
        // this(subShapes, vertices, WindingDir.CCW);
        super(vertices);
        this.subShapes = subShapes;

        for (Shape45 sub : subShapes)
            sub.setExpectedWinding(expectedWinding);

        // get depth of deepest nested sub-shape and add 1
        int n = 0;
        for (Shape45 sub : subShapes)
            if (sub.getNestedDepth() > n)
                n = sub.getNestedDepth();
        nestedDepth = n + 1;
    }

    private void setExpectedWinding(WindingDir parentWinding) {

        if (parentWinding == WindingDir.CCW)
            expectedWinding = WindingDir.CW;
        
        if (parentWinding == WindingDir.CW)
            expectedWinding = WindingDir.CCW;
        
        for (Shape45 sub : subShapes)
            sub.setExpectedWinding(expectedWinding);
    }

    public int getNumSubShapes() {
        return subShapes.length;
    }

    public Shape45 getSubShape(int index) {
        return subShapes[index];
    }

    /**
     * <p>TODO: unit test</p>
     */
    public Shape45[] getSubShapes() {
        // make protective copy of sub-shapes before returning it
        Shape45[] subs = new Shape45[subShapes.length];
        System.arraycopy(subShapes, 0, subs, 0, subShapes.length);
        return subs;
    }

    public int getNumShapesRecursive() {
        int count = 1;
        for (Shape45 s : subShapes)
            count += s.getNumShapesRecursive();
        return count;
    }

    public Shape45 getSubShapeRecursive(int index) {
        // index 0 refers to the whole shape
        if (index == 0)
            return this;
        // sub-shapes
        index--;
        for (Shape45 s : subShapes) {
            if (index < s.getNumShapesRecursive()) {
                return s.getSubShapeRecursive(index);
            } else {
                index -= s.getNumShapesRecursive();
            }
        }
        // index out of range
        return null;
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
        // index in outline
        if (index < getNumVertices())
            return super.getVertex(index);
        // index in sub-shape
        int count = getNumVertices();
        for (int i = 0; i < subShapes.length; i++) {
            if (index - count < subShapes[i].getTotalNumVertices())
                return subShapes[i].getVertex(index - count);
            count += subShapes[i].getTotalNumVertices();
        }
        // index is out of range
        return null;
    }

    public Shape45 getSubShapeForVertexIndex(int index) {

        // is vertex index in outline?
        if (index < getNumVertices()) {
            return this;
        }

        // 
        index -= getNumVertices();
        for (int i = 0; i < subShapes.length; i++) {
            if (index < subShapes[i].getTotalNumVertices())
                return subShapes[i].getSubShapeForVertexIndex(index);
            index -= subShapes[i].getTotalNumVertices();
        }
        
        // index is out of range
        return null;
    }    

    /**
     * @return The index of sub-shape which contains the vertex at {@code
     * index}, or {@code -1} if {@code index} is out of range.
     */
    public int getSubShapeIndexForVertexIndex(int index) {

        // is vertex index in outline?
        if (index < getNumVertices()) {
            return 0;
        }

        index -= getNumVertices();
        int count = 1;
        for (int i = 0; i < subShapes.length; i++) {
            if (index < subShapes[i].getTotalNumVertices())
                return count + subShapes[i].getSubShapeIndexForVertexIndex(index);
            index -= subShapes[i].getTotalNumVertices();
            count += subShapes[i].getNumShapesRecursive();
        }
        
        // index is out of range
        return -1;
    }    
    
    public Box2D getBoundingBox() {
        if (boundingBox == null)
            makeBoundingBox();
        return boundingBox;
    }

    public int getCenterX() {
        if (boundingBox == null)
            makeBoundingBox();
        return boundingBox.centerX;
    }

    public int getCenterY() {
        if (boundingBox == null)
            makeBoundingBox();
        return boundingBox.centerY;
    }

    public void makeBoundingBox() {
        Pt2D v = getVertex(0);
        int lowX  = v.x();
        int highX = v.x();
        int lowY  = v.y();
        int highY = v.y();
        for (int i = 1; i < getNumVertices(); i++) {
            v = getVertex(i);
            if (v.x() < lowX)  lowX  = v.x();
            if (v.x() > highX) highX = v.x();
            if (v.y() < lowY)  lowY  = v.y();
            if (v.y() > highY) highY = v.y();
        }
        boundingBox = new Box2D(lowX, lowY, highX, highY);
    }

    
    
    /*--------------------------- DIAGNOSTIC ---------------------------*/

    /**
     * <p>Returns true if shape is valid. False otherwise.</p>
     *
     * <p>Note that if the shape is not valid, many other methods cannot be
     * relied on to work properly.</p>
     *
     * <p>WARNING: currently incomplete.</p>
     *
     * <p>TODO:</p>
     * <ul>
     * <li>sub-shapes properly nested</li>
     * <li>intersection without edge-intersection</li>
     * </ul>
     *
     * @return True, if shape is valid. False otherwise.
     */
    public boolean isValid() {
        if (valid == null)
            valid = testIsValid();
        return valid;
    }

    /**
     * <p>Called only once - the first time isValid is called.</p>
     */
    private boolean testIsValid() {
            
        // OUTLINE
        
        // OUTLINE: AT LEAST THREE VERTICES
        if (getNumVertices() < 3) return false;

        // OUTLINE: ALL ANGLES MUST BE DIVISIBLE BY 45 DEGREES
        if (!is45Compliant()) return false;
        
        // OUTLINE: WINDING DIRECTION MUST BE CCW
        if (getWindingDir() != expectedWinding) return false;
        
        // OUTLINE: NO DUPLICATE VERTICES
        if (getNumDuplicateVertices() != 0) return false;
        
        // OUTLINE: NO INTERSECTING EDGES
        if (getNumEdgeIntersections() !=0) return false;

        // SUB-SHAPES

        for (Shape45 sub : subShapes) {
            
            // SUB-SHAPE: ALL EDGES MUST BE INSIDE OUTLINE
            
            // SUB-SHAPES MAY NOT INTERSECT ONEANOTHER
            for (Shape45 sub1 : subShapes)
                for (Shape45 sub2 : subShapes)
                    if (sub1 != sub2)
                        if (sub1.intersects45IgnoreSharedVertices(sub2))
                            return false;

            if (!sub.isValid()) return false;

        }

        return true;
    }

    public WindingDir getWindingDir() {
        if (isCWWinding())
            return WindingDir.CW;
        if (isCCWWinding())
            return WindingDir.CCW;
        return WindingDir.INDETERMINATE;
    }

    /**
     * <p>WARNING: Not guaranteed to work unless shape is 45-compliant.</p>
     */
    public int getNumEdgeIntersections() {
        int num = 0;
        Line[] edges = getEdges();
        for (Line edge1 : edges) {
            for (Line edge2 : edges) {
                if (edge1 != edge2) {
                    if (Line.linesIntersect45IgnoreSharedEnds(edge1, edge2))
                        num++;
                }
            }
        }
        return num;
    }

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

    public int getNestedDepth() {
        return nestedDepth;
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
        // triangulateEarClipping();
    }

    /**
     * <p>Works for convex shapes with no sub-shapes and no zero-degree
     * angles.</p>
     */
    private void triangulateConvexSimple() {
        triangles = new Triangle[getNumVertices() - 2];
        for (int i = 0; i < triangles.length; i++) {
            triangles[i] = new Triangle(getVertex(0),
                                        getVertex(i + 1),
                                        getVertex(i + 2));
        }
    }

    /**
     * <p>Works for any shape which has no sub-shapes.</p>
     */
    private void triangulateEarClipping() {
        new EarClippingTriangulator();
    }

    private class EarClippingTriangulator {
        
        private boolean[] used = new boolean[getNumVertices()];
        private int a = 0;
        private int b = 0;
        private int c = 0;
        private boolean kill = false;

        public EarClippingTriangulator() {
            List<Triangle> tris = new ArrayList<>();

            while (numRemaining() >= 3
                   && !kill) {

                System.out.format("a=%s, b=%s, c=%s --- %s triangles --- remaining: %s\n",
                                  a, b, c, tris.size(), remainString());

                // get next triangle
                b = nextIndex(a);
                c = nextIndex(b);
                Triangle t = new Triangle(getVertex(a),
                                          getVertex(b),
                                          getVertex(c));
                
                if (!triangleIntersectsShape() &&
                    t.isCCWWinding()) {
                    tris.add(t);
                    used[b] = true;
                } else {
                    // move start point to next index
                    a = b;
                }

            }

            System.out.println("made " + tris.size() + " triangles");
            triangles = tris.toArray(new Triangle[tris.size()]);
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
            if (i >= used.length) {
                i = 0;
                kill = true;
            }
            while (used[i]) {
                i++;
                if (i >= used.length) {
                    i = 0;
                    kill = true;
                }
            }
            return i;
        }

        private boolean triangleIntersectsShape() {
            return edgeIntersectsShape(a, b) ||
                   edgeIntersectsShape(b, c) ||
                   edgeIntersectsShape(c, a);
        }

        private boolean edgeIntersectsShape(int i1, int i2) {
            if (contingentIndices(i1, i2))
                return false;

            Line ln = new Line(getVertex(i1), getVertex(i2));
            if (intersects45IgnoreSharedVertices(ln))
                return true;
                
            return false;
        }

        private boolean contingentIndices(int i1, int i2) {
            if (i1 < i2 && i1 == i2 - 1)
                return true;
            if (i1 == getNumVertices() - 1 &&
                i2 == 0)
                return true;
            return false;
        }
    }
    


    /*------------- TRANSFORMATIONS (return a new Shape45) -------------*/

    /**
     * @return A new {@code Shape45} which is an identical copy of this one but
     * shifted by {@code x/y} units.
     */
    public Shape45 shift(int x, int y) {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].shift(x, y);
        Pt2D[] newVerts = transposeVertices(x, y);
        return new Shape45(newSubs, newVerts);
    }

    /**
     * <p>Shift sub-shape at index by x/y units.</p>
     */
    public Shape45 shiftSubShape(int index, int x, int y) {
        // index 0 refers to the whole shape
        if (index == 0)
            return shift(x, y);
        // index in sub-shape
        index--;
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (index >= 0 &&
                index < subShapes[i].getNumShapesRecursive()) {
                newSubs[i] = subShapes[i].shiftSubShape(index, x, y);
            } else {
                newSubs[i] = subShapes[i];
            }
            index -= subShapes[i].getNumShapesRecursive();
        }
        return new Shape45(newSubs, copyVertices());
    }

    /**
     * <p>WARNING: Returns {@code null} if index is zero. To be consistent with
     * sub-shape indexing, zero refers to the whole shape, therefore deleting
     * the sub-shape at index zero deletes the whole shape.</p>
     */
    public Shape45 deleteSubShapeRecursive(int index) {
        // index 0 - delete whole shape!
        if (index == 0)
            return null;
        // index out of bounds - delete nothing
        if (index >= getNumShapesRecursive())
            return this;
        // subtract one - first sub-shape was the whole shape itself
        index--;
        List<Shape45> newSubs = new ArrayList<>();
        for (int i = 0; i < subShapes.length; i++) {
            // if index is zero, don't add this shape
            if (index != 0) {
                if (index < subShapes[i].getNumShapesRecursive()) {
                    newSubs.add(subShapes[i].deleteSubShapeRecursive(index));
                } else {
                    newSubs.add(subShapes[i]);
                }
            }
            index -= subShapes[i].getNumShapesRecursive();
        }
        return new Shape45(newSubs.toArray(new Shape45[newSubs.size()]),
                           copyVertices());
    }
    
    public Shape45 addSubShapeRecursive(int index, Shape45 newSubShape) {
        // if index out of range, return without modifying
        if (index < 0 ||
            index >= getNumShapesRecursive())
            return this;

        // index 0 - add subshape to this and return
        if (index == 0) {
            Shape45[] newSubs = new Shape45[subShapes.length + 1];
            // System.arraycopy(subShapes, 0, newSubs, 0, subShapes.length);
            for (int i = 0; i < subShapes.length; i++) {
                newSubs[i] = subShapes[i];
            }
            newSubs[subShapes.length] = newSubShape;
            return new Shape45(newSubs, copyVertices());
        }

        // find index in sub-shapes
        index--;
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (index >= 0 &&
                index < subShapes[i].getNumShapesRecursive()) {
                newSubs[i] = subShapes[i].addSubShapeRecursive(index, newSubShape);
            } else {
                newSubs[i] = subShapes[i];
            }
            index -= subShapes[i].getNumShapesRecursive();
        }

        return new Shape45(newSubs, copyVertices());
    }

    public Shape45 reverseWinding() {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].reverseWinding();
        return new Shape45(newSubs, reverseVertices());
    }

    public Shape45 reverseSubShapeWinding(int index) {
        
        if (index == 0)
            return reverseWinding();

        index--;
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (index >= 0 &&
                index < subShapes[i].getNumShapesRecursive()) {
                newSubs[i] = subShapes[i].reverseSubShapeWinding(index);
            } else {
                newSubs[i] = subShapes[i];
            }
            index -= subShapes[i].getNumShapesRecursive();
        }
        return new Shape45(newSubs, copyVertices());
    }

    public Shape45 rotateOutlineVertices(int amt) {
        return new Shape45(getSubShapes(), rotateVertexOrder(amt));
    }

    public Shape45 rotateSubShapeOutlineVertices(int index, int amt) {
        
        if (index == 0)
            return rotateOutlineVertices(amt);

        index--;
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (index >= 0 &&
                index < subShapes[i].getNumShapesRecursive()) {
                newSubs[i] = subShapes[i].rotateSubShapeOutlineVertices(index, amt);
            } else {
                newSubs[i] = subShapes[i];
            }
            index -= subShapes[i].getNumShapesRecursive();
        }
        return new Shape45(newSubs, copyVertices());
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

    public Shape45 deleteVertex(int index) {
        
        // OUTLINE
        int newLen = getNumVertices();
        if (index < getNumVertices())
            newLen--;
        Pt2D[] newVertices = new Pt2D[newLen];
        for (int i = 0; i < getNumVertices(); i++) {
            if (i < index)
                newVertices[i] = super.getVertex(i);
            else if (i > index)
                newVertices[i - 1] = super.getVertex(i);
        }
        
        // SUB-SHAPES
        int count = getNumVertices();
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (count < index &&
                index - count < subShapes[i].getTotalNumVertices()) {
                newSubs[i] = subShapes[i].deleteVertex(index - count);
                count--;
            } else {
                newSubs[i] = subShapes[i];
            }                
            count += subShapes[i].getTotalNumVertices();
        }

        return new Shape45(newSubs, newVertices);
    }

    public Shape45 addVertexAfter(int index) {
        
        // OUTLINE
        int len = (index < getNumVertices() ? getNumVertices() + 1 : getNumVertices());
        Pt2D[] newVertices = new Pt2D[len];
        for (int i = 0; i < getNumVertices(); i++) {
            if (i <= index) {
                newVertices[i] = super.getVertex(i);
                if (i == index) {
                    // new point equidistant between
                    Pt2D v = Geom2D.midPointInt(super.getVertex(i),
                                                super.getVertex(wrapIndex(i + 1)));
                    // make sure not duplicate vertex before adding it
                    while (containsVertex(v))
                        v = v.transpose(0, 1);
                    newVertices[i + 1] = v;
                }
            } else {
                newVertices[i + 1] = super.getVertex(i);
            }
        }

        // SUB-SHAPES
        int count = getNumVertices();
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (count < index &&
                index - count < subShapes[i].getTotalNumVertices()) {
                newSubs[i] = subShapes[i].addVertexAfter(index - count);
                count++;
            } else {
                newSubs[i] = subShapes[i];
            }                
            count += subShapes[i].getTotalNumVertices();
        }

        return new Shape45(newSubs, newVertices);
    }

    public Shape45 reflectX(int center) {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].reflectX(center);
        Pt2D[] newVerts = reflectVerticesX(center);
        return new Shape45(newSubs, newVerts);
    }

    public Shape45 reflectY(int center) {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].reflectY(center);
        Pt2D[] newVerts = reflectVerticesY(center);
        return new Shape45(newSubs, newVerts);
    }

    public Shape45 rotate90(int centerX, int centerY) {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].rotate90(centerX, centerY);
        Pt2D[] newVerts = rotateVertices90(centerX, centerY);
        return new Shape45(newSubs, newVerts);
    }

    public List<Shape45> subtract45(Shape45 s) {

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
                Pt2Df p = l1.getIntersectionPoint45(l2);
                if (p != null) {
                    if (l1.boundingBoxContains(p) &&
                        l2.boundingBoxContains(p))
                        iPoints.add(new IPt(p, hi, ai));
                } else {
                    // line has failed to make an intersection point...
                    // ... probably means that it's paralell...
                    // ... so, check that neither end is situated on home-line
                    if (l1.contains45(l2.start())) {
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



    /*------------------------------ MISC ------------------------------*/

    /**
     * <p>Makes souce code for shape which can be pasted directly into program
     * code.</p>
     */
    public String getSourceCode() {
        StringBuffer sb = new StringBuffer();
        sb.append("new Shape45(");

        // SUB-SHAPES
        if (subShapes.length == 1) {
            sb.append(subShapes[0].getSourceCode() + ",\n");
        } else if (subShapes.length > 1) {
            sb.append("new Shape45[] {\n");
            for (Shape45 s : subShapes) {
                sb.append(s.getSourceCode() + ",\n");
            }
            sb.append("},\n");
        }

        // OUTLINE
        for (int i = 0; i < getNumVertices(); i++) {
            Pt2D v = getVertex(i);
            sb.append("new Pt2D(" + v.x() + ", " + v.y() + ")");
            if (i < getNumVertices() - 1)
                sb.append(",\n");
        }
        
        sb.append(")");
        return sb.toString();
    }

}
