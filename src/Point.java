/**
 * Point data structure representing 3D points in Euclidean plane.
 *
 * Static factory methods from to construct a Point object from GpsService fields.
 * Static method distance to calculate Euclidean distance.
 */

package src;

public class Point{
    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAlt() {
        return alt;
    }

    double lat;
    double lon;
    double alt;
    double x;
    double y;
    double z;
    public static final double radius_ = 6371000;

    public Point(double lat, double lon, double alt){
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;

        double lat_ = lat/180*Math.PI;
        double lon_ = lon/180*Math.PI;
        double alt_ = alt/3.281 + radius_;

        this.x = Math.cos(lat_)*Math.cos(lon_)*alt_;
        this.y = Math.cos(lat_)*Math.sin(lon_)*alt_;
        this.z = Math.sin(lat_)*alt_;
    }

    public static Point from(double lat, double lon, double alt){
        return new Point(lat,lon,alt);
    }

    public static Double distance(Point p1, Point p2){
        return Math.sqrt(Math.pow(p1.x - p2.x,2)+
                Math.pow(p1.y - p2.y,2)+
                Math.pow(p1.z - p2.z,2));
    }

    public static void main(String[] args) {
        Point p1 = Point.from(41.466138, 15.547839, 0);
        Point p2 = Point.from(41.467216, 15.547025, 0);
        System.out.println(Point.distance(p1,p2));
    }
}
