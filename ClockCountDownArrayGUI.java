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


public class ClockCountDownArrayGUI {
    public Cell<Long> time;

    public StreamSink<Unit> sTick;

    private TimerSystem sys;


    public ClockCountDownArrayGUI(){
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
        frame.setLayout(new FlowLayout());

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
                    frame.add(label);
                    frame.add(counterLabel);
                    frame.add(runningSumLabel);
                }
        );
        frame.setSize(400,160);
        frame.setVisible(true);

        while (true) {
            GUI.sTick.send(Unit.UNIT);
        }
    }


}
