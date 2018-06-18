package info.bschambers.gridgeom;

import java.util.Set;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class TriangleTest {

    private Triangle t1 = new Triangle(new Pt2D(0, 0),
                                       new Pt2D(3, 0),
                                       new Pt2D(3, 3));

    private Triangle t2 = new Triangle(new Pt2D(6, 1),
                                       new Pt2D(2, 1),
                                       new Pt2D(5, 5));

    private Triangle degenerate1 = new Triangle(new Pt2D(3, 1),
                                                new Pt2D(7, 5),
                                                new Pt2D(6, 4));

    @Test
    public void testIsDegenerate() {
        assertFalse(t1.isDegenerate());
        assertFalse(t2.isDegenerate());
        assertTrue(degenerate1.isDegenerate());
    }

    @Test
    public void testWindingDir() {

        assertTrue(t1.isCCWWinding());
        assertFalse(t1.isCWWinding());

        assertTrue(t2.isCWWinding());
        assertFalse(t2.isCCWWinding());

        assertFalse(degenerate1.isCWWinding());
        assertFalse(degenerate1.isCCWWinding());

    }

}
