package testing;

import nz.sodium.Stream;

import javax.swing.*;
import java.awt.*;

public class LabelTest extends GpsGUI{

    public LabelTest(String name, Dimension windowSize) {
        super(name, windowSize);
    }

    public static void main(String[] args) {
        LabelTest testFrame = new LabelTest("Label Test", new Dimension(600,200));

        GPanel mainPanel = new GPanel(BoxLayout.PAGE_AXIS);
        GPanel row1 = new GPanel(BoxLayout.LINE_AXIS);
        GPanel.GTextField textField = new GPanel.GTextField("Hello");
        GPanel.GButton submitButton = new GPanel.GButton("Submit");
        Stream<String> submitValue = submitButton.sClicked.snapshot(textField.text, (u,t)->t);
        GPanel.GLabel textLabel = new GPanel.GLabel(submitValue.hold(""));
        row1.add(textField);
        row1.add(submitButton);
        row1.add(textLabel);
        mainPanel.add(row1);
        testFrame.frame.add(mainPanel);
        testFrame.frame.setVisible(true);
    }

}
