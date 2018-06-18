package info.bschambers.gridgeom;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * <p>Immutable data type which represents a group of 2D shapes.</p>
 *
 * <p>Rules:</p>
 * <ul>
 * <li>sub-shapes may share corner vertices but should not intersect or share edges</li>
 * <li>all angles must be divisible by 45 degrees</li>
 * </ul>
 *
 * <p>Computation-intensive work i.e. triangulation is not done until required,
 * and is then memoised for quick repeat-access.</p>
 */
public class ShapeGroup implements Iterable<Shape45> {

    private Shape45[] shapes;
    private Integer numVertices = null;
    private Box2D boundingBox = null;
    private Triangle[] triangles = null;
    private int nestedDepth;
    private Boolean valid = null;

    public ShapeGroup(Shape45 s) {
        this(new Shape45[] { s });
    }

    public ShapeGroup(Shape45 ... shapes) {
        this.shapes = shapes;
        nestedDepth = 0;
        for (Shape45 s : shapes)
            if (s.getNestedDepth() > nestedDepth)
                nestedDepth = s.getNestedDepth();
    }

    /**
     * <p>WARNING: currently incomplete.</p>
     *
     * <p>TODO:</p>
     * <ul>
     * <li>test for illegally nested shapes</li>
     * <li>test for intersection without edge-intersection</li>
     * </ul>
     */
    public boolean isValid() {
        if (valid == null) {
            
            valid = true;
            for (Shape45 s : shapes)
                if (!s.isValid())
                    valid = false;

            // shapes must not intersect ... test outlines only -
            // Shape45.isValid() will have taken care of other potential
            // conditions
            for (Shape45 s1 : shapes)
                for (Shape45 s2 : shapes)
                    if (s1 != s2)
                        if (s1.getOutline().intersects45IgnoreSharedVertices(s2.getOutline()))
                            valid = false;

            // top-level shapes must not be illegally nested
            // TODO...

        }
        return valid;
    }

    public int getNumShapes() {
        return shapes.length;
    }

    public Shape45 getShape(int index) {
        return shapes[index];
    }

    public int getNumShapesRecursive() {
        int count = 0;
        for (Shape45 s : shapes)
            count += s.getNumShapesRecursive();
        return count;
    }

    public Shape45 getShapeRecursive(int index) {
        int count = 0;
        for (Shape45 s : shapes) {
            if (index - count < s.getNumShapesRecursive()) {
                return s.getSubShapeRecursive(index - count);
            }
            count +=s.getNumShapesRecursive();
        }
        return null;
    }

    /**
     * @return Total combined number of vertices of all shapes.
     */
    public int getNumVertices() {
        if (numVertices == null) {
            numVertices = 0;
            for (Shape45 s : shapes)
                numVertices += s.getTotalNumVertices();
        }
        return numVertices;
    }

    /**
     * <p>Get vertex in shape or sub-shape.</p>
     *
     * <p>Vertices are given in order of depth-first search, i.e:</p>
     *
     * <ul>
     * <li>Shape 1 outline</li>
     * <li>Shape 1 hole 1</li>
     * <li>Shape 1 hole 1 subshape 1 ... etc</li>
     * <li>Shape 2 outline</li>
     * <li>... etc</li>
     * </ul>
     *
     * @return vertex at index, or {@code null} if index is out of range.
     */
    public Pt2D getVertex(int index) {
        int count = 0;
        for (int i = 0; i < shapes.length; i++) {
            if (index - count < shapes[i].getTotalNumVertices())
                return shapes[i].getVertex(index - count);
            count += shapes[i].getTotalNumVertices();
        }
        // index is out of range
        return null;
    }

    public Shape45 getShapeForVertexIndex(int index) {
        for (int i = 0; i < shapes.length; i++) {
            if (index < shapes[i].getTotalNumVertices())
                return shapes[i].getSubShapeForVertexIndex(index);
            index -= shapes[i].getTotalNumVertices();
        }
        // index is out of range
        return null;
    }

    /**
     * @return The index of sub-shape which contains the vertex at {@code
     * index}, or {@code -1} if {@code index} is out of range.
     */
    public int getSubShapeIndexForVertexIndex(int index) {
        int count = 0;
        for (int i = 0; i < shapes.length; i++) {
            if (index < shapes[i].getTotalNumVertices())
                return count + shapes[i].getSubShapeIndexForVertexIndex(index);
            index -= shapes[i].getTotalNumVertices();
            count += shapes[i].getNumShapesRecursive();
        }
        // index is out of range
        return -1;
    }    

    public int getNestedDepth() {
        return nestedDepth;
    }
    
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



    /*------------ TRANSFORMATIONS (return new ShapeGroup) -------------*/

    /**
     * @return A new {@code ShapeGroup} which is an identical copy of this one,
     * except with the vertex at index {@code i} set to the given {@code x/y}
     * co-ordinates.
     */
    public ShapeGroup setVertex(int index, int x, int y) {
        int count = 0;
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (index - count < shapes[i].getTotalNumVertices()) {
                newShapes[i] = shapes[i].setVertex(index - count, x, y);
            } else {
                newShapes[i] = shapes[i];
            }
            count += shapes[i].getTotalNumVertices();
        }
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup shiftVertex(int index, int x, int y) {
        Pt2D v = getVertex(index);
        return setVertex(index, v.x() + x, v.y() + y);
    }

    public ShapeGroup shift(int x, int y) {
        Shape45[] nShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++)
            nShapes[i] = shapes[i].shift(x, y);
        return new ShapeGroup(nShapes);
    }

    public ShapeGroup deleteVertex(int index) {
        int count = 0;
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (index - count < shapes[i].getTotalNumVertices()) {
                newShapes[i] = shapes[i].deleteVertex(index - count);
            } else {
                newShapes[i] = shapes[i];
            }
            count += shapes[i].getTotalNumVertices();
        }
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup addVertexAfter(int index) {
        int count = 0;
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (index - count < shapes[i].getTotalNumVertices()) {
                newShapes[i] = shapes[i].addVertexAfter(index - count);
            } else {
                newShapes[i] = shapes[i];
            }
            count += shapes[i].getTotalNumVertices();
        }
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup reflectX() {
        int center = getCenterX();
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++)
            newShapes[i] = shapes[i].reflectX(center);
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup reflectY() {
        int center = getCenterY();
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++)
            newShapes[i] = shapes[i].reflectY(center);
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup rotate90(int centerX, int centerY) {
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++)
            newShapes[i] = shapes[i].rotate90(centerX, centerY);
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup reverseWinding() {
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++)
            newShapes[i] = shapes[i].reverseWinding();
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup reverseSubShapeWinding(int index) {
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (index >= 0 &&
                index < shapes[i].getNumShapesRecursive()) {
                newShapes[i] = shapes[i].reverseSubShapeWinding(index);
            } else {
                newShapes[i] = shapes[i];
            }
            index -= shapes[i].getNumShapesRecursive();
        }
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup rotateSubShapeVertexOrder(int subShapeIndex, int amt) {
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (subShapeIndex >= 0 &&
                subShapeIndex < shapes[i].getNumShapesRecursive()) {
                newShapes[i] = shapes[i].rotateSubShapeOutlineVertexOrder(subShapeIndex,
                                                                          amt);
            } else {
                newShapes[i] = shapes[i];
            }
            subShapeIndex -= shapes[i].getNumShapesRecursive();
        }
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup addContainingBox() {

        Box2D box = getBoundingBox();
        Pt2D[] verts = new Pt2D[4];
        verts[0] = new Pt2D(box.lowX - 1,  box.lowY -1);
        verts[1] = new Pt2D(box.highX + 1, box.lowY -1);
        verts[2] = new Pt2D(box.highX + 1, box.highY + 1);
        verts[3] = new Pt2D(box.lowX - 1,  box.highY + 1);

        Shape45[] subs = new Shape45[shapes.length];
        for (int i = 0; i < subs.length; i++)
            subs[i] = shapes[i].reverseWinding();

        Shape45 container = new Shape45(subs, verts);
        return new ShapeGroup(container);
    }

    public ShapeGroup shiftSubShape(int index, int x, int y) {
        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (index >= 0 &&
                index < shapes[i].getNumShapesRecursive()) {
                newShapes[i] = shapes[i].shiftSubShape(index, x, y);
            } else {
                newShapes[i] = shapes[i];
            }
            index -= shapes[i].getNumShapesRecursive();
        }
        return new ShapeGroup(newShapes);
    }

    public ShapeGroup deleteSubShape(int index) {

        if (index < 0 ||
            index >= getNumShapesRecursive())
            return this;
        
        List<Shape45> newShapes = new ArrayList<>();
        for (int i = 0; i < shapes.length; i++) {

            // if index is zero, don't add this shape
            if (index != 0) {

                if (index < shapes[i].getNumShapesRecursive()) {
                    newShapes.add(shapes[i].deleteSubShapeRecursive(index));
                } else {
                    newShapes.add(shapes[i]);
                }
                    
            }

            index -= shapes[i].getNumShapesRecursive();
        }
        
        return new ShapeGroup(newShapes.toArray(new Shape45[newShapes.size()]));
    }

    /**
     * <p>Add a new sub-shape to the shape at index (recursive sub-shape
     * indexing used).</p>
     */
    public ShapeGroup addSubShape(int index, Shape45 newSub) {

        // index out of range - return unmodified
        if (index < 0 ||
            index >= getNumShapesRecursive())
            return this;

        Shape45[] newShapes = new Shape45[shapes.length];

        for (int i = 0; i < shapes.length; i++) {
            if (index >= 0 &&
                index < shapes[i].getNumShapesRecursive()) {
                newShapes[i] = shapes[i].addSubShapeRecursive(index, newSub);
            } else {
                newShapes[i] = shapes[i];
            }
            index -= shapes[i].getNumShapesRecursive();
        }
        
        return new ShapeGroup(newShapes);
    }
    
    /**
     * <p>Add a new sub-shape at the same level as the shape at {@code index}
     * (recursive sub-shape indexing used). The new sub-shape will have the same
     * parent-shape as the shape at {@code index}.</p>
     */
    public ShapeGroup addSubShapeAtSameLevel(int index, Shape45 newSub) {

        // index out of range - return unmodified
        if (index < 0 ||
            index >= getNumShapesRecursive())
            return this;

        Shape45[] newShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (index == 0) {
                // shape is at top level - add it and exit early
                Shape45[] topShapes = new Shape45[shapes.length + 1];
                System.arraycopy(shapes, 0, topShapes, 0, shapes.length);
                topShapes[shapes.length] = newSub;
                return new ShapeGroup(topShapes);
            } else if (index > 0 &&
                       index < shapes[i].getNumShapesRecursive()) {
                // delegate to shape
                newShapes[i] = addSubShapeAtSameLevel(shapes[i],
                                                      index,
                                                      newSub);
            } else {
                newShapes[i] = shapes[i];
            }
            index -= shapes[i].getNumShapesRecursive();
        }
        
        return new ShapeGroup(newShapes);
    }
    
    private Shape45 addSubShapeAtSameLevel(Shape45 s, int index, Shape45 newSubShape) {

        // index out of range - return unmodified
        if (index < 1 ||
            index >= s.getNumShapesRecursive())
            return s;

        index--;
        Shape45[] subs = new Shape45[s.getNumSubShapes()];
        for (int i = 0; i < s.getNumSubShapes(); i++) {
            if (index == 0) {
                // shape is at top level - add it and exit early
                Shape45[] newSubs = new Shape45[s.getNumSubShapes() + 1];
                // Shape45[] topShapes = new Shape45[shapes.length + 1];
                System.arraycopy(s.getSubShapes(), 0, newSubs, 0, s.getNumSubShapes());
                newSubs[s.getNumSubShapes()] = newSubShape;
                return new Shape45(newSubs, s.getOutline());
            } else if (index > 0 &&
                       index < s.getSubShape(i).getNumShapesRecursive()) {
                // delegate to sub-shape
                subs[i] = addSubShapeAtSameLevel(s.getSubShape(i), index, newSubShape);
            } else {
                subs[i] = s.getSubShape(i);
            }
            index -= s.getSubShape(i).getNumShapesRecursive();
        }
        return new Shape45(subs, s.getOutline());
    }
    
    public ShapeGroup subtract45(ShapeGroup sg) {
        ShapeGroup output = this;
        for (Shape45 s : sg)
            output = output.subtract45(s);
        return output;
    }

    public ShapeGroup subtract45(Shape45 s) {
        List<Shape45> output = new ArrayList<>();
        for (Shape45 sub : shapes)
            output.addAll(sub.subtract45(s));
        return new ShapeGroup(output.toArray(new Shape45[output.size()]));
    }

    
    
    /*---------------------------- GEOMETRY ----------------------------*/
    
    private void triangulate() {

        int count = 0;
        for (Shape45 s : this)
            count += s.getNumTriangles();
        
        triangles = new Triangle[count];
        int i = 0;
        for (Shape45 s : this) {
            for (int n = 0; n < s.getNumTriangles(); n++) {
                triangles[i] = s.getTriangle(n);
                i++;
            }
        }
    }

    /**
     * <p>TODO: </p>
     */
    public Set<Pt2Df> getIntersectionPoints45(ShapeGroup gs) {
        Set<Pt2Df> points = new HashSet<>();
        for (Shape45 s1 : shapes) {
            for (Shape45 s2 : gs.shapes) {
                points.addAll(s1.getOutline().getIntersectionPoints45(s2.getOutline()));
            }
        }
        return points;
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
    
    

    /*--------------------------- ITERATION ----------------------------*/

    public Iterator<Shape45> iterator() {
        return new ShapeIterator();
    }

    private class ShapeIterator implements Iterator<Shape45> {
        private int i = 0;
        @Override
        public Shape45 next() {
            return shapes[i++];
        }
        @Override
        public boolean hasNext() {
            return i < shapes.length;
        }
    }
    
    public Iterator<Triangle> triangleIterator() {
        return new TriangleIterator();
    }
    
    private class TriangleIterator implements Iterator<Triangle> {
        private int i = 0;
        @Override
        public Triangle next() {
            return getTriangle(i++);
            // return triangles[i++];
        }
        @Override
        public boolean hasNext() {
            return i < getNumTriangles();
            // return i < triangles.length;
        }
    }


    
    /*------------------------------ MISC ------------------------------*/

    /**
     * <p>Makes source code for shape which can be pasted directly into program
     * code.</p>
     */
    public String getSourceCode() {
        StringBuffer sb = new StringBuffer();
        sb.append("new ShapeGroup(");
        for (int i = 0; i < shapes.length; i++) {
            Shape45 s = shapes[i];
            sb.append(s.getSourceCode());
            if (i < shapes.length - 1)
                sb.append(",\n");
        }
        sb.append(")");
        return sb.toString();
    }

}
