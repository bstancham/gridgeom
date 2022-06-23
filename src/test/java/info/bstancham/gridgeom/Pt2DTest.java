package info.bstancham.gridgeom;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Pt2DTest {

    @Test
    public void testEquals() {
        Pt2D p1 = new Pt2D(-17, 31);
        Pt2D p2 = new Pt2D(-17, 31);
        Pt2D p3 = new Pt2D(-17, 32);
        assertFalse(p1 == p2);
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testEqualsValue() {
        // equalsValue should be interoperable between Pt2D, Pt2Df & Pt2Dd
        assertTrue(new Pt2D(78, 321).equalsValue(new Pt2Df(78.0f, 321.0f)));
        assertTrue(new Pt2D(78, 321).equalsValue(new Pt2Dd(78.0, 321.0)));
        assertFalse(new Pt2D(78, 321).equalsValue(new Pt2Df(78.1f, 321.0f)));
        assertFalse(new Pt2D(78, 321).equalsValue(new Pt2Dd(78.0, 320.999)));
    }

}
