package src;

import nz.sodium.*;

import javax.swing.*;
import java.awt.*;


public class GpsGUI {
    public CellSink<Double> time;
    public StreamSink<Unit> sTick;

    public JFrame frame;

    public void runLoop(int dt){
        long t0 = System.currentTimeMillis();
        long tLast = t0;
        while (true) {
            long t = System.currentTimeMillis();
            long tIdeal = tLast + dt;
            long toWait = tIdeal - t;
            if (toWait > 0)
                try {
                    Thread.sleep(toWait);
                } catch (InterruptedException e) {
                }
            time.send((double) (tIdeal - t0) * 0.001);
            sTick.send(Unit.UNIT);
            tLast = tIdeal;
        }
    }

    public GpsGUI(String name, Dimension windowSize){
        frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(windowSize);

        Transaction.runVoid(
                ()->{
                    time = new CellSink<>(0.0);
                    sTick = new StreamSink<>();
                }
        );
    }

}
