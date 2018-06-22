package info.bschambers.gridgeom;

import org.junit.Test;
import static org.junit.Assert.*;

public class LineTest {

    @Test
    public void testGetIntersectionPoint45() {

        // horiz/vert
        assertEquals(new Pt2Df(5f, 7f),
                     new Line(3, 7, 11, 7).getIntersectionPoint45(new Line(5, 2, 5, 3)));

        // vert/horiz

    }

    @Test
    public void testContains45() {
        // horiz
        assertFalse(new Line(1, 4, 9, 4).contains45(new Pt2D(10, 4)));
        assertTrue(new Line(1, 4, 9, 4).contains45(new Pt2D(9, 4)));
        assertTrue(new Line(1, 4, 9, 4).contains45(new Pt2D(3, 4)));
        // vert
        assertFalse(new Line(-7, 4, -7, 13).contains45(new Pt2D(-7, 3)));
        assertTrue(new Line(-7, 4, -7, 13).contains45(new Pt2D(-7, 4)));
        assertTrue(new Line(-7, 4, -7, 13).contains45(new Pt2D(-7, 10)));
        // diag (positive)
        assertFalse(new Line(1, 3, 5, 7).contains45(new Pt2D(6, 8)));
        assertFalse(new Line(1, 3, 5, 7).contains45(new Pt2D(0, 4))); // diag (negative)
        assertTrue(new Line(1, 3, 5, 7).contains45(new Pt2D(5, 7)));
        assertTrue(new Line(1, 3, 5, 7).contains45(new Pt2D(2, 4)));

        // TODO: diag (negative)
        
    }
    
    @Test
    public void testContains() {
        // horiz
        assertFalse(new Line(1, 4, 9, 4).contains(new Pt2D(10, 4)));
        assertTrue(new Line(1, 4, 9, 4).contains(new Pt2D(9, 4)));
        assertTrue(new Line(1, 4, 9, 4).contains(new Pt2D(3, 4)));
        // vert
        assertFalse(new Line(-7, 4, -7, 13).contains(new Pt2D(-7, 3)));
        assertTrue(new Line(-7, 4, -7, 13).contains(new Pt2D(-7, 4)));
        assertTrue(new Line(-7, 4, -7, 13).contains(new Pt2D(-7, 10)));
        // diag (positive)
        assertFalse(new Line(1, 3, 5, 7).contains(new Pt2D(6, 8)));
        assertFalse(new Line(1, 3, 5, 7).contains(new Pt2D(0, 4))); // diag (negative)
        assertTrue(new Line(1, 3, 5, 7).contains(new Pt2D(5, 7)));
        assertTrue(new Line(1, 3, 5, 7).contains(new Pt2D(2, 4)));

        // TODO: diag (negative)
        
    }
    
}
