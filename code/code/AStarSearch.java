import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	 *  Operates by choosing the paths that consist of the most promising 
	 *  heuristic estimate. I.e. Not only shortest edge weight but also lowest
	 *  distance estimate from node to goal. 
	 * 
	 * @return LinkedHashMap<Segment, Double> - shortest path of segments and distances*/
	public LinkedHashMap<Segment, Double> search(){
		
		LinkedHashMap<Segment, Double> path = new LinkedHashMap<Segment, Double>();
		PriorityQueue<FringeNode> fringe = new PriorityQueue<FringeNode>();
		List<Segment> pathL = new ArrayList<Segment>();
		
		//Initialize all Nodes: Visited-False, PathFrom-Null
		for(Node n : graph.nodes.values()){
			n.setVisited(false);
			n.setPathFrom(null);
		}
		for(Segment s : graph.segments)
			s.setPathDistance(0);
		
		//Enqueue Start Node
		fringe.offer(new FringeNode(origin, null, 0, calcHeuristic(origin, destination)));
		
		while (!fringe.isEmpty()) {

			FringeNode fn = fringe.poll(); 			//Poll the most promising Node - based of lowest heuristic estimate			
			Node node = fn.getNode();				//GOOD TEST 14392 - 14795, 15150-15510 , DISCONNECTED, WORKING - 14655, 15152
			
			displayInfo(fn, calcHeuristic(origin, destination));
			
			if(fn.getParent()!=null){				//Exception: Initial Start Node
				
				Segment seg = graph.getSegmentFromPoints(fn.getParent(), fn.getNode());
				seg.setPathDistance(fn.getDistToGoal());
				
				pathL.add(seg);
				path.put(seg, fn.getDistToGoal());
				
			}
			if (!node.isVisited()) {				//Initialize if not visited - mark as visited, set pathFrom and cost
				node.setVisited(true);
				node.setPathFrom(fn.getParent());
				node.setCost(fn.getCostToHere());
			}

			if (node.equals(destination)){ 			//END CONDITION - Reached Goal
				path = trimPath(path);				//Ensures that only the shortest and reachable path is considered
				break;
			}

			Node to = null;
			for (Segment s : node.getOutNeighbours()) { 		//Add Neighbors to Fringe
				
				if(node.nodeID == s.start.nodeID)				
					to = s.end;
				else if(node.nodeID != s.start.nodeID)			//Exception: Ensure that 'to' Node is not the same as 'from' Node
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
	
	/**Ensures that the path returned is the shortest and reachable*/
	public LinkedHashMap<Segment,Double> trimPath(LinkedHashMap<Segment, Double> path){
		
		int counter = 0, startID = 0, endID = 0;				//Prune path, ensure only right path exists
		Map<Segment, Double> tmpMap = new LinkedHashMap<Segment,Double>();
		Map<Segment, Double> tmpPath = new LinkedHashMap<Segment,Double>();
		
		for(Map.Entry<Segment, Double> e : path.entrySet()){		//Copy of Original Path - Avoid Concurrency
			e.getKey().start.setPathVisited(false);
			e.getKey().end.setPathVisited(false);
			tmpPath.put(e.getKey(), e.getValue());
		}
		
		List<Segment> segs = new ArrayList<>(path.keySet());		//List of actual segments
		
		Node start = null;
		Node end = null;
		if(segs.get(0).start.nodeID == origin.nodeID){
			start = segs.get(0).start;
			end = segs.get(0).end;
		}
		else if(segs.get(0).start.nodeID != origin.nodeID){
			start = segs.get(0).end;
			end = segs.get(0).start;
		}															//Sort Start and End
		for(int i = 1; i < segs.size(); i++){
			
			//Establish start and end
			//i.e. start = .. end = ..
			if(end.nodeID == segs.get(i).start.nodeID){
				start = segs.get(i).start;
				end = segs.get(i).end;
			}
			else if(end.nodeID != segs.get(i).start.nodeID){
				start = segs.get(i).end;
				end = segs.get(i).start;
			}
			
			if(!start.isPathVisited()){
				start.setPathVisited(true);
				start.setPos(i);
			}
			else if(start.isPathVisited()){													//if visited
				//check previous instance and delete from there to here
				//i.e. i = 9, i = 7
				int j = start.getPos();
				while(j<i){	
					segs.remove(j);			//Remove Segment from previous position to here
					j++;
				}
				start.setPos(i);	//Set new Pos
			}
		}
		
		for(Map.Entry<Segment, Double> e : tmpPath.entrySet()){
			
			if(!segs.contains(e.getKey()))			//If not in actual list then remove
				path.remove(e.getKey());
		}
		
		return path;
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
