
public class FringeNode implements Comparable {

	private Node node;
	private Node parent;
	private double costToHere;
	private double totEstCost;
	
	private double totalCostToGoal;
	private double distToGoal;
	
	public FringeNode(Node node, Node parent, double costToHere, double totEstCost){
		this.node = node;
		this.parent = parent;
		this.costToHere = costToHere;
		this.totEstCost = totEstCost;
		
		if(costToHere == 0)
			this.distToGoal = totEstCost;
		
		this.totalCostToGoal = totEstCost;
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



	public double getCostToHere() {
		return costToHere;
	}

	public void setCostToHere(double costToHere) {
		this.costToHere = costToHere;
	}


	

	public double getTotEstCost() {
		return totEstCost;
	}



	public void setTotEstCost(double totEstCost) {
		this.totEstCost = totEstCost;
	}

	

	public double getDistToGoal() {
		return distToGoal;
	}



	public void setDistToGoal(double distToGoal) {
		this.distToGoal = distToGoal;
	}



	public double getTotalCostToGoal() {
		return totalCostToGoal;
	}

	public void setTotalCostToGoal(double totalCostToGoal) {
		this.totalCostToGoal = totalCostToGoal;
	}

	@Override
	public int compareTo(Object o) {

		FringeNode fn = (FringeNode) o;
		int comp = 0;
		
		if(fn.totEstCost < totEstCost)
			comp = 1;
		else if(fn.totEstCost > totEstCost)
			comp = -1;
		else 
			comp = 0;
		
		return comp;
	}
	
	
}
