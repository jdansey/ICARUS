//SETUP
/**
 * container class to hold connected latitude and longitude as a single point
 * @author Jacob.Dansey
 *
 */
public class Point {
	private double lat;
	private double lng;
	
	//getters and setters
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	
	/**
	 * main constructor
	 * @param lat
	 * @param lng
	 */
	public Point(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	
}
