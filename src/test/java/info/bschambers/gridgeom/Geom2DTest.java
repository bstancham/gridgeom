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

}
