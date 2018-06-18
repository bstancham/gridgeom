package info.bschambers.gridgeom;

import java.util.Set;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class Shape45Test {

    private Shape45 triangle = new Shape45(new Pt2D(0, 0),
                                           new Pt2D(2, 2),
                                           new Pt2D(0, 2));
    
    @Test
    public void testValidate() {

        // assertTrue(triangle.validate());

        // ALL ANGLES MUST BE DIVISIBLE BY 45 DEGREES

        // WINDING DIRECTION MUST BE CCW

        // OUTLINE: NO INTERSECTING EDGES

        // OUTLINE: NO SHARED VERTICES

        // HOLE: ALL EDGES MUST BE INSIDE OUTLINE

        // HOLE MAY NOT INTERSECT

    }
    
}
