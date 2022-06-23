package info.bstancham.gridgeom;

import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static info.bstancham.gridgeom.Geom2D.WindingDir;

public class PolygonTest {

    private Polygon triangle = new Polygon(new Pt2D(0, 0),
                                           new Pt2D(2, 2),
                                           new Pt2D(0, 2));
    
    @Test
    public void testGetIntersectionPoints45() {

        // very simple cases

        Polygon rect1 = new Polygon(new Pt2D(1, 2),
                                    new Pt2D(5, 2),
                                    new Pt2D(5, 6),
                                    new Pt2D(1, 6));

        Polygon rect2 = new Polygon(new Pt2D(4, 3),
                                    new Pt2D(6, 3),
                                    new Pt2D(6, 7),
                                    new Pt2D(4, 7));

        Set<Pt2Df> expected = new HashSet<>();
        expected.add(new Pt2Df(5.0f, 3.0f));
        expected.add(new Pt2Df(4.0f, 6.0f));

        Set<Pt2Df> actual = rect1.getIntersectionPoints45(rect2);
        assertEquals(2, actual.size());
        // assertTrue(expected.equals(actual));
        assertEquals(expected, actual);

        // transpose rect2 so that only one shared corner vertex intersects
        
        rect2 = rect2.shift(1, 3);
        expected = new HashSet<>();
        expected.add(new Pt2Df(5, 6));
        assertEquals(expected, rect1.getIntersectionPoints45(rect2));

        // intersection point should still be found for any point on line, even
        // if it's one that produces null in Line.getIntersectionPoint
        // i.e. parallel line or...

        Polygon rect3 = new Polygon(new Pt2D(3, 9),
                                    new Pt2D(0, 9),
                                    new Pt2D(0, 3),
                                    new Pt2D(3, 3));

        Polygon rect4 = new Polygon(new Pt2D(3, 1),
                                    new Pt2D(5, 1),
                                    new Pt2D(5, 7),
                                    new Pt2D(3, 7),
                                    new Pt2D(3, 6),
                                    new Pt2D(3, 4));

        expected = new HashSet<>();
        expected.add(new Pt2Df(3, 3));
        expected.add(new Pt2Df(3, 4));
        expected.add(new Pt2Df(3, 6));
        expected.add(new Pt2Df(3, 7));
        assertEquals(expected, rect3.getIntersectionPoints45(rect4));
        
    }

    @Test
    public void testWindingDir() {

        // convex and 45-compliant
        Polygon poly = new Polygon(new Pt2D(4, -2),
                                   new Pt2D(5, -3),
                                   new Pt2D(6, -3),
                                   new Pt2D(10, 1),
                                   new Pt2D(10, 3),
                                   new Pt2D(6, 3),
                                   new Pt2D(4, 1));
        assertEquals(WindingDir.CCW, poly.getWindingDir());
        assertEquals(WindingDir.CW, poly.reverseWinding().getWindingDir());

        // convex and non-45-compliant

        // non-convex

        // non-convex and non-45-compliant
        poly = new Polygon(new Pt2D(10, 5),
                           new Pt2D(11, 4),
                           new Pt2D(12, 8),
                           new Pt2D(16, 5),
                           new Pt2D(16, 8),
                           new Pt2D(17, 10),
                           new Pt2D(12, 11),
                           new Pt2D(15, 13),
                           new Pt2D(11, 14),
                           new Pt2D(9, 9));
        assertEquals(WindingDir.CCW, poly.getWindingDir());
        assertEquals(WindingDir.CW, poly.reverseWinding().getWindingDir());

        // this one failed with the old sum-of-angles based method
        poly = new Polygon(new Pt2D(4, 9),
                           new Pt2D(8, 9),
                           new Pt2D(8, 13),
                           new Pt2D(8, 15),
                           new Pt2D(4, 15),
                           new Pt2D(2, 13),
                           new Pt2D(2, 10),
                           new Pt2D(2, 8),
                           new Pt2D(3, 9),
                           new Pt2D(3, 10),
                           new Pt2D(3, 11),
                           new Pt2D(3, 12),
                           new Pt2D(4, 13));
        assertEquals(WindingDir.CCW, poly.getWindingDir());
        assertEquals(WindingDir.CW, poly.reverseWinding().getWindingDir());
        
    }
    
}
