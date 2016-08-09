import java.util.*;


/** container class for the current location of cell towers at that point in time. Used in array to make schedule
 * hasBeenMoved boolean for local search to know it has already been adjusted
 * increasedPriorityList for scheduled constraint cell phones to move up in priority
 * */
public class TimeBlock {
	private Location[] towerLocations;
	private boolean[] manualOverride;
	private int day;
	private boolean hasBeenMoved;
	private Set<CellPhone> increasedPriorityList;
	private Set<CellPhone>  currentCoverage;
	private double maxPotential;
	
	//getters and setters for variables
	public boolean hasBeenMoved() {
		return hasBeenMoved;
	}
	public void setHasBeenMoved(boolean hasBeenMoved) {
		this.hasBeenMoved = hasBeenMoved;
	}
	public Location[] getTowerLocations() {
		return towerLocations;
	}
	public void setTowerLocations(Location[] value) {
		this.towerLocations = new Location[value.length];
		for(int x = 0; x < value.length; x++){
			this.towerLocations[x] = value[x];
		}
	}
	public void setTower(Location location, int towerSpot){
		this.towerLocations[towerSpot] = location;
	}
	public Location getTower(int towerSpot){
		return this.towerLocations[towerSpot];
	}
	public Set<CellPhone> getIncreasedPriorityList() {
		return increasedPriorityList;
	}
	public void setIncreasedPriorityList(Set<CellPhone> increasedPriorityList) {
		this.increasedPriorityList = increasedPriorityList;
	}
	public Set<CellPhone> getCurrentCoverage() {
		return currentCoverage;
	}
	public void setCurrentCoverage(Set<CellPhone> currentValue) {
		this.currentCoverage = new HashSet<CellPhone>(currentValue);
	}
	public double getMaxPotential() {
		return maxPotential;
	}
	public void setMaxPotential(double maxPotential) {
		this.maxPotential = maxPotential;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public boolean[] getManualOverride() {
		return manualOverride;
	}
	public void setManualOverride(boolean[] manualOverride) {
		this.manualOverride = manualOverride;
	}
	/**
	 * main constructor standard domain and no increased priority
	 * @param timeBlock - used to calculate day
	 * @param sizeOfTimeBlock - used to calculate day
	 * @param numTowers - used to build location array
	 */
	public TimeBlock(int timeBlock, double sizeOfTimeBlock, int numTowers) {
		this.increasedPriorityList = new HashSet<CellPhone>();
		this.increasedPriorityList = new HashSet<CellPhone>();
		this.hasBeenMoved = false;
		this.day = (int) Math.floor(timeBlock*sizeOfTimeBlock/(24*60*60));
		this.towerLocations = new Location[numTowers];
		this.manualOverride = new boolean[numTowers];
		this.currentCoverage = new HashSet<CellPhone>();
	}
	
	/**
	 * add scheduled constraint by increasing priority of that cell phone for this time block
	 * @param phone - phone to add to set of increased priority
	 */
	public void addIncreasedPriority(CellPhone phone){
		this.increasedPriorityList.add(phone);
		
	}
	/**
	 * manually moving a tower to a specific location
	 * @param towerSpot - the tower
	 * @param location - where it is going to be locked in to
	 */
	public void userOverride(int towerSpot, Location location){
		this.setTower(location, towerSpot);
		manualOverride[towerSpot] = true;
	}
	
	
	
}
