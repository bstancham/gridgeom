package info.bschambers.gridgeom;

import java.util.Arrays;

/**
 * <p>Static methods for 2D geometry.</p>
 */
public class Geom2D {

    private Geom2D() {}

    public enum WindingDir {
        CW, CCW, INDETERMINATE
    };

    public static final double EIGHTH_TURN = Math.PI * 0.25;
    public static final double QUARTER_TURN = Math.PI * 0.5;
    public static final double THREE_EIGHTH_TURN = Math.PI * 0.75;
    public static final double HALF_TURN = Math.PI;
    public static final double FIVE_EIGHTH_TURN = Math.PI * 1.25;
    public static final double THREE_QUARTER_TURN = Math.PI * 1.5;
    public static final double SEVEN_EIGHTH_TURN = Math.PI * 1.75;
    public static final double FULL_TURN = Math.PI * 2.0;

    public static float distAbs(float start, float end) {
        return Math.abs(end - start);
    }

    public static int distAbs(int start, int end) {
        return Math.abs(end - start);
    }

    public static int dist(int start, int end) {
        return end - start;
    }

    public static int midPointInt(int a, int b) {
        int low = Math.min(a, b);
        int high = Math.max(a, b);
        return low + ((high - low) / 2);
    }
    
    public static Pt2D midPointInt(Pt2D p1, Pt2D p2) {
        return new Pt2D (midPointInt(p1.x(), p2.x()),
                         midPointInt(p1.y(), p2.y()));
    }



    /*--------------------------- LINE ANGLE ---------------------------*/

    /**
     * @return The angle ABC.
     */
    public static double angle(Pt2D a, Pt2D b, Pt2D c) {
        return angle(a.toFloat(), b.toFloat(), c.toFloat());
    }
    
    public static double angle(Pt2Df a, Pt2Df b, Pt2Df c) {
	double angleBA = lineAngle(b,a);
	double angleBC = lineAngle(b,c);
	if (angleBC < angleBA) angleBC += (Math.PI * 2);
	return angleBC - angleBA;
    }

    public static double angleTurned(Pt2D a, Pt2D b, Pt2D c) {
        return angleTurned(a.toFloat(), b.toFloat(), c.toFloat());
    }
    
    /**
     * @return The angle of the turn from path of line AB to line BC.
     */
    public static double angleTurned(Pt2Df a, Pt2Df b, Pt2Df c) {
        double angleAB = lineAngle(a,b);
        double angleBC = lineAngle(b,c);
        boolean onLeftSide = onRelativeLeftSide(new Linef(a,b), c);
        if (onLeftSide && angleAB < angleBC) angleAB += (Math.PI * 2);
        if (!onLeftSide && angleAB > angleBC) angleBC += (Math.PI * 2);
        return angleAB - angleBC;
    }

    public static int turnDirection(Pt2D a, Pt2D b, Pt2D c) {
        return turnDirection(a.toFloat(), b.toFloat(), c.toFloat());
    }
    
    /**
     * @return {@code -1} if turn direction is counter-clockwise (or left).<br/>
     * {@code 1} if turn is clockwise (or right).<br/>
     * {@code 0} if turn has angle of zero.
     */
    public static int turnDirection(Pt2Df a, Pt2Df b, Pt2Df c) {
        double angle = angleTurned(a, b, c);
        if (angle > 0) return -1;
        if (angle < 0) return 1;
        return 0;
    }

    public static double lineAngle(Pt2Df start, Pt2Df end) {
        return lineAngle(start.x(), start.y(), end.x(), end.y());
    }
    
    public static double lineAngle(Pt2D start, Pt2D end) {
        return lineAngle(start.toFloat(), end.toFloat());
    }
    
    public static double lineAngle(Line ln) {
        return lineAngle(ln.start().x(), ln.start().y(),
                         ln.end().x(), ln.end().y());
    }

    public static double lineAngle(double x1, double y1, double x2, double y2) {
        
	// get line length (using pythagoras)
	// ... square on the hypotenuse is equal to the squares on the other two sides...
        double xDist = x2 - x1;
        double yDist = y2 - y1;

	// TEST FOR DEGENERATE LINE CONDITION
	if (xDist == 0 && yDist == 0)
            throw new IllegalArgumentException("degenerate line");

	// TEST FOR ORTHOGONAL SPECIAL CASES
	if (xDist == 0)
	    return (yDist > 0 ? 0.0 : HALF_TURN);
	if (yDist == 0)
	    return (xDist > 0 ? QUARTER_TURN : THREE_QUARTER_TURN);

        // TEST FOR 45 DEGREE INCREMENTS
        if (xDist == yDist)
            return (xDist > 0 ? EIGHTH_TURN : FIVE_EIGHTH_TURN);
            // return EIGHTH_TURN;
        if (xDist == -yDist)
            return (xDist > 0 ? THREE_EIGHTH_TURN : SEVEN_EIGHTH_TURN);

	// ALL OTHER CASES
	// ... get length of hypotenuse...
	// ... square of hypotenuse is equal to sum of other two squares...
	double lenAdjacent = Math.abs(xDist);
	double lenOpposite = Math.abs(yDist);
	double squareHypotenuse = lenOpposite * lenOpposite + lenAdjacent * lenAdjacent;
	double lenHypotenuse = Math.sqrt(squareHypotenuse);
	// get angle (using trigonometry)
	double angle = Math.asin(lenAdjacent/lenHypotenuse);

	// make quadrant adjustments
	if (xDist > 0 && yDist < 0) angle = 2 * QUARTER_TURN - angle;
	else if (xDist < 0 && yDist < 0) angle += QUARTER_TURN * 2;
	else if (xDist < 0 && yDist > 0) angle = 4 * QUARTER_TURN - angle;
	return angle;
    }

    /**
     * ... gets point on circumference of circle of given radius...
     *
     * @param angle Angle in radians(?)
     */
    public static Pt2Dd circlePoint(double angle, double radius) {
        return new Pt2Dd(circlePointX(angle, radius),
                         circlePointY(angle, radius));
    }
    
    public static double circlePointX(double angle, double radius) {
        return Math.sin(angle) * radius;
    }

    public static double circlePointY(double angle, double radius) {
        return Math.cos(angle) * radius;
    }

    public static Pt2Df getPointForXValue(Linef lineSegment, float x) {
        return new Pt2Df(x, getYForXValue(lineSegment, x));
    }

    public static float getYForXValue(Linef lineSegment, float x) {
        return (lineSegment.slope() * x) + lineSegment.intercept();
    }

    

    /*----------- LINE LENGTH (DISTANCE BETWEEN TWO POINTS) ------------*/

    public static double dist(Pt2D p1, Pt2D p2) {
        return dist(p1.x(), p1.y(), p2.x(), p2.y());
    }

    public static double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(distSquared(x1, y1, x2, y2));
    }
    
    public static double distSquared(Pt2Df p1, Pt2Df p2) {
        return distSquared(p1.x(), p1.y(), p2.x(), p2.y());
    }

    public static double distSquared(double x1, double y1, double x2, double y2) {
        // find length using Pythagoras' theorem
	double lenAdjacent = Math.abs(x2 - x1);
	double lenOpposite = Math.abs(y2 - y1);
	return lenOpposite * lenOpposite + lenAdjacent * lenAdjacent;
    }



    /*-------------- FIND RELATIVE LEFT-HAND SIDE OF LINE --------------*/

    /**
     * TODO: IMPLEMENT FULLY...
     */
    public static boolean pointIsOnLine(Linef line, Pt2Df p) {
        // SPECIAL PERPENDICULAR CASES
        if (line.isVert() &&
            p.x() == line.startX()) return true;
        if (line.isHoriz() &&
            p.y() == line.startY()) return true;
        // else
        return false;
    }

    public static boolean collinear(Pt2D a, Pt2D b, Pt2D c) {
        // find bottom-left point
        Pt2D[] points = new Pt2D[] { a, b, c };
        Arrays.sort(points);
        return lineAngle(points[0], points[1]) == lineAngle(points[0], points[2]);
        
        // return lineAngle(a, b) == lineAngle(a, c);
    }
    
    // /**
    //  * TODO: IMPLEMENT FULLY...
    //  */
    // public static boolean pointIsOnLineSegment(LineDbl line, PointDbl p) {
    //     return false;
    // }

    public static boolean onRelativeLeftSide(Linef line, Pt2Df p) {
	// SPECIAL CASE: point is on line...
	if (pointIsOnLine(line, p)) return false;
	// SPECIAL CASE: vertical line...
	if (line.isVert()) {
	    if (line.startY() < line.endY()) { // pointing upwards
		return p.x() < line.startX();
	    } else { // pointing downwards
		return p.x() > line.startX();
	    }
	}
	// ... ALL OTHER LINES
	boolean out = p.y() > getPointForXValue(line, p.x()).y();
	if (line.endX() > line.startX())
            return out;
        else
            return !out;
    }

}
