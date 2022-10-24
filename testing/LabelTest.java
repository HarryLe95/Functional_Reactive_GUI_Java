/**
 * Test basic GUI Structure - test GpsGUI class and driver
 */

import nz.sodium.Stream;
import nz.sodium.Transaction;
import src.*;

import javax.swing.*;
import java.awt.*;

public class LabelTest extends GpsGUI {

    public LabelTest(String name, Dimension windowSize) {
        super(name, windowSize);
    }

    public void addLabels(){
        GPanel mainPanel = new GPanel(BoxLayout.PAGE_AXIS);
        GPanel row1 = new GPanel(BoxLayout.LINE_AXIS);
        GTextField textField = new GTextField("Hello");
        GButton submitButton = new GButton("Submit");
        Stream<String> submitValue = submitButton.sClicked.snapshot(textField.text, (u,t)->t);
        GLabel textLabel = new GLabel(submitValue.hold(" "),true);
        row1.add(textField);
        row1.add(submitButton);
        row1.add(textLabel);
        mainPanel.add(row1);
        frame.add(mainPanel);
    }
    public static void main(String[] args) {
        LabelTest testFrame = new LabelTest("Label Test", new Dimension(420,80));
        Transaction.runVoid(
                ()->{
                    testFrame.addLabels();
                }
        );

        testFrame.frame.setVisible(true);
        testFrame.runLoop(100);
    }

}
