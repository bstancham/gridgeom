package info.bschambers.gridgeom.testgui;

import java.util.function.Supplier;

public class KeyBinding {

    protected final char c;
    protected final Supplier<String> description;
    protected final Runnable action;

    public KeyBinding(char c, String description, Runnable action) {
        this(c, () -> description, action);
    }
    
    public KeyBinding(char c, Supplier<String> description, Runnable action) {
        this.c = c;
        this.description = description;
        this.action = action;
    }

    public boolean keyTyped(char c) {
        if (c == this.c) {
            action.run();
            return true;
        }
        return false;
    }

    public String getHelpText() {
        return c + " = " + description.get();
    }



    public static class Double extends KeyBinding {
        
        protected final char c2;
        protected final Runnable action2;

        public Double(char c1, char c2, String description,
                      Runnable action1, Runnable action2) {
            this(c1, c2, () -> description, action1, action2);
        }
        
        public Double(char c1, char c2, Supplier<String> description,
                      Runnable action1, Runnable action2) {
            super(c1, description, action1);
            this.c2 = c2;
            this.action2 = action2;
        }

        @Override
        public boolean keyTyped(char c) {
            if (c == this.c) {
                action.run();
                return true;
            }
            if (c == this.c2) {
                action2.run();
                return true;
            }
            return false;
        }

        public String getHelpText() {
            return c + "/" + c2 + " = " + description.get();
        }

    }
    
}
