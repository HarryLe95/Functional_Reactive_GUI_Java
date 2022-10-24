/**
 * GUI experiment with filter to test functionality for task 3 of the assignment.
 * GUI first row contains an input field, which is fed through a filter if SetNumber button is clicked.
 * Filter is controlled by paramters in the second row.
 *
 */

import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.Transaction;
import src.*;

import javax.swing.*;
import java.awt.*;

public class FilterTest extends GpsGUI {
    private GTextField inputField;
    private GButton setNumberButton;
    private GLabel filteredLabel;
    private GTextField ubField;
    private GButton setUBButton;
    private GLabel ubLabel;
    private int maxInt;
    private int minInt;
    public FilterTest(String name, Dimension windowSize) {
        super(name, windowSize);
        maxInt = (int) (Math.pow(2, 31) - 1);
        minInt = -(int) Math.pow(2,31);
        inputField = new GTextField("0");
        setNumberButton = new GButton("SetNumber");
        ubField = new GTextField("0");
        setUBButton = new GButton("SetBound");
    }

    public void addNumberRow(GPanel mainPanel, Cell<Integer> ub) {
        GPanel row1 = new GPanel(BoxLayout.LINE_AXIS);

        Stream<Integer> valueInt = setNumberButton.sClicked.snapshot(
                inputField.text, ((u, i) -> {
                    try {
                        return Integer.parseInt(i);
                    } catch (NumberFormatException e) {
                        return maxInt;
                    }
                })).filter(i -> i <= ub.sample());
        filteredLabel = new GLabel(valueInt.hold(0).map(
                i -> Integer.toString(i)), true);

        row1.add(inputField);
        row1.add(setNumberButton);
        row1.add(filteredLabel);
        mainPanel.add(row1);
    }

    public Cell<Integer> addFilterRow(GPanel mainPanel) {
        GPanel row2 = new GPanel(BoxLayout.LINE_AXIS);
        Cell<Integer> ub = setUBButton.sClicked.snapshot(ubField.text,
                (u, i) -> {
                    try {
                        return Math.max(Math.min(Integer.parseInt(i),maxInt),minInt);
                    } catch (NumberFormatException e) {
                        return maxInt;
                    }
                }).hold(maxInt);

        ubLabel = new GLabel(ub.map(i -> Integer.toString(i)), true);


        row2.add(ubField);
        row2.add(setUBButton);
        row2.add(ubLabel);
        mainPanel.add(row2);
        return ub;
    }

    public static void main(String[] args) {
        FilterTest test = new FilterTest("FilterTest",new Dimension(500,120));
        GPanel mainPanel = new GPanel(BoxLayout.PAGE_AXIS);
        Transaction.runVoid(
                ()->{
                    Cell<Integer> ub = test.addFilterRow(mainPanel);
                    test.addNumberRow(mainPanel,ub);
                }
        );
        test.frame.add(mainPanel);
        test.frame.setVisible(true);
        test.runLoop(100);
    }
}
