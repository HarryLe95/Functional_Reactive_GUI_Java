package src;

import nz.sodium.Cell;
import swidgets.SLabel;

import java.awt.*;

public class GLabel extends SLabel {
    public final Dimension labelSize;

    public GLabel(Cell<String> text, Dimension labelSize, boolean setBackgroundColor) {
        super(text);
        this.labelSize = new Dimension(labelSize);
        this.setMaximumSize(labelSize);
        this.setMinimumSize(labelSize);
        this.setBackground(Color.WHITE);
        this.setOpaque(setBackgroundColor);

    }

    public GLabel(Cell<String> text, Dimension labelSize) {
        this(text, labelSize, true);
    }

    public GLabel(Cell<String> text, boolean setBackgroundColor) {
        this(text, new Dimension(150, 20), setBackgroundColor);
    }

    public GLabel(Cell<String> text) {
        this(text, true);
    }

}
