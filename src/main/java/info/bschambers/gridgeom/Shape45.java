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
public class Shape45 {

    private WindingDir expectedWinding = WindingDir.CCW;
    private Polygon outline;
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
        this(subShapes, new Polygon(vertices));
    }
    
    public Shape45(Shape45[] subShapes, Polygon outline) {
        this.subShapes = subShapes;
        this.outline = outline;

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

    /**
     * <p>The expected winding direction of this shape's outline.</p>
     *
     * <p>To find the actual winding of the outline you can use:
     * {@code getOutline().getWindingDir()}.</p>
     */
    public WindingDir getExpectedWinding() {
        return expectedWinding;
    }

    public Polygon getOutline() {
        return outline;
    }

    /**
     * @return The number of shapes in the local sub-shape array. To get the
     * total number of nested shapes use {@link Shape45#getNumShapesRecursive
     * getNumShapesRecursive}.
     */
    public int getNumSubShapes() {
        return subShapes.length;
    }

    /**
     * @return The shape at {@code index} in the local sub-shapes array. To get
     * a deeper-nested shape use {@link Shape45#getSubShapeRecursive
     * getSubShapeRecursive}.
     * 
     * @throws ArrayIndexOutOfBoundsException If {@code index} is out of bounds.
     */
    public Shape45 getSubShape(int index) {
        return subShapes[index];
    }

    /**
     * <p>TODO: unit test</p>
     * @return A copy of the sub-shapes array.
     */
    public Shape45[] getSubShapes() {
        // make protective copy of sub-shapes before returning it
        Shape45[] subs = new Shape45[subShapes.length];
        System.arraycopy(subShapes, 0, subs, 0, subShapes.length);
        return subs;
    }

    /**
     * @return The total number of nested shapes, including {@code this} (i.e
     * the outline) and all nested shapes to any depth.
     */
    public int getNumShapesRecursive() {
        int count = 1;
        for (Shape45 s : subShapes)
            count += s.getNumShapesRecursive();
        return count;
    }

    /**
     * @return The shape at {@code index}. Index {@code 0} refers to the top
     * level i.e. the whole shape.
     */
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

    public int getNumOutlineVertices() {
        return outline.getNumVertices();
    }
    
    /**
     * @return Total combined number of vertices of outline and all sub-shapes.
     */
    public int getTotalNumVertices() {
        if (totalNumVertices == null) {
            totalNumVertices = getNumOutlineVertices();
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
    public Pt2D getVertexRecursive(int index) {
        // index in outline
        if (index < getNumOutlineVertices())
            return outline.getVertex(index);
        // index in sub-shape
        int count = getNumOutlineVertices();
        for (int i = 0; i < subShapes.length; i++) {
            if (index - count < subShapes[i].getTotalNumVertices())
                return subShapes[i].getVertexRecursive(index - count);
            count += subShapes[i].getTotalNumVertices();
        }
        // index is out of range
        return null;
    }

    /**
     * @return The shape whose outline contains vertex-index {@code index}, or
     * {@code null} if {@code index} is out of range.
     */
    public Shape45 getSubShapeForVertexIndex(int index) {
        // index in outline?
        if (index < getNumOutlineVertices()) {
            return this;
        }
        // index in sub-shape?
        index -= getNumOutlineVertices();
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
        // vertex index in outline?
        if (index < getNumOutlineVertices()) {
            return 0;
        }
        // vertex index in sub-shape?
        index -= getNumOutlineVertices();
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
    
    /**
     * @return The index of the first vertex of the sub-shape at {@code index},
     * or {@code -1} if {@code index} is out of range.
     */
    public int getVertexIndexForSubShapeIndex(int index) {
        if (index == 0)
            // return first vertex of outline
            return 0;
        // vertex index in sub-shape?
        index--;
        int count = getNumOutlineVertices();
        for (int i = 0; i < subShapes.length; i++) {
            if (index < subShapes[i].getNumShapesRecursive())
                return count + subShapes[i].getVertexIndexForSubShapeIndex(index);
            index -= subShapes[i].getNumShapesRecursive();
            count += subShapes[i].getTotalNumVertices();
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

    private void makeBoundingBox() {
        Pt2D v = getOutline().getVertex(0);
        int lowX  = v.x();
        int highX = v.x();
        int lowY  = v.y();
        int highY = v.y();
        for (int i = 1; i < getNumOutlineVertices(); i++) {
            v = getOutline().getVertex(i);
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
        if (getNumOutlineVertices() < 3) return false;

        // OUTLINE: ALL ANGLES MUST BE DIVISIBLE BY 45 DEGREES
        if (!outline.is45Compliant()) return false;
        
        // OUTLINE: WINDING DIRECTION MUST BE CCW
        if (outline.getWindingDir() != expectedWinding) return false;
        
        // OUTLINE: NO DUPLICATE VERTICES
        if (outline.getNumDuplicateVertices() != 0) return false;
        
        // OUTLINE: NO INTERSECTING EDGES
        if (getNumOutlineSelfIntersections45() !=0) return false;

        // SUB-SHAPES

        for (Shape45 sub : subShapes) {
            
            // SUB-SHAPE: ALL EDGES MUST BE INSIDE OUTLINE
            
            // SUB-SHAPES MAY NOT INTERSECT ONE-ANOTHER
            for (Shape45 sub2 : subShapes)
                if (sub != sub2)
                    if (sub.getOutline().intersectsIgnoreSharedVertices45(sub2.getOutline()))
                        return false;

            if (!sub.isValid()) return false;

        }

        return true;
    }

    /**
     * @return True, if shape and all sub-shapes comply with the 45 degree rule,
     * i.e. every angle is either divisible by 45 degrees or is zero.
     */
    public boolean is45Compliant() {
        if (!outline.is45Compliant())
            return false;
        for (Shape45 sub : subShapes)
            if (!sub.is45Compliant())
                return false;
        return true;
    }
    
    /**
     * <p>WARNING: Not guaranteed to work unless shape is 45-compliant.</p>
     */
    public int getNumOutlineSelfIntersections45() {
        int num = 0;
        Line[] edges = outline.getEdges();
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

    public int getNestedDepth() {
        return nestedDepth;
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
        return new Shape45(newSubs, getOutline().shift(x, y));
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
        return new Shape45(newSubs, getOutline());
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
                           getOutline());
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
            return new Shape45(newSubs, getOutline());
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

        return new Shape45(newSubs, getOutline());
    }

    public Shape45 reverseWinding() {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].reverseWinding();
        return new Shape45(newSubs, getOutline().reverseWinding());
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
        return new Shape45(newSubs, getOutline());
    }

    public Shape45 rotateOutlineVertexOrder(int amt) {
        return new Shape45(getSubShapes(),
                           getOutline().rotateVertexOrder(amt));
    }

    public Shape45 rotateSubShapeOutlineVertexOrder(int index, int amt) {
        
        if (index == 0)
            return rotateOutlineVertexOrder(amt);

        index--;
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++) {
            if (index >= 0 &&
                index < subShapes[i].getNumShapesRecursive()) {
                newSubs[i] = subShapes[i].rotateSubShapeOutlineVertexOrder(index, amt);
            } else {
                newSubs[i] = subShapes[i];
            }
            index -= subShapes[i].getNumShapesRecursive();
        }
        return new Shape45(newSubs, getOutline());
    }

    /**
     * @return A new {@code Shape45} which is an identical copy of this one,
     * except with the vertex at index {@code i} set to the given {@code x/y}
     * co-ordinates.
     */
    public Shape45 setVertex(int index, int x, int y) {
        
        // OUTLINE
        Pt2D[] newVertices = new Pt2D[getNumOutlineVertices()];
        for (int i = 0; i < getNumOutlineVertices(); i++) {
            if (i == index)
                newVertices[i] = new Pt2D(x, y);
            else
                newVertices[i] = getOutline().getVertex(i);
        }

        // SUB-SHAPES
        int count = getNumOutlineVertices();
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
        int newLen = getNumOutlineVertices();
        if (index < getNumOutlineVertices())
            newLen--;
        Pt2D[] newVertices = new Pt2D[newLen];
        for (int i = 0; i < getNumOutlineVertices(); i++) {
            if (i < index)
                newVertices[i] = getOutline().getVertex(i);
            else if (i > index)
                newVertices[i - 1] = getOutline().getVertex(i);
        }
        
        // SUB-SHAPES
        int count = getNumOutlineVertices();
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
        int len = (index < getNumOutlineVertices() ?
                   getNumOutlineVertices() + 1 :
                   getNumOutlineVertices());
        Pt2D[] newVertices = new Pt2D[len];
        for (int i = 0; i < getNumOutlineVertices(); i++) {
            if (i <= index) {
                newVertices[i] = getOutline().getVertex(i);
                if (i == index) {
                    // new point equidistant between
                    Pt2D v = Geom2D.midPointInt(getOutline().getVertex(i),
                                                getOutline().getVertexWrapped(i + 1));
                    // make sure not duplicate vertex before adding it
                    while (getOutline().containsVertex(v))
                        v = v.transpose(0, 1);
                    newVertices[i + 1] = v;
                }
            } else {
                newVertices[i + 1] = getOutline().getVertex(i);
            }
        }

        // SUB-SHAPES
        int count = getNumOutlineVertices();
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
        return new Shape45(newSubs, getOutline().reflectX(center));
    }

    public Shape45 reflectY(int center) {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].reflectY(center);
        return new Shape45(newSubs, getOutline().reflectY(center));
    }

    public Shape45 rotate90(int centerX, int centerY) {
        Shape45[] newSubs = new Shape45[subShapes.length];
        for (int i = 0; i < subShapes.length; i++)
            newSubs[i] = subShapes[i].rotate90(centerX, centerY);
        return new Shape45(newSubs, outline.rotate90(centerX, centerY));
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
        if (subShapes.length == 0)
            triangles = Polygon.triangulate(getOutline());
        else
            triangles = triangulateDivideAndConquer(this);
    }

    /**
     * <p>Works for any valid shape.</p>
     */
    private Triangle[] triangulateDivideAndConquer(Shape45 s) {
        return new DivideAndConquerTriangulator(s).getTriangles();
    }

    private class DivideAndConquerTriangulator {

        private List<Triangle> tris = new ArrayList<>();

        public DivideAndConquerTriangulator(Shape45 shape) {
            System.out.println("DivideAndConquerTriangulator");
            Shape45[] workingSubShapes = shape.subShapes;

            // TOP-LEVEL
            
            for (int i = 0; i < workingSubShapes.length; i++) {
                Shape45 sub = workingSubShapes[i];
                System.out.println("make bridge to subshape " + i);
                shape = makeBridge(shape, sub);
            }

            System.out.println("outline after division:");
            for (Pt2D p : shape.getOutline())
                System.out.println(p);
            
            for (Triangle t : Polygon.triangulateEarClipping(shape.getOutline()))
                tris.add(t);

            // NESTED SHAPES

            for (Shape45 hole : workingSubShapes)
                for (Shape45 nested : hole.subShapes)
                    for (Triangle t : triangulateDivideAndConquer(nested))
                        tris.add(t);
        }

        public Triangle[] getTriangles() {
            return tris.toArray(new Triangle[tris.size()]);
        }

        /**
         * <p>Incorporates sub-shape {@code sub} in to the outline of {@code
         * shape} by making a bridge between them.</p>
         *
         * <p>The bridge is made by finding a quadrilateral which incorporates
         * an edge from both shapes, without intersecting any other edges. The
         * quadrilateral is split into two triangles, which are added to the
         * triangles list, then {@code shape} and {@code sub} are stitched
         * together along the connecting edges into a single outline. Any
         * remaining sub-shapes are added to the new resulting shape.</p>
         *
         * @return A new version of {@code shape}, with sub-shape {@code sub}
         * merged into outline.
         */
        private Shape45 makeBridge(Shape45 shape, Shape45 sub) {

            finding:
            for (int oi = 0; oi < shape.getNumOutlineVertices(); oi++) {
                for (int si = 0; si < sub.getNumOutlineVertices(); si++) {

                    // two vertices in outline
                    Pt2D vo1 = shape.getOutline().getVertex(oi);
                    Pt2D vo2 = shape.getOutline().getVertexWrapped(oi + 1);
                    // two vertices in subshape
                    Pt2D vs1 = sub.getOutline().getVertex(si);
                    Pt2D vs2 = sub.getOutline().getVertexWrapped(si + 1);

                    // To be safe, there must be no intersecting edges, bridge-triangles
                    // must have counter-clockwise winding, and there must be no vertices
                    // contained inside the area of the bridge-shape...
                    Triangle t1 = new Triangle(vo1, vo2, vs1);
                    Triangle t2 = new Triangle(vo1, vs1, vs2);
                    if (t1.isCCWWinding() &&
                        t2.isCCWWinding() &&
                        !bridgeLinesIntersect(shape, vo1, vo2, vs1, vs2)) {

                        // add two new triangles
                        tris.add(t1);
                        tris.add(t2);
                        
                        // bridge gap and merge subshape

                        // outline vertices
                        Pt2D[] newVerts = new Pt2D[shape.getNumOutlineVertices() +
                                                   sub.getNumOutlineVertices()];
                        int index = 0;
                        for (int i = 0; i < shape.getNumOutlineVertices(); i++) {
                            newVerts[index++] = shape.getOutline().getVertexWrapped(oi + 1 + i);
                        }
                        for (int i = 0; i < sub.getNumOutlineVertices(); i++) {
                            newVerts[index++] = sub.getOutline().getVertexWrapped(si + 1 + i);
                        }
                        
                        // subshapes
                        Shape45[] newSubs = new Shape45[shape.getNumSubShapes() - 1];
                        index = 0;
                        for (Shape45 ss : shape.subShapes)
                            if (ss != sub)
                                newSubs[index++] = ss;

                        shape = new Shape45(newSubs, newVerts);
                        break finding;
                    }
                }
            }

            return shape;
        }

        private boolean bridgeLinesIntersect(Shape45 shape,
                                             Pt2D o1, Pt2D o2, Pt2D s1, Pt2D s2) {
            return bridgeLineIntersects(shape, o2, s1) ||
                   bridgeLineIntersects(shape, s1, o1) ||
                   bridgeLineIntersects(shape, s2, o1);
        }

        private boolean bridgeLineIntersects(Shape45 shape, Pt2D a, Pt2D b) {
            Line ln = new Line(a, b);
            if (shape.getOutline().intersectsIgnoreSharedVertices(ln))
                return true;
            for (Shape45 sub : shape.subShapes)
                if (sub.getOutline().intersectsIgnoreSharedVertices(ln))
                    return true;
            return false;
        }
        
    }


    
    /*----------------------- BOOLEAN MODELLING ------------------------*/

    public List<Shape45> subtract45(Shape45 s) {

        // get edges
        List<Line> homeLines = new ArrayList<>(); //getEdges());
        for (Line edge : getOutline().getEdges())
            homeLines.add(edge);
        List<Line> awayLines = new ArrayList<>(); //s.getEdges());
        for (Line edge : s.getOutline().getEdges())
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
         * @return Negative num = less than, zero = equal, positive num =
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
     * <p>Makes source code for shape which can be pasted directly into program
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
        for (int i = 0; i < outline.getNumVertices(); i++) {
            Pt2D v = outline.getVertex(i);
            sb.append("new Pt2D(" + v.x() + ", " + v.y() + ")");
            if (i < outline.getNumVertices() - 1)
                sb.append(",\n");
        }
        
        sb.append(")");
        return sb.toString();
    }

}
