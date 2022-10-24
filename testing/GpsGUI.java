package testing;

import nz.sodium.*;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;
import swidgets.*;

import javax.swing.*;
import java.awt.*;


public class GpsGUI {
    public TimerSystem sys;
    public CellSink<Double> time;
    public StreamSink<Unit> sTick;

    public JFrame frame;

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
