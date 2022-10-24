package src;

import swidgets.STextField;

import java.awt.*;

public class GTextField extends STextField {
    public final Dimension textLabelSize;

    public GTextField(String initText, Dimension textLabelSize){
        super(initText);
        this.textLabelSize = textLabelSize;
        this.setMinimumSize(textLabelSize);
        this.setMaximumSize(textLabelSize);

    }
    public GTextField(String initText) {
        this(initText, new Dimension(150,20));
    }
}
