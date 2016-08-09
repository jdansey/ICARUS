//Container class to hold information on scheduled constraints
public class ScheduledConstraint {
	private CellPhone cellPhone;
	private double startTime;
	private double endTime;
	
	//getters and setters
	public CellPhone getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(CellPhone cellPhone) {
		this.cellPhone = cellPhone;
	}
	public double getStartTime() {
		return startTime;
	}
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	public double getEndTime() {
		return endTime;
	}
	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	//main constructor
	public ScheduledConstraint(CellPhone cellPhone, double startTime, double endTime) {
		this.cellPhone = cellPhone;
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
