/**
 * GUI experiment with sodium TimerSystem. GUI contains a timer on the screen that is incremented every second.
 *
 * Every 100 ms, event sMain is fired which has no handler.
 */

import nz.sodium.*;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;
import swidgets.*;
import testing.GPanel;
import testing.GpsGUI;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;


public class TimerSystemTest extends GpsGUI {

    public TimerSystemTest(String name, Dimension windowSize) {
        super(name, windowSize);
    }

    public static void main(String[] args) {
        TimerSystemTest test = new TimerSystemTest("Timer Test", new Dimension(600,150));
        GPanel mainPanel = new GPanel(BoxLayout.PAGE_AXIS);

        long t0 = System.currentTimeMillis();
        long tLast = t0;

        GPanel.GLabel label = Transaction.run(
                () -> {
                    CellLoop<Double> timeLoop = new CellLoop<>();
                    GPanel.GLabel label_ = new GPanel.GLabel(timeLoop.map(i -> String.format("%.3f",i)));
                    timeLoop.loop(test.time);
                    return label_;
                }
        );
        mainPanel.add(label);
        test.frame.add(mainPanel);
        test.frame.setVisible(true);


        while (true) {
            long t = System.currentTimeMillis();
            long tIdeal = tLast + 100;
            long toWait = tIdeal - t;
            if (toWait > 0)
                try { Thread.sleep(toWait); } catch (InterruptedException e) {}
            test.time.send((double)(tIdeal - t0) * 0.001);
            test.sTick.send(Unit.UNIT);
            tLast = tIdeal;
        }
    }
}
