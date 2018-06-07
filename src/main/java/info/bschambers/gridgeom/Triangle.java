package info.bschambers.gridgeom;

public class Triangle extends AbstractShape {

    public Triangle(Pt2D v1, Pt2D v2, Pt2D v3) {
        super(new Pt2D[] { v1, v2, v3 });
    }
    
}
