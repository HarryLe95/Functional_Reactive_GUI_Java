import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;
import swidgets.SButton;
import swidgets.SLabel;
import swidgets.STextField;

import javax.swing.*;
import java.awt.*;

public class GUIRefactored {
    public static JPanel addPanel(int axis) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        return panel;
    }

    public static void addComponent(JPanel panel, Component component, boolean boxSpacing, Dimension boxDimension) {
        panel.add(component);
        if (boxSpacing) {
            panel.add(Box.createRigidArea(boxDimension));
        }
    }

    public static SLabel createLabel(Cell<String> text, Dimension labelSize, boolean setBackgroundColor) {
        SLabel label = new SLabel(text);
        label.setMinimumSize(labelSize);
        label.setMaximumSize(labelSize);
        if (setBackgroundColor) {
            label.setBackground(Color.WHITE);
            label.setOpaque(true);
        }
        return label;
    }

    public static STextField createTextField(String initText, Dimension labelSize) {
        STextField label = new STextField(initText);
        label.setMinimumSize(labelSize);
        label.setMaximumSize(labelSize);
        return label;
    }

    public static void main(String[] args) {
        TimerSystem sys = new MillisecondsTimerSystem();
        Cell<Long> time = sys.time;
        long t0 = time.sample();
        // Initialise the GPS Service
        GpsService serv = new GpsService();
        // Retrieve Event Streams
        Stream<GpsEvent>[] streams = serv.getEventStreams();

        //GUI Frame
        JFrame frame = new JFrame("GUI App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = addPanel(BoxLayout.PAGE_AXIS);
        JPanel latestEventPanel = addPanel(BoxLayout.LINE_AXIS);
        JPanel filteredEventPanel = addPanel(BoxLayout.LINE_AXIS);
        JPanel controlTitlePanel = addPanel(BoxLayout.LINE_AXIS);
        JPanel controlLabelPanel = addPanel(BoxLayout.LINE_AXIS);
        JPanel controlTextPanel = addPanel(BoxLayout.LINE_AXIS);

        mainPanel.add(latestEventPanel);
        mainPanel.add(filteredEventPanel);
        mainPanel.add(controlTitlePanel);
        mainPanel.add(controlLabelPanel);
        mainPanel.add(controlTextPanel);

        Stream<GpsEvent> mergedStreams = new Stream<>();

        //Set up trackers:
        JPanel rows[] = new JPanel[streams.length];
        SLabel trackers_lat[] = new SLabel[streams.length];
        SLabel trackers_long[] = new SLabel[streams.length];

        //Set track simplified displays
        for (int i = 0; i < streams.length; i++) {
            mergedStreams = mergedStreams.orElse(streams[i]);
            trackers_lat[i] = createLabel(streams[i].map(j -> String.format("%.8f", j.latitude)).hold(""),
                    new Dimension(80, 20), true);
            trackers_long[i] = createLabel(streams[i].map(j -> String.format("%.8f", j.longitude)).hold(""),
                    new Dimension(80, 20), true);

            rows[i] = addPanel(BoxLayout.LINE_AXIS);
            addComponent(rows[i], new SLabel(new Cell<String>("Tracker")), true,
                    new Dimension(10, 0));
            addComponent(rows[i], new SLabel(new Cell<String>(Integer.toString(i))), true,
                    new Dimension(10, 0));
            addComponent(rows[i], new SLabel(new Cell<String>("Latitude")), true,
                    new Dimension(10, 0));
            addComponent(rows[i], trackers_lat[i], true, new Dimension(10, 0));
            addComponent(rows[i], new SLabel(new Cell<String>("Longitude")), true,
                    new Dimension(10, 0));
            addComponent(rows[i], trackers_long[i], true, new Dimension(10, 0));
            mainPanel.add(rows[i]);
        }

        //Set Restriction Labels and Control Panel
        STextField LatRangeUB = createTextField("90", new Dimension(80, 20));

        STextField LatRangeLB = createTextField("-90", new Dimension(80, 20));

        STextField LongRangeUB = createTextField("180", new Dimension(80, 20));

        STextField LongRangeLB = createTextField("-180", new Dimension(80, 20));

        SButton setRange = new SButton("Set");

        SLabel LatRangeLabelLB = createLabel(setRange.sClicked.snapshot(LatRangeLB.text,
                (u, t) -> {
                    try {
                        int threshold = Integer.parseInt(t);
                        if (threshold < -90) {
                            threshold = -90;
                        }
                        if (threshold > 90) {
                            threshold = 90;
                        }
                        return Integer.toString(threshold);
                    } catch (NumberFormatException e) {
                        return "-90";
                    }
                }
        ).hold("-90"), new Dimension(80, 20), false);

        SLabel LatRangeLabelUB = createLabel(setRange.sClicked.snapshot(LatRangeUB.text,
                (u, t) -> {
                    try {
                        int threshold = Integer.parseInt(t);
                        if (threshold < -90) {
                            threshold = -90;
                        }
                        if (threshold > 90) {
                            threshold = 90;
                        }
                        return Integer.toString(threshold);
                    } catch (NumberFormatException e) {
                        return "90";
                    }
                }
        ).hold("90"), new Dimension(80, 20), false);

        SLabel LongRangeLabelLB = createLabel(setRange.sClicked.snapshot(LongRangeLB.text,
                (u, t) -> {
                    try {
                        int threshold = Integer.parseInt(t);
                        if (threshold < -180) {
                            threshold = -180;
                        }
                        if (threshold > 180) {
                            threshold = 180;
                        }
                        return Integer.toString(threshold);
                    } catch (NumberFormatException e) {
                        return "-180";
                    }
                }
        ).hold("-180"), new Dimension(80, 20), false);

        SLabel LongRangeLabelUB = createLabel(setRange.sClicked.snapshot(LongRangeUB.text,
                (u, t) -> {
                    try {
                        int threshold = Integer.parseInt(t);
                        if (threshold < -180) {
                            threshold = -180;
                        }
                        if (threshold > 180) {
                            threshold = 180;
                        }
                        return Integer.toString(threshold);
                    } catch (NumberFormatException e) {
                        return "180";
                    }
                }
        ).hold("180"), new Dimension(80, 20), false);


        addComponent(controlTitlePanel, new SLabel(new Cell<>("Control Panel")),
                false, new Dimension(10, 0));
        addComponent(controlLabelPanel, new SLabel(new Cell<>("LatLB")),
                true, new Dimension(10, 0));
        addComponent(controlLabelPanel, LatRangeLabelLB, true, new Dimension(10, 0));
        addComponent(controlLabelPanel, new SLabel(new Cell<>("LatUB")),
                true, new Dimension(10, 0));
        addComponent(controlLabelPanel, LatRangeLabelUB, true, new Dimension(10, 0));
        addComponent(controlLabelPanel, new SLabel(new Cell<>("LongLB")),
                true, new Dimension(10, 0));
        addComponent(controlLabelPanel, LongRangeLabelLB, true, new Dimension(10, 0));
        addComponent(controlLabelPanel, new SLabel(new Cell<>("LongLU")),
                true, new Dimension(10, 0));
        addComponent(controlLabelPanel, LongRangeLabelUB, true, new Dimension(10, 0));
        addComponent(controlTextPanel, LatRangeLB, false, new Dimension(10, 0));
        addComponent(controlTextPanel, LatRangeUB, false, new Dimension(10, 0));
        addComponent(controlTextPanel, LongRangeLB, false, new Dimension(10, 0));
        addComponent(controlTextPanel, LongRangeUB, false, new Dimension(10, 0));
        addComponent(controlTextPanel, setRange, false, new Dimension(10, 0));

        //Set latest event and filtered event
        Stream<String> latestEventStream = mergedStreams.map(
                i -> i.name.replace("Tracker", "") + "," +
                        String.format("%3.3f", i.latitude) + "," +
                        String.format("%3.3f", i.longitude) + "," +
                        String.format("%d", (time.sample() - t0) / 1000));
        SLabel latestEventLabel = createLabel(latestEventStream.hold(""),
                new Dimension(120, 20), true);

        Stream<String> filteredEventStream = mergedStreams.filter(
                i -> (i.latitude >= Double.parseDouble(LatRangeLB.text.sample())) &&
                        (i.latitude <= Double.parseDouble(LatRangeUB.text.sample())) &&
                        (i.longitude >= Double.parseDouble(LongRangeLB.text.sample())) &&
                        (i.longitude <= Double.parseDouble(LongRangeUB.text.sample()))
        ).map(
                i -> i.name.replace("Tracker", "") + "," +
                        String.format("%3.3f", i.latitude) + "," +
                        String.format("%3.3f", i.longitude) + "," +
                        String.format("%d", (time.sample() - t0) / 1000));
        SLabel filteredEventLabel = createLabel(filteredEventStream.hold(""),
                new Dimension(120, 20), true);

        addComponent(latestEventPanel, createLabel(new Cell<>("Latest Event"),
                new Dimension(100, 20), false), true,
                new Dimension(10, 0));
        addComponent(latestEventPanel, latestEventLabel, true,
                new Dimension(10, 0));
        addComponent(filteredEventPanel, createLabel(new Cell<>("Filtered Event"),
                new Dimension(100, 20), false), true,
                new Dimension(10, 0));
        addComponent(filteredEventPanel, filteredEventLabel, true,
                new Dimension(10, 0));

        frame.add(mainPanel);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
