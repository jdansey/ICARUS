import java.util.Set;

/**
 * Location class is a specific point for a cell tower along with which cell phones that point reaches
 * @author Jacob.Dansey
 *
 */
public class Location extends Point {
	private Set<CellPhone> cellPhones;
	private int number;
	private boolean isOffline;
	
	//getters and setters
	public Set<CellPhone> getCellPhones() {
		return cellPhones;
	}
	public Location(){
		super(0.0,0.0);
	}
	public void setCellPhones(Set<CellPhone> cellPhones) {
		this.cellPhones = cellPhones;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isOffline() {
		return isOffline;
	}

	public void setOffline(boolean isOffline) {
		this.isOffline = isOffline;
	}
	
	/**
	 * constructor
	 * @param lat - latitude of location
	 * @param lng - longitude of location
	 * @param cellPhones - set of cell phones that location services
	 * @param number - a way to differentiate all the locations
	 */
	public Location(double lat, double lng, Set<CellPhone> cellPhones, int number) {
		super(lat, lng);
		this.cellPhones = cellPhones;
		this.number = number;
		this.isOffline = false;
	}

}
