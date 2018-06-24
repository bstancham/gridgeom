package info.bschambers.gridgeom;

/**
 * <p>Immutable data type representing a point in 2D space with {@code float}
 * co-ordinates.</p>
 */
public class Pt2Df {

    private float x;
    private float y;

    public Pt2Df(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public float x() { return x; }
    public float y() { return y; }

    @Override
    public boolean equals(Object obj) {
        // some safe optimizations
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        // cast guaranteed to succeed here
        Pt2Df that = (Pt2Df) obj;
        // test for equality in all significant fields
        if (that.x != x) return false;
        if (that.y != y) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int) ((x * 31) + (y * 47));
    }

    /**
     * <p>Returns the slope between this point and the input point.  Formally,
     * if the two points are {@code (x0, y0)} and {@code (x1, y1)}, then the
     * slope is {@code (y1 - y0) / (x1 - x0)}.</p>
     *
     * @param p The input point.
     * @return The slope between this point and the input point.<br>
     * SPECIAL CASES:<br>
     * - horizontal line returns {@code +0.0}.<br>
     * - vertical line returns {@code Float.POSITIVE_INFINITY}.<br>
     * - degenerate line returns {@code Float.NEGATIVE_INFINITY}.
     */
    public float slopeTo(Pt2Df p) {
        if (p.equals(this)) return Float.NEGATIVE_INFINITY; // degenerate
        if (p.y == this.y) return +0.0f;                    // horizontal
        if (p.x == this.x) return Float.POSITIVE_INFINITY;  // vertical
        return (p.y - (float) this.y) / (p.x - (float) this.x);
    }

}
