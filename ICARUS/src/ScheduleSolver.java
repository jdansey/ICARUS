import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

//ScheduleSolver class takes in cell phones and cell towers and generates a weekly schedule
/** 
 * takes in cell phones and cell towers and generates a weekly schedule. Uses local search algorithm to generate schedule
 * @required CellTower.java CellPhone.java TimeBlock.java Location.Java
*/
public class ScheduleSolver {
	private Set<CellPhone> allCellPhones;	//used to check constraints
	private boolean dailyFirst;				//used to check if daily constraints are done yet for given day
	private CellTower[] cellTowers;	
	public static final int MAX_PRI = 3;
	public static final int MAX_SUB_PRI = 999;
	private TimeBlock[] schedule;
	private double sizeOfTimeBlock;			//allows for variable size blocks currently set at 15 min
	private List<List<Location>> domain;
	private PriorityQueue<ScheduledConstraint> prioritizedScheduledConstraints; //initial guess should approach biggest priority scheduled constraints first
	private int numLoops;
	
	//getters and setters
	public Set<CellPhone> getAllCellPhones() {
		return allCellPhones;
	}
	public void setAllCellPhones(Set<CellPhone> allCellPhones) {
		this.allCellPhones = allCellPhones;
	}
	public boolean isDailyFirst() {
		return dailyFirst;
	}
	public void setDailyFirst(boolean dailyFirst) {
		this.dailyFirst = dailyFirst;
	}
	public int getNumLoops() {
		return numLoops;
	}
	public void setNumLoops(int numLoops) {
		this.numLoops = numLoops;
	}
	public CellTower[] getCellTowers() {
		return cellTowers;
	}
	public void setCellTowers(CellTower[] cellTowers) {
		this.cellTowers = cellTowers;
	}
	public TimeBlock[] getSchedule() {
		return schedule;
	}
	public void setSchedule(TimeBlock[] schedule) {
		this.schedule = schedule;
	}
	public double getSizeOfTimeBlock() {
		return sizeOfTimeBlock;
	}
	public void setSizeOfTimeBlock(double sizeOfTimeBlock) {
		this.sizeOfTimeBlock = sizeOfTimeBlock;
	}
	public List<List<Location>> getDomain() {
		return domain;
	}
	public void setDomain(List<List<Location>> domain) {
		this.domain = domain;
	}
	
	public ScheduleSolver(){
		
	}
	
	/**
	  * constructs the schedule solver, uses a case structure to change order in which cell towers are picked.Note: Doesn't do much.
	  * Takes the array of cell towers and converts those locations in to a list of locations each cell tower can be add (domain).
	  * @param cellTower - array of cell towers used in schedule
	  * @param allCellPhones - a set of all cell phones that are in the problem
	  * @param arrSize - number of blocks wanted in a weekly schedule. Example 672 gives 15 minute weekly block sizes
	  * @param order - changes ordering of cell phones for small example of like 4. Was used to test if it made a difference. It kind of doesn't.
	  * @param numLoops - number of loops taken by local search before you time it out 
	  * @return ScheduleSolver - initialized with proper values
	*/
	public ScheduleSolver(CellTower[] cellTowers, Set<CellPhone> allCellPhones, int arrSize, int order, int numLoops) {
		super();
		//switch statement not really in use was used to test if order in which cell towers were used was a large influence
		if(cellTowers.length >= 4){ 
			switch(order){
				case 1:
					CellTower temp = cellTowers[0];
					cellTowers[0] = cellTowers[1];
					cellTowers[1] = temp;
					temp = cellTowers[2];
					cellTowers[2] = cellTowers[3];
					cellTowers[3] = temp;
					break;
				case 2:
					CellTower temp2 = cellTowers[1];
					cellTowers[1] = cellTowers[2];
					cellTowers[2] = temp2;
					break;
				case 3:
					CellTower temp3 = cellTowers[0];
					cellTowers[0] = cellTowers[3];
					cellTowers[3] = temp3;
					break;
				case 4:
					CellTower temp4 = cellTowers[2];
					cellTowers[2] = cellTowers[3];
					cellTowers[3] = temp4;
					break;
				case 5:
					CellTower temp5 = cellTowers[0];
					cellTowers[0] = cellTowers[1];
					cellTowers[1] = temp5;
					break;
				default:
			}
		}
		//set parameters to variables
		this.setNumLoops(numLoops);
		this.allCellPhones = allCellPhones;
		this.dailyFirst = true;
		this.cellTowers = cellTowers;
		//size of time block is number of seconds in a week divided by array size
		this.sizeOfTimeBlock = (double) 604800/arrSize;
		//generates priority queue used to take care of scheduled constraints first
		Comparator<ScheduledConstraint> comparator = new ScheduledConstraintComparator();
		this.prioritizedScheduledConstraints = new PriorityQueue<ScheduledConstraint>(10, comparator);
		//get set of all locations for each cell phone
		List<List<Location>> domain = new ArrayList<List<Location>>();
		for(CellTower cellTower : cellTowers){
			List<Location> locations = new ArrayList<Location>();
			locations.addAll(cellTower.getLocations());
			domain.add(locations);	
		}
		this.domain = domain;
		//create schedule
		this.schedule = new TimeBlock[arrSize];
		for(int x = 0; x < arrSize; x++){
			schedule[x] = new TimeBlock(x, this.sizeOfTimeBlock, this.cellTowers.length);
		
		}
	}

	/**
	 * top function that does schedule creation. Runs an initial guess to get a baseline of the schedule. Tests if it meets constraints.
	 * If not (usually the case), runs local search a set number of times to go back and optimize the schedule even more
	 * 
	 */
	public void generateSchedule(){
		//for measuring run time
		long startTime = System.currentTimeMillis();
		//intelligently make initial guess at schedule
		initialGuess();
		//if all constraints not met do local search
		if(allWeeklyConstraintsMet() && allDailyConstraintsMet() == 8){
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println(this.allCellPhones.size() + "\t" + elapsedTime);
			printSchedule();
			System.out.println("\nALL CONSTRAINTS MET");
		}else{
			//run local search and if all constraints not met return false
			boolean constraintsMet = localSearch(numLoops);
			//stop run-time
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println(this.allCellPhones.size() + "\t" + elapsedTime);
		    //print schedule values as well as actual schedule
			printValue();
			printValue2();
			printSchedule();
			printNoCoverage();
			if(constraintsMet){
				System.out.println("\nALL CONSTRAINTS MET");
			}
			
		}
	}
	
	/**
	 * print value of schedule
	 */
	private void printValue(){
		double filledValue = 0;
		//keep track of all phones that have been completed
		for(Iterator<CellPhone> it = this.allCellPhones.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			if(phone.getWeeklyConstraint() < (this.sizeOfTimeBlock*-1)){
				double numBlocks = phone.getWeeklyRequired()/this.sizeOfTimeBlock;
				filledValue += (phone.getValue()*numBlocks);
			}
		}
		//get aggregate value of all locations
		double value = 0;
		for(int x = 0; x < this.schedule.length; x++){
			value += valueOfLocations(x);
		}
		value += filledValue; //include phones that got all constraints filled
		value = value/this.schedule.length; //normalize for different array sizes
		System.out.println("\nAGGREGATE SCHEDULE VALUE / SIZE OF TIME BLOCK: " + value);
	}
	
	/**
	 * print percent complete * schedule value aggregate
	 */
	private void printValue2(){
		double value = 0;
		//iterate over all phones accruing value = % complete *value of phone
		for(Iterator<CellPhone> it = this.allCellPhones.iterator(); it.hasNext();){
			double percentComplete = 0;
			CellPhone phone = it.next();
			if(phone.getWeeklyConstraint() < 0){
				percentComplete = 1;
			}else{
				percentComplete = (phone.getWeeklyRequired() - phone.getWeeklyConstraint()) / phone.getWeeklyRequired();
			}
			value += percentComplete*phone.getValue();
		}
		System.out.println("PERCENT COMPLETE * CELLPHONE VALUE: " + value);
	}
	
	/**
	 * print cell phones that didn't get coverage along with priority
	 */
	private void printNoCoverage(){
		System.out.println("Phones that did not receive coverage");
		for(Iterator<CellPhone> it = this.allCellPhones.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			if(phone.getWeeklyConstraint() == phone.getWeeklyRequired()){
				double priority = phone.getMainPriority() + ((double) phone.getSubPriority()/1000);
				
				System.out.printf(phone.getName() + "\t"+ "%.3f\n", priority);
			}
		}
	}
	
	/**
	 * run through once through to get baseline for schedule. Handles scheduled constraints as priority first with priorityPicks().
	 * Loops over entire schedule assigning value based on variableChoices() function
	 * Time must be subtracted from each choice to propagate constraints
	 */
	private void initialGuess(){
		Set<CellPhone> cellPhonesInService = new HashSet<CellPhone>(); //keep track of cell phones that get coverage
		Set<CellPhone> previousConstraintFilled = new HashSet<CellPhone>();
		Location [] currentLocation = new Location[this.cellTowers.length]; //holds previous time blocks location
			
		//handle scheduled constraint time blocks first
		priorityPicks();
			
		//fill in rest of schedule
		for(int timeBlock = 0; timeBlock < this.schedule.length; timeBlock++){
			//skip over ones that have already been set and remove flag to use for local search
			if(schedule[timeBlock].hasBeenMoved()){
				schedule[timeBlock].setHasBeenMoved(false);
				continue;
			}
			//initialize location and get day value for block
			Location[] location = new Location[this.cellTowers.length];
			int day = schedule[timeBlock].getDay();
			
			//retrieve best location from function variableChoices then set tower location to those spots
			location = variableChoices(timeBlock, cellPhonesInService,previousConstraintFilled, currentLocation, day);
			this.schedule[timeBlock].setTowerLocations(location);
			
			//keep track of previous result
			currentLocation = location;
			//remember old constraints filled
			previousConstraintFilled.clear();
			for(CellPhone phone : cellPhonesInService){
				if(phone.getDailyConstraint(day) < 0){
					previousConstraintFilled.add(phone);
				}
			}
			//get new cell phone info each selection
			cellPhonesInService.clear();
			for(int y = 0; y < currentLocation.length; y++){
				for(CellPhone phone : currentLocation[y].getCellPhones()){
					cellPhonesInService.add(phone);
				}
			}
			subtractTime(cellPhonesInService, day);
			schedule[timeBlock].setCurrentCoverage(cellPhonesInService);
			//add back travel time
		}
		return;
	}
	
	/**
	 * place values down for scheduled constraints first. Follows order of priority queue to place highest priority cell phones in preferred
	 * time blocks first. Allows for most optimal schedule.
	 */
	private void priorityPicks(){
		while(this.prioritizedScheduledConstraints.size() > 0){ //for each constraint in the priority queue remove and optimize those time blocks
			ScheduledConstraint constraint = this.prioritizedScheduledConstraints.remove();
			Set<CellPhone> previousPhonesInService = new HashSet<CellPhone>();
			//walk through every block of time with increased priority
			for(int timeBlock = (int) constraint.getStartTime(); timeBlock <= constraint.getEndTime(); timeBlock++){
				//if already optimized then skip
				if(schedule[timeBlock].hasBeenMoved()){
					continue;
				}
				int day = schedule[timeBlock].getDay();
				
				//check if anything from previous restrictions has changed
				Set<CellPhone> previousPriority = new HashSet<CellPhone>();
				Set<CellPhone> currentPriority = new HashSet<CellPhone>();
				if(timeBlock != 0){
					previousPriority = schedule[timeBlock - 1].getIncreasedPriorityList();
				}
				currentPriority = schedule[timeBlock].getIncreasedPriorityList();
				boolean samePriority = currentPriority.containsAll(previousPriority) && previousPriority.containsAll(currentPriority);
				//if first time block or restricted phones changed then move to most useful locations
				if(previousPhonesInService.size() == 0 || !samePriority){
					schedule[timeBlock].setTowerLocations(mostUsefulLocations(timeBlock,day));
				}else{ 
					//check if any constraints have been filled
					Set<CellPhone> constraintFilled = new HashSet<CellPhone>();
					for(Iterator<CellPhone> it = previousPhonesInService.iterator(); it.hasNext();){
						CellPhone phone = it.next();
						if(phone.getDailyConstraint(day) < 0){
							constraintFilled.add(phone);
						}
					}
					//if so
					if(constraintFilled.size() > 0){
						//see which towers that applies too
						int towerSpot = 0;
						for(Location tower : schedule[timeBlock-1].getTowerLocations()){
							boolean isServicedBy = false;
							for(CellPhone phoneDone : constraintFilled){
								if(tower.getCellPhones().contains(phoneDone)){
									isServicedBy = true;
									break;
								}
							}
							if(isServicedBy){ //if tower has a constraint filled pick new location
								previousPhonesInService.removeAll(tower.getCellPhones());
								Location location = mostUsefulLocation(timeBlock, towerSpot,previousPhonesInService,day);
								schedule[timeBlock].setTower(location, towerSpot);
								previousPhonesInService.addAll(location.getCellPhones());
								towerSpot++;
							}else{ //else stay where its at
								schedule[timeBlock].setTower(schedule[timeBlock-1].getTower(towerSpot), towerSpot);
								towerSpot++;
							}
						}
					}else{ //if no priority changes and no constraints filled keep location
						schedule[timeBlock].setTowerLocations(schedule[timeBlock - 1].getTowerLocations());
					}
				}
				//set that it's been moved and subtract
				schedule[timeBlock].setHasBeenMoved(true);
				Set<CellPhone> phonesInService = new HashSet<CellPhone>();
				for(Location location : schedule[timeBlock].getTowerLocations()){
					phonesInService.addAll(location.getCellPhones());
				}
				schedule[timeBlock].setCurrentCoverage(phonesInService);
				previousPhonesInService = phonesInService;
				subtractTime(phonesInService,day);
			}
		}
	}

	/**
	 *  make choice for tower locations for this time block. If there were no changes from last time then stay, if there were
	 * use most useful location function.
	 * @param timeBlock - given block in schedule array
	 * @param cellPhonesInService - set of all cell phones that were in service from last time
	 * @param previousConstraintFilled - set of cell phones that had their constraints filled
	 * @param currentLocation - previous block of times location array for each tower
	 * @param day - the day of the current block
	 * @return Location[] - best locations for the towers to be at for given time block
	 */
	private Location[] variableChoices(int timeBlock, Set<CellPhone> cellPhonesInService, Set<CellPhone> previousConstraintFilled, Location [] currentLocation, int day){
		
		//on new day reset daily first and start over from most useful
		int amountOfHour = (int) Math.floor(3600/this.sizeOfTimeBlock);
		if((timeBlock % (24 * amountOfHour)) == 0){
			this.dailyFirst = true;
			return mostUsefulLocations(timeBlock,day);
		}
		
		
		//check if priorities changed from last time
		Set<CellPhone> previousPriority = new HashSet<CellPhone>();
		Set<CellPhone> currentPriority = new HashSet<CellPhone>();
		if(timeBlock != 0){
			previousPriority = schedule[timeBlock - 1].getIncreasedPriorityList();
		}
		currentPriority = schedule[timeBlock].getIncreasedPriorityList();
		boolean samePriority = currentPriority.containsAll(previousPriority) && previousPriority.containsAll(currentPriority);
		//if not then check if any constraints have been filled
		
		if(samePriority){
			Set<CellPhone> constraintFilled = new HashSet<CellPhone>();
			//make list of phones who filled constraints
			for(CellPhone phone : cellPhonesInService){
				if(phone.getDailyConstraint(day) < 0){
					constraintFilled.add(phone);
				}
			}
			if(constraintFilled.size() > previousConstraintFilled.size() && constraintFilled.containsAll(previousConstraintFilled)){
				//see which towers that applies too
				int towerSpot = 0;
				for(Location tower : schedule[timeBlock-1].getTowerLocations()){
					boolean isServicedBy = false;
					for(CellPhone phoneDone : constraintFilled){
						if(tower.getCellPhones().contains(phoneDone)){
							isServicedBy = true;
							break;
						}
					}
					if(isServicedBy){ //if tower has a constraint filled pick new location
						cellPhonesInService.removeAll(tower.getCellPhones());
						Location location = mostUsefulLocation(timeBlock, towerSpot,cellPhonesInService,day);
						schedule[timeBlock].setTower(location, towerSpot);
						cellPhonesInService.addAll(location.getCellPhones());
						towerSpot++;
					}else{ //else stay where its at
						schedule[timeBlock].setTower(schedule[timeBlock-1].getTower(towerSpot), towerSpot);
						towerSpot++;
					}
				}
				previousConstraintFilled = constraintFilled;
				return schedule[timeBlock].getTowerLocations();
			}else{ //if no constraints filled and same priority then return previous result
				previousConstraintFilled = constraintFilled;
				return schedule[timeBlock - 1].getTowerLocations();
			}
		}else{ //if priority changed then run most useful locations on it to find optimal
			return mostUsefulLocations(timeBlock,day);
		}
	}
	
	/**
	 * runs most useful location over every cell tower
	 * @param timeBlock - given block in schedule array
	 * @param day - day of current block
	 * @return Location[] - given no info about previous block find the best locations for towers
	 */
	private Location[] mostUsefulLocations(int timeBlock, int day){
		Set<CellPhone> phonesInService = new HashSet<CellPhone>();
		Location[] returnLocation = new Location[this.cellTowers.length];
		//for each cell tower pick best possible location
		for(int towerSpot = 0; towerSpot < cellTowers.length; towerSpot++){
			if(schedule[timeBlock].getManualOverride()[towerSpot]){
				returnLocation[towerSpot] = schedule[timeBlock].getTowerLocations()[towerSpot];
			}else{
				returnLocation[towerSpot] = mostUsefulLocation(timeBlock,towerSpot,phonesInService, day);
			}
			phonesInService.addAll(returnLocation[towerSpot].getCellPhones());
		}
		return returnLocation;
	}
	
	/**
	 * determine most useful location to place cell tower based on cell phone priority scores
	 * @param timeBlock - current block in schedule array
	 * @param towerSpot - current tower being worked on
	 * @param cellPhonesInService - keep track of cell phones that are being serviced by other towers
	 * @param day - current day of time block used for checking daily constraints
	 * @return Location - single location of most useful spot for a given tower
	 */
	private Location mostUsefulLocation(int timeBlock, int towerSpot, Set<CellPhone> cellPhonesInService, int day){
		double highestValue = 0;
		Location returnLocation = new Location();
		for(Location towerLocation : this.domain.get(towerSpot)){
			double sumAmount = 0;
			for(CellPhone phone : towerLocation.getCellPhones()){
				boolean constraintNotMet = false;
				//test correct constraint
				if(this.dailyFirst){
					constraintNotMet = phone.getDailyConstraint(day) > 0;
				}else{
					constraintNotMet = phone.getWeeklyConstraint() > 0;
				}
				
				if(!cellPhonesInService.contains(phone) && constraintNotMet){
					double value = phone.getValue();
					//add increased priority capability
					if(schedule[timeBlock].getIncreasedPriorityList().contains(phone)){
						value = phone.getRestrictedValue();
					}
					sumAmount += value;

				}
			}
			if(sumAmount > highestValue){
				highestValue = sumAmount;
				returnLocation = towerLocation;
			}
		}
		//if daily first but all daily are met turn daily first off and re run
		if(this.dailyFirst){
			if(highestValue == 0){
				this.dailyFirst = false;
				return mostUsefulLocation(timeBlock,towerSpot, cellPhonesInService,day);
			}
		} //else return location from calculation if nowhere else to go then stay
		if(highestValue == 0){
			if(timeBlock == 0){
				return schedule[timeBlock].getTowerLocations()[towerSpot];
			}
			return schedule[timeBlock-1].getTowerLocations()[towerSpot];
		}else{
			return returnLocation;
		}
		
	}
	
	/**
	 * calculate value of location spots by summing over all cell phones getting service
	 * @param timeBlock - given block from schedule array
	 * @return double - value of current location
	 */
	private double valueOfLocations(int timeBlock){
		double value = 0;
		double maxPotential = 0;
		int day = schedule[timeBlock].getDay();
		Set<CellPhone> phonesInService = schedule[timeBlock].getCurrentCoverage();
		for(CellPhone phone : phonesInService){
			if(schedule[timeBlock].getIncreasedPriorityList().contains(phone)){
				maxPotential += phone.getRestrictedValue();
			}else{
				maxPotential += phone.getValue();
			}
			//if the constraint is at least one time block away from being empty the value of it is zero 
			//continued value for if daily or weekly not met yet
			if(phone.getWeeklyConstraint() <= (this.sizeOfTimeBlock * -1) && phone.getDailyConstraint(day) < (this.sizeOfTimeBlock * -1)){
				continue;
			}
			if(schedule[timeBlock].getIncreasedPriorityList().contains(phone)){
				value += phone.getRestrictedValue();
			}else{
				value += phone.getValue();
			}
		}
		this.schedule[timeBlock].setMaxPotential(maxPotential);
		return value;
	}
	
	/**
	 * determine value of a given phone coverage and compare to its current state
	 * @param phonesInService - phones getting service to check against current state
	 * @param timeBlock - current block of schedule array
	 * @param currentValue - value of current time block
	 * @return boolean - true if greater value, false if current state is greater
	 */
	private boolean valueOfCoverage(Set<CellPhone> phonesInService, int timeBlock, double currentValue){
		double value = 0;
		double maxPotential = 0;
		int day = schedule[timeBlock].getDay();
		for(CellPhone phone : phonesInService){
			if(schedule[timeBlock].getIncreasedPriorityList().contains(phone)){
				maxPotential += phone.getRestrictedValue();
			}else{
				maxPotential += phone.getValue();
			}
			//if the constraint is at least one time block away from being empty the value of it is zero 
			//continued value for if daily or weekly not met yet
			if(phone.getWeeklyConstraint() <= (this.sizeOfTimeBlock * -1) && phone.getDailyConstraint(day) < (this.sizeOfTimeBlock * -1)){
				continue;
			}
			if(schedule[timeBlock].getIncreasedPriorityList().contains(phone)){
				value += phone.getRestrictedValue();
			}else{
				value += phone.getValue();
			}
		}
		if(value > currentValue){
			this.schedule[timeBlock].setMaxPotential(maxPotential);
			return true;
		}
		return false;
	}
	/**
	 * continue optimizing every spot until maxima reached. NEVER USED
	 * @param maxRuns - number of loops through schedule
	 */
	private void hillClimb(int maxRuns){
		boolean optimized = false;
		this.dailyFirst = false;
		int run = 0;
		while(!optimized && (maxRuns > run)){
			optimized = true;
			run++;
			for (int timeBlock = 0; timeBlock < this.schedule.length; timeBlock++){
				double currentValue = this.valueOfLocations(timeBlock);
				Location[] currentLocation = new Location[this.cellTowers.length];
				currentLocation = schedule[timeBlock].getTowerLocations();
				Location[] locations = optimizeLocation(timeBlock, schedule[timeBlock].getDay());
				if(this.isEqual(locations, currentLocation)){
					continue;
				}
				
				Set<CellPhone> phonesInService = new HashSet<CellPhone>();
				for(int x = 0; x < locations.length; x++){
					phonesInService.addAll(locations[x].getCellPhones());
				}
				if(valueOfCoverage(phonesInService, timeBlock, currentValue)){
					optimized = false;
					//grab current cell phones in coverage and set to previous
					Set<CellPhone> previousPhonesInService = new HashSet<CellPhone>();
					for(int y = 0; y < this.cellTowers.length; y++){
						for(CellPhone phone : schedule[timeBlock].getTowerLocations()[y].getCellPhones()){
							previousPhonesInService.add(phone);
						}
					}
					schedule[timeBlock].setTowerLocations(locations);
					
					//generate list of cell phones getting service
					Set<CellPhone> cellPhonesInService = new HashSet<CellPhone>();
					for(Location location : locations){
							cellPhonesInService.addAll(location.getCellPhones());
					}
					
					
					//subtract time from constraint lists of cell phones getting service and add back to cell phones no longer getting service
					Set<CellPhone> subtractSet = new HashSet<CellPhone>(cellPhonesInService);
					Set<CellPhone> addSet = new HashSet<CellPhone>(previousPhonesInService);
					subtractSet.removeAll(previousPhonesInService);
					addSet.removeAll(cellPhonesInService);
					subtractTime(subtractSet,schedule[timeBlock].getDay());
					addTime(addSet,schedule[timeBlock].getDay());
					schedule[timeBlock].setHasBeenMoved(true);
					schedule[timeBlock].setCurrentCoverage(cellPhonesInService);
				}
				
			}
		}
	}
	
	/**
	 * make adjustments after initial guess to fine tune schedule by focusing on getting optimal for week. Iterate over schedule picking lowest value
	 * and optimizing it max loops number of times. break out of loop if all constraints are met. 
	 * @param maxLoops - number of times pick the lowest time block and optimize
	 * @return boolean - true if all constraints met, false if not
	 */
	private boolean localSearch(int maxLoops){
		for(int x = 0; x <= maxLoops; x++){
			//if everything met, then exit local search and return true
			int dayNotMet = this.allDailyConstraintsMet();
			boolean isDailyMet = dayNotMet == 8;
			boolean isWeeklyMet = this.allWeeklyConstraintsMet();
			if(isDailyMet && isWeeklyMet){
				return true;
			}
			
			//pick a time block based on daily or weekly needs
			int timeBlock = 0;
			if(!isWeeklyMet){
				this.dailyFirst = false;
				timeBlock = timeChoice(false, dayNotMet);
			}else{
				this.dailyFirst = true;
				timeBlock = timeChoice(true, dayNotMet);
			}
			
			//grab current cell phones in coverage and set to previous
			Set<CellPhone> previousPhonesInService = new HashSet<CellPhone>();
			for(int y = 0; y < this.cellTowers.length; y++){
				for(CellPhone phone : schedule[timeBlock].getTowerLocations()[y].getCellPhones()){
					previousPhonesInService.add(phone);
				}
			}
			
			int day = schedule[timeBlock].getDay();
			//determine optimal location and grab previous blocks data
			Location[] value = optimizeLocation(timeBlock, day);
			
			//set value to optimal location
			schedule[timeBlock].setTowerLocations(value);
			
			//generate list of cell phones getting service
			Set<CellPhone> cellPhonesInService = new HashSet<CellPhone>();
			for(int y = 0; y < this.cellTowers.length; y++){
				for(CellPhone phone : schedule[timeBlock].getTowerLocations()[y].getCellPhones()){
					cellPhonesInService.add(phone);
				}
			}
			
			//subtract time from constraint lists of cell phones getting service and add back to cell phones no longer getting service
			Set<CellPhone> subtractSet = new HashSet<CellPhone>(cellPhonesInService);
			Set<CellPhone> addSet = new HashSet<CellPhone>(previousPhonesInService);
			subtractSet.removeAll(previousPhonesInService);
			addSet.removeAll(cellPhonesInService);
			subtractTime(subtractSet,day);
			addTime(addSet,day);
			schedule[timeBlock].setHasBeenMoved(true);
			schedule[timeBlock].setCurrentCoverage(cellPhonesInService);
			
		}
		return false;
	}
	
	/**
	 * for localSearch(): find lowest valued time block
	 * @param focusOnDaily - whether or not the daily or weekly constraints are in violation
	 * @param day - current day to be checking, only important if above focusOnDaily true
	 * @return int - lowest valued time block in schedule array
	 */
	private int timeChoice(boolean focusOnDaily, int day){
		double lowestValue = Double.POSITIVE_INFINITY;
		double maxPotential = Double.POSITIVE_INFINITY;
		int startBlock = 0;
		int endBlock = this.schedule.length;
		int returnBlock = 0;
		if(focusOnDaily){ //if daily is only issue then iterate only over specific day
			startBlock = this.schedule.length/7*day;
			endBlock = this.schedule.length/7*(day + 1);
		}
		for(int timeBlock = startBlock; timeBlock < endBlock; timeBlock++){
			//if it hasn't been moved and is lower than the current lowest make current choice
			double value = valueOfLocations(timeBlock);
			if(!schedule[timeBlock].hasBeenMoved() && value <= lowestValue){
				if(value == lowestValue && schedule[timeBlock].getMaxPotential() >= maxPotential){
					//do nothing if tied in value but max potential is higher
				}else{
					maxPotential = schedule[timeBlock].getMaxPotential();
					lowestValue = value;
					returnBlock = timeBlock;
				}
			}
		}
		return returnBlock;
	}
	
	/**
	 * for localSearch(): after initial guess optimize lowest valued time block to increase schedule value. If not manually ironclad, run most
	 * useful location on the cell tower
	 * @param timeBlock - given block from schedule array
	 * @param day - day of given block
	 * @return
	 */
	private Location[] optimizeLocation(int timeBlock, int day){
		Location[] currentLocation = new Location[this.cellTowers.length];
		System.arraycopy(schedule[timeBlock].getTowerLocations(), 0, currentLocation, 0, cellTowers.length);
		Set<CellPhone> currentPhonesInService = new HashSet<CellPhone>();
		//pick most useful locations for each tower
		for(int towerSpot = 0; towerSpot < currentLocation.length; towerSpot++){
			currentPhonesInService.clear();
			for(Location otherTowers : currentLocation){
				if(otherTowers != currentLocation[towerSpot]){
				currentPhonesInService.addAll(otherTowers.getCellPhones());
				}
			}
			//check if manual override in play
			if(schedule[timeBlock].getManualOverride()[towerSpot]){
				currentLocation[towerSpot] = schedule[timeBlock].getTowerLocations()[towerSpot];
			}else{
				currentLocation[towerSpot] = mostUsefulLocation(timeBlock, towerSpot, currentPhonesInService, day);
			}
		}
		return currentLocation;
	}

	/**
	 * subtract time from constraints based on cell phones in service
	 * @param cellPhonesInService - set of cell phones getting serviced for this time block
	 * @param day - given day need to know to reduce daily constraint properly
	 */
	private void subtractTime(Set<CellPhone> cellPhonesInService, int day){
		for(Iterator<CellPhone> it = cellPhonesInService.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			//function of cell phone that reduces weekly constraint and appropriate daily constraint
			phone.reduceTime(day, this.sizeOfTimeBlock);
		}
	}
	
	/**
	 * add time to constraints based on cell phones no longer in service
	 * @param cellPhonesNoLongerInService - set of cell phones that were readjusted and no longer getting service
	 * @param day - given day need to know to increase daily constraint properly
	 */
	private void addTime(Set<CellPhone> cellPhonesNoLongerInService, int day){
		for(Iterator<CellPhone> it = cellPhonesNoLongerInService.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			//function of cell phone that increases weekly constraint and appropriate daily constraint
			phone.addTime(day, this.sizeOfTimeBlock);
		}
	}
	
	/**
	 * check if all weekly constraints have been met by iterating over the set of cell phones
	 * @return boolean - true if all weekly constraints met, false if not
	 */
	private boolean allWeeklyConstraintsMet(){
		for(Iterator<CellPhone> it = this.allCellPhones.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			if(phone.getWeeklyConstraint() > 0){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * check if all daily constraints have been met by checking the value of each phone for each day
	 * @return int - returns the day of the constraint that was not met, 8 means all were met
	 */
	private int allDailyConstraintsMet(){
		for(Iterator<CellPhone> it = this.allCellPhones.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			for(int day = 0; day < 7; day++)
			if(phone.getDailyConstraint(day) > 0){
				return day;
			}
		}
		return 8;
	}
	
	//Not in use, checks if a specific day has been met
	/*private boolean specificDayConstraintsMet(int day){
		for(Iterator<CellPhone> it = this.allCellPhones.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			if(phone.getDailyConstraint(day) > 0){
				return false;
			}
		}
		return true;
	}*/
	
	/**
	 * add scheduled constraint by giving phone increased priority over that period of time. Also add to priority queue
	 * @param phone - given cell phone that is getting preferred status
	 * @param timeStart - start of time with preferred status
	 * @param timeEnd - end of time with preferred status
	 */
	public void addScheduledConstraint(CellPhone phone, double timeStart, double timeEnd){
		//associate time in seconds with proper time block
		double startTimeBlock = Math.floor(timeStart/this.sizeOfTimeBlock);
		double endTimeBlock = Math.floor(timeEnd/this.sizeOfTimeBlock);
		//add constraint to priority queue
		this.prioritizedScheduledConstraints.add(new ScheduledConstraint(phone,startTimeBlock,endTimeBlock));
		//iterate over blocks of time and add increased priority
		for(int x = (int)startTimeBlock; x < endTimeBlock;x++){
			schedule[x].addIncreasedPriority(phone);
		}	
	}
	
	/**
	 * add scheduled constraint after schedule has been generated. Iterate over time blocks adding increased priority and re-optimizing. Also
	 * re-run local search to fix cell phones that lost service
	 * @param cell - cell phone that is getting increased priority
	 * @param timeStart - start of time with preferred status
	 * @param timeEnd - end of time with preferred status.
	 */
	public void iterativeAddScheduledConstraint(CellPhone cell, double timeStart, double timeEnd){
		int startTimeBlock = (int) Math.floor(timeStart/this.sizeOfTimeBlock);
		int endTimeBlock = (int) Math.floor(timeEnd/this.sizeOfTimeBlock);
		//change increased priority and re-run most usefu
		for(int timeBlock = startTimeBlock; timeBlock < endTimeBlock;timeBlock++){
			schedule[timeBlock].addIncreasedPriority(cell);
			//grab current cell phones in coverage and set to previous
			Set<CellPhone> previousPhonesInService = new HashSet<CellPhone>();
			for(int y = 0; y < this.cellTowers.length; y++){
				for(CellPhone phone : schedule[timeBlock].getTowerLocations()[y].getCellPhones()){
					previousPhonesInService.add(phone);
				}
			}
			
			int day = schedule[timeBlock].getDay();
			//determine optimal location and grab previous blocks data
			Location[] value = optimizeLocation(timeBlock, day);
			
			//set value to optimal location
			schedule[timeBlock].setTowerLocations(value);
			
			//generate list of cell phones getting service
			Set<CellPhone> cellPhonesInService = new HashSet<CellPhone>();
			for(int y = 0; y < this.cellTowers.length; y++){
				for(CellPhone phone : schedule[timeBlock].getTowerLocations()[y].getCellPhones()){
					cellPhonesInService.add(phone);
				}
			}
			
			//subtract time from constraint lists of cell phones getting service and add back to cell phones no longer getting service
			Set<CellPhone> subtractSet = new HashSet<CellPhone>(cellPhonesInService);
			Set<CellPhone> addSet = new HashSet<CellPhone>(previousPhonesInService);
			subtractSet.removeAll(previousPhonesInService);
			addSet.removeAll(cellPhonesInService);
			subtractTime(subtractSet,day);
			addTime(addSet,day);
			schedule[timeBlock].setCurrentCoverage(cellPhonesInService);
			
		}
		//re-run local search over amount of blocks that got switched reset movement of day to contain local search?
		dayResetHasBeenMoved(schedule[startTimeBlock].getDay());
		int blocks = (int) Math.floor(endTimeBlock - startTimeBlock);
		localSearch((blocks));
		//re-print schedule
		System.out.println("Added " + cell.getName() + "scheduled priority");
		printSchedule();
		
	}
	
	/**
	 * reset hasBeenMoved for a given day
	 * @param day - day to reset movements
	 */
	private void dayResetHasBeenMoved(int day){
		int startBlock = this.schedule.length/7*day;
		int endBlock = this.schedule.length/7*(day + 1);
		for( int timeBlock = startBlock; timeBlock < endBlock; timeBlock++){
			schedule[timeBlock].setHasBeenMoved(false);
		}
	}
	
	/**
	 * user overrides system and manually moves a tower to a location for a duration. re-optimize over time blocks as well as run local search
	 * to fix cell phones that lost service
	 * @param towerSpot - user choice for moving tower
	 * @param location - user choice for where to place tower
	 * @param timeStart - time start of tower being locked
	 * @param timeEnd = time end of tower being locked
	 */
	public void manualChoice(int towerSpot, Location location, double timeStart, double timeEnd){
		int startTimeBlock = (int) Math.floor(timeStart/this.sizeOfTimeBlock);
		int endTimeBlock = (int) Math.floor(timeEnd/this.sizeOfTimeBlock);
		//change tower manual override and lock in place
		for(int timeBlock = startTimeBlock; timeBlock < endTimeBlock;timeBlock++){
			schedule[timeBlock].userOverride(towerSpot, location);
			//grab current cell phones in coverage and set to previous
			Set<CellPhone> previousPhonesInService = new HashSet<CellPhone>();
			for(int y = 0; y < this.cellTowers.length; y++){
				for(CellPhone phone : schedule[timeBlock].getTowerLocations()[y].getCellPhones()){
					previousPhonesInService.add(phone);
				}
			}
			
			int day = schedule[timeBlock].getDay();
			//determine optimal location and grab previous blocks data
			Location[] value = optimizeLocation(timeBlock, day);
			
			//set value to optimal location
			schedule[timeBlock].setTowerLocations(value);
			
			//generate list of cell phones getting service
			Set<CellPhone> cellPhonesInService = new HashSet<CellPhone>();
			for(int y = 0; y < this.cellTowers.length; y++){
				for(CellPhone phone : schedule[timeBlock].getTowerLocations()[y].getCellPhones()){
					cellPhonesInService.add(phone);
				}
			}
			
			//subtract time from constraint lists of cell phones getting service and add back to cell phones no longer getting service
			Set<CellPhone> subtractSet = new HashSet<CellPhone>(cellPhonesInService);
			Set<CellPhone> addSet = new HashSet<CellPhone>(previousPhonesInService);
			subtractSet.removeAll(previousPhonesInService);
			addSet.removeAll(cellPhonesInService);
			subtractTime(subtractSet,day);
			addTime(addSet,day);
			schedule[timeBlock].setCurrentCoverage(cellPhonesInService);
			
		}
		//re-run local search over amount of blocks that got switched reset movement of day to contain local search?
		dayResetHasBeenMoved(schedule[startTimeBlock].getDay());
		int blocks = (int) Math.floor(endTimeBlock - startTimeBlock);
		localSearch((blocks));
		//re-print schedule
		System.out.println("User moved Cell Tower " + towerSpot + "to Location " + location.getNumber());
		printSchedule();
	}
	/**
	 * print current schedule by day only on change of tower locations
	 */
	public void printSchedule(){
		//if you want to print the cell tower locations and what they mean
		/*int x = 0; 
		for(List<Location> locations : this.domain){
			System.out.println("--- Cell Tower " + x + " ---");
			x++;
			for(Location location : locations){
				String locString = Integer.toString(location.getNumber());
				for(CellPhone phone : location.getCellPhones()){
					locString = locString + " " + phone.getName();
				}
				System.out.println(locString + " " + location.getLat() + ", " + location.getLng() +"\n");
			}
		}*/
		String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
		Location[] currentLocation = schedule[0].getTowerLocations();
		int day = 0;
		System.out.println("--- " + days[day++] + " ---");
		System.out.println("TIME\tLOCATIONS\tVALUE\tPOTENTIAL");
		String locs = "";
		for(Location location : currentLocation){
			locs = locs + location.getNumber() + " ";
		}
		
		System.out.println("0:00" + "\t"+ locs + "\t" + valueOfLocations(0) + "\t" + schedule[0].getMaxPotential());
		Location[] previousLocation = currentLocation;
		
		for(int i = 1; i < schedule.length;i++){
			currentLocation = schedule[i].getTowerLocations();
			double value = valueOfLocations(i);
			if(printNewDay(i,days,day)){
				day++;
				locs = "";
				for(Location location : currentLocation){
					locs = locs + location.getNumber() + " ";
				}
				System.out.println(block2time(i) + "\t"+ locs + "\t" + value + "\t" + schedule[i].getMaxPotential());
				if(isEqual(currentLocation,previousLocation)){
					previousLocation = currentLocation;
				}
				continue;
			}
			if(!isEqual(currentLocation,previousLocation)){
				locs = "";
				for(Location location : currentLocation){
					locs = locs + location.getNumber() + " ";
				}
				System.out.println(block2time(i) + "\t"+ locs + "\t" + value + "\t" + schedule[i].getMaxPotential());
				previousLocation = currentLocation;
			}
			if(i == schedule.length-1){
				System.out.println(block2time(schedule.length) + "\tEND\n");
			}
		}
		
	}
	/**
	 * For printSchedule(): check if previous location matches current location
	 * @param currentLocation
	 * @param previousLocation
	 * @return
	 */
	private boolean isEqual(Location[] currentLocation, Location[]previousLocation){
		for(int x = 0; x < currentLocation.length;x++){
			if(currentLocation[x].getNumber() != previousLocation[x].getNumber()){
				return false;
			}
		}
		return true;
	}
	/**
	 * For printSchedule(): prints the day of the week after checking if it is a new day or not based on time block of schedule array
	 * @param x - current block of schedule
	 * @param days - string array container for days of the week
	 * @param day - what day it is to be used in days array
	 * @return boolean - true if new day, false if not
	 */
	private boolean printNewDay(int x,String[] days, int day){
		int amountOfHour = (int) Math.floor(3600/this.sizeOfTimeBlock);
		if((x % (24 * amountOfHour)) == 0){
			System.out.println("--- " + days[day] + " ---");
			return true;
		}
		return false;
	}
	
	/**
	 * For printSchedule(): converts time block to time string in format "HOUR:MINUTE" military time
	 * @param x - current time block
	 * @return String - converts time block to "Hour:Minute"
	 */
	private String block2time(int x){
		int amountOfHour = (int) Math.floor(3600/this.sizeOfTimeBlock);
		int hours = x/amountOfHour;
		hours = hours % 24;
		int minutes = x%amountOfHour*(60/amountOfHour);
		if(minutes == 0){
			return Integer.toString(hours) + ":00";
		}
		return Integer.toString(hours) + ":" + Integer.toString(minutes);
	}
		
	/**
	 * retrieve setup from geojson file
	 * @param fileName - name of geojson file to be retrieved
	 * @return StringBuffer - json string that contains infromation for setup of problem
	 */
	private static StringBuffer getPhoneData(String fileName){
		// This will reference one line at a time
		String line = null;
		StringBuffer response = new StringBuffer();
		try {
		    // FileReader reads text files in the default encoding.
		    FileReader fileReader = 
		        new FileReader(fileName);
		
		    // Always wrap FileReader in BufferedReader.
		    BufferedReader bufferedReader = 
		        new BufferedReader(fileReader);
		    
		    
		    while((line = bufferedReader.readLine()) != null) {
		        response.append(line);
		    }   
		
		    // Always close files.
		    bufferedReader.close();   
		    return response;
		}
		catch(FileNotFoundException ex) {
		   response.append("Unable to open file '" + fileName + "'"); 
		   System.out.println("Unable to open file '" + fileName + "'");
		   return response;
		   
		}
		catch(IOException ex) {
		    response.append("Error reading file '"  + fileName + "'");
		    System.out.println("Error reading file '"  + fileName + "'");
		    return response;
		    // Or we could just do this: 
		    // ex.printStackTrace();
		}
	}
	
	/**
	 * Parses geojson file and creates problem setup
	 * @param filename - name of file to be parsed
	 * @param arraySize - size of array for weekly schedule. Example 672 gives block sizes of 15 minutes
	 * @param order - doesn't really do much changes order in which some towers are picked
	 * @param numLoops - number of times local search picks a value to optimize
	 * @return ScheduleSolver - intialized with data from file
	 * @throws JSONException
	 */
	public static ScheduleSolver setupProblem(String filename, int arraySize, int order, int numLoops) throws JSONException {
		//get phone setup from file
		String responseString = getPhoneData(filename).toString();
		JSONObject response = new JSONObject(responseString);
		JSONArray features = response.getJSONArray("features");
		
		//create cell phones and place into HashSet
		Set<CellPhone> allCellPhones = new HashSet<CellPhone>();
		
		//create a list of lists of locations for cell tower generation
		List<List<Location>> domain = new ArrayList<List<Location>>();
		//create a list of cell tower attributes
		List<TowerAttributeContainer> container = new ArrayList<TowerAttributeContainer>();
		//parse cell phones and cell towers
		//Important cell towers must be located after cell phones in geojson file
		for(int num = 0; num < features.length(); num ++){
			String type = features.getJSONObject(num).getJSONObject("geometry").getString("type");
			if(type.contains("Polygon")){ //polygon mean it is a cell phone
				//retrieve information from properties
				double lat = features.getJSONObject(num).getJSONObject("properties").getDouble("lat");
				double lng = features.getJSONObject(num).getJSONObject("properties").getDouble("lng");
				double power = features.getJSONObject(num).getJSONObject("properties").getDouble("power");
				double dailyConstraint = features.getJSONObject(num).getJSONObject("properties").getDouble("dailyConstraint");
				double weeklyConstraint = features.getJSONObject(num).getJSONObject("properties").getDouble("weeklyConstraint");
				int mainPriority = features.getJSONObject(num).getJSONObject("properties").getInt("mainPriority");
				int subPriority = features.getJSONObject(num).getJSONObject("properties").getInt("subPriority");
				String name = features.getJSONObject(num).getJSONObject("properties").getString("name");
				String provider = features.getJSONObject(num).getJSONObject("properties").getString("type");
				//iterate over lats/lngs of polygon to make area
				JSONArray coordinates = features.getJSONObject(num).getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0);
				double[][]points = new double[coordinates.length()][2];
				for(int x = 0; x < coordinates.length(); x++){
					points[x][0] = coordinates.getJSONArray(x).getDouble(0);
					points[x][1] = coordinates.getJSONArray(x).getDouble(1);
				}
				//construct cell phone and place in set
				CellPhone phone = new CellPhone(lat, lng, power, points, dailyConstraint, weeklyConstraint, mainPriority, subPriority, name, provider);
				allCellPhones.add(phone);
			}else{ //else it is a tower location
				//retrieve information from properties
				int towerNum = features.getJSONObject(num).getJSONObject("properties").getInt("number");
				while(towerNum >= domain.size() || domain.size() == 0){
					domain.add(new ArrayList<Location>());
					container.add(new TowerAttributeContainer());
				}
				double power = features.getJSONObject(num).getJSONObject("properties").getDouble("power");
				double speed = features.getJSONObject(num).getJSONObject("properties").getDouble("speed");
				String tech = features.getJSONObject(num).getJSONObject("properties").getString("type");
				JSONArray coordinates = features.getJSONObject(num).getJSONObject("geometry").getJSONArray("coordinates");
				double lat = coordinates.getDouble(1);
				double lng = coordinates.getDouble(0);
				JSONArray cellNameArray = features.getJSONObject(num).getJSONObject("properties").getJSONArray("cellPhones");
				//place names of cell phones in a list
				List<String> phoneNames = new ArrayList<String>();
				for(int x = 0; x < cellNameArray.length(); x++){
					phoneNames.add(cellNameArray.getString(x));
				}
				//iterate over all cell phones and place those with same name in a set
				Set<CellPhone> cellPhones = new HashSet<CellPhone>();
				for(CellPhone phone : allCellPhones){
					if(phoneNames.contains(phone.getName())){
						cellPhones.add(phone);
					}
				}
				//construct Location with cellPhones and current length of list as number
				int n = domain.get(towerNum).size();
				//give proper numbering of location
				Location location = new Location(lat, lng, cellPhones, n);
				
				//place location in list
				domain.get(towerNum).add(location);
				//check if tower attributes have been recorded yet, if not record it
				container.set(towerNum, new TowerAttributeContainer(power,speed, tech));
			}
		}
		CellTower[] cellTowers = new CellTower[domain.size()];
		for(int x = 0; x < domain.size(); x++){
			cellTowers[x] = new CellTower(domain.get(x),container.get(x).getPower(),container.get(x).getSpeed(),container.get(x).getType());
		}
		//return a fully initialized schedule solver with proper values
		return new ScheduleSolver(cellTowers,allCellPhones,arraySize, order, numLoops);
	}
	
	/**
	 * function used to show distribution of values over a single time block for presentation purposes. NEVER USED
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public void generateAllLocationValues() throws FileNotFoundException, UnsupportedEncodingException{
		int first = 0;
		int second = 0;
		int third = 0;
		int fourth = 0;
		String line = "";
		PrintWriter writer = new PrintWriter("LocationValues.txt", "UTF-8");
		for(int z = 0; z < this.domain.get(2).size(); z++ ){
			for(int y = 0; y < this.domain.get(3).size(); y++ ){
				line += "\t" + z +"," + y;
			}
		}
		writer.println(line);
		line = "";
		Location[] locations = new Location[this.cellTowers.length];
		while(first < this.domain.get(0).size()){
			second = 0; third = 0; fourth = 0;
			while(second < this.domain.get(1).size()){
				third = 0; fourth = 0;
				line = first + "," + second;
				while(third < this.domain.get(2).size()){
					fourth = 0;
					while(fourth < this.domain.get(3).size()){
						locations[3] = this.domain.get(3).get(fourth);
						locations[2] = this.domain.get(2).get(third);
						locations[1] = this.domain.get(1).get(second);
						locations[0] = this.domain.get(0).get(first);
						double max = 0;
						Set<CellPhone> phones = new HashSet<CellPhone>();
						for(Location location : locations){
							phones.addAll(location.getCellPhones());
						}
						for(CellPhone phone : phones){
							max+= phone.getValue();
						}
						line += "\t" + max;
						fourth++;	
					}
					third++;
				}
				writer.println(line);
				second++;
			}
			first++;
		}
		writer.close();
	}
	
	public static void main(String [] args) throws JSONException {
		//insert geojson file name and size of weekly array you want
		int order = 2;
		ScheduleSolver algorithm = setupProblem("ICARUSSetup.geojson",672, order, 671);
		//add scheduled constraints
		
		
		//generate schedule
		algorithm.generateSchedule();
	}
}
		
		
		
		/*
		 * Manual Build old school setup to show how much better geojson reading is
		 */
		//generate cell phones
			/*double[][] cabinPoints = {{-106.364471,39.18721647242402},{-106.34507299081098,39.18695543667812},{-106.32829066121754,39.1783375932975},{-106.31016818596622,39.17463119821012},{-106.29692064881338,39.16409088820595},{-106.27801853933934,39.15902594578697},{-106.27850132546106,39.14043842626069},{-106.27387545828087,39.12779833752079},{-106.26857092356799,39.11616383246622},{-106.25753501914332,39.10512149438643},{-106.26868001567838,39.09198681808788},{-106.25641905936047,39.078686430354146},{-106.24223323838629,39.06112262347062},{-106.24340757721751,39.04405092575889},{-106.24878592757709,39.02667438256181},{-106.28222024099757,39.028104014390344},{-106.31241679618523,39.03636624967053},{-106.33537232669876,39.04767111425486},{-106.34596442179581,39.04779110510782},{-106.35786930561426,39.05965426787044},{-106.364471,39.060171377419934},{-106.3743174306068,39.043742385093154},{-106.38230847083564,39.04938806715249},{-106.38872231910402,39.05505296451079},{-106.40555035335933,39.0481105543418},{-106.40927882876002,39.057221814857584},{-106.42145091944558,39.059867263862245},{-106.43001421193206,39.066075824306246},{-106.40519193184235,39.08174846617384},{-106.39539544910754,39.088217414365594},{-106.38664504258188,39.09201747129287},{-106.388084227995,39.09492046812273},{-106.39615414278411,39.100008659130935},{-106.41129251762561,39.110530515823065},{-106.40226017942632,39.11332261352408},{-106.40155213954999,39.12078726291095},{-106.41278330195574,39.14358905684545},{-106.40330121036219,39.15111572047749},{-106.39100902450329,39.15535811105319},{-106.37927953721348,39.16451898714492},{-106.364471,39.18721647242402}};
			double[][] lakePoints = {{-106.585832,38.88728427482806},{-106.57128468874313,38.90685505325521},{-106.55466773617745,38.91001166063449},{-106.53957117752196,38.90602361591494},{-106.52879279854278,38.89646608365018},{-106.51710610424317,38.88885267060347},{-106.50608080301801,38.88045891794641},{-106.50113901367484,38.86894747883977},{-106.48665147356878,38.86043154792511},{-106.49428953144135,38.846636586384385},{-106.48991551087865,38.83534121595519},{-106.49483101110388,38.82411553116758},{-106.48806273452459,38.810586991152135},{-106.50822962592582,38.80454358246694},{-106.52741755267759,38.80229316263697},{-106.53714399163829,38.79742602855778},{-106.54873381216026,38.795577951382896},{-106.55042631071417,38.781204171148566},{-106.5599856260954,38.77336007156399},{-106.571845370764,38.76652495419867},{-106.585832,38.785539384423444},{-106.59309451911554,38.79964370760091},{-106.59843709955734,38.80514989498714},{-106.60519071113924,38.805772943643525},{-106.62356517566005,38.79489846701324},{-106.63005346060841,38.800898076779845},{-106.63497812135972,38.8075472678473},{-106.62585828950981,38.819486258982224},{-106.6128229683564,38.828542848719685},{-106.62320153525228,38.830765741285255},{-106.62004004512474,38.83537619767723},{-106.61312654603846,38.83874423502834},{-106.61945040497977,38.84388443454759},{-106.61556465339098,38.84717696247805},{-106.61999480641795,38.8547068347077},{-106.62262726387344,38.86402661190179},{-106.61925087483102,38.87118842543596},{-106.61127181725568,38.874250545068506},{-106.60540397652667,38.882268957543964},{-106.59643865029417,38.8875088295821},{-106.585832,38.88728427482806}};
			double[][] ceoPoints = {{-106.096297,38.835741283669385},{-106.06387966633639,38.828257560344674},{-106.03263038147048,38.821478649211734},{-106.01097826816302,38.79928169013013},{-105.99407168607263,38.7784723968335},{-105.97465666914434,38.76361159324394},{-105.95520982679825,38.74868106362976},{-105.9646082627396,38.72108698511022},{-105.9584276993371,38.70370925061932},{-105.96282320171396,38.685259926111435},{-105.93621036050284,38.66870989589539},{-105.9332411856616,38.64854363570387},{-105.95443103056103,38.63271910382093},{-105.9343369362882,38.60422308504994},{-105.93983991509904,38.57986676278127},{-105.97447514517489,38.573523712032596},{-105.94239778582876,38.50295993522583},{-105.96929339065645,38.473617926053365},{-106.02224053569337,38.490386055617215},{-106.06051144946639,38.49197381415103},{-106.096297,38.502899623785204},{-106.1297100876689,38.50369524960588},{-106.15146792098741,38.53595479132009},{-106.17273268627386,38.55143841062876},{-106.19749941248071,38.559863232547485},{-106.19638921100024,38.59051534925827},{-106.20518126435653,38.6069636386518},{-106.18033500279338,38.6353329416355},{-106.19061221211948,38.64484967842361},{-106.17948343765362,38.65849000549686},{-106.20123605588215,38.668780731300544},{-106.20775301389388,38.68255661803917},{-106.1744852953626,38.68859454176095},{-106.21435296240838,38.71568265970235},{-106.16051980332277,38.705164767169336},{-106.1797560920442,38.73387966220886},{-106.21037619276952,38.79116252689457},{-106.20227281599713,38.83079366264019},{-106.17206414197629,38.85039535955093},{-106.13469990480384,38.8576214080994},{-106.09629700000002,38.835741283669385}};
			double[][] internPoints = {{-106.12391700000002,39.46853408029594},{-106.11480504813538,39.46798011791068},{-106.1059178888765,39.46633189722835},{-106.09747473786162,39.46363007859345},{-106.08968379823659,39.459941307596004},{-106.0827371041269,39.45535656246508},{-106.07680577300779,39.4499888993141},{-106.07203578574808,39.44397065219216},{-106.06854439885409,39.43745015847125},{-106.06641727657073,39.43058809178255},{-106.06570641153827,39.42355349421774},{-106.06642888224408,39.41651960661576},{-106.06877728197473,39.40971252161739},{-106.075450921986,39.40447663345177},{-106.08112530431987,39.39953547321025},{-106.08567786657494,39.394010284217174},{-106.09088154902608,39.38842137818924},{-106.1024874719594,39.391062261618515},{-106.10593996420731,39.38080133241129},{-106.11481665382482,39.37915517212512},{-106.12391700000002,39.37860191970407},{-106.1325537988752,39.38141854507978},{-106.14189403579272,39.38080133241129},{-106.1503288780572,39.38349994190451},{-106.15811448314079,39.38718466914852},{-106.16505933909924,39.391764931745534},{-106.17099250838558,39.39712811236397},{-106.17576783019642,39.40314231573604},{-106.17926752584088,39.40965960031969},{-106.18140511775589,39.41651960661576},{-106.1821275884617,39.42355349421774},{-106.18141672342928,39.43058809178255},{-106.17928960114594,39.43745015847125},{-106.17579821425193,39.44397065219216},{-106.17102822699222,39.4499888993141},{-106.1650968958731,39.45535656246508},{-106.15815020176342,39.459941307596004},{-106.1503592621385,39.46363007859345},{-106.1419161111235,39.46633189722835},{-106.13302895186463,39.46798011791068},{-106.12391700000002,39.46853408029594}};
			double[][] phone5Points = {{-105.67199999999997,39.36043607885631}, {-105.65291487043616,39.34887683780164}, {-105.63970214601768,39.33258113268706}, {-105.6297447505213,39.319842524779695}, {-105.62291278743935,39.3079592052792}, {-105.61502373890676,39.2997738229322}, {-105.60633467199085,39.29259863209982}, {-105.60695110368351,39.28132133315089}, {-105.60321549015009,39.2729651864516}, {-105.58128292928161,39.26678808643333}, {-105.57875821173347,39.25565636972258}, {-105.57555786875587,39.24382003717342}, {-105.56135876726395,39.22779932944486}, {-105.54928382235393,39.20717124157876}, {-105.54682444876065,39.185137420634575}, {-105.59108852513572,39.19296149006893}, {-105.59838699924272,39.1771205966863}, {-105.61401382681674,39.16743693501778}, {-105.62417113088249,39.1415133220309}, {-105.64815108330502,39.138907396997915}, {-105.67199999999997,39.12532218950368}, {-105.69710613338844,39.13273966520736}, {-105.71778042543599,39.146413687739}, {-105.74029464686576,39.1517351598894}, {-105.75874661476496,39.16309188145899}, {-105.76796169748845,39.18126956229224}, {-105.77033660291997,39.20028436648571}, {-105.78558197585555,39.210801909760995}, {-105.78208964250877,39.22793395950154}, {-105.80825271989912,39.238902666634914}, {-105.79933052188503,39.25562889846974}, {-105.81684228320155,39.27336637553254}, {-105.78440074747704,39.283911222922036}, {-105.76737894420404,39.29326617276253}, {-105.78555819570128,39.31946670441702}, {-105.76736189263967,39.32942230430918}, {-105.75435991647454,39.34333691036643}, {-105.73674247853734,39.35393047122745}, {-105.71570791886793,39.3596893340868}, {-105.6932439487896,39.35939784807468}}; 
			double[][] phone6Points = {{-105.68023681640632,38.95170689533217}, {-105.66611902367832,38.951799103571624}, {-105.65183304686468,38.95046124487874}, {-105.63835231373382,38.94640559942324}, {-105.62604420255812,38.94048428680736}, {-105.6148366726569,38.93333627758625}, {-105.6054200765188,38.92474597349803}, {-105.5978489798694,38.915114936798325}, {-105.59230931734902,38.90468068763958}, {-105.5889367317515,38.89370044657686}, {-105.59327892375987,38.882447672745776}, {-105.60422555971252,38.873082218914725}, {-105.61488890530178,38.865927730451496}, {-105.62773400167994,38.86163617617013}, {-105.63180541414913,38.85506821415129}, {-105.62760832321376,38.84147725521902}, {-105.63167699567057,38.830404639587286}, {-105.64904828123244,38.83479571030935}, {-105.66005739764678,38.83410079021218}, {-105.67128116476225,38.83843734040335}, {-105.68023681640632,38.835580137678}, {-105.68974157950802,38.83573487629183}, {-105.69934354473,38.83667344546049}, {-105.70957616207764,38.83762485498928}, {-105.71702927067759,38.843032405336174}, {-105.72220129952109,38.84978943557181}, {-105.73761976918108,38.8499980263276}, {-105.7454325197445,38.85659347020887}, {-105.74942583371053,38.86495606761186}, {-105.74738929894622,38.87418057007357}, {-105.74580296785835,38.882461512739326}, {-105.74525599394683,38.89047850411421}, {-105.74290887822445,38.89831241705821}, {-105.7415362408893,38.906764556920656}, {-105.73949704048309,38.91596578820636}, {-105.72919727131027,38.92056273215931}, {-105.72884894499538,38.934516917615504}, {-105.72055622549573,38.94401807707774}, {-105.70726910259997,38.94718145928024}, {-105.69330039175367,38.94662688890322}};
			double[][] phone7Points = {{-105.34239999999998,38.57158323879431}, {-105.32449787132875,38.5849294391887}, {-105.30426174330645,38.58834107313697}, {-105.29001725908326,38.576944772320644}, {-105.2614652598773,38.58364904201367}, {-105.23649987042386,38.57933926860116}, {-105.22421387354231,38.563676924932814}, {-105.20254362619542,38.55223374709681}, {-105.18779553424218,38.535781142662785}, {-105.17981045629081,38.51662818869237}, {-105.10445851669544,38.496349948465095}, {-105.10288820043998,38.466643560809565}, {-105.11929106928392,38.43960247131673}, {-105.13820162040791,38.41489530358563}, {-105.16446621301063,38.39514239718125}, {-105.1967604301898,38.38234495253233}, {-105.21264895526438,38.35648534955287}, {-105.27454742922212,38.39218451448442}, {-105.30537884883239,38.40725542603556}, {-105.32511637953355,38.411024852118196}, {-105.34239999999998,38.3774263567729}, {-105.36603101916162,38.379617961633635}, {-105.38265039193716,38.39947811376535}, {-105.40434406718286,38.401286308127005}, {-105.43449749380005,38.39721392699025}, {-105.4476019400466,38.41412210346268}, {-105.46300470663317,38.42789343138693}, {-105.46768649075615,38.44652579536379}, {-105.48024520680168,38.46140322741456}, {-105.47519027938682,38.48004147990356}, {-105.47427160333883,38.496523483840356}, {-105.45742448315521,38.51079699456309}, {-105.45579710311583,38.52536545492296}, {-105.45152641491357,38.54003880200477}, {-105.458087700934,38.56224277713456}, {-105.45385630132279,38.583653820657915}, {-105.42590460489812,38.586407694173865}, {-105.39860186034473,38.58279880731541}, {-105.3685605873241,38.55951447213568}, {-105.36168504171737,38.59176161439534}};
			double[][] phone8Points = {{-105.1776,39.11691180876944}, {-105.15426776901941,39.114295027555485}, {-105.1316376405027,39.10975103799799}, {-105.11046124426441,39.10223150133016}, {-105.09256221617719,39.090809325854096}, {-105.07476532432005,39.07977961257518}, {-105.06105865576626,39.065683151138664}, {-105.04722774338248,39.05151265133761}, {-105.0449023980838,39.03341436752951}, {-105.03974179535993,39.01688147754891}, {-105.03637899265503,38.999914875132184}, {-105.04590423171763,38.98371011629353}, {-105.04906511210969,38.9674547400706}, {-105.06227916032037,38.954246653834666}, {-105.08046202333195,38.94506741649265}, {-105.09988555977444,38.93952666361395}, {-105.11404166134831,38.93193007347716}, {-105.13031147990891,38.927787867819696}, {-105.14448732473508,38.92070434759889}, {-105.15891313138135,38.908188500897765}, {-105.1776,38.913224181598125}, {-105.19468312875948,38.9160763703743}, {-105.2124171279161,38.91661697962644}, {-105.23578164451946,38.91113280194064}, {-105.25399532584642,38.918162263473114}, {-105.26781051004842,38.9297876296606}, {-105.28440180069903,38.93959298640545}, {-105.29501713208231,38.95341605833582}, {-105.30200569104525,38.96850498124599}, {-105.31607825912673,38.98286784731777}, {-105.30467366955575,38.99992902081429}, {-105.3026993028988,39.01532552525148}, {-105.29582693503342,39.029779527272616}, {-105.28319321157788,39.04173875742932}, {-105.2813180177807,39.05846351818976}, {-105.28251035811444,39.08138696911106}, {-105.26614329436227,39.094550201628046}, {-105.2459979073895,39.10414923211156}, {-105.22416093394641,39.111180492105}, {-105.2011720938242,39.11547006083628}, {-105.1776,39.11691180876944}};
			double[][] phone9Points = {{-105.58960000000005,38.30042537665106}, {-105.57030264613168,38.29711826741088}, {-105.55474877670707,38.285687036561185}, {-105.53238019863561,38.28962927819472}, {-105.51786976296383,38.27897872957496}, {-105.50050040286193,38.271417151167675}, {-105.49384934192274,38.25608609188686}, {-105.48169895279953,38.244626839300714}, {-105.47860380016095,38.229776451300395}, {-105.46523871776239,38.21690975410821}, {-105.46585624841278,38.20143277305245}, {-105.46529157382162,38.18595912023034}, {-105.46992677188793,38.1708696269134}, {-105.4775050267828,38.15653551384979}, {-105.4878383596095,38.14330918861258}, {-105.5006714492052,38.13151564117141}, {-105.51568800398688,38.12144450868457}, {-105.53501853386297,38.11720862143148}, {-105.55542313828084,38.11873983202074}, {-105.57401547202726,38.12409217178712}, {-105.58960000000005,38.13368645230963}, {-105.59935352825443,38.15307018797661}, {-105.60913757461053,38.1542127327336}, {-105.61522741217605,38.16194930606119}, {-105.61761535794211,38.171179304314755}, {-105.62216382996617,38.17589538677249}, {-105.62469288930015,38.181448262081084}, {-105.63213848079077,38.184454102008246}, {-105.6331285777162,38.19037463448535}, {-105.63896061363724,38.19534487093393}, {-105.63681505358349,38.20148459458482}, {-105.64897844773913,38.20887402508637}, {-105.64671532828531,38.21606451617882}, {-105.64786336512444,38.224806317229906}, {-105.65390024058804,38.23817404327644}, {-105.66323518883094,38.25929305243678}, {-105.66330981528138,38.28111484924912}, {-105.64671203820865,38.28946327611456}, {-105.62855090049112,38.2955771935615}, {-105.60931919920793,38.29920579301993}};
			double[][] phone10Points = {{-105.0568,39.627415711030025}, {-105.02798971856785,39.625098636409106}, {-104.99998394372945,39.61967667082873}, {-104.97383959827177,39.61040558776186}, {-104.9523610389542,39.595717675759445}, {-104.93993912036855,39.575013496024596}, {-104.92691635120998,39.55767790153173}, {-104.91964958928696,39.53880878498198}, {-104.92202418610485,39.5186999301489}, {-104.92066268738317,39.50155445810782}, {-104.92692181110932,39.48492400253968}, {-104.92988813426139,39.46940395960785}, {-104.93677302238991,39.454822619397724}, {-104.92946048528286,39.434816580222055}, {-104.94010792076801,39.419445955997574}, {-104.95530935143219,39.406536868586045}, {-104.97956916415055,39.40283501250839}, {-105.000405143337,39.399453130121195}, {-105.02103514087041,39.399931259005875}, {-105.04085580394532,39.407197118612494}, {-105.0568,39.40090088742795}, {-105.07265661024255,39.40764428898875}, {-105.085795651403,39.41605297188556}, {-105.10088746830273,39.418145647084536}, {-105.10977766570723,39.42865432604146}, {-105.11945466081688,39.436591160507554}, {-105.11148100958773,39.4542969945525}, {-105.10575567160191,39.46570834360752}, {-105.13748725419373,39.46472722322749}, {-105.13993597137818,39.474800816294994}, {-105.16548327640994,39.4849478454977}, {-105.14141797240548,39.49530023235553}, {-105.14024333789648,39.50588616995671}, {-105.13852071426473,39.51707534867232}, {-105.16129420658801,39.54349757015425}, {-105.15183751765133,39.558229962315934}, {-105.1458226452382,39.57939815503215}, {-105.1340183731393,39.6017404964228}, {-105.11425905051351,39.621201048666734}, {-105.08586381545348,39.62633160362621}};
			CellPhone cabin = new CellPhone(39.092029, -106.364471,15000, cabinPoints,6*60*60,74*60*60,2,350, "Cabin");
			CellPhone lake = new CellPhone(38.835383, -106.585832,12000, lakePoints,4*60*60,56*60*60,3,1, "Lake");
			CellPhone ceo = new CellPhone(38.668834, -106.096297,25000,ceoPoints, 12*60*60,130*60*60,1,100, "CEO");
			CellPhone intern = new CellPhone(39.423568, -106.123917,5000, internPoints,3*60*60,20*60*60,3,100, "Intern");
			CellPhone developer = new CellPhone(39.2557,-105.6720,17000, phone5Points,8*60*60,56*60*60,1,700, "Developer");
			CellPhone driver = new CellPhone(38.8825,-105.6802, 8000, phone6Points,4*60*60,30*60*60,2,200, "Driver");
			CellPhone flats = new CellPhone(39.0000,-105.1776,13000, phone8Points,6*60*60,28*60*60,2,400, "Flats");
			CellPhone jogger = new CellPhone(38.4966,-105.3424, 22000, phone7Points,2*60*60,15*60*60,3,50, "Jogger");
			CellPhone cliff = new CellPhone(38.2015,-105.5896,11000, phone9Points,5*60*60,35*60*60,2,599, "Cliff");
			CellPhone suburban = new CellPhone(	39.4850, -105.0568, 16000, phone10Points,7*60*60,50*60*60,1,999, "Suburban");
			CellPhone offline1 = new CellPhone(0*60*60,3*60*60,1,200,"Offline");
			CellPhone offline2 = new CellPhone(0*60*60,3*60*60,1,200,"Offline");
			//create set
			CellPhone[] allCells = new CellPhone[] {cabin, lake, ceo, intern, developer, driver, flats, jogger, cliff, suburban ,offline1, offline2};
			Set<CellPhone> set1 = new HashSet<CellPhone>();
			Set<CellPhone> set2 = new HashSet<CellPhone>();
			Set<CellPhone> set3 = new HashSet<CellPhone>();
			Set<CellPhone> set4 = new HashSet<CellPhone>();
			Set<CellPhone> set5 = new HashSet<CellPhone>();
			Set<CellPhone> set6 = new HashSet<CellPhone>();
			Set<CellPhone> set7 = new HashSet<CellPhone>();
			Set<CellPhone> set8 = new HashSet<CellPhone>();
			Set<CellPhone> set9 = new HashSet<CellPhone>();
			Set<CellPhone> offlineSet1 = new HashSet<CellPhone>();
			Set<CellPhone> offlineSet2 = new HashSet<CellPhone>();
			Set<CellPhone> allCellPhones = new HashSet<CellPhone>(Arrays.asList(allCells));
			set1.add(cabin);
			set1.add(lake);
			set1.add(ceo);
			set2.add(cabin);
			set2.add(intern);
			set3.add(intern);
			set3.add(developer);
			set4.add(ceo);
			set4.add(driver);
			set5.add(driver);
			set5.add(jogger);
			set6.add(developer);
			set6.add(driver);
			set6.add(flats);
			set7.add(jogger);
			set7.add(cliff);
			set8.add(flats);
			set8.add(jogger);
			set9.add(suburban);
			set9.add(flats);
			offlineSet1.add(offline1);
			offlineSet2.add(offline2);//offline location
		//create optimal locations
			Location location1 = new Location(38.90460104675991,-106.2976510703916,set1,1);
			Location location2 = new Location(39.23522752528903,-106.1688355088951, set2,2);
			Location location10 = new Location(0.0,0.0,offlineSet1,99);
			Location location11 = new Location(0.0,0.0,offlineSet2,99);
			Location location3 = new Location(39.3393, -106.0056, set3,3);
			Location location4 = new Location(38.8359,-105.9363, set4, 4);
			Location location5 = new Location(38.7027,-105.5453, set5, 5);
			Location location6 = new Location(39.0482,-105.5128, set6, 6);
			Location location7 = new Location(38.3211,-105.3023, set7, 7);
			Location location8 = new Location(38.7339,-105.1403, set8, 8);
			Location location9 = new Location(39.2610,-105.1088, set9, 9);
			location10.setOffline(true);
			location11.setOffline(true);
			List<Location> locationsCellTower1 = new ArrayList<Location>();
			List<Location> locationsCellTower2 = new ArrayList<Location>();
			locationsCellTower1.add(location1);
			locationsCellTower1.add(location2);
			locationsCellTower1.add(location3);
			locationsCellTower1.add(location4);
			locationsCellTower1.add(location5);
			locationsCellTower1.add(location6);
			locationsCellTower1.add(location7);
			locationsCellTower1.add(location8);
			locationsCellTower1.add(location9);
			locationsCellTower1.add(location10);
			locationsCellTower2.add(location1);
			locationsCellTower2.add(location2);
			locationsCellTower2.add(location3);
			locationsCellTower2.add(location4);
			locationsCellTower2.add(location5);
			locationsCellTower2.add(location6);
			locationsCellTower2.add(location7);
			locationsCellTower2.add(location8);
			locationsCellTower2.add(location9);
			locationsCellTower2.add(location11);
		//generate cell tower
			CellTower celltower1 = new CellTower(locationsCellTower1,20.1168);
			CellTower celltower2 = new CellTower(locationsCellTower2,20.1168);
			CellTower[] cellTowers = {celltower1, celltower2};
		//generate schedule solver
			ScheduleSolver algorithm = new ScheduleSolver(cellTowers,allCellPhones, 672);
			algorithm.addScheduledConstraint(ceo, 9*60*60, 17*60*60); //CEO 9-5 Monday
			algorithm.addScheduledConstraint(ceo, 9*60*60+(24*60*60), 17*60*60+(24*60*60)); //CEO 9-5 Tuesday 
			algorithm.addScheduledConstraint(ceo, 9*60*60+(24*60*60)*2, 17*60*60+(24*60*60)*2); //CEO 9-5 Wednesday
			algorithm.addScheduledConstraint(ceo, 9*60*60+(24*60*60)*3, 17*60*60+(24*60*60)*3); //CEO 9-5 Thursday
			algorithm.addScheduledConstraint(ceo, 9*60*60+(24*60*60)*4, 17*60*60+(24*60*60)*4); //CEO 9-5 Friday
			algorithm.addScheduledConstraint(offline1, 1*60*60+(24*60*60)*6, 3*60*60+(24*60*60)*6); //Offline 1-3AM Saturday
			algorithm.addScheduledConstraint(offline1, 60*60, 3*60*60); //Offline 1-3AM Monday
			algorithm.addScheduledConstraint(driver,17*60*60+(24*60*60)*2, 20*60*60+(24*60*60)*2); //Driver 5-8 Wednesday
			algorithm.addScheduledConstraint(cliff,13*60*60+(24*60*60), 15*60*60+(24*60*60)); //Cliff 1-3 Tuesday
			algorithm.addScheduledConstraint(developer,15*60*60+(24*60*60)*0, 17*60*60+(24*60*60)*0); //Developer 3-5 Monday
			algorithm.addScheduledConstraint(developer,15*60*60+(24*60*60)*1, 17*60*60+(24*60*60)*1); //Developer 3-5 Tuesday
			algorithm.addScheduledConstraint(developer,15*60*60+(24*60*60)*2, 17*60*60+(24*60*60)*2); //Developer 3-5 Wednesday
			algorithm.addScheduledConstraint(developer,15*60*60+(24*60*60)*3, 17*60*60+(24*60*60)*3); //Developer 3-5 Thursday
			algorithm.addScheduledConstraint(developer,15*60*60+(24*60*60)*4, 17*60*60+(24*60*60)*4); //Developer 3-5 Friday
			algorithm.addScheduledConstraint(suburban,7*60*60+(24*60*60)*0, 10*60*60+(24*60*60)*0); //Suburbs 7-10AM Monday
			algorithm.addScheduledConstraint(suburban,7*60*60+(24*60*60)*1, 10*60*60+(24*60*60)*1); //Suburbs 7-10AM Tuesday
			algorithm.addScheduledConstraint(suburban,7*60*60+(24*60*60)*2, 10*60*60+(24*60*60)*2); //Suburbs 7-10AM Wednesday
			algorithm.addScheduledConstraint(suburban,7*60*60+(24*60*60)*3, 10*60*60+(24*60*60)*3); //Suburbs 7-10AM Thursday
			algorithm.addScheduledConstraint(suburban,7*60*60+(24*60*60)*4, 10*60*60+(24*60*60)*4); //Suburbs 7-10AM Friday
			algorithm.addScheduledConstraint(lake,18*60*60+(24*60*60)*3, 19*60*60+(24*60*60)*3); //Lake 6-7 Thursday
			algorithm.addScheduledConstraint(intern,21*60*60+(24*60*60)*4, 23*60*60+(24*60*60)*4); //Intern 9-11 Friday
			algorithm.addScheduledConstraint(cabin,22*60*60+(24*60*60)*5, 24*60*60+(24*60*60)*5); //Cabin 10-12 Saturday
		//generate schedule
			algorithm.generateSchedule();
	}*/
