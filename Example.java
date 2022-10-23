import nz.sodium.*;

/** 
 * An example of how to use the GpsService class
 */
public class Example {

    public static void main(String[] args){

        // Initialise the GPS Service
        GpsService serv = new GpsService();
        // Retrieve Event Streams
        Stream<GpsEvent>[] streams = serv.getEventStreams();

        // Attach a handler method to each stream
        for(Stream<GpsEvent> s : streams){
            s.listen((GpsEvent ev) -> System.out.println(ev));
        }
    }

} 
