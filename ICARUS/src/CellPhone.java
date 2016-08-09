import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.io.PrintWriter;
import java.io.PrintWriter;

import org.json.*;
import java.net.*;
import java.util.Random;
import java.util.Vector;

//CellPhone class contains all information pertaining to cell phone and the ability to generate area polygon with topography API
public class CellPhone {
	private final static String ElevationKey = "AIzaSyByUEe71wQ_jl6uvkKyW5yMdQlurD7HYpY";
	private final String USER_AGENT = "Mozilla/5.0";
	public static final int MAX_PRI = 3;
	public static final int MAX_SUB_PRI = 999;
	private double lat; //cell phone latitude
	private double lng; //cell phone longitude
	private double power; //distance in meters out the cell phone emitter is without any elevation changes
	private int mainPriority;
	private int subPriority;
	private double dailyConstraint[];
	private double dailyRequired;
	private double weeklyConstraint;
	private double weeklyRequired;
	private double value;
	private double restrictedValue;
	private String type;
	private Area area = new Area(); //total area of cell phone emitter 
	private Vector<Point> points = new Vector<Point>();
	private String name;

	
	//getters and setters for variables
	public double getDailyRequired() {
		return dailyRequired;
	}
	public void setDailyRequired(double dailyRequired) {
		this.dailyRequired = dailyRequired;
	}
	public double getWeeklyRequired() {
		return weeklyRequired;
	}
	public void setWeeklyRequired(double weeklyRequired) {
		this.weeklyRequired = weeklyRequired;
	}
	public double getDailyConstraint(int day) {
		return dailyConstraint[day];
	}
	public void setDailyConstraint(double[] dailyConstraint) {
		this.dailyConstraint = dailyConstraint;
	}
	public double getWeeklyConstraint() {
		return weeklyConstraint;
	}
	public void setWeeklyConstraint(double weeklyConstraint) {
		this.weeklyConstraint = weeklyConstraint;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getMainPriority() {
		return mainPriority;
	}
	public void setMainPriority(int mainPriority) {
		this.mainPriority = mainPriority;
	}
	public int getSubPriority() {
		return subPriority;
	}
	public void setSubPriority(int subPriority) {
		this.subPriority = subPriority;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) throws Exception {
		this.lat = lat;
		setArea();
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) throws Exception {
		this.lng = lng;
		setArea();
	}
	public Area getArea() {
		return area;
	}
	public void setArea(Area area) {
		this.area = area;
	}
	public double getPower() {
		return power;
	}
	public void setPower(double power) throws Exception {
		this.power = power;
		setArea();
	}
	public Vector<Point> getPoints() {
		return points;
	}

	public void setPoints(Vector<Point> points) {
		this.points = points;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public double getRestrictedValue() {
		return restrictedValue;
	}
	public void setRestrictedValue(double restrictedValue) {
		this.restrictedValue = restrictedValue;
	}
	/**
	 * constructor for seeing area
	 * @param lat
	 * @param lng
	 * @param power
	 * @param name
	 * @throws Exception - generates area with API so throws if hitting api fails
	 */
	public CellPhone(double lat, double lng, double power, String name) throws Exception{
		this.lat = lat;
		this.lng = lng;
		this.power = power;
		this.name = name;
		setArea();
	}
	/**
	 * main constructor includes all necessary info for a cell phone and generates area from google elevation api
	 * @param lat
	 * @param lng
	 * @param power
	 * @param dailyConstraint
	 * @param weeklyConstraint
	 * @param mainPriority
	 * @param subPriority
	 * @param name
	 * @param type
	 * @throws Exception
	 */
	public CellPhone(double lat, double lng, double power,double dailyConstraint, double weeklyConstraint, int mainPriority, int subPriority, String name, String type) throws Exception{
		this.lat = lat;
		this.lng = lng;
		this.power = power;
		this.name = name;
		this.mainPriority = mainPriority;
		this.subPriority = subPriority;
		this.dailyConstraint = new double[] {dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint};
		this.weeklyConstraint = weeklyConstraint;
		this.weeklyRequired = weeklyConstraint;
		this.dailyRequired = dailyConstraint;
		this.value = (MAX_PRI + 1 - mainPriority)^2 * 1000 + MAX_SUB_PRI + 1 - subPriority;
		int restrictedMain = 0;
		int restrictedSub = 0;
		if(mainPriority != 1){
			restrictedMain = mainPriority - 1;
			restrictedSub = subPriority;
		}else{
			restrictedMain = mainPriority;
			restrictedSub = 1;
		}
		this.restrictedValue = (MAX_PRI + 1 - restrictedMain)^2 * 1000 + MAX_SUB_PRI + 1 - restrictedSub;
		setArea();
	}

	/**
	 * offline constructor doesn't hit API
	 * @param dailyConstraint
	 * @param weeklyConstraint
	 * @param mainPriority
	 * @param subPriority
	 * @param name
	 */
	public CellPhone(double dailyConstraint, double weeklyConstraint, int mainPriority, int subPriority, String name){
		this.name = name;
		this.lat = 0.0;
		this.lng = 0.0;
		this.mainPriority = mainPriority;
		this.subPriority = subPriority;
		this.dailyConstraint = new double[] {dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint};
		this.weeklyConstraint = weeklyConstraint;
		this.dailyRequired = dailyConstraint;
		this.weeklyConstraint = weeklyConstraint;
		this.value = (MAX_PRI + 1 - mainPriority) * 1000 + MAX_SUB_PRI + 1 - subPriority;
		double restrictedMain = 0;
		double restrictedSub = 0;
		if(mainPriority != 1){
			restrictedMain = mainPriority - 1;
			restrictedSub = subPriority;
		}else{
			restrictedMain = mainPriority;
			restrictedSub = 1;
		}
		this.restrictedValue = (MAX_PRI + 1 - restrictedMain) * 1000 + MAX_SUB_PRI + 1 - restrictedSub;
	}
	
	/**
	 * for putting area in manually
	 * @param lat
	 * @param lng
	 * @param power
	 * @param points
	 * @param dailyConstraint
	 * @param weeklyConstraint
	 * @param mainPriority
	 * @param subPriority
	 * @param name
	 * @param type
	 */
	public CellPhone(double lat, double lng, double power, double[][] points, double dailyConstraint, double weeklyConstraint, int mainPriority, int subPriority, String name, String type){
		this.lat = lat;
		this.lng = lng;
		this.power = power;
		this.name = name;
		this.mainPriority = mainPriority;
		this.subPriority = subPriority;
		this.dailyConstraint = new double[] {dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint,dailyConstraint};
		this.weeklyConstraint = weeklyConstraint;
		this.dailyRequired = dailyConstraint;
		this.weeklyRequired = weeklyConstraint;
		this.type = type;
		this.value = (MAX_PRI + 1 - mainPriority) * 1000 + MAX_SUB_PRI + 1 - subPriority;
		double restrictedMain = 0;
		double restrictedSub = 0;
		if(mainPriority != 1){
			restrictedMain = mainPriority - 1;
			restrictedSub = subPriority;
		}else{
			restrictedMain = mainPriority;
			restrictedSub = 1;
		}
		this.restrictedValue = (MAX_PRI + 1 - restrictedMain) * 1000 + MAX_SUB_PRI + 1 - restrictedSub;
		pathBuilder(points);
	}
	
	/**
	 * reduce time on constraints
	 * @param day - day to reduce time from daily constraint array
	 * @param amount - amount to reduce constraint by
	 */
	public void reduceTime(int day, double amount){
		this.dailyConstraint[day] -= amount;
		this.weeklyConstraint -= amount;
	}
	
	/**
	 * add time on constraints, same as above but add instread of subtract
	 * @param day - day to increase time from daily constraint array
	 * @param amount - amount to increase constraint by
	 */
	public void addTime(int day, double amount){
		this.dailyConstraint[day] += amount;
		this.weeklyConstraint += amount;
	}
	
	/**
	 * manually build area with array of lats and longs
	 * @param points - array of lat/lng that make up the boundary of the cell phone area
	 */
	public void pathBuilder(double[][] points){
		Path2D.Double bounds = new Path2D.Double();
		this.points.clear();
		bounds.moveTo(points[0][1],points[0][0]);	
		this.points.add(new Point(points[0][1],points[0][0]));
		for(int x = 1; x < points.length; x++){
			bounds.lineTo(points[x][1], points[x][0]);
			this.points.add(new Point(points[x][1],points[x][0]));
		}
		bounds.closePath();
		this.area = new Area(bounds);
	}
	
	/**
	 * set area of cell phone polygon based on center point and power, hits elevation api and generates points that create a path
	 * no longer hits api just generates perfect circle for scaling purposes, uncomment to bring back
	 */
	public void setArea() throws Exception{
		//reset area and begin drawing new path
		this.area.reset();
		
		//for scaling purposes only do perfect circles
		this.area = this.generateCircle(lat, lng, power);
		//removed elevation calls to speed up scaling
		/*double radians;
		Path2D.Double bounds = new Path2D.Double();
		//test elevation in 40 different directions for the omni-directional cell signal
		for(radians = 0; radians <= 6.28; radians +=(2*Math.PI/20)){
			//find farthest point possible in direction of bearing
			Point farthestPoint = calculateNewPoint(this.power,radians,this.lat,this.lng);
			//make elevation path api call to google elevation api
			//returns 50 elevation data points in the path determined by power and bearing
			String responseString = elevationCall(farthestPoint.getLat(),farthestPoint.getLng()).toString();
			JSONObject response = new JSONObject(responseString);
			JSONArray results = response.getJSONArray("results");
			
			
			//declare elevation and step variables
			double cellPhoneElevation = results.getJSONObject(0).getDouble("elevation");
			double previousElevation = cellPhoneElevation;
			double distance = this.power;
			int steps = 0;
			//iterate down path decreasing distance by 1/50th and decreasing distance if elevation increases
			while(distance > 0){
				steps++;
				
				if (steps > 49) { //maximum size reached so set lat, lng to max
					steps = 49;
					break;
				}
				//subtract step forward from distance
				distance -= this.power/49;
				double currentElevation = results.getJSONObject(steps).getDouble("elevation");
				//if currentElevation is higher than cell phone elevation and the previous elevation 
				//then subtract change from the larger of cellPhone and previous
				if (currentElevation > cellPhoneElevation){
					if (currentElevation > previousElevation){
						if(cellPhoneElevation > previousElevation){
							distance -= 15*(currentElevation - cellPhoneElevation);
						}else{
							distance -= 15*(currentElevation - previousElevation);
						}
					}
				}
				previousElevation = currentElevation;				
			}
			//save the last step and elevation of it to bounds path, convert meters overshot in to currect lat/lng
			//by using negative distance and bearing back to center point for equation
			double farthestLat = results.getJSONObject(steps).getJSONObject("location").getDouble("lat");
			double farthestLng = results.getJSONObject(steps).getJSONObject("location").getDouble("lng");
			Point finalPoint = calculateNewPoint(Math.abs(distance),radians+Math.PI,farthestLat,farthestLng);
			points.add(finalPoint);
			//if first time then place bound start there, after that make lines
			if (radians == 0){
				bounds.moveTo(finalPoint.getLat(),finalPoint.getLng());
			}else{
				bounds.lineTo(finalPoint.getLat(), finalPoint.getLng());
			}
			
		}
		//close off path and make class area equal to  it
		bounds.closePath();
		this.area = new Area(bounds);*/
		
	}
	/**
	 * generate a circle from center point and radius by changing bearing and using calculate new point and creating circle from those points
	 * @param lat
	 * @param lng
	 * @param power
	 * @return Area
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
			this.points.addElement(new Point(farthestPoint.getLat(), farthestPoint.getLng()));
		}
		this.points.addElement(points.get(0));
		bounds.closePath();
		Area circle = new Area(bounds);
		return circle;
	}
	
	/**
	 * calculate a new point based on distance, bearing and starting location
	 * @param distance
	 * @param bearing
	 * @param lat
	 * @param lng
	 * @return Point - lat/lng of new point
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
		double lon2 = lon1 + a;

		lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;
		//convert from radians back to degrees
		lat2 = Math.toDegrees(lat2);
		lon2 = Math.toDegrees(lon2);
		Point newPoint = new Point(lat2,lon2);
		return newPoint;
	}
	
	/**
	 * make api call to google elevation api generates 50 elevation data points along path of center of cell phone to given point
	 * @param lat 
	 * @param lng 
	 * @return StringBuffer json string of api return
	 * @throws Exception - in case hitting api fails
	 */
	private StringBuffer elevationCall(double lat, double lng) throws Exception{
		String ELEVATION_API_URL =  "https://maps.googleapis.com/maps/api/elevation/json?";
	    String urlParameters = "path=" + this.lat + "," + this.lng +"%7C"+lat+"," + lng + "&samples=50&key="+CellPhone.ElevationKey;
	    
	    URL obj = new URL(ELEVATION_API_URL + urlParameters);
	    System.out.println(obj);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	    //add request header
	    con.setRequestMethod("POST");
	    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	    con.setRequestProperty("Content-Language", "en-US");
	    
	    // optional default is GET
	 	con.setRequestMethod("GET");

	 	//add request header
	 	con.setRequestProperty("User-Agent", USER_AGENT);

	 	BufferedReader in = new BufferedReader(
	 		       new InputStreamReader(con.getInputStream()));
	 	String inputLine;
	 	StringBuffer response = new StringBuffer();

	 	while ((inputLine = in.readLine()) != null) {
	 		response.append(inputLine);
	 	}
	 	in.close();
;
	 	return response;
	}
	
	/**
	 * test if area of cellPhone emitter overlaps with another area
	 * @param area - checks if area is overlapping another area by making a clone and checking if an intersection exists
	 * @return boolean true if overlapping
	 */
	public boolean isOverlapping(Area area){
		Area clone = (Area) this.area.clone();
		clone.intersect(area);
		return !clone.isEmpty();
	}
	
	//generate 100 random cell phone areas in a given lat/lng square
	public static void main(String[] args) throws Exception {
		//running this file generates random cell phones to a geojson file
		double latMin = 37;
		double latMax = 42;
		double lngMin = -110;
		double lngMax = -103;
		double powerMin = 5000;
		double powerMax = 25000;
		double dailyMin = 4000;
		double dailyMax = 36000;
		int mainMax = 6;
		int mainMin = 1;
		int subMax = 999;
		int subMin = 1;
		PrintWriter writer = new PrintWriter("10_cellPhones.geojson", "UTF-8");
		writer.println("{");
		writer.println("\"type\": \"FeatureCollection\",");
		writer.println( "\"features\": [");
		int amount = 100;
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
			if(x != amount){
				writer.println("},");
			}
		}
		writer.println("}");
		writer.println("]");
		writer.println("}");
		writer.close();	
    }
}