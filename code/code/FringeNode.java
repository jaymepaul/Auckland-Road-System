
public class FringeNode implements Comparable {

	private Node neighbor;
	private Node from;
	private double costToNeighbor;
	private double heuristic;
	
	private double totalCostToGoal;
	private double totalDistanceToGoal;
	
	public FringeNode(Node neighbor, Node from, double costToNeighbor, double heuristic){
		this.neighbor = neighbor;
		this.from = from;
		this.costToNeighbor = costToNeighbor;
		this.heuristic = heuristic;
		
		if(costToNeighbor == 0)
			this.totalDistanceToGoal = heuristic;
		
		this.totalCostToGoal = heuristic;
	}

	public Node getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(Node neighbor) {
		this.neighbor = neighbor;
	}

	public Node getFrom() {
		return from;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

	public double getCostToNeighbor() {
		return costToNeighbor;
	}

	public void setCostToNeighbor(double costToNeighbor) {
		this.costToNeighbor = costToNeighbor;
	}

	public double getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(double heuristic) {
		this.heuristic = heuristic;
	}
	

	public double getTotalDistanceToGoal() {
		return totalDistanceToGoal;
	}

	public void setTotalDistanceToGoal(double totalDistanceToGoal) {
		this.totalDistanceToGoal = totalDistanceToGoal;
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
		
		return (int) (totalCostToGoal - fn.totalCostToGoal);
	}
	
	
}
