
public class DisplayNode {

	private String name;
	private String city;
	private int speed;
	private double dist;

	public DisplayNode(String name, String city, double dist, int speed){
		this.name = name;
		this.city = city;
		this.speed = speed;
		this.dist = dist;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}





}
