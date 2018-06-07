package info.bschambers.gridgeom.testgui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.*;
import java.awt.Component;
import java.awt.BorderLayout;

public class TestFrame extends JFrame
    implements MouseListener,
               MouseMotionListener,
               MouseWheelListener,
               KeyListener {

    private CanvasPanel canvas;

    public TestFrame(String title, CanvasMode[] modes) {
	super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // canvas to paint on
        canvas = new CanvasPanel(modes);
	setContentPane(createContentPane((Component) canvas));

        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseWheelListener(this);
        addKeyListener(this);
    }

    public void showFrame(int xLoc, int yLoc, int xSize, int ySize) {
	setLocation(xLoc, yLoc);
	setSize(xSize, ySize);
	setVisible(true);
    }

    private JPanel createContentPane(Component comp) {
	// Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
	// add component and set up
	contentPane.add(comp, BorderLayout.CENTER);
        contentPane.setOpaque(true);
        return contentPane;
    }

    private void exit() {
        dispose();
        System.out.println("goodbye...");
        System.exit(0);
    }



    /*------------------------- MOUSE METHODS --------------------------*/

    // mouse position
    private int lastX = -1;
    private int lastY = -1;
    private int dirX = 0;
    private int dirY = 0;

    @Override
    public void mouseClicked(MouseEvent arg0) {
        // System.out.println("mouseClicked");
    }
    
    @Override
    public void mouseEntered(MouseEvent arg0) {
        // System.out.println("mouseEntered");
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // System.out.println("mouseExited");
    }

    @Override public void mousePressed(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        // requestFocus();
        // System.out.println("mousePressed: " + lastX + ", " + lastY);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        // System.out.println("mouseReleased");
    }

    @Override
    public void mouseDragged(MouseEvent event) {		
        // take note of position
        int x = event.getX();
        int y = event.getY();
        dirX = x - lastX;
        dirY = y - lastY;
        lastX = x;
        lastY = y;
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        canvas.setMouseX(e.getX());
        canvas.setMouseY(e.getY());
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
	// zoom(e.getWheelRotation());
    }



    /*------------------------ KEYBOARD METHODS ------------------------*/

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println("pressed key: " + e.getKeyChar());

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            exit();
        
        canvas.getCurrentMode().keyPressed(e);
        canvas.update();
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // System.out.println("released key: " + e.getKeyChar());
    }

    
    @Override
    public void keyTyped(KeyEvent e) {
        // System.out.println("key typed: " + e.getKeyChar());
        char c = e.getKeyChar();

        try {
            int i = Integer.parseInt("" + c);
            canvas.switchMode(i);
        } catch (Exception ex) {}
        
        canvas.keyTyped(e);
        canvas.update();
        repaint();
    }

}
