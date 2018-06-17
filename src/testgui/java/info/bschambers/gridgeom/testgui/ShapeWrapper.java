package info.bschambers.gridgeom.testgui;

import java.awt.Color;
import info.bschambers.gridgeom.*;

/**
 * <p>Encapsulates a {@link ShapeGroup} for interactive editing.</p>
 */
public class ShapeWrapper {

    private ShapeGroup originalShape;
    private ShapeGroup modShape;
    private String name;
    private Color col = Color.GRAY;
    private int xPos = 0;
    private int yPos = 0;
    private int vertexIndex = 0;
    private int subShapeIndex = 0;

    public ShapeWrapper(String name, Shape45 shape) {
        this(name, new ShapeGroup(shape));
    }
    
    public ShapeWrapper(String name, ShapeGroup shape) {
        this.name = name;
        originalShape = shape;
        modShape = shape;
    }

    public ShapeWrapper copy() {
        ShapeWrapper sw = new ShapeWrapper(name, originalShape);
        sw.modShape = modShape;
        sw.col = col;
        sw.xPos = xPos;
        sw.yPos = yPos;
        return sw;
    }

    public void reset() {
        modShape = originalShape;
    }

    /**
     * <p>Prints source code for shape to standard out. This can be copied and
     * pasted directly into a program.</p>
     */
    public void printSourceCode() {
        System.out.println("\nnew ShapeWrapper(\"" + name() + "\",\n"
                           + shape().getSourceCode() + ")"
                           + "\n\n... you can copy & paste this "
                           + "in to your program source code...\n");
    }
    


    /*---------------------- GETTERS AND SETTERS -----------------------*/

    /**
     * @return Position-adjusted shape.
     */
    public ShapeGroup shape() {
        return modShape.shift(xPos, yPos);
    }

    public String name() {
        return name;
    }

    public void setColor(Color c) {
        col = c;
    }

    public Color getColor() {
        return col;
    }

    public Pt2D getPosition() {
        return new Pt2D(xPos, yPos);
    }

    public int getPosX() { return xPos; }
    public int getPosY() { return yPos; }

    public void setPosition(int x, int y) {
        xPos = x;
        yPos = y;
    }


    
    /*----------------------------- VERTEX -----------------------------*/

    public int vertexIndex() { return vertexIndex; }

    public void incrVertexIndex(int amount) {
        vertexIndex += amount;
        if (vertexIndex < 0)
            vertexIndex = shape().getNumVertices() - 1;
        else if (vertexIndex >= shape().getNumVertices())
            vertexIndex = 0;
    }

    public Pt2D getVertex() {
        return modShape.getVertex(vertexIndex);
    }
    
    /**
     * Sets the current vertex.
     */
    public void setVertex(int x, int y) {
        modShape = modShape.setVertex(vertexIndex, x - xPos, y - yPos);
    }

    public void shiftVertex(int x, int y) {
        modShape = modShape.shiftVertex(vertexIndex, x, y);
    }

    public Pt2D getCanvasVertex() {
        return modShape.getVertex(vertexIndex).transpose(xPos, yPos);
    }

    public boolean deleteCurrentVertex() {
        Shape45 sub = modShape.getShapeForVertexIndex(vertexIndex);
        if (sub.getNumVertices() > 3) {
            modShape = modShape.deleteVertex(vertexIndex);
            return true;
        } else {
            System.out.println("Can't delete vertex - shape must have at least three vertices!");
            return false;
        }
    }

    public void addVertexAfterCurrent() {
        modShape = modShape.addVertexAfter(vertexIndex);
    }

    /**
     * <p>Rotate outline vertices in the sub-shape of the currently selected
     * vertex.</p>
     */
    public void rotateVertices(int amt) {
        int index = getSubShapeIndexForVertexIndex();
        System.out.println("rotate vertex order: sub-shape index " + index);
        if (index >= 0)
            modShape = modShape.rotateOutlineVertices(index, amt);
    }


    
    /*--------------------------- SUB-SHAPE ----------------------------*/

    public int subShapeIndex() { return subShapeIndex; }

    public void incrSubShapeIndex(int amount) {
        subShapeIndex += amount;
        // wrap index
        if (subShapeIndex < 0)
            subShapeIndex = shape().getNumShapesRecursive() - 1;
        else if (subShapeIndex >= shape().getNumShapesRecursive())
            subShapeIndex = 0;
    }

    public Shape45 getSubShape() {
        return modShape.getShapeRecursive(subShapeIndex).shift(xPos, yPos);
    }

    public int getSubShapeIndexForVertexIndex() {
        return modShape.getSubShapeIndexForVertexIndex(vertexIndex);
    }

    public Shape45 getShapeForVertexIndex() {
        return modShape.getShapeForVertexIndex(vertexIndex).shift(xPos, yPos);
    }

    /**
     * <p>Shift current sub-shape by specified number of units in x/y
     * direction.</p>
     */
    public void shiftSubShape(int x, int y) {
        modShape = modShape.shiftSubShape(subShapeIndex, x, y);
    }
    
    public void deleteCurrentSubShape() {
        modShape = modShape.deleteSubShape(subShapeIndex);
    }

    /**
     * <p>Adds a new sub-shape, as a sub-shape of the current sub-shape.</p>
     */
    public void addSubShapeOfCurrent() {
        modShape = modShape.addSubShape(subShapeIndex,
                                        newSubShape());
    }

    public void addSubShapeAtCurrentLevel() {
        modShape = modShape.addSubShapeAtSameLevel(subShapeIndex,
                                                   newSubShape());
    }

    public void addContainingBox() {
        modShape = modShape.addContainingBox();
    }

    private Shape45 newSubShape() {
        Shape45 sub = getSubShape();
        Shape45 newSub = new Shape45(new Pt2D(0, 0),
                                     new Pt2D(0, 1),
                                     new Pt2D(1, 0));
        newSub.shift(sub.getCenterX(), sub.getCenterY());
        return newSub;
    }

    public void reverseSubShapeWinding() {
        modShape = modShape.reverseSubShapeWinding(subShapeIndex);
    }

    

    /*------------------------- TRANSFORMATION -------------------------*/

    public void reverseWinding() {
        modShape = modShape.reverseWinding();
    }

    public void reflectX() {
        modShape = modShape.reflectX();
    }

    public void reflectY() {
        modShape = modShape.reflectY();
    }

    public void rotate90() {
        modShape = modShape.rotate90(modShape.getCenterX(),
                                     modShape.getCenterY());
    }

}
