package info.bschambers.gridgeom;

/**
 * <p>Immutable data type representing a line segment with {@code float}
 * co-ordinates.</p>
 */
public class Linef {

    private Pt2Df start;
    private Pt2Df end;
    private Float slope = null;
    private Float intercept = null;
    private Float angle = null;

    public Linef(float x1, float y1, float x2, float y2) {
        this(new Pt2Df(x1, y1), new Pt2Df(x2, y2));
    }
    
    public Linef(Pt2Df start, Pt2Df end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "<Linef: " + start + ", " + end + ">";
    }


    
    /*--------------------------- ACCESSORS ----------------------------*/

    public Pt2Df start()  { return start; }
    public Pt2Df end()    { return end; }

    public float startX() { return start.x(); }
    public float startY() { return start.y(); }

    public float endX()   { return end.x(); }
    public float endY()   { return end.y(); }

    /** May return a negative number. */
    public float distX() {
        return end.x() - start.x();
    }

    /** May return a negative number. */
    public float distY() {
        return end.y() - start.y();
    }

    /**
     * <p>The slope part of the line equation:<br/>
     * {@code Y = (slope * X) + intercept}</p>
     *
     * <p>SPECIAL CASES:</p>
     * <p>See documentation for {@link Pt2Df#slopeTo}...</p>
     */
    public float slope() {
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
     *
     * <p>Vertical line returns {@code Float.POSITIVE_INFINITY}.<br/>
     * Degenerate line returns {@code Float.NEGATIVE_INFINITY}.</p>
     */
    public float intercept() {
        if (intercept == null) {
            if (isDegenerate())
                intercept = Float.NEGATIVE_INFINITY;
            else if (isVert())
                intercept = Float.POSITIVE_INFINITY;
            else
                intercept = startY() - startX() * slope();
        }
        return intercept;
    }

    public float angle() {
        if (angle == null)
            angle = (float) Geom2D.lineAngle(this);
        return angle;
    }

    public boolean isDegenerate() {
        return start().equals(end);
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
        return distX() == distY()
            && start.x() != end.x();
    }

    public boolean isDiag45Negative() {
        return distX() == -distY()
            && start.x() != end.x();
    }

    public boolean is45Compliant() {
        return isHoriz() || isVert() || isDiag45();
    }

    public boolean isParallel(Linef ln) {
        return slope() == ln.slope();
    }

    /**
     * @return True, if point {@code p} equals start or end point of this line.
     */
    public boolean hasVertex(Pt2Df p) {
        return p.equals(start) || p.equals(end);
    }

    

    /*--------------- TRANSFORMATION (return new Linef) ----------------*/

    public Linef shift(Pt2Df amt) {
        return new Linef(start.sum(amt), end.sum(amt));
    }
    


    /*-------------------------- INTERSECTION --------------------------*/

    /**
     * <p>Same as {@code Line.linesIntersect(this, ln)}.</p>
     */
    public boolean intersects(Linef ln) {
        return Linef.linesIntersect(this, ln);
    }

    /**
     * <p>Same as {@code Linef.linesIntersect45(this, ln)}.</p>
     */
    public boolean intersects45(Linef ln) {
        return Linef.linesIntersect45(this, ln);
    }

    /**
     * <p>WARNING: returns {@code null} if lines are parallel, or if either line
     * is degenerate.</p>
     */
    public Pt2Df getIntersectionPoint(Linef ln) {
        // test for degenerate lines and for parallel condition
        if (isDegenerate() || ln.isDegenerate() || isParallel(ln))
            return null;

        // check for vertical line
        if (isVert())
            return new Pt2Df(startX(),
                             (float) ((ln.slope() * startX()) + ln.intercept()));
        if (ln.isVert())
            return new Pt2Df(ln.startX(),
                             (float) ((slope() * ln.startX()) + intercept()));

        double x = -(intercept() - ln.intercept()) / (slope() - ln.slope());
        double y = (slope() * x) + intercept();
        return new Pt2Df((float) x, (float) y);
    }

    /**
     * <p>Guaranteed to return exact results if both lines are 45-compliant -
     * otherwise returns {@code null}.</p>
     *
     * <p>WARNING: returns {@code null} if either line is not 45-compliant!</p>
     */
    public Pt2Df getIntersectionPoint45(Linef l) {

        if (isHoriz()) {
            if (l.isHoriz()) {
                return null;
            } else if (l.isVert()) {
                return new Pt2Df(l.start.x(), start.y());
            } else if (l.isDiag45Positive()) {
                float yDist = l.start.y() - start.y();
                float x = l.start.x() - yDist;
                return new Pt2Df(x, start.y());
            } else if (l.isDiag45Negative()) {
                float yDist = l.start.y() - start.y();
                float x = l.start.x() + yDist;
                return new Pt2Df(x, start.y());
            }
        }

        if (isVert()) {
            if (l.isVert()) {
                return null;
            } else if (l.isHoriz()) {
                return new Pt2Df(start.x(), l.start.y());
            } else if (l.isDiag45Positive()) {
                float xDist = l.start.x() - start.x();
                float y = l.start.y() - xDist;
                return new Pt2Df(start.x(), y);
            } else if (l.isDiag45Negative()) {
                float xDist = l.start.x() - start.x();
                float y = l.start.y() + xDist;
                return new Pt2Df(start.x(), y);
            }
        }

        if (isDiag45()) {
            
            if (l.isVert() || l.isHoriz())
                return l.getIntersectionPoint45(this);

            if (l.isDiag45()) {
                // both are diag
                if (isDiag45Positive() && l.isDiag45Negative())
                    return getIntersectionPointPosNeg45(l);
                else if (isDiag45Negative() && l.isDiag45Positive())
                    return l.getIntersectionPointPosNeg45(this);
            }
        }
        
        // else
        return null;
    }

    /**
     * <p>WARNING: only works for diag-45 lines!</p>
     */
    private float getDiagYConst() {

        // find equation for each line in form x = y + yConst

        if (isDiag45Positive()) {
            float xFrom0 = Math.min(start.x(), end.x());
            return Math.min(start.y(), end.y()) - xFrom0;
        }

        if (isDiag45Negative()) {
            float xFrom0 = Math.min(start.x(), end.x());
            return Math.max(start.y(), end.y()) + xFrom0;
        }

        return 0;
    }

    private Pt2Df getIntersectionPointPosNeg45(Linef l) {
        float thisConst = getDiagYConst();
        float thatConst = l.getDiagYConst();
        
        float smaller = Math.min(thisConst, thatConst);
        float larger = Math.max(thisConst, thatConst);
        float diff = larger - smaller;
        float half = diff / 2.0f;
        
        float x = (thisConst == smaller ? half : -half);
        float y = smaller + half;
        
        return new Pt2Df(x, y);
    }

    /**
     * @return True, if {@code p} is contained within the bounding-box of this
     * line.
     */
    public boolean boundingBoxContains(Pt2Df p) {
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

    public boolean contains45(Pt2D p) {
        return contains45(p.toFloat());
    }

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

    /**
     * <p>Redirects to {@code contains45} if line is 45-compliant, therefore for
     * 45-compliant lines, absolute accuracy can be guaranteed.</p>
     *
     * @return True, if point {@code p} is a point on this line segment.
     */
    public boolean contains(Pt2Df p) {

        if (is45Compliant())
            return contains45(p);

        if (boundingBoxContains(p))
            return slope() == start().slopeTo(p);

        return false;
    }



    /*------------------------- STATIC METHODS -------------------------*/

    public static boolean linesIntersect(Linef l1, Linef l2) {
        Pt2Df p = l1.getIntersectionPoint(l2);
        if (p == null)
            return false;
        return l1.boundingBoxContains(p) && l2.boundingBoxContains(p);
    }
    
    /**
     * <p>NOTE: Always returns {@code false} unless both lines are
     * 45-compliant!</p>
     */
    public static boolean linesIntersect45(Linef l1, Linef l2) {
        Pt2Df p = l1.getIntersectionPoint45(l2);
        if (p == null)
            return false;
        return l1.contains45(p) && l2.contains45(p);
    }

}
