package info.bschambers.gridgeom.testgui;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.awt.event.KeyEvent;

public class KbdMode {

    private List<KeyBinding> bindings = new ArrayList<>();

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

    public boolean keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        for (KeyBinding kb : bindings)
            if (kb.keyTyped(c))
                return true;
        return false;
    }

    public static String boolStr(boolean val) {
        return "(" + (val ? "TRUE" : "FALSE" ) + ")";
    }
    
}
