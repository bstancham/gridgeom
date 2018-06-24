package info.bschambers.gridgeom;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * <pre>
 * <code>
 *   Y                 c2
 * 18+ . . . . . . . . + . . . . . .
 * 17+ . . . d1. . . . | . . . . . d2
 * 16+ . . . +---------+-----------+
 * 15+ . . . . . . . . | . . . . . .
 * 14+ . . . . . . . . | e1. . . . .
 * 13+ . a1. . . . . . | a4. . . e2.
 * 12+ . +-------------+-+-------+ .
 * 11+ . | . . . . . . | | . . . . .
 * 10+ . | . . . . . . | |f1 . . . f2
 * 9 + . | . b4. . b3. | +---------+
 * 8 + . | . +-----+ . | | . . . . .
 * 7 + . | . | . . | . | | . . . . .
 * 6 + . +---+-----+---+-+ . . . . .
 * 5 + . a2. | . . | . | a3. . . . .
 * 4 + . . . | . . | . | . . . . . .
 * 3 + . . . +-----+ . | . . . . . .
 * 2 + . . . b1. . b2. + . . . . . .
 * 1 + . . . . . . . . c1. . . . . .
 * 0 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ X
 *   0 1 2 3 4 5 6 7 8 9 10        15
 * </code>
 * </pre>
 *
 */
public class Digraph2DTest {
    
    // shape A (rectangle)
    private int idShapeA = 1;
    private Pt2Df a1 = new Pt2Df (2, 12);
    private Pt2Df a2 = new Pt2Df (2, 6);
    private Pt2Df a3 = new Pt2Df (10, 6);
    private Pt2Df a4 = new Pt2Df (10, 12);

    // shape B (rectangle)
    private int idShapeB = 2;
    private Pt2Df b1 = new Pt2Df (4, 3);
    private Pt2Df b2 = new Pt2Df (7, 3);
    private Pt2Df b3 = new Pt2Df (7, 8);
    private Pt2Df b4 = new Pt2Df (4, 8);

    // shape C (line)
    private int idShapeC = 1;
    private Pt2Df c1 = new Pt2Df (9, 2);
    private Pt2Df c2 = new Pt2Df (9, 18);
    
    // shape D (line)
    private int idShapeD = 1;
    private Pt2Df d1 = new Pt2Df (4, 16);
    private Pt2Df d2 = new Pt2Df (15, 16);
    
    // shape E (line)
    private int idShapeE = 1;
    private Pt2Df e1 = new Pt2Df (10, 12);
    private Pt2Df e2 = new Pt2Df (14, 12);

    // shape F (line)
    private int idShapeF = 1;
    private Pt2Df f1 = new Pt2Df (10, 9);
    private Pt2Df f2 = new Pt2Df (15, 9);

    private void addShapeA(Digraph2D dg) {
        dg.addLine(a1, a2, idShapeA);
        dg.addLine(a2, a3, idShapeA);
        dg.addLine(a3, a4, idShapeA);
        dg.addLine(a4, a1, idShapeA);
    }

    @Test
    public void testAddAndRemoveNodes() {
        Digraph2D dg = new Digraph2D();

        dg.addLine(a1, a2, idShapeA);
        dg.addLine(b1, b2, idShapeA);
        assertEquals(4, dg.getNumNodes());
        assertTrue(dg.isConnected(a1, a2));
        assertTrue(dg.isConnectedBackward(a2, a1));
        assertEquals(1, dg.getNode(a1).getNumConnectionsForward());

        assertTrue(dg.remove(a2));
        assertEquals(3, dg.getNumNodes());
        assertEquals(0, dg.getNode(a1).getNumConnectionsForward());

        assertFalse(dg.isConnected(a1, a2));
        assertFalse(dg.isConnectedBackward(a2, a1));

        ///

        dg.addLine(a2, a3, idShapeA);
        assertTrue(dg.isConnected(a2, a3));

    }

    @Test
    public void testAddIntersectingLine() {
        Digraph2D dg = new Digraph2D();

        // horizontal line
        dg.addLine(a2, a3, idShapeA);
        assertEquals(2, dg.getNumNodes());
        assertTrue(dg.isConnected(a2, a3));

        // add intersecting vertical line
        dg.addLine(b2, b3, idShapeB);

        // node should have been added at intersection point
        Pt2Df iPt = new Pt2Df(7, 6);
        assertTrue(dg.contains(iPt));
        assertEquals(5, dg.getNumNodes());

        // a1 & a2 no longer directly connected
        assertFalse(dg.isConnected(a2, a3));
        assertFalse(dg.isConnected(b2, b3));

        // new connections should exist with intersection point
        assertTrue(dg.isConnected(a2, iPt));
        assertTrue(dg.isConnected(iPt, a3));
        assertTrue(dg.isConnected(b2, iPt));
        assertTrue(dg.isConnected(iPt, b3));



        // ADD LINE WITH MULTIPLE INTERSECTIONS
        
        dg = new Digraph2D();
        dg.addLine(a2, a3, idShapeA);
        dg.addLine(a4, a1, idShapeA);
        dg.addLine(d1, d2, idShapeD);
        assertEquals(6, dg.getNumNodes());
        assertTrue(dg.isConnected(a2, a3));
        assertTrue(dg.isConnected(a4, a1));
        assertTrue(dg.isConnected(d1, d2));

        // add line to make multiple intersections
        dg.addLine(c1, c2, idShapeC);
        
        // three new nodes should exist at intersections
        // ... plus the two end points (total of 5 new nodes)
        assertEquals(11, dg.getNumNodes());
        
        Pt2Df iPt1 = new Pt2Df(9, 6); 
        Pt2Df iPt2 = new Pt2Df(9, 12); 
        Pt2Df iPt3 = new Pt2Df(9, 16);
        assertTrue(dg.contains(iPt1));
        assertTrue(dg.contains(iPt2));
        assertTrue(dg.contains(iPt3));

        // check that the correct connections are present
        assertFalse(dg.isConnected(a2, a3));
        assertTrue(dg.isConnected(a2, iPt1));
        assertTrue(dg.isConnected(iPt1, a3));
        
        assertFalse(dg.isConnected(a4, a1));
        assertTrue(dg.isConnected(a4, iPt2));
        assertTrue(dg.isConnected(iPt2, a1));
        
        assertFalse(dg.isConnected(d1, d2));
        assertTrue(dg.isConnected(d1, iPt3));
        assertTrue(dg.isConnected(iPt3, d2));
       
        assertFalse(dg.isConnected(c1, c2));
        assertTrue(dg.isConnected(c1, iPt1));
        assertTrue(dg.isConnected(iPt1, iPt2));
        assertTrue(dg.isConnected(iPt2, iPt3));
        assertTrue(dg.isConnected(iPt3, c2));



        // END-POINT OF ONE LINE INTERSECTS OTHER LINE

        dg = new Digraph2D();
        dg.addLine(a4, a3, idShapeA);
        assertEquals(2, dg.getNumNodes());
        assertTrue(dg.isConnected(a4, a3));
        
        dg.addLine(f1, f2, idShapeF);
        assertEquals(4, dg.getNumNodes());
        assertTrue(dg.isConnected(f1, f2));
        assertFalse(dg.isConnected(a4, a3));
        Pt2Df iPt4 = new Pt2Df(10, 9);
        assertTrue(dg.isConnected(a4, iPt4));
        assertTrue(dg.isConnected(iPt4, a3));

        // same, but the end-point is in the first line rather than second...
        
        dg = new Digraph2D();
        dg.addLine(f1, f2, idShapeF);
        assertEquals(2, dg.getNumNodes());
        assertTrue(dg.isConnected(f1, f2));

        
        dg.addLine(a4, a3, idShapeA);
        assertFalse(dg.isConnected(a4, a3));
        
        // assertEquals(4, dg.getNumNodes());
        // assertFalse(dg.isConnected(a4, a3));
        // assertTrue(dg.isConnected(a4, iPt4));
        // assertTrue(dg.isConnected(iPt4, a3));




        // SHARED END POINTS
        
        
    }

    // @Test
    // public void testNodeConnection() {
    //     Digraph2D dg = new Digraph2D();
    //     Digraph2D.Node n1 = new dg.Node(a1, idShapeA);
    //     Digraph2D.Node n2 = new dg.Node(a2, idShapeA);
    //     // no connections should exist
    //     assertEquals(1, n1.getNumConnectionsForward());
    //     assertEquals(1, n2.getNumConnectionsForward());
    // }

    @Test
    public void testAddLine() {
        Digraph2D dg = new Digraph2D();

        // empty digraph should have no nodes
        assertEquals(0, dg.getNumNodes());

        // add a line and check that nodes have been added
        dg.addLine(a1, a2, idShapeA);
        assertEquals(2, dg.getNumNodes());
        // check that nodes have been added
        assertTrue(dg.contains(a1)); // start point of line
        assertTrue(dg.contains(a2)); // end point of line
        assertTrue(dg.contains(new Pt2Df(2, 12))); // should be same as a1
        // check that false-positive is not returned
        assertFalse(dg.contains(a3));
        // check that nodes contain correct co-ordinates
        assertEquals(a1, dg.getNode(a1).getPoint());
        assertEquals(new Pt2Df(2, 12), dg.getNode(a1).getPoint());
        // getNode() should return null if non-existent
        assertNull(dg.getNode(a3));

        // test that correct connections have been added
        assertTrue(dg.isConnected(a1, a2));
        assertFalse(dg.isConnected(a2, a1));
        assertFalse(dg.isConnected(a1, a1)); // shouldn't be connected to self
        assertFalse(dg.isConnectedBackward(a1, a2));
        assertTrue(dg.isConnectedBackward(a2, a1));


        // // add next line
        // dg.addLine(a2, a3, idShapeB);
        // assertEquals(3, dg.getNumNodes()); // should be one new node only
        // assertTrue(dg.containsNode(a3));
        // assertEquals(a3, dg.getNode(a3).getPoint());



    }

}
