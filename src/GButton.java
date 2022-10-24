package src;

import swidgets.SButton;

import java.awt.*;

public class GButton extends SButton {
    public final Dimension buttonSize;

    public GButton(String label, Dimension buttonSize) {
        super(label);
        this.buttonSize = buttonSize;
        this.setMaximumSize(buttonSize);
        this.setMinimumSize(buttonSize);
    }

    public GButton(String label) {
        this(label, new Dimension(100,20));
    }
}
