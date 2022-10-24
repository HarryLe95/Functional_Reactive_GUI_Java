/**
 * GUI experiment to test continuous addition and removal of items in a container at fix intervals.
 * <p>
 * Every 1s, an addition event causes an internal list to add an item with a value of 1.
 * Every 1s, a removal event causes the list to remove all items older than 5s ago.
 * The screen will show  a running sum, which stablises to 5.
 * Under the hood it should look like this:
 * 0 - {}
 * 1 - {1:1}
 * 2 - {1:1,2:1}
 * 3 - {1:1,2:1,3:1}
 * 4 - {1:1,2:1,3:1,4:1}
 * 5 - {1:1,2:1,3:1,4:1,5:1}
 * 6 - {2:1,3:1,4:1,5:1,6:1} - event recorded at time 1 removed
 * 7 - {3:1,4:1,5:1,6:1,7:1} - event recorded at time 2 removed ...
 * */

import nz.sodium.*;
import src.*;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;


public class CellContainerTest extends GpsGUI {
    private GLabel timeElapsedLabel;

    private GLabel timeLastAddLabel;

    private GLabel timeLastRemoveLabel;
    private GLabel timeRunningSumLabel;

    public CellContainerTest(String name, Dimension windowSize) {
        super(name, windowSize);
    }

    public void addLabel(double dtAdd, double dtRemove) {
        GPanel mainPanel = new GPanel(BoxLayout.PAGE_AXIS);
        GPanel row1 = new GPanel(BoxLayout.LINE_AXIS);
        GPanel row2 = new GPanel(BoxLayout.LINE_AXIS);


        CellLoop<HashMap<Double, Double>> timeDict = new CellLoop<>();
        CellLoop<Double> timeLastAdd = new CellLoop<>();
        CellLoop<Double> timeLastRemove = new CellLoop<>();

        Stream<Unit> sAdd = Stream.filterOptional(
                sTick.map(u -> {
                    return time.sample() - timeLastAdd.sample() >= dtAdd
                            ? Optional.of(Unit.UNIT)
                            : Optional.<Unit>empty();
                })
        );


        Stream<HashMap<Double, Double>> sAddValue = sAdd.snapshot(timeDict, (u,t)->{
           HashMap<Double,Double> newDict = new HashMap<>(t);
           newDict.put(time.sample(), 1.0);
           return newDict;
        });

        Stream<HashMap<Double, Double>> sRemoveValue = sAdd.snapshot(timeDict, (u,t)->{
            HashMap<Double,Double> newDict = new HashMap<>(t);
            ArrayList<Double> toRemove = new ArrayList<>();
            for (double key: newDict.keySet()){
                if (key <= time.sample()-dtRemove){
                    toRemove.add(key);
                }
            }
            for (double key: toRemove){
                newDict.remove(key,newDict.get(key));
            }
            newDict.put(time.sample(), 1.0);
            return newDict;
        });

        Stream<HashMap<Double,Double>> sMerge = sRemoveValue.orElse(sAddValue);

        timeLastAdd.loop(sAdd.map(u -> time.sample()).hold(0.0));
        timeLastRemove.loop(sAdd.map(u->time.sample()).hold(0.0));
        timeDict.loop(sMerge.hold(new HashMap<>()));

        timeElapsedLabel = new GLabel(time.map(i->String.format("%.3f",i)));
        timeLastAddLabel = new GLabel(timeLastAdd.map(i->String.format("%.3f",i)));
        timeLastRemoveLabel = new GLabel(timeLastRemove.map(i->String.format("%.3f",i)));
        timeRunningSumLabel = new GLabel(timeDict.map(
                i -> {
                    double sum = 0;
                    for (double j : i.values()) {
                        sum += j;
                    }
                    return String.format("%.3f",sum);
                }
        ));

        row1.add(new GLabel(new Cell<>("Time Elapsed")));
        row1.add(timeElapsedLabel);
        row1.add(new GLabel(new Cell<>("Running Sum")));
        row1.add(timeRunningSumLabel);
        row2.add(new GLabel(new Cell<>("Time Last Add")));
        row2.add(timeLastAddLabel);
        row2.add(new GLabel(new Cell<>("Time Last Remove")));
        row2.add(timeLastRemoveLabel);

        mainPanel.add(row1);
        mainPanel.add(row2);
        frame.add(mainPanel);
    }

    public static void main(String[] args) {
        CellContainerTest test = new CellContainerTest("Running Test", new Dimension(600, 110));
        Transaction.runVoid(
                () -> {
                    test.addLabel(1,5);
                }
        );

        test.frame.setVisible(true);
        test.runLoop(10);

    }
}
