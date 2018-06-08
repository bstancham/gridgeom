package info.bschambers.gridgeom;

public class Box2D {

    public final int lowX;
    public final int highX;
    public final int lowY;
    public final int highY;
    public final int centerX;
    public final int centerY;
    
    public Box2D(int x1, int y1, int x2, int y2) {
        lowX = Math.min(x1, x2);
        highX = Math.max(x1, x2);
        lowY = Math.min(y1, y2);
        highY = Math.max(y1, y2);
        centerX = lowX + ((highX - lowX) / 2);
        centerY = lowY + ((highY - lowY) / 2);
    }

}
