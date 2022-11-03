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

    Stream<GpsEvent> sFiltered;

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

    /**
     * Add display track for 10 trackers
     *
     * @param pMain   - main panel
     * @param streams - streams of event as a list of Stream<GpsEvent>
     * @param tClear  - period to retain Gps data to calculate total distance traveled - in seconds. Defaults to
     *                5 minutes or 300 seconds
     */
    public void addTrackDisplays(GPanel pMain, Stream<GpsEvent>[] streams, double tClear) {
        GPanel[] pTrackers = new GPanel[streams.length];
        GLabel[] lLat = new GLabel[streams.length];
        GLabel[] lLong = new GLabel[streams.length];
        GLabel[] lDist = new GLabel[streams.length];
        CellLoop<HashMap<Double, Point>> cDistances[] = new CellLoop[streams.length];
        CellLoop<Double> cTimeLast = new CellLoop<>();

        //Unit stream sent every tClear seconds (300s) to check buffer removal logic
        Stream<Unit> sUpdateTime = Stream.filterOptional(
                sTick.map(i -> {
                    return time.sample() - cTimeLast.sample() >= tClear
                            ? Optional.of(Unit.UNIT) : Optional.<Unit>empty();
                })
        );

        //cTimeLast holds the timestamp of the sUpdateTime event
        cTimeLast.loop(sUpdateTime.map(i -> time.sample()).hold(0.0));

        sMerged = new Stream<>();
        sFiltered = new Stream<>();

        for (int index = 0; index < streams.length; index++) {
            //Merge and Filtered Merge streams
            sMerged = sMerged.orElse(streams[index]);
            sFiltered = sFiltered.orElse(streams[index].filter(
                    i -> (i.latitude >= cLatBounds.sample().first) &&
                            (i.latitude <= cLatBounds.sample().second) &&
                            (i.longitude >= cLongBounds.sample().first) &&
                            (i.longitude <= cLongBounds.sample().second)));


            cDistances[index] = new CellLoop<>();
            lLat[index] = new GLabel(streams[index].map(j -> String.format("%.8f", j.latitude)).hold(""),
                    new Dimension(150, 20), true);
            lLong[index] = new GLabel(streams[index].map(j -> String.format("%.8f", j.longitude)).hold(""),
                    new Dimension(150, 20), true);

            //Buffer that holds and removes data every tClear seconds (300s or 5mins)
            //Buffer implemented as a dictionary with key - event and value - point object
            Stream<HashMap<Double, Point>> sUpdateEvent =
                    streams[index].snapshot(new Cell<>(index), (j, k) -> {
                        HashMap<Double, Point> newDict = new HashMap<>(cDistances[k].sample());
                        Point newPoint = Point.from(j.latitude, j.longitude, j.altitude);

                        //Put latest arrived event to buffer
                        newDict.put(time.sample(), newPoint);

                        //Remove outdated events
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

            Stream<HashMap<Double, Point>> sRemoveTime = sUpdateTime.snapshot(new Cell<>(index),
                    (u, k) -> {
                        HashMap<Double, Point> newDict = new HashMap<>(cDistances[k].sample());
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
            Stream<HashMap<Double, Point>> sUpdateDistance = sUpdateEvent.orElse(sRemoveTime);
            cDistances[index].loop(sUpdateDistance.hold(new HashMap<>()));

            //Distance label - calculate total pairwise distance travelled in the buffer
            //Only calculate distances for events that are in the filter range
            lDist[index] = new GLabel(cDistances[index].map(
                    j -> {
                        if (j.isEmpty()) {
                            return " ";
                        }
                        //Filter valid points
                        //Valid points stored in an array sorted by time of arrival
                        ArrayList<Double> sortedTime = new ArrayList<>(j.keySet());
                        Collections.sort(sortedTime);
                        ArrayList<Point> validArray = new ArrayList<>();
                        for (Double time : sortedTime) {
                            Point p = j.get(time);
                            if ((p.getLat() >= cLatBounds.sample().first)&&
                                    (p.getLat() <= cLatBounds.sample().second)&&
                                    (p.getLon()>= cLongBounds.sample().first)&&
                                    (p.getLon()<= cLongBounds.sample().second)) {
                                validArray.add(p);
                            }
                        }

                        //Get Total distance
                        if (validArray.isEmpty()){
                            return "0";
                        }

                        double totalDistance = 0;
                        Point currentPoint = validArray.get(0);
                        for (int i = 1; i < validArray.size(); i++) {
                            Point nextPoint = validArray.get(i);
                            totalDistance += Point.distance(nextPoint, currentPoint);
                            currentPoint = nextPoint;
                        }
                        return String.format("%.3f", totalDistance);
                    }
            ), new Dimension(150, 20));

            pTrackers[index] = new GPanel(BoxLayout.LINE_AXIS);
            pTrackers[index].add(new GLabel(new Cell<>("Tracker " + index), new Dimension(60, 20),
                    false));
            pTrackers[index].add(new GLabel(new Cell<>("Latitude"), new Dimension(60, 20),
                    false));
            pTrackers[index].add(lLat[index]);
            pTrackers[index].add(new GLabel(new Cell<>("Longitude"), new Dimension(60, 20),
                    false));
            pTrackers[index].add(lLong[index]);
            pTrackers[index].add(new GLabel(new Cell<>("Distance"), new Dimension(60, 20),
                    false));
            pTrackers[index].add(lDist[index]);
            pMain.add(pTrackers[index]);
        }
    }

    /**
     * Add control panel consisting of the labels, the textfields, and a set button to manage filter params
     *
     * @param pMain - main panel
     */
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

    /**
     * Add a display to show the latest event and a display to show the filtered latest event
     *
     * @param pMain - main panel
     */
    public void addEventDisplays(GPanel pMain) {
        GPanel pDisplay = new GPanel(BoxLayout.LINE_AXIS);

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


        Stream<String> sStringFiltered = sFiltered.map(
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
        pDisplay.add(new GLabel(new Cell<>("Latest Event"), new Dimension(80, 20)
                , false));
        pDisplay.add(lLatest);
        pDisplay.add(new GLabel(new Cell<>("Filtered Event"), new Dimension(80, 20)
                , false));
        pDisplay.add(lFiltered);
        pMain.add(pDisplay);
    }

    public static void main(String[] args) {
        GUIFinal GUI = new GUIFinal("GpsTracker", new Dimension(850, 600));
        GPanel pMain = new GPanel(BoxLayout.PAGE_AXIS);
        GpsService serv = new GpsService();
        // Retrieve Event Streams
        Stream<GpsEvent>[] streams = serv.getEventStreams();
        Transaction.runVoid(
                () -> {
                    GUI.addTrackDisplays(pMain, streams, 300);
                    GUI.addEventDisplays(pMain);
                    GUI.addControlPanel(pMain);
                }
        );
        GUI.frame.add(pMain);
        GUI.frame.setVisible(true);
        GUI.runLoop(10);
    }
}
