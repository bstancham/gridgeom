package info.bschambers.gridgeom.testgui;

import info.bschambers.gridgeom.*;

public class App {

    private static void createAndShowGUI() {

        CanvasMode[] modes = new CanvasMode[] {
            new LineAngleMode(),
            new IntersectLinesMode(),
            new DisplayShapesMode(),
        };

        TestFrame frame
            = new TestFrame("grid-triangulation interactive tester",
                            modes, makeShapeSets());
        frame.showFrame(0, 0, 1200, 800);
    }

    public static void main(String[] args) {
        System.out.println("running interactive testing app...");
        // Schedule a job for the event-dispatching thread:
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() { createAndShowGUI(); }
            });
    }



    /*--------------------------- SHAPE SETS ---------------------------*/

    private static ShapeSet[] makeShapeSets() {
        return new ShapeSet[] {
            makeRectilinearShapes(),
            makeMultipleShapes(),
            makePerforatedShapes(),
            makeFaultyShapes(),
        };
    }

    private static ShapeSet makeRectilinearShapes() {
        return new ShapeSet("simple rectilinear",

                            new ShapeWrapper("rectangle 3x5",
                                             new Shape45(new Pt2D(0, 0),
                                                         new Pt2D(3, 0),
                                                         new Pt2D(3, 5),
                                                         new Pt2D(0, 5))),

                            new ShapeWrapper("triangle",
                                             new Shape45(new Pt2D(0, 1),
                                                         new Pt2D(0, 4),
                                                         new Pt2D(3, 1))),

                            // convex (simple)
        
                            new ShapeWrapper("convex polygon 1",
                                             new Shape45(new Pt2D(0, 1),
                                                         new Pt2D(0, 4),
                                                         new Pt2D(2, 6),
                                                         new Pt2D(6, 6),
                                                         new Pt2D(6, 4),
                                                         new Pt2D(2, 0),
                                                         new Pt2D(1, 0))),
        
                            // non-convex (simple)
        
                            new ShapeWrapper("rectilinear 1",
                                             new ShapeGroup(new Shape45(new Pt2D(0, 0),
                                                                        new Pt2D(5, 0),
                                                                        new Pt2D(5, 2),
                                                                        new Pt2D(3, 2),
                                                                        new Pt2D(3, 3),
                                                                        new Pt2D(7, 3),
                                                                        new Pt2D(7, 1),
                                                                        new Pt2D(8, 1),
                                                                        new Pt2D(8, 5),
                                                                        new Pt2D(0, 5)))),
                       
                            new ShapeWrapper("non-convex polygon 1",
                                             new Shape45(new Pt2D(0, 0),
                                                         new Pt2D(0, 1),
                                                         new Pt2D(2, 1),
                                                         new Pt2D(2, 6),
                                                         new Pt2D(6, 6),
                                                         new Pt2D(6, 0),
                                                         new Pt2D(5, 0),
                                                         new Pt2D(5, 4),
                                                         new Pt2D(3, 4),
                                                         new Pt2D(3, 0))),
        
                            new ShapeWrapper("non-convex polygon (cross 1)",
                                             new Shape45(new Pt2D(2, 0),
                                                         new Pt2D(4, 0),
                                                         new Pt2D(4, 2),
                                                         new Pt2D(6, 2),
                                                         new Pt2D(6, 4),
                                                         new Pt2D(4, 4),
                                                         new Pt2D(4, 6),
                                                         new Pt2D(2, 6),
                                                         new Pt2D(2, 4),
                                                         new Pt2D(0, 4),
                                                         new Pt2D(0, 2),
                                                         new Pt2D(2, 2))),
        
                            new ShapeWrapper("non-convex polygon (cross 2)",
                                             new Shape45(new Pt2D(2, 0),
                                                         new Pt2D(3, 0),
                                                         new Pt2D(3, 3),
                                                         new Pt2D(6, 3),
                                                         new Pt2D(6, 4),
                                                         new Pt2D(3, 4),
                                                         new Pt2D(3, 6),
                                                         new Pt2D(2, 6),
                                                         new Pt2D(2, 4),
                                                         new Pt2D(0, 4),
                                                         new Pt2D(0, 3),
                                                         new Pt2D(2, 3)))
        
                            );
    }

    private static ShapeSet makeMultipleShapes() {
        return new ShapeSet("multiple",
                            
                            new ShapeWrapper("multiple 1",
                                             new ShapeGroup(new Shape45(new Pt2D(0, 0),
                                                                        new Pt2D(0, 2),
                                                                        new Pt2D(3, 2),
                                                                        new Pt2D(3, 0)),
                                                            new Shape45(new Pt2D(0, 3),
                                                                        new Pt2D(0, 5),
                                                                        new Pt2D(3, 5),
                                                                        new Pt2D(3, 3)))),

                            new ShapeWrapper("chequer board",
                                             new ShapeGroup(new Shape45(new Pt2D(0, 0),
                                                                        new Pt2D(2, 0),
                                                                        new Pt2D(2, 2),
                                                                        new Pt2D(0, 2)),
                                                            new Shape45(new Pt2D(4, 0),
                                                                        new Pt2D(6, 0),
                                                                        new Pt2D(6, 2),
                                                                        new Pt2D(4, 2)),
                                                            new Shape45(new Pt2D(2, 2),
                                                                        new Pt2D(4, 2),
                                                                        new Pt2D(4, 4),
                                                                        new Pt2D(2, 4)),
                                                            new Shape45(new Pt2D(6, 2),
                                                                        new Pt2D(8, 2),
                                                                        new Pt2D(8, 4),
                                                                        new Pt2D(6, 4)),
                                                            new Shape45(new Pt2D(0, 4),
                                                                        new Pt2D(2, 4),
                                                                        new Pt2D(2, 6),
                                                                        new Pt2D(0, 6)),
                                                            new Shape45(new Pt2D(4, 4),
                                                                        new Pt2D(6, 4),
                                                                        new Pt2D(6, 6),
                                                                        new Pt2D(4, 6)),
                                                            new Shape45(new Pt2D(2, 6),
                                                                        new Pt2D(4, 6),
                                                                        new Pt2D(4, 8),
                                                                        new Pt2D(2, 8)),
                                                            new Shape45(new Pt2D(6, 6),
                                                                        new Pt2D(8, 6),
                                                                        new Pt2D(8, 8),
                                                                        new Pt2D(6, 8))))

                            );
    }

    private static ShapeSet makePerforatedShapes() {
        return new ShapeSet("perforated",
        
                            new ShapeWrapper("perforated square 1",
                                             new Shape45(new Shape45(new Pt2D(1, 4),
                                                                     new Pt2D(1, 5),
                                                                     new Pt2D(3, 5),
                                                                     new Pt2D(3, 4)),
                                                         new Pt2D(0, 0),
                                                         new Pt2D(6, 0),
                                                         new Pt2D(6, 6),
                                                         new Pt2D(0, 6))),
        
                            new ShapeWrapper("perforated square 2",
                                             new Shape45(new Shape45[] {
                                                     new Shape45(new Pt2D(1, 4),
                                                                 new Pt2D(1, 5),
                                                                 new Pt2D(3, 5),
                                                                 new Pt2D(3, 4)),
                                                     new Shape45(new Pt2D(4, 1),
                                                                 new Pt2D(4, 5),
                                                                 new Pt2D(5, 5),
                                                                 new Pt2D(5, 1)),
                                                 },
                                                 new Pt2D(0, 0),
                                                 new Pt2D(6, 0),
                                                 new Pt2D(6, 6),
                                                 new Pt2D(0, 6))),
                            
                            new ShapeWrapper("nested boxes 1",
                                             new ShapeGroup(new Shape45(new Shape45(new Shape45(new Shape45(new Pt2D(5, 10),
                                                                                                            new Pt2D(8, 10),
                                                                                                            new Pt2D(8, 5),
                                                                                                            new Pt2D(5, 5)),
                                                                                                new Pt2D(4, 4),
                                                                                                new Pt2D(9, 4),
                                                                                                new Pt2D(9, 11),
                                                                                                new Pt2D(4, 11)),
                                                                                    new Pt2D(3, 12),
                                                                                    new Pt2D(10, 12),
                                                                                    new Pt2D(10, 3),
                                                                                    new Pt2D(3, 3)),
                                                                        new Pt2D(2, 2),
                                                                        new Pt2D(11, 2),
                                                                        new Pt2D(11, 13),
                                                                        new Pt2D(2, 13))))

                            );


        
    }
    
    private static ShapeSet makeFaultyShapes() {
        return new ShapeSet("faulty",

                            new ShapeWrapper("rectangle 3x5 (bad winding)",
                                             new Shape45(new Pt2D(0, 0),
                                                         new Pt2D(0, 5),
                                                         new Pt2D(3, 5),
                                                         new Pt2D(3, 0))),
        
                            new ShapeWrapper("faulty cross",
                                             new Shape45(new Pt2D(2, 0),
                                                         new Pt2D(3, 0),
                                                         new Pt2D(3, 3),
                                                         new Pt2D(6, 3),
                                                         new Pt2D(4, 4),
                                                         new Pt2D(3, 4),
                                                         new Pt2D(3, 6),
                                                         new Pt2D(2, 6),
                                                         new Pt2D(2, 4),
                                                         new Pt2D(0, 4),
                                                         new Pt2D(0, 3),
                                                         new Pt2D(2, 3)))
                       
                            );
    }
    
}
