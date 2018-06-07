package info.bschambers.gridgeom;

public class Pt2D {

    private int x;
    private int y;

    public Pt2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public int x() { return x; }
    public int y() { return y; }

    public Pt2D transpose(int xx, int yy) {
        return new Pt2D(x + xx, y + yy);
    }

    public Pt2Df toFloat() {
        return new Pt2Df(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        // some safe optimizations
        if (obj == this) return true;
        if (obj == null) return false;
        // cast guaranteed to succeed here
        Pt2D that = (Pt2D) obj;
        // test for equality in all significant fields
        if (that.x != x) return false;
        if (that.y != y) return false;
        return true;
    }

    public boolean equalsValue(Pt2Df p) {
        return p.x() == x && p.y() == y;
    }

    public boolean equalsValue(Pt2Dd p) {
        return p.x() == x && p.y() == y;
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
    public double slopeTo(Pt2D p) {
        if (p.equals(this)) return Double.NEGATIVE_INFINITY; // degenerate
        if (p.y == this.y) return +0.0;                      // horizontal
        if (p.x == this.x) return Double.POSITIVE_INFINITY;  // vertical
        return (p.y - (double) this.y) / (p.x - (double) this.x);
    }

    public Pt2D sum(Pt2D p) {
        return new Pt2D(x + p.x,
                        y + p.y);
    }

}
