package info.bschambers.gridgeom.testgui;

import java.util.Set;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import info.bschambers.gridgeom.*;

public class IntersectShapesMode extends DisplayShapeMode {

    public IntersectShapesMode(ShapeSet[] shapeSets) {
        super(2, shapeSets);
    }
    
}
