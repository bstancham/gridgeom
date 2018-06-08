package info.bschambers.gridgeom.testgui;

import java.awt.Color;
import info.bschambers.gridgeom.*;

public class ShapeWrapper {

    private ShapeGroup originalShape;
    private ShapeGroup modShape;
    private String name;
    private Color col = Color.GRAY;
    private int xPos = 0;
    private int yPos = 0;
    private int vertexIndex = 0;

    public ShapeWrapper(String name, Shape45 shape) {
        this(name, new ShapeGroup(shape));
    }
    
    public ShapeWrapper(String name, ShapeGroup shape) {
        this.name = name;
        originalShape = shape;
        modShape = shape;
    }
    
    public String name() {
        return name;
    }

    /**
     * @return Position-adjusted shape.
     */
    public ShapeGroup shape() {
        return modShape.translate(xPos, yPos);
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
        // xPos = x - modShape.centre().x();
        // yPos = y - modShape.centre().y();
    }

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

    public Pt2D getCanvasVertex() {
        return modShape.getVertex(vertexIndex).transpose(xPos, yPos);
    }
    
    public void reset() {
        modShape = originalShape;
    }
    
    public ShapeWrapper copy() {
        ShapeWrapper sw = new ShapeWrapper(name, originalShape);
        sw.modShape = modShape;
        sw.col = col;
        sw.xPos = xPos;
        sw.yPos = yPos;
        return sw;
    }

    public void deleteCurrentVertex() {
        modShape = modShape.deleteVertex(vertexIndex);
    }

    public void addVertexAfterCurrent() {
        modShape = modShape.addVertexAfter(vertexIndex);
    }

    public void reflectX() {
        modShape = modShape.reflectX();
    }

    public void reflectY() {
        modShape = modShape.reflectY();
    }

    public void reverseWinding() {
        modShape = modShape.reverseWinding();
    }

}
