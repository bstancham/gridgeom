package info.bstancham.gridgeom.testgui;

import info.bstancham.gridgeom.*;

public class ShapeSet {

    private String name;
    private ShapeWrapper[] wrappers;
    private int index = 0;

    public ShapeSet(String name, ShapeWrapper ... wrappers) {
        this.name = name;
        this.wrappers = wrappers;
    }

    public String name() { return name; }

    public ShapeWrapper current() { return wrappers[index]; }

    public ShapeWrapper[] all() { return wrappers; }

    public int index() { return index; }

    public int size() { return wrappers.length; }

    public void previousShape() {
        index--;
        if (index < 0)
            index = wrappers.length - 1;
    }

    public void nextShape() {
        index++;
        if (index >= wrappers.length)
            index = 0;
    }

    public ShapeSet copy() {
        ShapeWrapper[] sws = new ShapeWrapper[wrappers.length];
        for (int i = 0; i < wrappers.length; i++)
            sws[i] = wrappers[i].copy();
        return new ShapeSet(name, sws);
    }
    
}
