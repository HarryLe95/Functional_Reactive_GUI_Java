import nz.sodium.Cell;
import nz.sodium.Stream;
import swidgets.SButton;
import swidgets.SLabel;
import swidgets.STextField;

import javax.swing.*;
import java.awt.*;

public class FilterTest {
    public static final Dimension panelSize = new Dimension(450,30);
    public static final Dimension buttonSize = new Dimension(100,20);
    public static final Dimension textSize = new Dimension(150,20);
    public static final Dimension labelSize = new Dimension(150,20);
    public static final Dimension spacing = new Dimension(10,10);

    public static JPanel addPanel(int axis) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        panel.setMinimumSize(panelSize);
        panel.setMaximumSize(panelSize);
        return panel;
    }

    public static void addComponent(JPanel panel, Component component, boolean boxSpacing) {
        panel.add(component);
        if (boxSpacing) {
            panel.add(Box.createRigidArea(spacing));
        }
    }

    public static SLabel createLabel(Cell<String> text, boolean setBackgroundColor) {
        SLabel label = new SLabel(text);
        label.setMinimumSize(labelSize);
        label.setMaximumSize(labelSize);
        if (setBackgroundColor) {
            label.setBackground(Color.WHITE);
            label.setOpaque(true);
        }
        return label;
    }

    public static STextField createTextField(String initText) {
        STextField label = new STextField(initText);
        label.setMinimumSize(textSize);
        label.setMaximumSize(textSize);
        return label;
    }

    public static SButton createButton(String initText){
        SButton button = new SButton(initText);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        return button;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Filter Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = addPanel(BoxLayout.PAGE_AXIS);
        JPanel row1 = addPanel(BoxLayout.LINE_AXIS);
        JPanel row2 = addPanel(BoxLayout.LINE_AXIS);

        mainPanel.add(row1);
        mainPanel.add(row2);
        frame.add(mainPanel);

        STextField valueText = createTextField("0");
        SButton setNum = createButton("SetNumbr");
        STextField upperBound = createTextField("0");
        SButton setBound = createButton("SetBound");

        Cell<Integer> ub = setBound.sClicked.snapshot(upperBound.text,
                (u,i) -> {
                    int maxInt = (int) Math.pow(2,31)-1;
                    try{
                        int ub_ = Integer.parseInt(i);
                        return ub_>maxInt?maxInt:ub_;
                    }catch (NumberFormatException e){
                        return maxInt;
                    }
                }).hold((int) Math.pow(2,31)-1);

        SLabel upperBoundLabel = createLabel(ub.map(i->Integer.toString(i)),true);

        Stream<Integer> valueInt = setNum.sClicked.snapshot(valueText.text, ((u, i) -> {
            try{
                return Integer.parseInt(i);
            }catch (NumberFormatException e){
                return (int) Math.pow(2,31)-1;
            }
        })).filter(i->i<= ub.sample());
        SLabel valueLabel = createLabel(valueInt.hold(0).map(i->Integer.toString(i)),true);

        addComponent(row1,valueText,true);
        addComponent(row1,setNum,true);
        addComponent(row1,valueLabel,true);
        addComponent(row2, upperBound,true);
        addComponent(row2, setBound, true);
        addComponent(row2, upperBoundLabel, true);


        frame.setSize(500,130);
        frame.setVisible(true);
    }
}
