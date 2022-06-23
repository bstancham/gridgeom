package info.bstancham.gridgeom;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static info.bstancham.gridgeom.Geom2D.WindingDir;

public class ShapeGroupTest {

    @Test
    public void testContains() {

        // convex polygon

        ShapeGroup sg = new ShapeGroup(new Shape45(new Pt2D(2, 4),
                                                   new Pt2D(3, 3),
                                                   new Pt2D(4, 3),
                                                   new Pt2D(8, 7),
                                                   new Pt2D(8, 9),
                                                   new Pt2D(4, 9),
                                                   new Pt2D(2, 7)));
        assertFalse(sg.contains(new Pt2D(4, 2)));
        assertFalse(sg.contains(new Pt2D(3, 9)));
        assertTrue(sg.contains(new Pt2D(4, 9)));
        assertFalse(sg.containsExcludeEdges(new Pt2D(3, 10)));
        assertTrue(sg.contains(new Pt2D(7, 7)));
        assertTrue(sg.containsExcludeEdges(new Pt2D(7, 7)));
        assertFalse(sg.contains(new Pt2D(9, 7)));
        
        // non-convex

        // non-45
        
    }

}
