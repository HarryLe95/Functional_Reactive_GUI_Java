/**
 * GUI Testing for label disappearing requirement in display field 2 and 3.
 * <p>
 * GUI contains a button that shows "Hello" on the panel. The "Hello" label automatically disappears after 3s
 * unless the button is clicked on again. The clicking of the button simulates a data arrival event, which is
 * shown on the display field and cleared after 3s unless a new arrival event occurs.
 * <p>
 * Button clicking triggers an event that records the time of clicking by updating CellLoop<Long> tClick and an event
 * sClickedLabel that updates the label with "Hello". sTick event fired every 100ms triggers a logic that checks for
 * whether 3s has elapsed since the last tClick, which then fires an sClearLabel event that clears the label.
 * sClickedLabel has priority over sClearLabel.
 */

import nz.sodium.*;
import src.*;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class DisappearingLabelTest extends GpsGUI {

    GButton button;
    GLabel tClickLabel;

    public DisappearingLabelTest(String name, Dimension windowSize) {
        super(name, windowSize);
        button = new GButton("Set");
    }

    public void addLabel() {
        GPanel mainFrame = new GPanel(BoxLayout.PAGE_AXIS);
        GPanel row1 = new GPanel(BoxLayout.LINE_AXIS);

        CellLoop<Double> tClick = new CellLoop<>();
        Stream<Double> sClickedTime = button.sClicked.map(u -> time.sample());
        Stream<String> sClickedLabel = button.sClicked.map(u -> "Hello");

        Stream<Unit> sElapsed = Stream.filterOptional(sTick.snapshot(tClick, (u, tClick_) -> time.sample() - tClick_ >= 3 ? Optional.of(Unit.UNIT) : Optional.empty()));
        Stream<String> sClearLabel = sElapsed.map(u -> " ");
        Stream<String> clickUpdateStream = sClickedLabel.orElse(sClearLabel);

        tClickLabel = new GLabel(clickUpdateStream.hold(" "), true);
        tClick.loop(sClickedTime.hold(0.0));
        row1.add(button);
        row1.add(tClickLabel);
        mainFrame.add(row1);
        frame.add(mainFrame);
    }

    public static void main(String[] args) {
        DisappearingLabelTest test = new DisappearingLabelTest("AutoClearTest", new Dimension(300, 80));
        Transaction.runVoid(() -> {
            test.addLabel();
        });
        test.frame.setVisible(true);
        test.runLoop(100);
    }
}
