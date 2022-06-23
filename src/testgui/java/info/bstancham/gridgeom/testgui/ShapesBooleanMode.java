package info.bstancham.gridgeom.testgui;

import java.util.Set;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import info.bstancham.gridgeom.*;

public class ShapesBooleanMode extends ShapeEditMode {

    private enum Operation {
        DIGRAPH,
        UNION,
        SUBTRACTION_1,
        SUBTRACTION_2,
        INTERSECTION
    };

    private Operation op = Operation.DIGRAPH;

    private boolean showIntersectionPoints = true;
    private boolean showResult = true;
    private boolean showDigraph = true;

    private TextBlock text = new TextBlock(500, 10);
    private Color textCol = Color.GRAY;
    
    @Override
    protected void initLocal() {
        super.initLocal();
    }

    private ShapeGroup shape1() {
        return getCanvas().getSlot(0).shape();
    }
    
    private ShapeGroup shape2() {
        return getCanvas().getSlot(1).shape();
    }

    @Override
    public void paint(Graphics g) {
        if (showGrid)
            getCanvas().paintGrid(g);
        getCanvas().paintCenter(g);

        text.clear();
        text.add(Color.CYAN, "OPERATION: " + op);
        
        if (showDigraph)
            paintDigraph(g);
        
        text.paint(g);
    }

    public void paintDigraph(Graphics g) {
        Digraph2D graph = new Digraph2D();

        int id1 = 1;
        int id2 = 2;

        addEdges(graph, shape1(), id1);
        addEdges(graph, shape2(), id2);

        text.add(Color.GRAY, "DIGRAPH: " + graph.getNumNodes() + " nodes");

        gfx().digraph(g, graph);
    }

    private void addEdges(Digraph2D graph, ShapeGroup sg, int shapeID) {
        for (int i = 0; i < sg.getNumEdges(); i++)
            graph.addLine(sg.getEdge(i), shapeID);
    }
    
}
