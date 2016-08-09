import java.util.Set;
import java.util.Vector;
//SETUP
/**
 * Set of potential cell phones used to check if optimally possible. NOT USED in current setup
 * @author Jacob.Dansey
 *
 */
public class LocationSet {
	private Set<CellPhone> cellPhoneSet;
	private Vector<Point> boundary;
	private boolean flag;
	
	//getters and setters
	public Set<CellPhone> getCellPhoneSet() {
		return cellPhoneSet;
	}
	public void setCellPhoneSet(Set<CellPhone> cellPhoneSet) {
		this.cellPhoneSet = cellPhoneSet;
	}
	public Vector<Point> getBoundary() {
		return boundary;
	}
	public void setBoundary(Vector<Point> boundary) {
		this.boundary = boundary;
	}
	
	public void addBoundaryPoint(Point point){
		this.boundary.add(point);
	}
	public boolean isContained(Set<CellPhone> set){
		return set.containsAll(cellPhoneSet);
	}
	public boolean isEqual(Set<CellPhone> set){
		return set.equals(cellPhoneSet);
	}
	public boolean isFlagged() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	/**
	 * constructor
	 * @param cellPhones
	 */
	public LocationSet(Set<CellPhone> cellPhones){
		this.cellPhoneSet = cellPhones;
		this.boundary = new Vector<Point>();
		this.flag = false;
	}

}
