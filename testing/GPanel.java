package testing;

import nz.sodium.*;
import swidgets.*;

import javax.swing.*;
import java.awt.*;

public class GPanel extends JPanel {
    public static final Dimension panelSize = new Dimension(450,200);
    public static final Dimension buttonSize = new Dimension(100,20);
    public static final Dimension textLabelSize = new Dimension(150,20);
    public static final Dimension labelSize = new Dimension(150,20);
    public static final Dimension spacing = new Dimension(10,10);

    public static class GLabel extends SLabel {
        public GLabel(Cell<String> text, boolean setBackgroundColor){
            super(text);
            this.setMinimumSize(labelSize);
            this.setMaximumSize(labelSize);
            if (setBackgroundColor) {
                this.setBackground(Color.WHITE);
                this.setOpaque(true);
            }
        }

        public GLabel(Cell<String> text){
            this(text,true);
        }

    }

    public static class GButton extends SButton{
        public GButton(String label){
            super(label);
            this.setMinimumSize(buttonSize);
            this.setMaximumSize(buttonSize);
        }
    }

    public static class GTextField extends STextField{
        public GTextField(String initText){
            super(initText);
            this.setMinimumSize(textLabelSize);
            this.setMaximumSize(textLabelSize);
        }
    }

    public GPanel(int axis){
        this.setLayout(new BoxLayout(this, axis));
        this.setMinimumSize(panelSize);
        this.setMaximumSize(panelSize);
    }

    @Override
    public Component add(Component component){
        super.add(component);
        super.add(Box.createRigidArea(spacing));
        return this;
    }
}
