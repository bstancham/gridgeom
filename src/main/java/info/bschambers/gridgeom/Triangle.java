package info.bschambers.gridgeom;

import static info.bschambers.gridgeom.Geom2D.WindingDir;

/**
 * <p>Immutable data type representing a triangle with integer co-ordinates.</p>
 */
public class Triangle extends Polygon {

    private Boolean degenerate = null;

    public Triangle(Pt2D v1, Pt2D v2, Pt2D v3) {
        super(new Pt2D[] { v1, v2, v3 });
    }

    public Pt2D a() {
        return getVertex(0);
    }

    public Pt2D b() {
        return getVertex(1);
    }

    public Pt2D c() {
        return getVertex(2);
    }

    /**
     * <p>The centroid is point where the three medians of the triangle
     * intersect.  It is also the 'center of gravity', and one of a triangle's
     * points of concurrency.</p>
     *
     * @return the centroid of the triangle.
     */
    public Pt2Df centroid() {
        // The coordinates of the centroid are simply the average of
        // the coordinates of the vertices.
        return new Pt2Df((a().x() + b().x() + c().x()) / 3.0f,
                         (a().y() + b().y() + c().y()) / 3.0f);
    }

    @Override
    public WindingDir getWindingDir() {
        if (winding == null) {
            if (isDegenerate()) {
                winding = WindingDir.INDETERMINATE;
            } else {
                int dir = Geom2D.turnDirection(a().toFloat(),
                                               b().toFloat(),
                                               c().toFloat());
                if      (dir < 0) winding = WindingDir.CCW;
                else if (dir > 0) winding = WindingDir.CW;
                else              winding = WindingDir.INDETERMINATE;
            }
        }
        return winding;
    }

    @Override
    public boolean isConvex() {
        return !isDegenerate();
    }

    public boolean isDegenerate() {
        if (degenerate == null)
            degenerate = Geom2D.collinear(a(), b(), c());
        return degenerate;
    }

}
