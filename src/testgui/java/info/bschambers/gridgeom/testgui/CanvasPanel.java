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

public class CanvasPanel extends JPanel {

    private Gfx gfx;
    private Color bgColor = Color.BLACK;
    private TextBlock helpBlock = new TextBlock(10, 10);
    private Color helpColor = Color.GRAY;

    private CanvasMode[] modes;
    private int modeIndex = 0;

    private boolean showHelp = true;

    private List<KeyBinding> bindings = new ArrayList<>();
    
    private int mouseX = 0;
    private int mouseY = 0;
    private int crosshairsSize = 30;
    private Color crosshairsColor = Color.RED;
    
    public CanvasPanel(CanvasMode[] modes) {
        this.modes = modes;
        this.gfx = new Gfx(this);
        for (CanvasMode cm : modes)
            cm.init(this);

        bindings.add(new KeyBinding('h', "toggle help",
                                    () -> toggleShowHelp()));
        bindings.add(new KeyBinding.Double('-', '=',
                                           "scaling -/+",
                                           () -> gfx.incrementScaling(-1),
                                           () -> gfx.incrementScaling(1)));

        this.gfx.setScaling(29);
    }

    public void toggleShowHelp() {
        showHelp = !showHelp;
    }

    public Gfx getGfx() {
        return gfx;
    }

    public void setMouseX(int val) { mouseX = val; }
    public void setMouseY(int val) { mouseY = val; }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    public void switchMode(int num) {
        if (num <= modes.length && num > 0) {
            modeIndex = num - 1;
            System.out.println("switched to mode " + num + ": "
                               + getCurrentMode().getTitle());
        } else {
            System.out.println("Can't switch to mode "
                               + num + " choose number between 1 and "
                               + modes.length);
        }
    }

    public CanvasMode getCurrentMode() { return modes[modeIndex]; }

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
            for (KeyBinding kb : bindings)
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

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        for (KeyBinding kb : bindings)
            kb.keyTyped(c);
        
        getCurrentMode().keyTyped(e);
    }
    
}
