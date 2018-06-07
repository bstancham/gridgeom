package info.bschambers.gridgeom;

/**
 * <p>Immutable data type...</p>
 */
public class Line {

    private Pt2D start;
    private Pt2D end;
    private Double length = null;
    private Double slope = null;
    private Double intercept = null;
    private Double angle = null;

    public Line(int x1, int y1, int x2, int y2) {
        this(new Pt2D(x1, y1), new Pt2D(x2, y2));
    }
    
    public Line(Pt2D start, Pt2D end) {
        this.start = start;
        this.end = end;
    }

    

    /*--------------------------- ACCESSORS ----------------------------*/

    public Pt2D start() { return start; }
    public Pt2D end()   { return end; }

    public int startX()     { return start.x(); }
    public int startY()     { return start.y(); }

    public int endX()       { return end.x(); }
    public int endY()       { return end.y(); }

    /** May return a negative number. */
    public int distX() { return end.x() - start.x(); }

    /** May return a negative number. */
    public int distY() { return end.y() - start.y(); }



    /*---------------------------- GEOMETRY ----------------------------*/

    public double length() {
        if (length == null) length = Geom2D.dist(start, end);
        return length;
    }
    
    /**
     * @return Angle of the line in radians.
     */
    public double angle() {
        if (angle == null) angle = Geom2D.lineAngle(this);
        return angle;
    }

    /**
     * The slope part of the line equation:
     * Y = (slope * X) + intercept
     *
     * SPECIAL CASES...
     * ... SEE DOCUMENTATION for Pt2D.slopeTo()...
     */
    public double slope() {
        if (slope == null) slope = start.slopeTo(end);
        return slope;
    }

    /**
     * The intercept part of the line equation:
     * Y = (slope * X) + intercept
     *
     * This is equivalent the Y co-ordinate of the point where the
     * line crosses the X axis (where X = 0).
     *
     *
     */
    public double intercept() {
        if (intercept == null) intercept = startY() - startX() * slope();
        return intercept;
    }

    public boolean isDegenerate() {
        return start.x() == end.x() && start.y() == end.y();
    }
    
    public boolean isHoriz() {
        return start.y() == end.y() && start.x() != end.x();
    }

    public boolean isVert() {
        return start.x() == end.x() && start.y() != end.y();
    }

    public boolean isDiag45() {
        return Math.abs(distX()) == Math.abs(distY())
            && start.x() != end.x();
    }

    public boolean isDiag45Positive() {
        return distX() == distY() && start.x() != end.x();
    }

    public boolean isDiag45Negative() {
        return distX() == -distY() && start.x() != end.x();
    }

    public boolean is45Compliant() {
        return isHoriz() || isVert() || isDiag45();
    }

    public Linef toFloat() {
        return new Linef(start.toFloat(), end.toFloat());
    }

    /**
     * <p>WARNING: returns {@code null} if either line is not 45-compliant!</p>
     */
    public Pt2Df getIntersectionPoint(Line l) {
        return toFloat().getIntersectionPoint(l.toFloat());
    }

    /**
     * @return True, if {@code p} is contained within the bounding-box of this
     * line.
     */
    public boolean boundingBoxContains(Pt2Df p) {
        // System.out.println("boundingBoxContains(Pt2Df p = " + p + ")");
        return
            p.x() >= Math.min(start.x(), end.x()) &&
            p.x() <= Math.max(start.x(), end.x()) &&
            p.y() >= Math.min(start.y(), end.y()) &&
            p.y() <= Math.max(start.y(), end.y());
    }

    /**
     * @return True, if {@code p} is contained within the bounding-box of this
     * line.
     */
    public boolean boundingBoxContains(Pt2D p) {
        return
            p.x() >= Math.min(start.x(), end.x()) &&
            p.x() <= Math.max(start.x(), end.x()) &&
            p.y() >= Math.min(start.y(), end.y()) &&
            p.y() <= Math.max(start.y(), end.y());
    }

    /**
     * <p>WARNING: only works if line is 45-compliant.</p>
     */
    public boolean contains(Pt2D p) {
        return contains(p.toFloat());
        // if (boundingBoxContains(p)) {
        //     if (isHoriz() || isVert())
        //         return true;
        //     if (isDiag45()) {
        //         return Geom2D.distAbs(p.x(), start.x())
        //             == Geom2D.distAbs(p.y(), start.y());
        //     }
        // }
        // return false;
    }
    
    /**
     * <p>WARNING: only works if line is 45-compliant.</p>
     */
    public boolean contains(Pt2Df p) {
        if (boundingBoxContains(p)) {
            if (isHoriz() | isVert())
                return true;
            if (isDiag45()) {
                return Geom2D.distAbs(p.x(), start.x())
                    == Geom2D.distAbs(p.y(), start.y());
            }
        }
        return false;
    }

    
    
    /*----------------------- LINE INTERSECTION ------------------------*/

    /**
     * <p>NOTE: won't work unless both lines are 45-compliant!</p>
     */
    public static boolean linesIntersect45(Line l1, Line l2) {
        Pt2Df p = l1.getIntersectionPoint(l2);
        if (p == null)
            return false;

        boolean endPt1 = l1.start.equalsValue(p) || l1.end.equalsValue(p);
        boolean endPt2 = l2.start.equalsValue(p) || l2.end.equalsValue(p);
        boolean sharedEnd = endPt1 && endPt2;

        return !sharedEnd && l1.contains(p) && l2.contains(p);
    }
    
}
