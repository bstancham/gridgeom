package info.bschambers.gridgeom;

import org.junit.Test;
import static org.junit.Assert.*;

public class Geom2DTest {

    private static final double EXACT = 0.0;

    @Test
    public void testLineAngle() {
        
        // test all angles divisible by 45 degrees
        
        // ... from 0, 0 origin...
        assertEquals(0.0, Math.toDegrees(Geom2D.lineAngle(0, 0, 0, 23.7)), EXACT);
        assertEquals(45.0, Math.toDegrees(Geom2D.lineAngle(0, 0, 23.7, 23.7)), EXACT);
        assertEquals(90.0, Math.toDegrees(Geom2D.lineAngle(0, 0, 12.3, 0)), EXACT);
        assertEquals(135.0, Math.toDegrees(Geom2D.lineAngle(0, 0, 47.1, -47.1)), EXACT);
        assertEquals(180.0, Math.toDegrees(Geom2D.lineAngle(0, 0, 0, -221.4)), EXACT);
        assertEquals(225.0, Math.toDegrees(Geom2D.lineAngle(0, 0, -42.912, -42.912)), EXACT);
        assertEquals(270.0, Math.toDegrees(Geom2D.lineAngle(0, 0, -38.9, 0)), EXACT);
        assertEquals(315.0, Math.toDegrees(Geom2D.lineAngle(0, 0, -89.6, 89.6)), EXACT);

    }

    @Test
    public void testTurnDirection() {
        
        Pt2D p1 = new Pt2D(2, 1);
        Pt2D p2 = new Pt2D(6, 1);
        Pt2D p3 = new Pt2D(5, 5);
        Pt2D p4 = new Pt2D(14, 17);

        assertEquals(-1, Geom2D.turnDirection(p1, p2, p3)); // LEFT/CCW
        assertEquals(1, Geom2D.turnDirection(p1, p3, p2)); // RIGHT/CW
        assertEquals(0, Geom2D.turnDirection(p1, p3, p4)); // NO TURN
        
    }

    @Test
    public void testCollinear() {
        Pt2D col1 = new Pt2D(3, 1);
        Pt2D col2 = new Pt2D(7, 5);
        Pt2D col3 = new Pt2D(6, 4);
        Pt2D p1 = new Pt2D(6, 5);
        assertTrue(Geom2D.collinear(col1, col2, col3));
        assertFalse(Geom2D.collinear(col1, col2, p1));
    }

}
