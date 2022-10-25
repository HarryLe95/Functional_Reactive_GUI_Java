import nz.sodium.CellLoop;
import nz.sodium.Stream;
import nz.sodium.Transaction;
import nz.sodium.Unit;
import src.GPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class GUITest extends GUIFinal {
    public GUITest(String name, Dimension windowSize) {
        super(name, windowSize);
    }

    public Stream<GpsEvent> generateEvent(String name, double lat, double lon, double dt) {
        Stream<GpsEvent> event = Transaction.run(
                () -> {
                    CellLoop<Double> cTime = new CellLoop<>();
                    Stream<Unit> sGen = Stream.filterOptional(
                            sTick.map(
                                    u -> {
                                        return time.sample() - cTime.sample() >= dt
                                                ? Optional.of(Unit.UNIT) : Optional.<Unit>empty();
                                    }
                            ));
                    cTime.loop(sGen.map(u -> time.sample()).hold(0.0));
                    return sGen.map(u -> new GpsEvent(name, lat, lon, time.sample()*3.281));
                });
        return event;
    }

    public static void main(String[] args) {
        GUITest GUI = new GUITest("GpsTracker", new Dimension(850, 600));
        GPanel pMain = new GPanel(BoxLayout.PAGE_AXIS);
        Stream<GpsEvent>[] streams = new Stream[10];
        for (int index = 0; index < streams.length; index++) {
            streams[index] = GUI.generateEvent(Integer.toString(index),
                    index*10, index*10,index + 1);
        }

        Transaction.runVoid(
                () -> {
                    GUI.addTrackDisplays(pMain, streams, 5);
                    GUI.addControlPanel(pMain);
                    GUI.addEventDisplays(pMain);
                }
        );
        GUI.frame.add(pMain);
        GUI.frame.setVisible(true);
        GUI.runLoop(10);
    }
}
