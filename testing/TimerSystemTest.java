/**
 * GUI experiment with sodium TimerSystem. GUI contains a timer on the screen that is incremented every second.
 *
 * Every 100 ms, event sMain is fired which has no handler.
 */

import nz.sodium.*;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;
import swidgets.*;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;


public class TimerSystemTest {

    static Stream<Long> periodic(TimerSystem sys, long period) {
        Cell<Long> time = sys.time;
        CellLoop<Optional<Long>> oAlarm = new CellLoop<>();
        Stream<Long> sAlarm = sys.at(oAlarm);
        oAlarm.loop(
                sAlarm.map(t -> Optional.of(t + period))
                        .hold(Optional.<Long>of(time.sample() + period)));
        return sAlarm;
    }

    public static void main(String[] args) {
        TimerSystem sys = new MillisecondsTimerSystem();
        Cell<Long> time = sys.time;
        StreamSink<Unit> sMain = new StreamSink<Unit>();

        JFrame frame = new JFrame("Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        long t0 = time.sample();
        long tCurrent = t0;

        Transaction.runVoid(
                () -> {
                    CellLoop<Long> timeLoop = new CellLoop<>();
                    SLabel label = new SLabel(timeLoop.map(i -> Long.toString(i)));
                    timeLoop.loop(time.map(i->(i-t0)/1000));
                    frame.add(label);
                }
        );
        frame.setSize(400,160);
        frame.setVisible(true);

        while (true) {
            long t = time.sample();
            long tDest = tCurrent + 100;
            long tDiff = tDest - t;
            if (tDiff > 0){
                try {Thread.sleep(tDiff);}
                catch (InterruptedException e){}
            }
            sMain.send(Unit.UNIT);
            tCurrent = tDest;
        }
    }


}
