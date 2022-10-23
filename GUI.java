import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;
import swidgets.SButton;
import swidgets.SLabel;
import swidgets.STextField;

import javax.swing.*;
import java.awt.*;

public class GUI {
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

        JPanel mainPanel = new JPanel();
        JPanel eventOutput = new JPanel();
        JPanel filteredEventOutput = new JPanel();


        eventOutput.setLayout(new BoxLayout(eventOutput, BoxLayout.LINE_AXIS));
        filteredEventOutput.setLayout(new BoxLayout(filteredEventOutput,BoxLayout.LINE_AXIS));

        JPanel controlPanelTitle = new JPanel();
        controlPanelTitle.add(new SLabel(new Cell<>("Control Panel")));
        controlPanelTitle.setLayout(new BoxLayout(controlPanelTitle, BoxLayout.LINE_AXIS));

        JPanel controlPanelLabel = new JPanel();
        controlPanelLabel.setLayout(new BoxLayout(controlPanelLabel, BoxLayout.LINE_AXIS));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));

        mainPanel.add(eventOutput);
        mainPanel.add(filteredEventOutput);
        mainPanel.add(controlPanelTitle);
        mainPanel.add(controlPanelLabel);
        mainPanel.add(controlPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        Stream<GpsEvent> mergedStreams = new Stream<>();

        //Set up trackers:
        JPanel rows[] = new JPanel[streams.length];
        SLabel trackers_lat[] = new SLabel[streams.length];
        SLabel trackers_long[] = new SLabel[streams.length];

        //Set track simplified displays
        for (int i = 0; i < streams.length; i++){
            mergedStreams = mergedStreams.orElse(streams[i]);
            trackers_lat[i] = new SLabel(streams[i].map(j->String.format("%.8f",j.latitude)).hold(""));
            trackers_long[i] = new SLabel(streams[i].map(j->String.format("%.8f",j.longitude)).hold(""));

            trackers_lat[i].setMinimumSize(new Dimension(80,20));
            trackers_lat[i].setMaximumSize(new Dimension(80,20));

            trackers_lat[i].setBackground(Color.WHITE);
            trackers_lat[i].setOpaque(true);

            trackers_long[i].setMinimumSize(new Dimension(80,20));
            trackers_long[i].setMaximumSize(new Dimension(80,20));
            trackers_long[i].setBackground(Color.WHITE);
            trackers_long[i].setOpaque(true);

            rows[i] = new JPanel();
            rows[i].setLayout(new BoxLayout(rows[i], BoxLayout.LINE_AXIS));
            rows[i].add(new SLabel(new Cell<String>("Tracker")));
            rows[i].add(Box.createRigidArea(new Dimension(10, 0)));
            rows[i].add(new SLabel(new Cell<String>(Integer.toString(i))));
            rows[i].add(Box.createRigidArea(new Dimension(10, 0)));
            rows[i].add(new SLabel(new Cell<String>("Latitude")));
            rows[i].add(Box.createRigidArea(new Dimension(10, 0)));
            rows[i].add(trackers_lat[i]);
            rows[i].add(Box.createRigidArea(new Dimension(10, 0)));
            rows[i].add(new SLabel(new Cell<String>("Longitude")));
            rows[i].add(Box.createRigidArea(new Dimension(10, 0)));
            rows[i].add(trackers_long[i]);
            mainPanel.add(rows[i]);
        }

        //Set Restriction Labels and Control Panel
        STextField LatRangeUB = new STextField("90");
        LatRangeUB.setMinimumSize(new Dimension(80,20));
        LatRangeUB.setMaximumSize(new Dimension(80,20));

        STextField LatRangeLB = new STextField("-90");
        LatRangeLB.setMinimumSize(new Dimension(80,20));
        LatRangeLB.setMaximumSize(new Dimension(80,20));

        STextField LongRangeUB = new STextField("180");
        LongRangeUB.setMinimumSize(new Dimension(80,20));
        LongRangeUB.setMaximumSize(new Dimension(80,20));

        STextField LongRangeLB = new STextField("-180");
        LongRangeLB.setMinimumSize(new Dimension(80,20));
        LongRangeLB.setMaximumSize(new Dimension(80,20));

        SButton setRange = new SButton("Set");

        SLabel LatRangeLabelLB = new SLabel(setRange.sClicked.snapshot(LatRangeLB.text,
                (u,t) -> {
            try{
                int threshold = Integer.parseInt(t);
                if (threshold < -90){
                    threshold = -90;
                }
                if (threshold >90){
                    threshold = 90;
                }
                return Integer.toString(threshold);
            }catch (NumberFormatException e){
                return "-90";
            }
        }
        ).hold("-90"));
        LatRangeLabelLB.setMinimumSize(new Dimension(80,20));
        LatRangeLabelLB.setMaximumSize(new Dimension(80,20));

        SLabel LatRangeLabelUB = new SLabel(setRange.sClicked.snapshot(LatRangeUB.text,
                (u,t) -> {
                    try{
                        int threshold = Integer.parseInt(t);
                        if (threshold < -90){
                            threshold = -90;
                        }
                        if (threshold >90){
                            threshold = 90;
                        }
                        return Integer.toString(threshold);
                    }catch (NumberFormatException e){
                        return "90";
                    }
                }
        ).hold("90"));
        LatRangeLabelUB.setMinimumSize(new Dimension(80,20));
        LatRangeLabelUB.setMaximumSize(new Dimension(80,20));

        SLabel LongRangeLabelLB = new SLabel(setRange.sClicked.snapshot(LongRangeLB.text,
                (u,t) -> {
                    try{
                        int threshold = Integer.parseInt(t);
                        if (threshold < -180){
                            threshold = -180;
                        }
                        if (threshold >180){
                            threshold =180;
                        }
                        return Integer.toString(threshold);
                    }catch (NumberFormatException e){
                        return "-180";
                    }
                }
        ).hold("-180"));
        LongRangeLabelLB.setMinimumSize(new Dimension(80,20));
        LongRangeLabelLB.setMaximumSize(new Dimension(80,20));

        SLabel LongRangeLabelUB = new SLabel(setRange.sClicked.snapshot(LongRangeUB.text,
                (u,t) -> {
                    try{
                        int threshold = Integer.parseInt(t);
                        if (threshold < -180){
                            threshold = -180;
                        }
                        if (threshold >180){
                            threshold =180;
                        }
                        return Integer.toString(threshold);
                    }catch (NumberFormatException e){
                        return "180";
                    }
                }
        ).hold("180"));
        LongRangeLabelUB.setMinimumSize(new Dimension(80,20));
        LongRangeLabelUB.setMaximumSize(new Dimension(80,20));

        controlPanelLabel.add(new SLabel(new Cell<>("LatLB")));
        controlPanelLabel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanelLabel.add(LatRangeLabelLB);
        controlPanelLabel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanelLabel.add(new SLabel(new Cell<>("LatUB")));
        controlPanelLabel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanelLabel.add(LatRangeLabelUB);
        controlPanelLabel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanelLabel.add(new SLabel(new Cell<>("LongLB")));
        controlPanelLabel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanelLabel.add(LongRangeLabelLB);
        controlPanelLabel.add(new SLabel(new Cell<>("LongUB")));
        controlPanelLabel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanelLabel.add(LongRangeLabelUB);

        controlPanel.add(LatRangeLB);
        controlPanel.add(LatRangeUB);
        controlPanel.add(LongRangeLB);
        controlPanel.add(LongRangeUB);
        controlPanel.add(setRange);

        //Set latest event and filtered event
        Stream<String> latestEventStream = mergedStreams.map(
                i->i.name.replace("Tracker","")+","+
                        String.format("%3.3f",i.latitude)+","+
                        String.format("%3.3f",i.longitude)+","+
                        String.format("%d", (time.sample()-t0)/1000));
        SLabel eventOutputLabel = new SLabel(latestEventStream.hold(""));

        Stream<String> filteredEventStream = mergedStreams.filter(
                i -> (i.latitude >= Double.parseDouble(LatRangeLB.text.sample()))&&
                        (i.latitude <= Double.parseDouble(LatRangeUB.text.sample()))&&
                        (i.longitude >= Double.parseDouble(LongRangeLB.text.sample()))&&
                        (i.longitude <= Double.parseDouble(LongRangeUB.text.sample()))
        ).map(
                i->i.name.replace("Tracker","")+","+
                        String.format("%3.3f",i.latitude)+","+
                        String.format("%3.3f",i.longitude)+","+
                        String.format("%d", (time.sample()-t0)/1000));
        SLabel filteredEventOutputLabel = new SLabel(filteredEventStream.hold(""));

        eventOutputLabel.setBackground(Color.WHITE);
        eventOutputLabel.setOpaque(true);
        eventOutputLabel.setMinimumSize(new Dimension(120,20));
        eventOutputLabel.setMaximumSize(new Dimension(120,20));

        filteredEventOutputLabel.setBackground(Color.WHITE);
        filteredEventOutputLabel.setOpaque(true);
        filteredEventOutputLabel.setMinimumSize(new Dimension(120,20));
        filteredEventOutputLabel.setMaximumSize(new Dimension(120,20));

        eventOutput.add(new SLabel(new Cell<>("Latest Event")));
        eventOutput.add(Box.createRigidArea(new Dimension(10, 0)));
        eventOutput.add(eventOutputLabel);

        filteredEventOutput.add(new SLabel(new Cell<>("Filtered Event")));
        filteredEventOutput.add(Box.createRigidArea(new Dimension(10, 0)));
        filteredEventOutput.add(filteredEventOutputLabel);

        frame.add(mainPanel);
        frame.setSize(400,400);
        frame.setVisible(true);
    }
}
