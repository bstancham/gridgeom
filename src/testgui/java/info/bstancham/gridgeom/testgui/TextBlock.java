package info.bstancham.gridgeom.testgui;

import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;

public class TextBlock {

    private List<String> lines = new ArrayList<>();
    private List<Color> colors = new ArrayList<>();
    private Color textColor = Color.GRAY;
    private Color bgColor = null;
    private int posX;
    private int posY;
    private int lineSpacing = 15;
    private Font fontMono = new Font(Font.MONOSPACED, 14, 14);
    private String separatorString = "<<<SEPARATOR>>>";
    private String separatorChar = "-";

    public TextBlock() {
        this(10, 10);
    }

    public TextBlock(int x, int y) {
        posX = x;
        posY = y;
    }

    /**
     * <p>Remove all lines, but don't reset colors.</p>
     */
    public void clear() {
        lines.clear();
        colors.clear();
    }

    /**
     * <p>Set the text color, so that subsequently added lines will have the
     * color {@code c}.</p>
     */
    public void setColor(Color c) { textColor = c; }

    /**
     * <p>Set the background color. This applies to the whole text block.</p>
     *
     * <p>If {@code c} is {@code null} then no background will be painted.</p>
     */
    public void setBGColor(Color c) { bgColor = c; }

    /**
     * <p>Set position of the top-left corner of the text-block.</p>
     */
    public void setPosition(int x, int y) {
        posX = x;
        posY = y;
    }

    /**
     * <p>Set the text color to {@code c}, and add all of the lines of {@code
     * text}.</p>
     */
    public void add(Color c, String ... text) {
        setColor(c);
        add(text);
    }

    public void add(String ... text) {
        for (String s : text) {
            colors.add(textColor);
            lines.add(s);
        }
    }

    public void addIfElse(boolean test,
                          Color colIfTrue, Color colIfFalse,
                          String text) {
        addIfElse(test, colIfTrue, text, colIfFalse, text);
    }
    
    public void addIfElse(boolean test,
                          Color colIfTrue, String textIfTrue,
                          Color colIfFalse, String textIfFalse) {
        if (test)
            add(colIfTrue, textIfTrue);
        else
            add(colIfFalse, textIfFalse);
    }

    public void addSeparator(Color c) {
        add(c, separatorString);
    }

    public void paint(Graphics g) {

        // find length of longest line
        int longLine = 0;
        for (String s : lines)
            if (!s.equals(separatorString)
                && s.length() > longLine)
                longLine = s.length();
        
        // build separator
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < longLine; i++)
            sb.append(separatorChar);
        String separator = sb.toString();
        
        // background
        if (bgColor != null) {
            int pad = 5;
            int charSize = 8;
            int x = posX - pad;
            int y = posY - pad;
            int w = pad + pad + (charSize * longLine);
            int h = pad + pad + (lineSpacing * lines.size());
            g.setColor(bgColor);
            g.fillRect(x, y, w, h);
        }

        // text
        g.setFont(fontMono);
        int y = posY + lineSpacing;
        for (int i = 0; i < lines.size(); i++) {
            // substitute separator string if required
            String str = lines.get(i);
            if (str.equals(separatorString))
                str = separator;
            g.setColor(colors.get(i));
            g.drawString(str, posX, y);
            y += lineSpacing;
        }
    }

}
