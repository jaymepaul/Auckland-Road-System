
public class FringeNode implements Comparable<FringeNode> {

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
		
		this.totalCostToGoal = totEstCost - costToHere;		//Purely Heuristic
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
	public int compareTo(FringeNode fn) {
		
		int cmp = 0;
		
		if (totalCostToGoal > fn.totalCostToGoal)
			cmp = 1;
		else if(totalCostToGoal < fn.totalCostToGoal)
			cmp = -1;
		else
			cmp = 0;
		
		return cmp;
	}
	
	
}
