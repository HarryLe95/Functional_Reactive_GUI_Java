import nz.sodium.*;
import src.Point;
import src.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class GUIFinal extends GpsGUI {
    CellLoop<Pair<Double, Double>> cLatBounds;
    CellLoop<Pair<Double, Double>> cLongBounds;
    CellLoop<Double> cTimeLatest;
    CellLoop<Double> cTimeFiltered;
    Stream<GpsEvent> sMerged;

    public GUIFinal(String name, Dimension windowSize) {
        super(name, windowSize);
        Transaction.runVoid(
                () -> {
                    cLatBounds = new CellLoop<>();
                    cLongBounds = new CellLoop<>();
                    cTimeLatest = new CellLoop<>();
                    cTimeFiltered = new CellLoop<>();
                }
        );
    }

    public void addTrackDisplays(GPanel pMain, Stream<GpsEvent>[] streams, double tClear) {
        GPanel[] pTrackers = new GPanel[streams.length];
        GLabel[] lLat = new GLabel[streams.length];
        GLabel[] lLong = new GLabel[streams.length];
        GLabel[] lDist = new GLabel[streams.length];
        CellLoop<HashMap<Double, Pair<Point, Double>>> cDistances[] = new CellLoop[streams.length];
        CellLoop<Double> cTimeLast = new CellLoop<>();

        Stream<Unit> sUpdateTime = Stream.filterOptional(
                sTick.map(i -> {
                    return time.sample() - cTimeLast.sample() >= tClear
                            ? Optional.of(Unit.UNIT) : Optional.<Unit>empty();
                })
        );

        cTimeLast.loop(sUpdateTime.map(i -> time.sample()).hold(0.0));

        sMerged = new Stream<>();
        for (int index = 0; index < streams.length; index++) {
            sMerged = sMerged.orElse(streams[index]);
            cDistances[index] = new CellLoop<>();
            lLat[index] = new GLabel(streams[index].map(j -> String.format("%.8f", j.latitude)).hold(""),
                    new Dimension(150, 20), true);
            lLong[index] = new GLabel(streams[index].map(j -> String.format("%.8f", j.longitude)).hold(""),
                    new Dimension(150, 20), true);

            Stream<HashMap<Double, Pair<Point, Double>>> sUpdateEvent =
                    streams[index].snapshot(new Cell<>(index), (j, k) -> {
                        HashMap<Double, Pair<Point, Double>> newDict = new HashMap<>(cDistances[k].sample());
                        Point newPoint = Point.from(j.latitude, j.longitude, j.altitude);
                        if (newDict.isEmpty()){
                            newDict.put(time.sample(), Pair.of(newPoint, 0.0));
                            return newDict;
                        }
                        double lastTime = Collections.max(newDict.keySet());
                        Point lastPoint = newDict.get(lastTime).first;
                        double lastDist = newDict.get(lastTime).second;
                        double newDist = lastDist + Point.distance(lastPoint, newPoint);
                        newDict.put(time.sample(), Pair.of(newPoint, newDist));
                        ArrayList<Double> toRemove = new ArrayList<>();
                        for (double key : newDict.keySet()) {
                            if (key <= time.sample() - tClear) {
                                toRemove.add(key);
                            }
                        }
                        for (double key : toRemove) {
                            newDict.remove(key, newDict.get(key));
                        }
                        return newDict;
                    });

            Stream<HashMap<Double, Pair<Point, Double>>> sRemoveTime = sUpdateTime.snapshot(new Cell<>(index),
                    (u, k) -> {
                        HashMap<Double, Pair<Point, Double>> newDict = new HashMap<>(cDistances[k].sample());
                        ArrayList<Double> toRemove = new ArrayList<>();
                        for (double key : newDict.keySet()) {
                            if (key <= time.sample() - tClear) {
                                toRemove.add(key);
                            }
                        }
                        for (double key : toRemove) {
                            newDict.remove(key, newDict.get(key));
                        }
                        return newDict;
                    }
            );
            Stream<HashMap<Double, Pair<Point, Double>>> sUpdateDistance = sUpdateEvent.orElse(sRemoveTime);
            cDistances[index].loop(sUpdateDistance.hold(new HashMap<>()));
            lDist[index] = new GLabel(cDistances[index].map(
                    j->{
                        if (j.isEmpty()){
                            return " ";
                        }
                        double tMin = Collections.min(j.keySet());
                        double tMax = Collections.max(j.keySet());
                        return String.format("%.3f", j.get(tMax).second -j.get(tMin).second);
                    }
            ),new Dimension(180,20));

            pTrackers[index] = new GPanel(BoxLayout.LINE_AXIS);
            pTrackers[index].add(new GLabel(new Cell<>("Tracker " + index), new Dimension(60,20),
                    false));
            pTrackers[index].add(new GLabel(new Cell<>("Latitude"), new Dimension(60,20),
                    false));
            pTrackers[index].add(lLat[index]);
            pTrackers[index].add(new GLabel(new Cell<>("Longitude"),new Dimension(60,20),
                    false));
            pTrackers[index].add(lLong[index]);
            pTrackers[index].add(new GLabel(new Cell<>("Distance"), new Dimension(60,20),
                    false));
            pTrackers[index].add(lDist[index]);
            pMain.add(pTrackers[index]);
        }
    }

    public void addControlPanel(GPanel pMain) {
        GPanel pControlTitle = new GPanel(BoxLayout.PAGE_AXIS);
        GPanel pControlLabel = new GPanel(BoxLayout.LINE_AXIS);
        GPanel pControlText = new GPanel(BoxLayout.LINE_AXIS);
        GPanel pControlButton = new GPanel(BoxLayout.PAGE_AXIS);

        GTextField tLatLB = new GTextField("-90", new Dimension(185, 20));
        GTextField tLatUB = new GTextField("90", new Dimension(185, 20));
        GTextField tLongLB = new GTextField("-180", new Dimension(185, 20));
        GTextField tLongUB = new GTextField("180", new Dimension(185, 20));
        GButton bSetBound = new GButton("Set", new Dimension(100, 20));
        cLatBounds.loop(bSetBound.sClicked.snapshot(tLatLB.text, tLatUB.text,
                (u, lb, ub) -> {
                    double LB, UB;
                    try {
                        LB = Double.parseDouble(lb);
                    } catch (NumberFormatException e) {
                        LB = -90;
                    }
                    try {
                        UB = Double.parseDouble(ub);
                    } catch (NumberFormatException e) {
                        UB = 90;
                    }
                    LB = Math.min(Math.max(LB, -90), 90);
                    UB = Math.min(Math.max(UB, -90), 90);
                    if (LB > UB) {
                        return Pair.of(UB, LB);
                    }
                    return Pair.of(LB, UB);
                }
        ).hold(Pair.of(-90.0, 90.0)));

        cLongBounds.loop(bSetBound.sClicked.snapshot(tLongLB.text, tLongUB.text,
                (u, lb, ub) -> {
                    double LB, UB;
                    try {
                        LB = Double.parseDouble(lb);
                    } catch (NumberFormatException e) {
                        LB = -180;
                    }
                    try {
                        UB = Double.parseDouble(ub);
                    } catch (NumberFormatException e) {
                        UB = 180;
                    }
                    LB = Math.min(Math.max(LB, -180), 180);
                    UB = Math.min(Math.max(UB, -180), 180);
                    if (LB > UB) {
                        return Pair.of(UB, LB);
                    }
                    return Pair.of(LB, UB);
                }
        ).hold(Pair.of(-180.0, 180.0)));

        GLabel lLatLB = new GLabel(cLatBounds.map(i -> String.format("%.3f", i.first)), new Dimension(125, 20));
        GLabel lLatUB = new GLabel(cLatBounds.map(i -> String.format("%.3f", i.second)), new Dimension(125, 20));
        GLabel lLongLB = new GLabel(cLongBounds.map(i -> String.format("%.3f", i.first)), new Dimension(125, 20));
        GLabel lLongUB = new GLabel(cLongBounds.map(i -> String.format("%.3f", i.second)), new Dimension(125, 20));

        pControlTitle.add(new GLabel(new Cell<>("Control Panel"), false));
        pControlLabel.add(new GLabel(new Cell<>("LatLB"), new Dimension(50, 20), false));
        pControlLabel.add(lLatLB);
        pControlLabel.add(new GLabel(new Cell<>("LatUB"), new Dimension(50, 20), false));
        pControlLabel.add(lLatUB);
        pControlLabel.add(new GLabel(new Cell<>("LongLB"), new Dimension(50, 20), false));
        pControlLabel.add(lLongLB);
        pControlLabel.add(new GLabel(new Cell<>("LongUB"), new Dimension(50, 20), false));
        pControlLabel.add(lLongUB);
        pControlText.add(tLatLB);
        pControlText.add(tLatUB);
        pControlText.add(tLongLB);
        pControlText.add(tLongUB);
        pControlButton.add(bSetBound);
        pMain.add(pControlTitle);
        pMain.add(pControlLabel);
        pMain.add(pControlText);
        pMain.add(pControlButton);
    }

    public void addEventDisplays(GPanel pMain) {
        GPanel pLatest = new GPanel(BoxLayout.LINE_AXIS);
        GPanel pFiltered = new GPanel(BoxLayout.LINE_AXIS);

        Stream<String> sStringLatest = sMerged.map(
                i -> i.name.replace("Tracker", "") + "," +
                        String.format("%3.3f", i.latitude) + "," +
                        String.format("%3.3f", i.longitude) + "," +
                        String.format("%.3f", time.sample()));
        Stream<Unit> sElapsedLatest = Stream.filterOptional(sTick.snapshot(cTimeLatest,
                (u, tClick_) -> time.sample() - tClick_ >= 3 ? Optional.of(Unit.UNIT) : Optional.empty()));
        Stream<String> sClearLatest = sElapsedLatest.map(u -> " ");
        Stream<String> sLatest = sStringLatest.orElse(sClearLatest);
        GLabel lLatest = new GLabel(sLatest.hold(" "),
                new Dimension(200, 20), true);


        Stream<String> sStringFiltered = sMerged.filter(
                i -> (i.latitude >= cLatBounds.sample().first) &&
                        (i.latitude <= cLatBounds.sample().second) &&
                        (i.longitude >= cLongBounds.sample().first) &&
                        (i.longitude <= cLongBounds.sample().second)).map(
                i -> i.name.replace("Tracker", "") + "," +
                        String.format("%3.3f", i.latitude) + "," +
                        String.format("%3.3f", i.longitude) + "," +
                        String.format("%.3f", time.sample()));
        Stream<Unit> sElapsedFiltered = Stream.filterOptional(sTick.snapshot(cTimeFiltered,
                (u, tClick_) -> time.sample() - tClick_ >= 3 ? Optional.of(Unit.UNIT) : Optional.empty()));
        Stream<String> sClearFiltered = sElapsedFiltered.map(u -> " ");
        Stream<String> sFiltered = sStringFiltered.orElse(sClearFiltered);

        GLabel lFiltered = new GLabel(sFiltered.hold(" "),
                new Dimension(200, 20), true);

        cTimeLatest.loop(sStringLatest.map(i -> time.sample()).hold(0.0));
        cTimeFiltered.loop(sStringFiltered.map(i -> time.sample()).hold(0.0));
        pLatest.add(new GLabel(new Cell<>("Latest Event"), false));
        pLatest.add(lLatest);
        pFiltered.add(new GLabel(new Cell<>("Filtered Event"), false));
        pFiltered.add(lFiltered);
        pMain.add(pLatest);
        pMain.add(pFiltered);
    }

    public static void main(String[] args) {
        GUIFinal GUI = new GUIFinal("GpsTracker", new Dimension(850, 600));
        GPanel pMain = new GPanel(BoxLayout.PAGE_AXIS);
        GpsService serv = new GpsService();
        // Retrieve Event Streams
        Stream<GpsEvent>[] streams = serv.getEventStreams();
        Transaction.runVoid(
                () -> {
                    GUI.addTrackDisplays(pMain, streams, 300000);
                    GUI.addControlPanel(pMain);
                    GUI.addEventDisplays(pMain);
                }
        );
        GUI.frame.add(pMain);
        GUI.frame.setVisible(true);
        GUI.runLoop(10);
    }
}
