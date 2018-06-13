package info.bschambers.gridgeom.testgui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

import info.bschambers.gridgeom.ShapeGroup;

public class CanvasPanel extends JPanel {

    private Gfx gfx;
    private Color bgColor = Color.BLACK;
    private TextBlock helpBlock = new TextBlock(10, 10);
    private int modeIndex = 0;
    private boolean showHelp = true;
    private Color helpColor = Color.GRAY;

    private KbdMode keys = new KbdMode();
    
    private int mouseX = 0;
    private int mouseY = 0;
    private int crosshairsSize = 30;
    private Color crosshairsColor = Color.RED;

    private int gridSizeX = 40;
    private int gridSizeY = 20;
    
    private CanvasMode[] modes;

    private ShapeSlot[] shapeSlots;
    private int shapeSlotIndex = 0;

    public CanvasPanel(int numSlots,
                       ShapeSet[] shapeSets,
                       CanvasMode[] modes) {
        
        buildShapeSlots(numSlots, shapeSets);
        this.modes = modes;
        this.gfx = new Gfx(this);
        for (CanvasMode cm : modes)
            cm.init(this);

        keys.add('h',"toggle help",
                 () -> toggleShowHelp());
        
        keys.add('-', '=',
                 () -> "scaling -/+",
                 () -> gfx.incrementScaling(-1),
                 () -> gfx.incrementScaling(1));

        keys.add('u', 'i',
                 () -> "prev/next shape-set (" + (1 + slot().shapeSetIndex()) +
                 " of " + slot().numShapeSets() + " --> " + shapeSet().name() + ")",
                 () -> previousShapeSet(),
                 () -> nextShapeSet());

        keys.add('o', 'p',
                 () -> "prev/next shape (" + (1 + shapeSet().index()) +
                 " of " + shapeSet().size() + " --> " + slot().wrapper().name() + ")",
                 () -> previousShape(),
                 () -> nextShape());
    
        if (numSlots > 1)
            keys.add('s',
                     () -> "switch shape-slot (" + (shapeSlotIndex + 1) +
                     " of " + shapeSlots.length + ")",
                     () -> switchShapeSlot());

        this.gfx.setScaling(29);
    }


    
    /*------------------------ SLOTS AND SHAPES ------------------------*/

    private void buildShapeSlots(int numSlots, ShapeSet[] shapeSets) {
        // init
        shapeSlots = new ShapeSlot[numSlots];
        for (int i = 0; i < numSlots; i++) {
            ShapeSet[] shapeSetsCopy = new ShapeSet[shapeSets.length];
            for (int ii = 0; ii < shapeSets.length; ii++)
                shapeSetsCopy[ii] = shapeSets[ii].copy();
            shapeSlots[i] = new ShapeSlot(shapeSetsCopy);
        }
        // transpose each shape-set
        int yStep = 12;
        int y = (numSlots > 1 ?
                 (yStep * numSlots) / 2 :
                 0);
        for (int i = 0; i < numSlots; i++) {
            int yOffset =  y - (i * yStep);
            shapeSlots[i].translate(0, yOffset);
        }
    }

    /**
     * @return The currently selected {@code ShapeSlot}.
     */
    public ShapeSlot slot() {
        return shapeSlots[shapeSlotIndex];
    }

    public ShapeSlot[] getShapeSlots() {
        return shapeSlots;
    }

    protected ShapeSet shapeSet() {
        return slot().shapeSet();
    }

    private void switchShapeSlot() {
        shapeSlotIndex++;
        if (shapeSlotIndex >= shapeSlots.length)
            shapeSlotIndex = 0;
        getCurrentMode().setKeyCursorPos(slot().wrapper().getPosX(),
                                         slot().wrapper().getPosY());
    }

    private void previousShapeSet() {
        slot().previousShapeSet();
    }

    private void nextShapeSet() {
        slot().nextShapeSet();
    }

    private void previousShape() {
        slot().shapeSet().previousShape();
    }

    private void nextShape() {
        slot().shapeSet().nextShape();
    }

    

    /*------------------------------------------------------------------*/

    public void toggleShowHelp() {
        showHelp = !showHelp;
    }

    public Gfx gfx() {
        return gfx;
    }

    public void setMouseX(int val) { mouseX = val; }
    public void setMouseY(int val) { mouseY = val; }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    /**
     * @param num Number from 1 to 9.
     */
    public void switchMode(int num) {
        if (num <= modes.length && num > 0) {
            modeIndex = num - 1;
            System.out.println("switch to mode " + num + ": "
                               + getCurrentMode().getTitle());
        } else {
            System.out.println("Can't switch to mode "
                               + num + " choose number between 1 and "
                               + modes.length);
        }
    }

    public CanvasMode getCurrentMode() {
        return modes[modeIndex];
    }

    public void update() {
        getCurrentMode().update();
    }

    @Override
    public void paintComponent(Graphics g) {
	clearDisplay(g);
        getCurrentMode().paint(g);

        helpBlock.clear();
        helpBlock.setColor(helpColor);
        if (showHelp) {
            helpBlock.add("MODE: " + getCurrentMode().getTitle());
            helpBlock.add("ESC : quit");
            helpBlock.add("1-9 : switch mode");
            helpBlock.add("cursor-keys : move");
            for (KeyBinding kb : keys.getKeyBindings())
                helpBlock.add(kb.getHelpText());
            for (KeyBinding kb : getCurrentMode().getKeyBindings())
                helpBlock.add(kb.getHelpText());
        } else {
            helpBlock.add("press 'h' to show help");
        }
        helpBlock.paint(g);
        
        crosshairs(g, crosshairsColor, mouseX, mouseY, crosshairsSize);
    }

    public void clearDisplay(Graphics g) {
        g.setColor(bgColor);
        g.fillRect(0, 0, getSize().width, getSize().height);
    }

    public void crosshairs(Graphics g, Color col, int x, int y, int size) {
        g.setColor(col);
        int half = size / 2;
        g.drawLine(x - half, y, x + half, y);
        g.drawLine(x, y - half, x, y + half);
    }

    public boolean keyTyped(KeyEvent e) {

        // if mode doesn't overshadow this keybinding then handle it locally
        if (getCurrentMode().keyTyped(e))
            return true;
        else
            return keys.keyTyped(e);
    }

    public void paintGrid(Graphics g) {
        g.setColor(Color.BLUE);
        gfx().grid(g, gridSizeX, gridSizeY);
    }

}
