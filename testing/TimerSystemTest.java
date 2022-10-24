/**
 * GUI experiment with sodium TimerSystem. GUI contains a timer on the screen that is incremented every second.
 * Every 100 ms, event sMain is fired which has no handler.
 */

import nz.sodium.*;

import src.GLabel;
import src.GPanel;
import src.GpsGUI;


import javax.swing.*;
import java.awt.*;


public class TimerSystemTest extends GpsGUI {

    public TimerSystemTest(String name, Dimension windowSize) {
        super(name, windowSize);
    }

    public void addLabel() {
        GPanel mainPanel = new GPanel(BoxLayout.LINE_AXIS);
        CellLoop<Double> timeLoop = new CellLoop<>();
        GLabel label = new GLabel(timeLoop.map(i -> String.format("%.3f", i)));
        timeLoop.loop(time);
        mainPanel.add(new GLabel(new Cell<>("Time elapsed")));
        mainPanel.add(label);
        frame.add(mainPanel);
    }

    public static void main(String[] args) {
        TimerSystemTest test = new TimerSystemTest("Timer Test", new Dimension(300, 80));
        Transaction.runVoid(
                ()->{test.addLabel();}
        );
        test.frame.setVisible(true);
        test.runLoop(100);
    }
}
