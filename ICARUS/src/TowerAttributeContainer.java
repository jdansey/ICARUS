/**
 * Container class for holding tower attributes used in parsing the setup
 * @author Jacob.Dansey
 *
 */
public class TowerAttributeContainer {
	private String type;
	private double power;
	private double speed;
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
	}
	public TowerAttributeContainer(double power, double speed, String type) {
		this.power = power;
		this.speed = speed;
		this.type = type;
	}
	public TowerAttributeContainer(){
		
	}

}
