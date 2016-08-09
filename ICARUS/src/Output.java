import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

//used to generate scatter plot of phones by priority and amount of constraint filled
public class Output extends ApplicationFrame {

	/**
	* A demonstration application showing an XY series containing a null value.
	*
	* @param title  the frame title.
	*/
	public Output(final String title, double[][] points) {
	   super(title);
	   generateGraph(title, points);
	   
	}
	
	public void generateGraph(final String title, double[][] points){
		final XYSeries series = new XYSeries("Constraint Amount Completed per Phone");
		   for(int x = 0; x < points.length; x++){
			   series.add(points[x][0], points[x][1]);
			   System.out.println(points[x][0] + "\t" + points[x][1]);
			   
		   }
		   final XYSeriesCollection data = new XYSeriesCollection(series);
		   final JFreeChart chart = ChartFactory.createScatterPlot(
		       title,
		       "Priority", 
		       "% Completed", 
		       data,
		       PlotOrientation.VERTICAL,
		       true,
		       true,
		       false
		   );
		
		   final ChartPanel chartPanel = new ChartPanel(chart);
		   chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		   setContentPane(chartPanel);
		
	}
	
	//****************************************************************************
	//* JFREECHART DEVELOPER GUIDE                                               *
	//* The JFreeChart Developer Guide, written by David Gilbert, is available   *
	//* to purchase from Object Refinery Limited:                                *
	//*                                                                          *
	//* http://www.object-refinery.com/jfreechart/guide.html                     *
	//*                                                                          *
	//* Sales are used to provide funding for the JFreeChart project - please    * 
	//* support us so that we can continue developing free software.             *
	//****************************************************************************
	
	/**
	* Starting point for the demonstration application.
	*
	* @param args  ignored.
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	*/
	//loop through cell phones gathering priority and percent completed
	public static double[][] generateScatterPoints(Set<CellPhone> phones){
		double[][] points1 = new double[phones.size()][2];
		int x = 0;
		for(CellPhone phone : phones){
			int main = phone.getMainPriority();
			double sub = (double) phone.getSubPriority();
			sub = sub / 1000;
			double totalPri = main + sub;
			double percentComplete = 0;
			if(phone.getWeeklyConstraint() < 0){
				percentComplete = 100;
			}else{
				percentComplete = (phone.getWeeklyRequired() - phone.getWeeklyConstraint()) / phone.getWeeklyRequired() * 100;
			}
			points1[x][0] = totalPri;
			points1[x][1] = percentComplete;
			x++;
		}
		return points1;
	}
	public static void main(final String[] args) throws JSONException, FileNotFoundException, UnsupportedEncodingException {
	//Set up different seeding of problem
	int order = 0;
	ScheduleSolver schedule1 = ScheduleSolver.setupProblem("ICARUSSetup.geojson",672, 0, 672);
	/*ScheduleSolver schedule2 = ScheduleSolver.setupProblem("50_cellPhones.geojson",672, 0, 672);
	ScheduleSolver schedule3 = ScheduleSolver.setupProblem("100_cellPhones.geojson",672, 0, 672);
	ScheduleSolver schedule4 = ScheduleSolver.setupProblem("500_cellPhones.geojson",672, 0, 672);
	ScheduleSolver schedule5 = ScheduleSolver.setupProblem("1000_cellPhones.geojson",672, 0, 672);
	ScheduleSolver schedule6 = ScheduleSolver.setupProblem("2000_cellPhones.geojson",672, 0, 672);*/
	//add scheduled constraints
	Set<CellPhone> cellPhones1 = schedule1.getAllCellPhones();
	/*Set<CellPhone> cellPhones2 = schedule2.getAllCellPhones();
	Set<CellPhone> cellPhones3 = schedule3.getAllCellPhones();
	Set<CellPhone> cellPhones4 = schedule4.getAllCellPhones();
	Set<CellPhone> cellPhones5 = schedule5.getAllCellPhones();
	Set<CellPhone> cellPhones6 = schedule6.getAllCellPhones();*/
	//add scheduled constraint
	
	//generate schedule
	schedule1.generateSchedule();
	/*schedule2.generateSchedule();
	schedule3.generateSchedule();
	schedule4.generateSchedule();
	schedule5.generateSchedule();
	schedule6.generateSchedule();*/
	//generate scatter plot points
	double[][] points1 = generateScatterPoints(cellPhones1);
	/*double[][] points2 = generateScatterPoints(cellPhones2);
	double[][] points3 = generateScatterPoints(cellPhones3);
	double[][] points4 = generateScatterPoints(cellPhones4);
	double[][] points5 = generateScatterPoints(cellPhones5);
	double[][] points6 = generateScatterPoints(cellPhones6);*/
	
	//create scatter plots
   final Output schedule1Output = new Output("Schedule 1",points1);
   schedule1Output.pack();
   RefineryUtilities.centerFrameOnScreen(schedule1Output);
   schedule1Output.setVisible(true);
   
  /* final Output schedule2Output = new Output("Schedule 2",points2);
   schedule2Output.pack();
   RefineryUtilities.centerFrameOnScreen(schedule2Output);
   schedule2Output.setVisible(true);
  
   final Output schedule3Output = new Output("Schedule 3",points3);
   schedule3Output.pack();
   RefineryUtilities.centerFrameOnScreen(schedule3Output);
   schedule3Output.setVisible(true);
   
   final Output schedule4Output = new Output("Schedule 4",points4);
   schedule4Output.pack();
   RefineryUtilities.centerFrameOnScreen(schedule4Output);
   schedule4Output.setVisible(true);
   
   final Output schedule5Output = new Output("Schedule 5",points5);
   schedule5Output.pack();
   RefineryUtilities.centerFrameOnScreen(schedule5Output);
   schedule5Output.setVisible(true);
   
   final Output schedule6Output = new Output("Schedule 6",points6);
   schedule6Output.pack();
   RefineryUtilities.centerFrameOnScreen(schedule6Output);
   schedule6Output.setVisible(true);
   //schedule1.generateAllLocationValues();*/
	}

}
	

