import java.util.LinkedHashMap;
import java.util.PriorityQueue;

public class AStarSearch {

	private Graph graph;
	private Node origin, destination;
	
	public AStarSearch(Graph graph, Node origin, Node destination){
		this.graph = graph;
		this.origin = origin;
		this.destination = destination;
	}
	
	/** A* Search, finds the shortest path from the origin to the destination
	 * Uses Fringe and Sets to keep track of neighbors 
	 * 
	 * @param Node origin, Node destination
	 * @return LinkedHashMap<Segment, Double> - shortest path*/
	public LinkedHashMap<Segment, Double> search(){
		
		LinkedHashMap<Segment, Double> path = new LinkedHashMap<Segment, Double>();
		PriorityQueue<FringeNode> fringe = new PriorityQueue<FringeNode>();
		
		//Initialize all Nodes: Visited-False, PathFrom-Null
		for(Node n : graph.nodes.values()){
			n.setVisited(false);
			n.setPathFrom(null);
		}
		
		//Enqueue Start Node
		fringe.offer(new FringeNode(origin, null, 0, calcHeuristic(origin, destination)));
		
		while (!fringe.isEmpty()) {

			FringeNode fn = fringe.poll(); 						
			Node node = fn.getNode();				//GOOD TEST 14392 - 14795, DISCONNECTED, WORKING - 14655, 15152
			
			if(fn.getParent()!=null)				//Add to Path
				path.put(graph.getSegmentFromPoints(fn.getParent(), fn.getNode()), fn.getDistToGoal());
			
			if (!node.isVisited()) {
				node.setVisited(true);
				node.setPathFrom(fn.getParent());
				node.setCost(fn.getCostToHere());
			}

			if (node.equals(destination)) 
				break;

			Node to = null;
			
			for (Segment s : node.getOutNeighbours()) { 				//Add Neighbors to Fringe
				
				if(node.nodeID == s.start.nodeID)				
					to = s.end;
				else if(node.nodeID != s.start.nodeID)			//StartID does not equal previous/reverse
					to = s.start;	
				
				//Exception: Check if valid one-way
//				if(s.road.oneWay == 1)		//1 = Oneway, 0 not
				
				if (!to.isVisited()) {
					//Check if this path is admissible and consistent					//SPECIAL CASE: DEAD END/BACKTRACK
					double costToNeigh = fn.getCostToHere() + s.length;					//Calculate Cost to here + edge weight from here to neighbor
					double estTotal = costToNeigh + calcHeuristic(to, destination);		//Calculate total estimate with heuristic
					
					FringeNode f = new FringeNode(to, node, costToNeigh, estTotal);		//Should only add once, and add only the best path, ensure that we can reach our destination!!
					f.setDistToGoal(calcHeuristic(to, destination));
					fringe.offer(f);
				}
			}	
		}
		
		return path;
	}
	
	/**Returns the Euclidean distance between the current node and the end destination
	 * 
	 * @param Node current, Node destination*/
	public static double calcHeuristic(Node current, Node destination) {
		
		double distance = 0;
		
		//Calculate Euclidean Distance
		distance = Math.sqrt(Math.pow(current.location.x - destination.location.x, 2) + 
				Math.pow(current.location.y - destination.location.y, 2));
		
		return distance;
	}
	
	private void displayInfo(FringeNode fn, double totalDist) {
		
		Node from = fn.getParent();
		Node to = fn.getNode();
		double cost = fn.getCostToHere();
		double totalCostToGoal = fn.getTotalCostToGoal();
		
		if(from == null){
			System.out.println(getRoadNameFromPoints(from, to) + "	TotalCostToGoal : "+ totalCostToGoal+"	Distance to Goal: " 
						+ fn.getDistToGoal() + "	TotalDist: " + totalDist);
		}
		else if(from!=null){
			System.out.println("Street Name: " + getRoadNameFromPoints(from, to) + "	TotalCostToGoal : "+ totalCostToGoal+
					"	Distance to Goal: " + fn.getDistToGoal() + "	FROM: " + fn.getParent().nodeID + "	TO: " + to.nodeID + "	TotalDist: " + totalDist);
		}
			
		
	}

	private String getRoadNameFromPoints(Node from, Node neighbor) {
		
		String name = null;
		
		if(from == null)
			name = "START: " + neighbor.nodeID;
		else if(from!=null){
			for(Segment s : graph.segments){
				if(s.start.nodeID == from.nodeID && s.end.nodeID == neighbor.nodeID ||
						s.start.nodeID == neighbor.nodeID && s.end.nodeID == from.nodeID){
					name = s.road.name;
				}
			}	
		}
		
		return name;
	}
}
