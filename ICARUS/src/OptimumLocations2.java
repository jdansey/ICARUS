import java.awt.geom.Rectangle2D.Double;
import java.awt.geom.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.json.*;
import java.net.*;
/**
 * Used second optimum location function as alternative for when I scaled problem up to 10,000. Greatly increases time it takes to generate setup because it 
 * doesn't use optimum location it just places the towers in a grid like pattern. Kind of cheating but all areas of graph are covered by a tower
 */
public class OptimumLocations2 {
	private Set<CellPhone> cellPhones;
	private double towerPower;
	private Set<Location> locations;
	public Set<CellPhone> getCellPhones() {
		return cellPhones;
	}
	public void setCellPhones(Set<CellPhone> cellPhones) {
		this.cellPhones = cellPhones;
	}
	public double getTowerPower() {
		return towerPower;
	}
	public void setTowerPower(double towerPower) {
		this.towerPower = towerPower;
	}
	public Set<Location> getLocations() {
		return locations;
	}
	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}
	public OptimumLocations2(Set<CellPhone> cellPhones, double power){
		this.cellPhones = cellPhones;
		this.towerPower = power;
		this.locations = new HashSet<Location>();
	}
	/**
	 * NOT USED. makes circles around the boundary of every cell phone. checks what other cell phones are in that circle, if it is a new set of cell phones in the circle
	 * it keeps it as an optimal spot. Also removes subsets it finds. 
	 */
	public void optimizeLocation(){
		int x = 0;
		for(Iterator<CellPhone> it = this.cellPhones.iterator(); it.hasNext();){
			//create circles around outline of phone area test where it overlaps with other areas
			CellPhone phone = it.next();
			Vector<Point> phoneBoundary = phone.getPoints();
			double lat = phone.getLat();
			double lng = phone.getLng();
			for(Point point: phoneBoundary){
				//find center of circle being created
				double bearing = calculateBearing(lat,point.getLat(),lng,point.getLng());
				Point centerPoint = calculateNewPoint(this.towerPower, bearing, point.getLat(), point.getLng());
				//generate circle convert to java Area object
				Area circleArea = generateCircle(centerPoint.getLat(),centerPoint.getLng(),this.towerPower);
				Set<CellPhone> pointSet = new HashSet<CellPhone>();
				pointSet.add(phone);
				//test if circle area overlaps other cell phone areas
				for(CellPhone testPhone : this.cellPhones){
					if (testPhone == phone){
						continue;
					}else{
						if(testPhone.isOverlapping(circleArea)){
							pointSet.add(testPhone);
						}
					}
					
				}
				boolean newLocation = true;
				if(this.locations.size() == 0){
					this.locations.add(new Location(centerPoint.getLat(), centerPoint.getLng(),pointSet,x++));
					continue;
				}
				for(Iterator<Location> itLocation = this.locations.iterator(); itLocation.hasNext();){
					Location location = itLocation.next();
					if(location.getCellPhones().containsAll(pointSet)){
						newLocation = false;
					}else if(pointSet.containsAll(location.getCellPhones())){
						itLocation.remove();
						continue;
					}
				}
				if(newLocation){
					this.locations.add(new Location(centerPoint.getLat(), centerPoint.getLng(), pointSet, x++));
				}
			}
		}
	}
	/**
	 * function I used to scale problem. Places towers in a grid pattern to cover all areas of the graph checks which cell phones are in circle and 
	 * those are the locations for the cell tower
	 */
	public void patternGenerate(){
		double minLat = 0;
		double minLng = 0;
		double maxLat = 0;
		double maxLng = 0;
		int x = 0;
		for(Iterator<CellPhone> it = this.cellPhones.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			if(x++ == 0){
				minLat = phone.getLat();
				minLng = phone.getLng();
				maxLat = phone.getLat();
				maxLng = phone.getLng();
			}
			if(phone.getLat() < minLat){
				minLat = phone.getLat();
			}
			if(phone.getLng() < minLng){
				minLng = phone.getLng();
			}
			if(phone.getLat() > maxLat){
				maxLat = phone.getLat();
			}
			if(phone.getLng() > maxLng){
				maxLng = phone.getLng();
			}
		}
		Point point = new Point(minLat, minLng);
		int num = 0;
		while(point.getLat() <= maxLat){
			while(point.getLng() <= maxLng){
				Area circle = generateCircle(point.getLat(),point.getLng(), this.towerPower);
				Set<CellPhone> cellPhones = new HashSet<CellPhone>();
				for(CellPhone phone : this.cellPhones){
					if(phone.isOverlapping(circle)){
						cellPhones.add(phone);
					}
				}
				this.locations.add(new Location(point.getLat(),point.getLng(),cellPhones,num++));
				point.setLng(calculateNewPoint(this.towerPower,90,point.getLat(),point.getLng()).getLng());
			}
			point.setLat(calculateNewPoint(this.towerPower,0,point.getLat(),point.getLng()).getLat());
			point.setLng(minLng);
		}
	}
	/**
	 * generates a circle using center point and radius
	 * @param lat
	 * @param lng
	 * @param power - radius
	 * @return Java Area that is the circle
	 */
	private Area generateCircle(double lat, double lng, double power){
		double radians;
		Path2D.Double bounds = new Path2D.Double();
		for(radians = 0; radians <= 6.28; radians +=(2*Math.PI/40)){
			//find farthest point possible in direction of bearing
			Point farthestPoint = calculateNewPoint(power,radians,lat,lng);
			if (radians == 0){
				bounds.moveTo(farthestPoint.getLat(),farthestPoint.getLng());
			}else{
				bounds.lineTo(farthestPoint.getLat(), farthestPoint.getLng());
			}
		}
		bounds.closePath();
		Area circle = new Area(bounds);
		return circle;
	}
	/**
	 * generates bearing between two points
	 * @param lat1
	 * @param lat2
	 * @param lng1
	 * @param lng2
	 * @return double bearing between two points
	 */
	private double calculateBearing(double lat1, double lat2, double lng1, double lng2){
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		lng1 = Math.toRadians(lng1);
		lng2 = Math.toRadians(lng2);
		
		double y = Math.sin(lng2-lng1) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
		        Math.sin(lat1)*Math.cos(lat2)*Math.cos(lng2-lng1);
		double brng = Math.atan2(y, x);
		return brng;
	}
	/**
	 * calculates midpoint between two points
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return Point middle of the two points
	 */
	public Point midPoint(double lat1,double lon1,double lat2,double lon2){

	    double dLon = Math.toRadians(lon2 - lon1);

	    //convert to radians
	    lat1 = Math.toRadians(lat1);
	    lat2 = Math.toRadians(lat2);
	    lon1 = Math.toRadians(lon1);

	    double Bx = Math.cos(lat2) * Math.cos(dLon);
	    double By = Math.cos(lat2) * Math.sin(dLon);
	    double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
	    double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

	    //print out in degrees
	    //System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
	    return new Point(Math.toDegrees(lat3),Math.toDegrees(lon3));
	}
	/**
	 * takes distance and bearing and center and calculates that new point
	 * @param distance
	 * @param bearing
	 * @param lat
	 * @param lng
	 * @return Point new point in direction and distance given from inputs
	 */
	private Point calculateNewPoint(double distance, double bearing, double lat, double lng) {
		//convert lat/lng to radians and distance in relation to radius of earth
		//http://www.movable-type.co.uk/scripts/latlong.html
		double dist = distance/6371000.0;
		double brng = bearing;
		double lat1 = Math.toRadians(lat);
		double lon1 = Math.toRadians(lng);

		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
		double a = Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
		//System.out.println("a = " +  a);
		double lon2 = lon1 + a;

		lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;
		//convert from radians back to degrees
		lat2 = Math.toDegrees(lat2);
		lon2 = Math.toDegrees(lon2);
		Point newPoint = new Point(lat2,lon2);
		return newPoint;
	}

}
