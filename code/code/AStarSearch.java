import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStarSearch {

	private Graph graph;
	private Node origin, destination;
	public int INF = (int)Double.POSITIVE_INFINITY;

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
	 * @return List<Segment> path - shortest path from start to end*/
	public List<Segment> searchDist(){

		PriorityQueue<FringeNode> fringe = new PriorityQueue<FringeNode>();
		List<Segment> path = new ArrayList<Segment>();
		
		//Initialize all Nodes: Visited-False, PathFrom-Null
		for(Node n : graph.nodes.values()){
			n.setVisited(false);
			n.setPathFrom(null);
		}
		for(Segment s : graph.segments)
			s.setPathDistance(0);

		//Enqueue Start Node
		fringe.offer(new FringeNode(origin, null, 0, calcDistHeuristic(origin, destination)));

		while (!fringe.isEmpty()) {

			FringeNode fn = fringe.poll(); 			//Poll the most promising Node - based of lowest heuristic estimate
			Node node = fn.getNode();

			//displayInfo(fn, calcDistHeuristic(origin, destination));

			if(fn.getParent()!=null){				//Exception: Initial Start Node

				Segment seg = graph.getSegmentFromPoints(fn.getParent(), fn.getNode());
				path.add(seg);

			}
			if (!node.isVisited()) {				//Initialize if not visited - mark as visited, set pathFrom and cost
				node.setVisited(true);
				node.setPathFrom(fn.getParent());
				node.setCost(fn.getCostToHere());
			}

			if (node.equals(destination))			//END CONDITION - Reached Goal
				break;

			Node to = null;
			for (Segment s : node.getOutNeighbours()) { 		//Add Neighbors to Fringe


				//Exception: Check if valid one-way
				if(s.road.oneWay == 1){		//1 = Oneway, 0 not
					//System.out.println("ONE WAY FOUND! @ "+s.road.roadID+ "	N1: "+s.start.nodeID+ "	N2: "+s.end.nodeID);
					if(s.start.nodeID == node.nodeID)				//Ensure that to = end of segment
						to = s.end;
					else											//If segment doesnt conform to one-way rule, then discard
						continue;
				}
				else if(s.road.oneWay == 0){
					if(node.nodeID == s.start.nodeID)
						to = s.end;
					else if(node.nodeID != s.start.nodeID)			//Exception: Ensure that 'to' Node is not the same as 'from' Node
						to = s.start;
				}

				if(path.size() > 0 && isRestricted(path.get(path.size()-1), s, node))	//If this segment is restricted then consider others
					continue;

				if (!to.isVisited()) {
					//Check if this path is admissible and consistent					//SPECIAL CASE: DEAD END/BACKTRACK
					double costToNeigh = fn.getCostToHere() + s.length;					//Calculate Cost to here + edge weight from here to neighbor
					double estTotal = costToNeigh + calcDistHeuristic(to, destination);		//Calculate total estimate with heuristic

					FringeNode f = new FringeNode(to, node, costToNeigh, estTotal);		//Should only add once, and add only the best path, ensure that we can reach our destination!!
					fringe.offer(f);
				}
			}
		}

		return trimPath(path);				//Ensures that only the shortest and reachable path is considered;
	}

	/**Finds the path that takes the least amount of time,
	 * Uses a Time heuristic estimate - calculated via
	 * finding shortest distance. Ensures that the algo
	 * is admissible (underestimates the totalCost)
	 *
	 *@return List<Segment> - fastest path from start to end
	 * */
	public List<Segment> searchPathTime(){

		List<Segment> timePath = new ArrayList<Segment>();
		PriorityQueue<FringeTimeNode> fringe = new PriorityQueue<FringeTimeNode>();
		
		//Initialize all Nodes: Visited-False, PathFrom-Null
		for(Node n : graph.nodes.values()){
			n.setVisited(false);
			n.setPathFrom(null);
		}

		//Enqueue Start Node
		fringe.offer(new FringeTimeNode(origin, null, 0, calcTimeHeuristic(origin, destination)));

		while(!fringe.isEmpty()){

			FringeTimeNode fn = fringe.poll();
			Node node = fn.getNode();

			//displayTimeInfo(fn, totalEstTime);


			if(fn.getParent()!=null){
				Segment seg = graph.getSegmentFromPoints(fn.getParent(), node);
				timePath.add(seg);
			}

			if(!node.isVisited()){
				node.setVisited(true);
				node.setPathFrom(fn.getParent());
				node.setCost(fn.getTimeCostToHere());
			}
			if(node == destination)
				break;

			Node to = null;
			for(Segment s : node.getOutNeighbours()){

				//======================RESTRICTIONS===========================//
				//Exception: Check if valid one-way
				if(s.road.oneWay == 1){		//1 = Oneway, 0 not
					//System.out.println("ONE WAY FOUND! @ "+s.road.roadID+ "	N1: "+s.start.nodeID+ "	N2: "+s.end.nodeID);
					if(s.start.nodeID == node.nodeID)				//Ensure that to = end of segment
						to = s.end;
					else											//If segment doesnt conform to one-way rule, then discard
						continue;
				}
				else if(s.road.oneWay == 0){
					if(node.nodeID == s.start.nodeID)
						to = s.end;
					else if(node.nodeID != s.start.nodeID)			//Exception: Ensure that 'to' Node is not the same as 'from' Node
						to = s.start;
				}

				if(timePath.size() > 0 && isRestricted(timePath.get(timePath.size()-1), s, node))	//If this segment is restricted then consider others
					continue;
				//======================RESTRICTIONS===========================//


				if(!to.isVisited()){

					double costToNeigh = fn.getTimeCostToHere() + ((s.length/getRoadSpeed(s.road.speed)) * 3600) - s.road.roadClass;		//Reduce cost based on road class level
					double estTotal = costToNeigh + calcTimeHeuristic(to, destination);

					if(to.hasLights){		// || node.hasLights
//						System.out.println("Traffic Light @ "+to.nodeID);
						estTotal += 5;		//Add Extra Cost if To Node has lights - Reduce its priority, more expensive
					}

					fringe.offer(new FringeTimeNode(to, node, costToNeigh, estTotal));
				}
			}
		}

		return trimPath(timePath);
	}


	/**Calculates total Time heuristic estimate based on
	 * shortest path considers speed limits and road class
	 *
	 * @retrun Time in seconds*/
	public static double calcTimeHeuristic(Node start, Node end){

		double time = 0;

		double dist = calcDistHeuristic(start, end);
		time = (dist / 150) * 3600;			//Divide by MAX SPEED

		return time;
	}

	/**Returns the Euclidean distance between the current node and the end destination
	 *
	 * @param Node current, Node destination*/
	public static double calcDistHeuristic(Node current, Node destination) {

		double distance = 0;

		//Calculate Euclidean Distance
		distance = Math.sqrt(Math.pow(current.location.x - destination.location.x, 2) +
				Math.pow(current.location.y - destination.location.y, 2));

		return distance;
	}


	/**Returns TRUE if the segment in question has restrictions applicable to it*/
	public boolean isRestricted(Segment prevSeg, Segment curSeg, Node n){

		boolean restrict = false;
		Restriction rest = null;

		Node N1 = null;
		Node N2 = null;		//CASE: Each NODE has multiple restrictions!

		if(prevSeg.start.nodeID != n.nodeID)
			N1 = prevSeg.start;
		else
			N1 = prevSeg.end;

		if(curSeg.end.nodeID != n.nodeID)
			N2 = curSeg.end;
		else
			N2 = curSeg.start;

		if(n.getRestrictions().size() > 0){

			for(Restriction r : n.getRestrictions()){
				if(r.getN1().nodeID == N1.nodeID && r.getN2().nodeID == N2.nodeID && r.getN().nodeID == n.nodeID
						&& prevSeg.road.roadID == r.getR1().roadID && curSeg.road.roadID == r.getR2().roadID){

					rest = r;				//Get the right Restriction
				}
			}

			if(rest != null){
				if (rest.getN1().nodeID == N1.nodeID && rest.getN2().nodeID == N2.nodeID && rest.getN().nodeID == n.nodeID
						&& rest.getR1().roadID == prevSeg.road.roadID && rest.getR2().roadID == curSeg.road.roadID ) {
					restrict = true;
					//System.out.println("RESTRICTION FOUND! ");
					//System.out.println("N1: " + N1.nodeID + "	N: " + n.nodeID + "	N2: " + N2.nodeID + "\n");
				}
			}
		}

		return restrict;
	}


	/**Ensures that the path to be highlighted excludes any incorrect routes/paths
	 *
	 * @return List<Segment> - path to be highlighted*/
	public List<Segment> trimPath(List<Segment> path){


		List<Segment> realPath = new ArrayList<Segment>();
		
		Node to = null;
		realPath.add(path.get(path.size()-1));
		if(path.get(path.size()-1).end == destination)
			to = path.get(path.size()-1).start;
		else
			to = path.get(path.size()-1).end;
		
		
		for(int i = path.size()-2 ; i >= 0; i--){
			
			if(path.get(i).start == to && path.get(i).end == to.getPathFrom()){			//Find Matching Segment
				realPath.add(path.get(i));
				to = path.get(i).end;
			}
			else if(path.get(i).end == to && path.get(i).start == to.getPathFrom()){
				realPath.add(path.get(i));
				to = path.get(i).start;
			}
		}
		
		return realPath;
	}


	private void displayInfo(FringeNode fn, double totalDist) {

		Node from = fn.getParent();
		Node to = fn.getNode();
		double cost = fn.getCostToHere();
		double totalCostToGoal = fn.getTotalCostToGoal();

		if(from == null){
			System.out.println(getRoadNameFromPoints(from, to) +"	Distance to Goal: "
						+ totalDist + "	TotalDist: " + totalDist);
		}
		else if(from!=null){
			System.out.println("Street Name: " + getRoadNameFromPoints(from, to) +
					"	Distance to Goal: " + fn.getDistToGoal() + "	FROM: " + fn.getParent().nodeID + "	TO: " + to.nodeID + "	TotalDist: " + totalDist);
		}
	}

	private void displayTimeInfo(FringeTimeNode fn, double totalEstTime) {

		Node from = fn.getParent();
		Node to = fn.getNode();

		if(fn.getParent() == null)
			System.out.println(getRoadNameFromPoints(from, to) + "	TotalTime: " + totalEstTime);
		else if(from!=null){
			System.out.println("Street Name: " + getRoadNameFromPoints(from, to) +
					"	Time to Goal: " + fn.getTotalTimeCostToGoal() + "	FROM: " + fn.getParent().nodeID + "	TO: " + to.nodeID + "	TotalTime: " + totalEstTime);
		}

	}

	/**Gets the right road name based on the points given
	 *
	 * @return String roadName*/
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

	/**Parses the road speed depending on the number given as input
	 *
	 * @return int speed - the speed in km/h */
	public static int getRoadSpeed(int speed){

		int spd = 0;

		switch(speed){
		case 0:
			spd = 5;
			break;
		case 1:
			spd = 20;
			break;
		case 2:
			spd = 40;
			break;
		case 3:
			spd = 60;
			break;
		case 4:
			spd = 80;
			break;
		case 5:
			spd = 100;
			break;
		case 6:
			spd = 110;
			break;
		case 7:
			spd = 150;		//No Limit
			break;
		}

		return spd;
	}
	

	public Node getOrigin() {
		return origin;
	}

	public void setOrigin(Node origin) {
		this.origin = origin;
	}

	public Node getDestination() {
		return destination;
	}

	public void setDestination(Node destination) {
		this.destination = destination;
	}


}
