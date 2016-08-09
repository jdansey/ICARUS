import java.io.PrintWriter;
import java.util.*;
/**
 * Cell tower class holds locations and information to determine locations and travel time between locations
 * @author Jacob.Dansey
 * @required Location.java CellPhone.java OptimumLocations.java* OptimumLocations2.java*
 * *only needed for different setup purposes
 */
public class CellTower {
	private List<Location> locations; //optimal locations
	private double power; //meters
	private double speed; //meters per second
	private String type;
	private double[][] travelTime; //seconds
	
	//getters and setters
	public List<Location> getLocations() {
		return locations;
	}
	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getPower() {
		return power;
	}
	public void setPower(double power) {
		this.power = power;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
		generateDistanceArray();
	}
	public double[][] getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(double[][] travelTime) {
		this.travelTime = travelTime;
	}
	
	/**
	 * Constructor without Location Finder
	 * @param locations - pre-processed list of locations for cell tower to be at
	 * @param power - power of cell tower
	 * @param speed - speed of cell tower for travel time calculation
	 * @param type - type of tower (CDMA, GSM)
	 */
	public CellTower(List<Location> locations, double power, double speed, String type){
		this.speed = speed;
		this.power = power;
		this.type = type;
		this.locations = locations;
		generateDistanceArray();
	}
	
	/**
	 * Constructor with Location Finder
	 * @param cellPhones - set of all cellphones to determine optimum location
	 * @param power - power of cell tower in optimum location calculation
	 * @param speed - speed of tower for travel purposes
	 * @param type - type of tower (CDMA,GSM)
	 * @throws Exception - if Elevation API screws up
	 */
	public CellTower(Set<CellPhone> cellPhones, double power, double speed, String type) throws Exception {
		this.power = power;
		this.speed = speed;
		this.type = type;
		Set<CellPhone> towerSpecificPhones = new HashSet<CellPhone>(cellPhones);
		//remove cell phones from list that are of a different type(frequency)
		for(Iterator<CellPhone> it = towerSpecificPhones.iterator(); it.hasNext();){
			CellPhone phone = it.next();
			String phoneType = phone.getType();
			if(type == "GSM"){
				if(phoneType == "Sprint" || phoneType == "Verizon" || phoneType == "U.S. Cellular"){
					it.remove();
				}
			}else{
				if(phoneType == "AT&T" || phoneType == "T-Mobile"){
					it.remove();
				}
			}
		}
		this.locations = new ArrayList<Location>();
		//retrieve locations
		OptimumLocations2 findLocation = new OptimumLocations2(towerSpecificPhones,this.power);
		findLocation.patternGenerate();
		locations = new ArrayList<Location>(findLocation.getLocations());
		int i = 0;
		for(Iterator<Location> it = locations.iterator(); it.hasNext();){
			it.next().setNumber(i++);
		}
		//add offline activity
		Location offline = new Location(0.0,0.0, new HashSet<CellPhone>(),99);
		offline.setOffline(true);
		locations.add(offline);
		
		//generate 2-d travel time array
		generateDistanceArray();
	}
	
	
	/**
	 * generate array of travel time between locations
	 */
	private void generateDistanceArray(){
		Location[] locations = this.locations.toArray(new Location[this.locations.size()]);
		
		this.travelTime = new double[locations.length][locations.length];
		for(int x = 0; x < locations.length;x++){
			locations[x].setNumber(x);
			for(int y = 0; y < locations.length; y++){
				if(x==y){
					this.travelTime[x][y] = 0;
				}else if(locations[x].isOffline() || locations[y].isOffline()){
					this.travelTime[x][y] = 0; //zero time to go on/offline
				}else{
					double lat1 = locations[x].getLat();
					double lon1 = locations[x].getLng();
					double lat2 = locations[y].getLat();
					double lon2 = locations[y].getLng();
					double distance = distanceBetweenPoints(lat1,lon1,lat2,lon2);
					double time = distance / this.speed;
					this.travelTime[x][y] = time;
					//System.out.println(time);
				}
			}
		}
	}
	
	/**
	 * calculates distance between two lat/lng points
	 * @param lat1 - first point latitude
	 * @param lon1 - first point longitude
	 * @param lat2 - second point latitude
	 * @param lon2 - second point longitude
	 * @return double - distance between points
	 */
	private double distanceBetweenPoints(double lat1, double lon1, double lat2, double lon2){
		double R = 6371000.0; // Meters
		
		//Haversine Formula
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double deltaLat = Math.toRadians(lat2-lat1);
		double deltaLon = Math.toRadians(lon2-lon1);

		double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
		        Math.cos(lat1Rad) * Math.cos(lat2Rad) *
		        Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		return R * c;
	}
	
	public static void main(String[] args) throws Exception {
		//apply mins and maxs to random cell phone generation
		double latMin = 30;
		double latMax = 44;
		double lngMin = -112;
		double lngMax = -96;
		double powerMin = 5000;
		double powerMax = 25000;
		double dailyMin = 4000;
		double dailyMax = 36000;
		Set<CellPhone> allPhones = new HashSet<CellPhone>();
		int mainMax = 6;
		int mainMin = 1;
		int subMax = 999;
		int subMin = 1;
		//print results in geojson form
		PrintWriter writer = new PrintWriter("10000_cellPhones2.geojson", "UTF-8");
		writer.println("{");
		writer.println("\"type\": \"FeatureCollection\",");
		writer.println( "\"features\": [");
		int amount = 10000;
		int numTowers = 15;
		//generate phones
		for(int x = 1; x <= amount; x++){
			//System.out.println("Cell phone " + x);
			String name = "CellPhone " + x;
			Random r = new Random();
			double latValue = latMin + (latMax - latMin) * r.nextDouble();
			double lngValue = lngMin + (lngMax - lngMin) * r.nextDouble();
			double powerValue = powerMin + (powerMax - powerMin) * r.nextDouble();
			double dailyValue = dailyMin + (dailyMax - dailyMin) * r.nextDouble();
			double weeklyValue = -15000 + (15000 - -15000) * r.nextDouble();
			weeklyValue += dailyValue*7;
			int mainValue = r.nextInt((mainMax - mainMin) + 1) + mainMin;
			int subValue = r.nextInt((subMax - subMin) + 1) + subMin;
			int carrierVal = r.nextInt((99 - 0) + 1) + 0;
			if(mainValue == 3){
				mainValue = 2;
			}
			if(mainValue > 3){
				mainValue = 3;
			}
			CellPhone phone = new CellPhone(latValue,lngValue,powerValue, name);
			allPhones.add(phone);
			//print phone in proper form and add to all phones set
			writer.println("{");
			writer.println( "\"type\": \"Feature\",");
			writer.println("\"properties\": {");
			writer.println("\"lat\": " + latValue + ",");
			writer.println("\"lng\": " + lngValue + ",");
			writer.println("\"power\": " + powerValue + ",");
			writer.println("\"dailyConstraint\": " + dailyValue + ",");
			writer.println("\"weeklyConstraint\": " + weeklyValue + ",");
			writer.println("\"name\": \"" + name + "\",");
			writer.println("\"mainPriority\": " + mainValue + ",");
			writer.println("\"subPriority\": " + subValue + ",");
			writer.println("\"type\": \"Verizon\"");
			writer.println("},");
			writer.println( "\"geometry\": {");
			writer.println("\"type\": \"Polygon\",");
			writer.println("\"coordinates\": [");
			writer.println("[");
			for(Point point : phone.getPoints()){
				writer.println("[" + point.getLng() + "," + point.getLat() + "],");
			}
			writer.println("[" + phone.getPoints().get(0).getLng() + "," + phone.getPoints().get(0).getLat() + "]");
			writer.println("]");
			writer.println("]");
			writer.println("}");
			writer.println("},");
			
		}
		//generate towers
		CellTower tower = new CellTower(allPhones, 100000,35, "CDMA");
		//for identical towers just repeat process and print to file
		for(int x = 0; x <= numTowers-1; x++){
			for(Location location : tower.getLocations()){
				writer.println("{");
				writer.println( "\"type\": \"Feature\",");
				writer.println("\"properties\": {");
				writer.println("\"number\": " + x + ",");
				writer.println("\"power\": \"" + tower.getPower() + "\",");
				writer.println("\"speed\": " + tower.getSpeed() + ",");
				writer.println("\"type\": \"" + tower.getType() + "\",");
				String cellPhones = "[";
				for(Iterator<CellPhone> it = location.getCellPhones().iterator(); it.hasNext();){
					CellPhone phone = it.next();
					if(it.hasNext()){
						cellPhones += "\"" + phone.getName() +"\",";
					}else{
						cellPhones += "\"" + phone.getName() +"\"";
					}
				}
				cellPhones += "]";
				writer.println("\"cellPhones\": " + cellPhones + "},");
				writer.println( "\"geometry\": {");
				writer.println("\"type\": \"Point\",");
				writer.println("\"coordinates\": [" + location.getLng() + "," + location.getLat() + "]");
				writer.println("}");
				writer.println("},");
			}
		}
		//remove last comma in geojson file to make it properly organized     
		writer.println("]");
		writer.println("}");
		writer.close();	
    }
}

