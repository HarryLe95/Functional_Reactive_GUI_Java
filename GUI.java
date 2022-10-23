import nz.sodium.Cell;
import nz.sodium.Stream;
import swidgets.SLabel;

import javax.swing.*;
import java.awt.*;

public class GUI {
    public static void main(String[] args) {

        // Initialise the GPS Service
        GpsService serv = new GpsService();
        // Retrieve Event Streams
        Stream<GpsEvent>[] streams = serv.getEventStreams();

        //GUI Frame
        JFrame frame = new JFrame("GUI App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        //Set up trackers:
        SLabel trackers_name[] = new SLabel[streams.length];
        SLabel trackers_lat[] = new SLabel[streams.length];
        SLabel trackers_long[] = new SLabel[streams.length];

        for (int i = 0; i < streams.length; i++){
            trackers_name[i] = new SLabel(new Cell<String>("Tracker " + Integer.toString(i)));
            trackers_lat[i] = new SLabel(streams[i].map(j->String.format("%.8f",j.latitude)).hold(""));
            trackers_long[i] = new SLabel(streams[i].map(j->String.format("%.8f",j.longitude)).hold(""));

            trackers_name[i].setSize(100,100);

            trackers_lat[i].setSize(120,100);
            trackers_lat[i].setBackground(Color.WHITE);
            trackers_lat[i].setOpaque(true);

            trackers_long[i].setSize(120,100);
            trackers_long[i].setBackground(Color.WHITE);
            trackers_long[i].setOpaque(true);

            frame.add(trackers_name[i]);
            frame.add(trackers_lat[i]);
            frame.add(trackers_long[i]);
        }




        frame.setSize(400,400);
        frame.setVisible(true);
    }
}
