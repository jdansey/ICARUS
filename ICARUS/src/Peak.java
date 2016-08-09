//SETUP
/**
 * container class which includes elevation at that specific point. NOT USED in current setup
 * @author Jacob.Dansey
 *
 */
public class Peak extends Point {
	private double elevation;
	//getters and setters
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	
	/**
	 * constructor
	 * @param lat
	 * @param lng
	 * @param elevation
	 */
	public Peak(double lat, double lng, double elevation) {
		super(lat, lng);
		this.setElevation(elevation);
	}
}
