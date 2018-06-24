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
        Pt2D col2 = new Pt2D(6, 4);
        Pt2D col3 = new Pt2D(7, 5);
        Pt2D p1 = new Pt2D(6, 5);
        // should pass whatever order points are given in
        assertTrue(Geom2D.collinear(col1, col2, col3));
        assertTrue(Geom2D.collinear(col3, col2, col1));
        assertTrue(Geom2D.collinear(col2, col3, col1));
        assertFalse(Geom2D.collinear(col1, col2, p1));
        // vertical line
        Pt2D vert1 = new Pt2D(-3, 34);
        Pt2D vert2 = new Pt2D(-3, 32);
        Pt2D vert3 = new Pt2D(-3, -7);
        assertTrue(Geom2D.collinear(vert1, vert2, vert3));
        assertTrue(Geom2D.collinear(vert3, vert2, vert1));
        assertTrue(Geom2D.collinear(vert2, vert3, vert1));
        assertFalse(Geom2D.collinear(vert1, vert2, p1));
    }

    @Test
    public void testDist() {

        // points on vertical line
        Pt2Df vert1 = new Pt2Df (21, 17);
        Pt2Df vert2 = new Pt2Df (21, 12);
        Pt2Df vert3 = new Pt2Df (21, 6);
        Pt2Df vert4 = new Pt2Df (21, 3);
        assertEquals(5.0, Geom2D.dist(vert1, vert2), EXACT);
        assertEquals(25.0, Geom2D.distSquared(vert1, vert2), EXACT);
        assertEquals(14.0, Geom2D.dist(vert4, vert1), EXACT);
        assertEquals(196.0, Geom2D.distSquared(vert4, vert1), EXACT);

        // diagonal
        Pt2Df diag1 = new Pt2Df (21, 17);
        Pt2Df diag2 = new Pt2Df (24, 21);
        assertEquals(5.0, Geom2D.dist(diag1, diag2), EXACT); // dist: x=3, y=4
        assertEquals(25.0, Geom2D.distSquared(diag1, diag2), EXACT);
        // diagonal (the other way)
        diag1 = new Pt2Df (-3, 13);
        diag2 = new Pt2Df (1, 10);
        assertEquals(5.0, Geom2D.dist(diag1, diag2), EXACT);
        assertEquals(25.0, Geom2D.distSquared(diag1, diag2), EXACT);
    }
    
}
