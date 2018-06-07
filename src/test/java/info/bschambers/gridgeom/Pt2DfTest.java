package info.bschambers.gridgeom;

import java.util.Set;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class Pt2DfTest {

    @Test
    public void testEquals() {
        Pt2Df p1 = new Pt2Df(1.7f, 3.1f);
        Pt2Df p2 = new Pt2Df(1.7f, 3.1f);
        Pt2Df p3 = new Pt2Df(1.7f, 3f);
        assertFalse(p1 == p2);
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }

}
