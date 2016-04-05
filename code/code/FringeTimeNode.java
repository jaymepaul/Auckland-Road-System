
public class FringeTimeNode implements Comparable<FringeTimeNode>{

	private Node node;
	private Node parent;
	private double timeCostToHere;
	private double totEstTimeCost;

	private double totalTimeCostToGoal;
	private double timeToGoal;

	public FringeTimeNode(Node node, Node parent, double timeCostToHere, double totEstTimeCost){

		this.node = node;
		this.parent = parent;
		this.timeCostToHere = timeCostToHere;
		this.totEstTimeCost = totEstTimeCost;

		this.totalTimeCostToGoal = totEstTimeCost - timeCostToHere;		//Purely Heuristic
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}



	public Node getParent() {
		return parent;
	}



	public void setParent(Node parent) {
		this.parent = parent;
	}


	public double getTimeCostToHere() {
		return timeCostToHere;
	}



	public void setTimeCostToHere(double timeCostToHere) {
		this.timeCostToHere = timeCostToHere;
	}



	public double getTotEstTimeCost() {
		return totEstTimeCost;
	}



	public void setTotEstTimeCost(double totEstTimeCost) {
		this.totEstTimeCost = totEstTimeCost;
	}



	public double getTimeToGoal() {
		return timeToGoal;
	}



	public void setTimeToGoal(double timeToGoal) {
		this.timeToGoal = timeToGoal;
	}



	public double getTotalTimeCostToGoal() {
		return totalTimeCostToGoal;
	}



	public void setTotalTimeCostToGoal(double totalTimeCostToGoal) {
		this.totalTimeCostToGoal = totalTimeCostToGoal;
	}



	@Override
	public int compareTo(FringeTimeNode fn) {

		int cmp = 0;

		if (totalTimeCostToGoal > fn.totalTimeCostToGoal)
			cmp = 1;
		else if(totalTimeCostToGoal < fn.totalTimeCostToGoal)
			cmp = -1;
		else
			cmp = 0;

		return cmp;
	}

}
