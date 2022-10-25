package src;

import javax.swing.*;
import java.awt.*;

public class GPanel extends JPanel {
    public final Dimension panelSize;
    public final Dimension spacing;

    public GPanel(int axis, Dimension panelSize, Dimension spacing){
        super();
        this.panelSize = new Dimension(panelSize);
        this.spacing = new Dimension(spacing);
        this.setLayout(new BoxLayout(this, axis));
        this.setMaximumSize(panelSize);
        this.setMaximumSize(panelSize);
        super.add(Box.createRigidArea(spacing));
    }

    public GPanel(int axis, Dimension panelSize){
        this(axis, panelSize, new Dimension(10,10));
    }
    public GPanel(int axis){
        this(axis, new Dimension(770, 30));
    }

    @Override
    public Component add(Component component){
        super.add(component);
        super.add(Box.createRigidArea(spacing));
        return this;
    }
}
