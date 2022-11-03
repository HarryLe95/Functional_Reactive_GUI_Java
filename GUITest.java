import nz.sodium.CellLoop;
import nz.sodium.Stream;
import nz.sodium.Transaction;
import nz.sodium.Unit;
import src.GPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class GUITest extends GUIFinal {
    public final double tClear;

    public Stream<GpsEvent>[] streams;

    /**
     * GUI Test constructors
     *
     * @param name       - name of the frame
     * @param windowSize - window dimension
     * @param numStreams - number of display streams to generate data
     * @param dtFactor   - controls each event stream fire rate. Each stream is fired at regular interval of (id+1)*dtFactor
     * @param tClear     - time period to retain event data in memory to calculate distance travelled. 5 mins or 300s in the
     *                   official GUI but reduced to 5s for testing purposes.
     */
    public GUITest(String name, Dimension windowSize,
                   int numStreams, double dtFactor, double tClear) {
        super(name, windowSize);
        this.tClear = tClear;
        this.streams = new Stream[numStreams];
        for (int index = 0; index < numStreams - 1; index++) {
            streams[index] = generateEvent(Integer.toString(index),
                    index * 10, index * 10, (index + 1) * dtFactor, false);
        }
        streams[numStreams - 1] = generateEvent(Integer.toString(numStreams - 1), 0,0,
                1, true);
    }

    /**
     * Generate GpsEvent stream based on input data
     *
     * @param name - event id
     * @param lat  - event lat
     * @param lon  - event long
     * @param dt   - event firing interval
     * @return a stream of GpsEvent.
     */
    public Stream<GpsEvent> generateEvent(String name, double lat, double lon, double dt, boolean changeLoc) {
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
                    if (!changeLoc) {
                        return sGen.map(u -> new GpsEvent(name, lat, lon, time.sample() * 3.281));
                    }
                    return sGen.map(u -> new GpsEvent(name, lat, Math.floorMod((int) Math.floor(time.sample()), 2) * 100, 0));

                });
        return event;
    }

    public static void main(String[] args) {
        GUITest GUI = new GUITest("GpsTracker", new Dimension(850, 600), 10, 0.5, 5);
        GPanel pMain = new GPanel(BoxLayout.PAGE_AXIS);

        Transaction.runVoid(
                () -> {
                    GUI.addTrackDisplays(pMain, GUI.streams, GUI.tClear);
                    GUI.addControlPanel(pMain);
                    GUI.addEventDisplays(pMain);
                }
        );
        GUI.frame.add(pMain);
        GUI.frame.setVisible(true);
        GUI.runLoop(10);
    }
}
