package info.bschambers.gridgeom;

import org.junit.Test;
import static org.junit.Assert.*;

public class LinefTest {

    private static final float EXACT = 0.0f;
    
    private Linef lnDiagPos1 = new Linef(1f, 4f, 5f, 8f);
    private Linef lnDiagPos2 = new Linef(3f, -6f, -9f, -18f);
    private Linef lnDiagNeg1 = new Linef(-2f, 5f, 6f, -3f);
    private Linef lnDiagNeg2 = new Linef(-1f, -11f, 6f, -18f);
    private Linef lnHoriz1 = new Linef(21f, 8f, 28f, 8f);
    private Linef lnHoriz2 = new Linef(10f, -9f, -4f, -9f);
    private Linef lnVert1 = new Linef(10f, -1f, 10f, 14f);
    private Linef lnVert2 = new Linef(7f, 5f, 7f, 1f);
    private Linef lnDegenerate1 = new Linef(5f, 6f, 5f, 6f);
    private Linef lnDegenerate2 = new Linef(0.314f, 42.7f, 0.314f, 42.7f);
    private Linef lnNon451 = new Linef(7f, -7f, 11f, -8f);
    private Linef lnNon452 = new Linef(3f, 11f, -2f, 1f);
    
    @Test
    public void testIsDegenerate() {
        assertFalse(lnDiagPos1.isDegenerate());
        assertFalse(lnDiagPos2.isDegenerate());
        assertFalse(lnDiagNeg1.isDegenerate());
        assertFalse(lnDiagNeg2.isDegenerate());
        assertFalse(lnHoriz1.isDegenerate());
        assertFalse(lnHoriz2.isDegenerate());
        assertFalse(lnVert1.isDegenerate());
        assertFalse(lnVert2.isDegenerate());
        assertTrue(lnDegenerate1.isDegenerate());
        assertTrue(lnDegenerate2.isDegenerate());
        assertFalse(lnNon451.isDegenerate());
        assertFalse(lnNon452.isDegenerate());
    }

    @Test
    public void testIsHoriz() {
        assertFalse(lnDiagPos1.isHoriz());
        assertFalse(lnDiagPos2.isHoriz());
        assertFalse(lnDiagNeg1.isHoriz());
        assertFalse(lnDiagNeg2.isHoriz());
        assertTrue(lnHoriz1.isHoriz());
        assertTrue(lnHoriz2.isHoriz());
        assertFalse(lnVert1.isHoriz());
        assertFalse(lnVert2.isHoriz());
        assertFalse(lnDegenerate1.isHoriz());
        assertFalse(lnDegenerate2.isHoriz());
        assertFalse(lnNon451.isHoriz());
        assertFalse(lnNon452.isHoriz());
    }

    @Test
    public void testIsVert() {
        assertFalse(lnDiagPos1.isVert());
        assertFalse(lnDiagPos2.isVert());
        assertFalse(lnDiagNeg1.isVert());
        assertFalse(lnDiagNeg2.isVert());
        assertFalse(lnHoriz1.isVert());
        assertFalse(lnHoriz2.isVert());
        assertTrue(lnVert1.isVert());
        assertTrue(lnVert2.isVert());
        assertFalse(lnDegenerate1.isVert());
        assertFalse(lnDegenerate2.isVert());
        assertFalse(lnNon451.isVert());
        assertFalse(lnNon452.isVert());
    }

    @Test
    public void testIsDiag45() {
        assertTrue(lnDiagPos1.isDiag45());
        assertTrue(lnDiagPos2.isDiag45());
        assertTrue(lnDiagNeg1.isDiag45());
        assertTrue(lnDiagNeg2.isDiag45());
        assertFalse(lnHoriz1.isDiag45());
        assertFalse(lnHoriz2.isDiag45());
        assertFalse(lnVert1.isDiag45());
        assertFalse(lnVert2.isDiag45());
        assertFalse(lnDegenerate1.isDiag45());
        assertFalse(lnDegenerate2.isDiag45());
        assertFalse(lnNon451.isDiag45());
        assertFalse(lnNon452.isDiag45());

        assertTrue(lnDiagPos1.isDiag45Positive());
        assertTrue(lnDiagPos2.isDiag45Positive());
        assertFalse(lnDiagNeg1.isDiag45Positive());
        assertFalse(lnDiagNeg2.isDiag45Positive());
        assertFalse(lnHoriz1.isDiag45Positive());
        assertFalse(lnHoriz2.isDiag45Positive());
        assertFalse(lnVert1.isDiag45Positive());
        assertFalse(lnVert2.isDiag45Positive());
        assertFalse(lnDegenerate1.isDiag45Positive());
        assertFalse(lnDegenerate2.isDiag45Positive());
        assertFalse(lnNon451.isDiag45Positive());
        assertFalse(lnNon452.isDiag45Positive());

        assertFalse(lnDiagPos1.isDiag45Negative());
        assertFalse(lnDiagPos2.isDiag45Negative());
        assertTrue(lnDiagNeg1.isDiag45Negative());
        assertTrue(lnDiagNeg2.isDiag45Negative());
        assertFalse(lnHoriz1.isDiag45Negative());
        assertFalse(lnHoriz2.isDiag45Negative());
        assertFalse(lnVert1.isDiag45Negative());
        assertFalse(lnVert2.isDiag45Negative());
        assertFalse(lnDegenerate1.isDiag45Negative());
        assertFalse(lnDegenerate2.isDiag45Negative());
        assertFalse(lnNon451.isDiag45Negative());
        assertFalse(lnNon452.isDiag45Negative());
    }

    @Test
    public void testIs45Compliant() {
        assertTrue(lnDiagPos1.is45Compliant());
        assertTrue(lnDiagPos2.is45Compliant());
        assertTrue(lnDiagNeg1.is45Compliant());
        assertTrue(lnDiagNeg2.is45Compliant());
        assertTrue(lnHoriz1.is45Compliant());
        assertTrue(lnHoriz2.is45Compliant());
        assertTrue(lnVert1.is45Compliant());
        assertTrue(lnVert2.is45Compliant());
        assertFalse(lnDegenerate1.is45Compliant());
        assertFalse(lnDegenerate2.is45Compliant());
        assertFalse(lnNon451.is45Compliant());
        assertFalse(lnNon452.is45Compliant());
    }
    
    @Test
    public void testSlopeIntercept() {
        assertEquals(1f, lnDiagPos1.slope(), EXACT);
        assertEquals(1f, lnDiagPos2.slope(), EXACT);
        assertEquals(-1f, lnDiagNeg1.slope(), EXACT);
        assertEquals(-1f, lnDiagNeg2.slope(), EXACT);
        assertEquals(0f, lnHoriz1.slope(), EXACT);
        assertEquals(0f, lnHoriz2.slope(), EXACT);
        assertEquals(Float.POSITIVE_INFINITY, lnVert1.slope(), EXACT);
        assertEquals(Float.POSITIVE_INFINITY, lnVert2.slope(), EXACT);
        assertEquals(-0.25f, lnNon451.slope(), EXACT);
        assertEquals(2f, lnNon452.slope(), EXACT);
        assertEquals(Float.NEGATIVE_INFINITY, lnDegenerate1.slope(), EXACT);
        assertEquals(Float.NEGATIVE_INFINITY, lnDegenerate2.slope(), EXACT);

        assertEquals(3f, lnDiagPos1.intercept(), EXACT);
        assertEquals(-9f, lnDiagPos2.intercept(), EXACT);
        assertEquals(3f, lnDiagNeg1.intercept(), EXACT);
        assertEquals(-12f, lnDiagNeg2.intercept(), EXACT);
        assertEquals(8f, lnHoriz1.intercept(), EXACT);
        assertEquals(-9f, lnHoriz2.intercept(), EXACT);
        assertEquals(Float.POSITIVE_INFINITY, lnVert1.intercept(), EXACT);
        assertEquals(Float.POSITIVE_INFINITY, lnVert2.intercept(), EXACT);
        assertEquals(-5.25f, lnNon451.intercept(), EXACT);
        assertEquals(5f, lnNon452.intercept(), EXACT);
        assertEquals(Float.NEGATIVE_INFINITY, lnDegenerate1.intercept(), EXACT);
        assertEquals(Float.NEGATIVE_INFINITY, lnDegenerate2.intercept(), EXACT);

        // some non-45 lines
        Linef l1 = new Linef(0f, 0f, 5f, 1f);
        Linef l2 = new Linef(-5f, 4f, -7f, -2f);
        assertEquals(0.2f, l1.slope(), EXACT);
        assertEquals(3f, l2.slope(), EXACT);
        assertEquals(0f, l1.intercept(), EXACT);
        assertEquals(19f, l2.intercept(), EXACT);
    }

    @Test
    public void testGetIntersectionPoint45() {
        assertEquals(new Pt2Df(0, 3), lnDiagPos1.getIntersectionPoint45(lnDiagNeg1));
        assertEquals(new Pt2Df(-1.5f, -10.5f), lnDiagNeg2.getIntersectionPoint45(lnDiagPos2));
        assertNull(lnDiagPos1.getIntersectionPoint45(lnDiagPos2)); // parallel
        assertNull(lnHoriz1.getIntersectionPoint45(lnNon451)); // non-45
        assertNull(lnNon452.getIntersectionPoint45(lnVert1)); // non-45
        
    }
    
    @Test
    public void testGetIntersectionPoint() {
        assertEquals(new Pt2Df(0, 3), lnDiagPos1.getIntersectionPoint(lnDiagNeg1));
        assertEquals(new Pt2Df(-1.5f, -10.5f), lnDiagNeg2.getIntersectionPoint(lnDiagPos2));
        assertNull(lnDiagPos1.getIntersectionPoint(lnDiagPos2)); // parallel
        assertEquals(new Pt2Df(-53f, 8f), lnHoriz1.getIntersectionPoint(lnNon451)); // non-45
        assertEquals(new Pt2Df(10f, 25f), lnNon452.getIntersectionPoint(lnVert1)); // non-45
        assertEquals(new Pt2Df(3, -6), lnNon451.getIntersectionPoint(lnDiagPos2)); // non-45
    }
    
}
