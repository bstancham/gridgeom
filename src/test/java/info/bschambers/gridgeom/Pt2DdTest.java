package info.bschambers.gridgeom;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Pt2DdTest {

    @Test
    public void testEquals() {
        Pt2Dd p1 = new Pt2Dd(1.7f, 3.1f);
        Pt2Dd p2 = new Pt2Dd(1.7f, 3.1f);
        Pt2Dd p3 = new Pt2Dd(1.7f, 3.11f);
        assertFalse(p1 == p2);
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }

}
