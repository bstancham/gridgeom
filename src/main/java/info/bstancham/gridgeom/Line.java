package info.bstancham.gridgeom;

/**
 * <p>Immutable data type representing a line segment with {@code int}
 * co-ordinates.</p>
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

    public Linef toFloat() {
        return new Linef(start.toFloat(), end.toFloat());
    }

    

    /*--------------------------- ACCESSORS ----------------------------*/

    public Pt2D start() { return start; }
    public Pt2D end()   { return end; }

    public int startX() { return start.x(); }
    public int startY() { return start.y(); }

    public int endX()   { return end.x(); }
    public int endY()   { return end.y(); }

    /** May return a negative number. */
    public int distX() {
        return end.x() - start.x();
    }

    /** May return a negative number. */
    public int distY() {
        return end.y() - start.y();
    }



    /*---------------------------- GEOMETRY ----------------------------*/

    public double length() {
        if (length == null)
            length = Geom2D.dist(start, end);
        return length;
    }
    
    /**
     * @return Angle of the line in radians.
     */
    public double angle() {
        if (angle == null)
            angle = Geom2D.lineAngle(this);
        return angle;
    }

    /**
     * <p>The slope part of the line equation:<br/>
     * {@code Y = (slope * X) + intercept}</p>
     *
     * <p>SPECIAL CASES:</p>
     * <p>... see documentation for {@link Pt2D#slopeTo}</p>
     */
    public double slope() {
        if (slope == null)
            slope = start.slopeTo(end);
        return slope;
    }

    /**
     * <p>The intercept part of the line equation:<br/>
     * {@code Y = (slope * X) + intercept}</p>
     *
     * <p>This is equivalent the Y co-ordinate of the point where the line
     * crosses the X axis (where X = 0).</p>
     */
    public double intercept() {
        if (intercept == null)
            intercept = startY() - startX() * slope();
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

    /**
     * <p>Same as {@code Line.linesIntersect(this, ln)}.</p>
     */
    public boolean intersects(Line ln) {
        return Line.linesIntersect(this, ln);
    }

    /**
     * <p>Same as {@code Line.linesIntersect45(this, ln)}.</p>
     */
    public boolean intersects45(Line ln) {
        return Line.linesIntersect45(this, ln);
    }

    /**
     * <p>WARNING: returns {@code null} if lines are parallel, or if either line
     * is degenerate.</p>
     */
    public Pt2Df getIntersectionPoint(Line l) {

        // test for degenerate lines and for parallel condition
        if (isDegenerate() || l.isDegenerate() ||
            slope() == l.slope())
            return null;

        // check for vertical line
        if (isVert())
            return new Pt2Df(startX(),
                             (float) ((l.slope() * startX()) + l.intercept()));
        if (l.isVert())
            return new Pt2Df(l.startX(),
                             (float) ((slope() * l.startX()) + intercept()));

        double x = -(intercept() - l.intercept()) / (slope() - l.slope());
        double y = (slope() * x) + intercept();
        return new Pt2Df((float) x, (float) y);
    }

    /**
     * <p>WARNING: returns {@code null} if either line is not 45-compliant!</p>
     */
    public Pt2Df getIntersectionPoint45(Line l) {
        return toFloat().getIntersectionPoint45(l.toFloat());
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
    public boolean contains45(Pt2D p) {
        return contains45(p.toFloat());
    }
    
    /**
     * <p>WARNING: only works if line is 45-compliant.</p>
     */
    public boolean contains45(Pt2Df p) {
        if (boundingBoxContains(p)) {
            if (isHoriz() || isVert())
                return true;
            if (isDiag45()) {
                return Geom2D.distAbs(p.x(), start.x())
                    == Geom2D.distAbs(p.y(), start.y());
            }
        }
        return false;
    }

    public boolean contains(Pt2D p) {
        if (boundingBoxContains(p)) {
            if (p.equals(start) ||
                p.equals(end))
                return true;
            return Geom2D.collinear(start, end, p);
        }
        return false;
    }

    
    
    /*----------------------- LINE INTERSECTION ------------------------*/

    public static boolean linesIntersect(Line l1, Line l2) {

        Pt2Df p = l1.getIntersectionPoint(l2);
        if (p == null)
            return false;

        return l1.boundingBoxContains(p) && l2.boundingBoxContains(p);
    }
    
    public static boolean linesIntersectIgnoreSharedEnds(Line l1, Line l2) {
        Pt2Df p = l1.getIntersectionPoint(l2);
        if (p == null)
            return false;

        boolean endPt1 = l1.start.equalsValue(p) || l1.end.equalsValue(p);
        boolean endPt2 = l2.start.equalsValue(p) || l2.end.equalsValue(p);
        boolean sharedEnd = endPt1 && endPt2;

        return !sharedEnd &&
            l1.boundingBoxContains(p) && l2.boundingBoxContains(p);
    }
    
    /**
     * <p>NOTE: won't work unless both lines are 45-compliant!</p>
     */
    public static boolean linesIntersect45(Line l1, Line l2) {
        Pt2Df p = l1.getIntersectionPoint45(l2);
        if (p == null)
            return false;

        return l1.contains45(p) && l2.contains45(p);
    }
    
    /**
     * <p>NOTE: won't work unless both lines are 45-compliant!</p>
     */
    public static boolean linesIntersect45IgnoreSharedEnds(Line l1, Line l2) {
        Pt2Df p = l1.getIntersectionPoint45(l2);
        if (p == null)
            return false;

        boolean endPt1 = l1.start.equalsValue(p) || l1.end.equalsValue(p);
        boolean endPt2 = l2.start.equalsValue(p) || l2.end.equalsValue(p);
        boolean sharedEnd = endPt1 && endPt2;

        return !sharedEnd && l1.contains45(p) && l2.contains45(p);
    }

    /**
     * <p>NOTE: won't work unless both lines are 45-compliant!</p>
     */
    public static boolean linesIntersect45IgnoreEnds(Line l1, Line l2) {
        Pt2Df p = l1.getIntersectionPoint45(l2);
        if (p == null)
            return false;

        boolean endPt = l1.start.equalsValue(p) || l1.end.equalsValue(p)
                     || l2.start.equalsValue(p) || l2.end.equalsValue(p);

        return !endPt && l1.contains45(p) && l2.contains45(p);
    }

}
