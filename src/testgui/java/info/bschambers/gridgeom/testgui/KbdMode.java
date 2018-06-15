package info.bschambers.gridgeom.testgui;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.awt.event.KeyEvent;

public class KbdMode {

    private List<KeyBinding> bindings = new ArrayList<>();
    private Runnable upAction = null;
    private Runnable downAction = null;
    private Runnable leftAction = null;
    private Runnable rightAction = null;

    public List<KeyBinding> getKeyBindings() {
        return bindings;
    }

    public void add(char c, String description, Runnable action) {
        bindings.add(new KeyBinding(c, description, action));
    }

    public void add(char c, Supplier<String> description, Runnable action) {
        bindings.add(new KeyBinding(c, description, action));
    }

    public void add(char c1, char c2, Supplier<String> description,
                       Runnable action1, Runnable action2) {
        bindings.add(new KeyBinding.Double(c1, c2, description, action1, action2));
    }

    public void setCursorKeys(String description,
                              Runnable upAction, Runnable downAction,
                              Runnable leftAction, Runnable rightAction) {
        this.upAction = upAction;
        this.downAction = downAction;
        this.leftAction = leftAction;
        this.rightAction = rightAction;
    }

    public boolean keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        for (KeyBinding kb : bindings)
            if (kb.keyTyped(c))
                return true;
        return false;
    }

    /**
     * <p>List of keys handled:</p>
     * <ul>
     * <li>UP</li>
     * <li>DOWN</li>
     * <li>LEFT</li>
     * <li>RIGHT</li>
     * </ul>
     */
    public boolean keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP && upAction != null) {
            upAction.run();
            return true;
        }
        if (code == KeyEvent.VK_DOWN && downAction != null) {
            downAction.run();
            return true;
        }
        if (code == KeyEvent.VK_LEFT && leftAction != null) {
            leftAction.run();
            return true;
        }
        if (code == KeyEvent.VK_RIGHT && rightAction != null) {
            rightAction.run();
            return true;
        }
        return false;
    }    

    public static String boolStr(boolean val) {
        return "(" + (val ? "TRUE" : "FALSE" ) + ")";
    }
    
}
