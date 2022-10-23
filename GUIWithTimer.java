import nz.sodium.*;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;
import swidgets.SLabel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public class GUIWithTimer {
    public Cell<Long> time;

    public StreamSink<Unit> sTick;

    private TimerSystem sys;


    public GUIWithTimer(){
        Transaction.runVoid(
                ()->{
                    this.sys = new MillisecondsTimerSystem();
                    this.time = sys.time;
                    this.sTick = new StreamSink<Unit>();
                }
        );
    }


    public static void main(String[] args) {
        ClockCountDownArrayGUI GUI = new ClockCountDownArrayGUI();

        JFrame frame = new JFrame("Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.LINE_AXIS));

        Long t0 = GUI.time.sample();

        Transaction.runVoid(
                () -> {
                    CellLoop<ArrayList<Integer>> array = new CellLoop<>();
                    CellLoop<Integer> counter = new CellLoop<>();
                    CellLoop<Double> timeLoop = new CellLoop<>();
                    SLabel label = new SLabel(timeLoop.map(i -> Double.toString(i)));
                    SLabel counterLabel = new SLabel(counter.map(i -> Integer.toString(i)));
                    SLabel runningSumLabel = new SLabel(array.map(
                            i -> {
                                int sum = 0;
                                for (int j:i){
                                    sum+=j;
                                }
                                return Integer.toString(sum);
                            }
                    ));

                    Stream<Unit> sUpdate = Stream.filterOptional(
                            GUI.sTick.snapshot(counter, (u,c)->
                                    (GUI.time.sample() - t0)/1000 > c
                                            ?Optional.of(Unit.UNIT) :Optional.<Unit>empty()
                            ));

                    counter.loop(sUpdate.snapshot(counter, (u,c)->c+1).hold(0));
                    array.loop(sUpdate.snapshot(array,counter, (u,a,c)-> {
                                ArrayList<Integer> newList = new ArrayList<>(a);
                                newList.add(c);
                                return newList;
                            }
                    ).hold(new ArrayList<>()));
                    timeLoop.loop(GUI.time.map(i -> (double) (i-t0)/1000));
                    row1.add(new SLabel(new Cell<String>("Time Elapsed")));
                    row1.add(Box.createRigidArea(new Dimension(5, 0)));
                    row1.add(label);
                    row1.add(Box.createRigidArea(new Dimension(5, 0)));
                    row1.add(new SLabel(new Cell<String>("Seconds Elapsed")));
                    row1.add(Box.createRigidArea(new Dimension(5, 0)));
                    row1.add(counterLabel);
                    row1.add(Box.createRigidArea(new Dimension(5, 0)));
                    row1.add(new SLabel(new Cell<String>("Running Sum")));
                    row1.add(Box.createRigidArea(new Dimension(5, 0)));
                    row1.add(runningSumLabel);
                    row1.add(Box.createRigidArea(new Dimension(5, 0)));
                }
        );
        mainPanel.add(row1);
        frame.add(mainPanel);
        frame.setSize(400,160);
        frame.setVisible(true);

        while (true) {
            GUI.sTick.send(Unit.UNIT);
        }
    }


}
