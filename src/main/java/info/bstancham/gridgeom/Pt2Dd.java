package info.bstancham.gridgeom;

/**
 * <p>Immutable data type representing a point in 2D space with {@code double}
 * co-ordinates.</p>
 */
public class Pt2Dd {

    private double x;
    private double y;

    public Pt2Dd(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public double x() { return x; }
    public double y() { return y; }

    public Pt2D toInt() {
        return new Pt2D((int) x, (int) y);
    }

    public Pt2Df toFloat() {
        return new Pt2Df((float) x, (float) y);
    }

    @Override
    public boolean equals(Object obj) {
        // some safe optimizations
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        // cast guaranteed to succeed here
        Pt2Dd that = (Pt2Dd) obj;
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
     * Returns the slope between this point and the input point.
     * Formally, if the two points are (x0, y0) and (x1, y1), then the slope
     * is (y1 - y0) / (x1 - x0).
     *
     * @param p The input point.
     * @return The slope between this point and the input point.<br>
     * SPECIAL CASES:<br>
     * - horizontal line returns +0.0.<br>
     * - vertical line returns Double.POSITIVE_INFINITY.<br>
     * - degenerate line returns Double.NEGATIVE_INFINITY.
     */
    public double slopeTo(Pt2Dd p) {
        if (p.equals(this)) return Double.NEGATIVE_INFINITY; // degenerate
        if (p.y == this.y) return +0.0;                      // horizontal
        if (p.x == this.x) return Double.POSITIVE_INFINITY;  // vertical
        return (p.y - this.y) / (p.x - this.x);
    }

}
