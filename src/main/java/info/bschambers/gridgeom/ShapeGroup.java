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
 * <p>Computer-intensive work i.e. triangulation is not done until required, and
 * is then memoised for quick repeat-access.</p>
 */
public class ShapeGroup implements Iterable<Shape45> {

    private Shape45[] shapes;
    private Integer numVertices = null;
    private Triangle[] triangles = null;

    public ShapeGroup(Shape45 s) {
        this(new Shape45[] { s });
    }

    public ShapeGroup(Shape45 ... shapes) {
        this.shapes = shapes;
    }

    public boolean isValid() {
        boolean result = true;
        for (Shape45 s : shapes)
            if (!s.isValid())
                result = false;
        return result;
    }

    public int getNumShapes() {
        return shapes.length;
    }

    public Shape45 getShape(int index) {
        return shapes[index];
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

    public ShapeGroup translate(int x, int y) {
        Shape45[] nShapes = new Shape45[shapes.length];
        for (int i = 0; i < shapes.length; i++)
            nShapes[i] = shapes[i].transpose(x, y);
        return new ShapeGroup(nShapes);
    }

    public ShapeGroup subtract(ShapeGroup sg) {
        ShapeGroup output = this;
        for (Shape45 s : sg)
            output = output.subtract(s);
        return output;
    }

    public ShapeGroup subtract(Shape45 s) {
        List<Shape45> output = new ArrayList<>();
        for (Shape45 sub : shapes)
            output.addAll(sub.subtract(s));
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

    public Set<Pt2Df> getIntersectionPoints(ShapeGroup gs) {
        Set<Pt2Df> points = new HashSet<>();
        for (Shape45 s1 : shapes) {
            for (Shape45 s2 : gs.shapes) {
                points.addAll(s1.getIntersectionPoints(s2));
            }
        }
        return points;
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
            return triangles[i++];
        }
        @Override
        public boolean hasNext() {
            return i < triangles.length;
        }
    }
    
}
