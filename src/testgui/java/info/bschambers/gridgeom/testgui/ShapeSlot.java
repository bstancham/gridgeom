package info.bschambers.gridgeom.testgui;

import info.bschambers.gridgeom.ShapeGroup;

public class ShapeSlot {
        
    private ShapeSet[] shapeSets;
    private int shapeSetIndex = 0;

    public ShapeSlot(ShapeSet[] shapeSets) {
        this.shapeSets = shapeSets;
    }

    public ShapeGroup shape() {
        return wrapper().shape();
    }

    public ShapeWrapper wrapper() {
        return shapeSet().current();
    }

    public ShapeSet shapeSet() {
        return shapeSets[shapeSetIndex];
    }

    public int numShapeSets() {
        return shapeSets.length;
    }

    public int shapeSetIndex() {
        return shapeSetIndex;
    }

    public void previousShapeSet() {
        shapeSetIndex--;
        if (shapeSetIndex < 0) shapeSetIndex = shapeSets.length - 1;
    }

    public void nextShapeSet() {
        shapeSetIndex++;
        if (shapeSetIndex >= shapeSets.length) shapeSetIndex = 0;
    }

    public void translate(int x, int y) {
        for (ShapeSet ss : shapeSets)
            for (ShapeWrapper sw : ss.all())
                sw.setPosition(x, y);
    }
                
}

