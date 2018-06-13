package info.bschambers.gridgeom.testgui;

import java.util.Set;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import info.bschambers.gridgeom.*;

public class ValidateShapeMode extends DisplayShapesMode {

    private TextBlock text = new TextBlock(500, 10);

    // public ValidateShapeMode(ShapeSet[] shapeSets) {
    //     super(1, shapeSets);
    //     // text.setBGColor(Color.RED);
    // }

    @Override
    public void update() {
        super.update();
        ShapeWrapper ds = slot().wrapper();
        if (slot().shape().isValid())
            ds.setColor(Color.GREEN);
        else
            ds.setColor(Color.RED);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Color textCol = Color.GRAY;

        text.clear();
        text.add(textCol, "NAME: " + slot().wrapper().name());
        text.addIfElse(slot().shape().isValid(),
                       Color.GREEN, "VALIDATION: PASSED",
                       Color.RED,   "VALIDATION: FAILED");

        // EACH SUB-SHAPE
        int n = 1;
        for (Shape45 s : slot().shape()) {

            Color col = textCol;
            text.addSeparator(col);
            text.add(col, "OUTLINE " + n++);

            // 45 DEGREE ANGLES
            text.addIfElse(s.is45Compliant(),
                           textCol, "45 DEGREE RULE: passed",
                           Color.RED,  "45 DEGREE RULE: FAILED");
            
            // WINDING DIRECTION
            String str = "WINDING: ";
            if (s.isCCWWinding()) {
                str += "CCW";
            } else if (s.isCWWinding()) {
                str += "CW";
                col = Color.PINK;
            } else {
                str += "unknown";
                col = Color.RED;
            }
            text.add(col, str
                     + " --- sum of angles: " + Math.toDegrees(s.getSumAngles())
                     + " degrees (" + s.getSumAngles() + ")");

            // DUPLICATE VERTICES
            int numDuplicates = s.getNumDuplicateVertices();
            text.addIfElse(numDuplicates == 0,
                           textCol, "DUPLICATE VERTICES: " + numDuplicates,
                           Color.RED,  "DUPLICATE VERTICES: " + numDuplicates);

            // EDGE INTERSECTIONS
            numDuplicates = s.getNumEdgeIntersections();
            text.addIfElse(numDuplicates == 0,
                           textCol, "EDGE INTERSECTIONS: " + numDuplicates,
                           Color.RED,  "EDGE INTERSECTIONS: " + numDuplicates);

            text.add(textCol, "NUM SUB-SHAPES: " + s.getNumSubShapes());

        }
        
        text.paint(g);
        
    }
    
}
