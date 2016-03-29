import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;
	
	//Articulation Points
	private HashSet<Node> articulationPoints;
	private Stack<StackElement> activationStack;
	
	private int INF = (int)Double.POSITIVE_INFINITY;

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e, String source) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
		
			if(source == "Origin")
				getSearchOrigin().setText(Integer.toString(closest.nodeID));
			else if(source == "Destination")	
				getSearchDestination().setText(Integer.toString(closest.nodeID));
			
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}
		
	}
	

	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons, File restrictions) {
		graph = new Graph(nodes, roads, segments, polygons, restrictions);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}
	
	@Override
	protected void findShortestPath(String origin, String destination) {
		
		Node start = graph.nodes.get(Integer.parseInt(origin));			//Currently operating based on NodeID's
		Node end = graph.nodes.get(Integer.parseInt(destination));		
		
		LinkedHashMap<Segment, Double> path = AStarSearch(start, end);	//RoadName + Destination to Goal
				
		StringBuilder sb = new StringBuilder();
		sb.append("START: "+start.nodeID+"	END: "+end.nodeID+"\n"+
		"Start At Intersection: "+start.nodeID+"	Distance To Goal: "+calcHeuristic(start, end)+"\n");
		for(Map.Entry<Segment, Double> e : path.entrySet())
			sb.append("Street: " + e.getKey().road.name + "	Distance To Goal: " + e.getValue() +"\n");
		sb.append("REACHED END GOAL!");
		
		getTextOutputArea().setText(sb.toString());
		graph.setHighlightPath(path, start, end);
		
	}
	
	/** A* Search, finds the shortest path from the origin to the destination
	 * Uses Fringe and Sets to keep track of neighbors 
	 * 
	 * @param Node origin, Node destination*/
	public LinkedHashMap<Segment, Double> AStarSearch(Node origin, Node destination){
		
		System.out.println("PERFORMING A* SEARCH..");
		System.out.println("ORIGIN ID: " + origin.nodeID + "	DEST ID: " + destination.nodeID);
		
		LinkedHashMap<Segment, Double> path = new LinkedHashMap<Segment, Double>();
		PriorityQueue<FringeNode> fringe = new PriorityQueue<FringeNode>();
		double totalDist = calcHeuristic(origin,destination);
		
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
			
			displayInfo(fn, totalDist);				//Display Info
			if(fn.getParent()!=null)	
				path.put(graph.getSegmentFromPoints(fn.getParent(), fn.getNode()), fn.getDistToGoal());
			
			if (!node.isVisited()) {
				node.setVisited(true);
				node.setPathFrom(fn.getParent());
				node.setCost(fn.getCostToHere());
			}

			if (node.equals(destination)) {
				System.out.println("REACHED END GOAL!");
				break;
			}

			Node to = null;
			
			for (Segment s : node.getOutNeighbours()) { 				//Add Neighbors to Fringe
				
				if(node.nodeID == s.start.nodeID)
					to = s.end;
				else if(node.nodeID != s.start.nodeID)			//StartID does not equal previous/reverse
					to = s.start;	
				
				if (!to.isVisited()) {
					//Check if this path is admissible and consistent					//SPECIAL CASE: DEAD END/BACKTRACK
					double costToNeigh = fn.getCostToHere() + s.length;						//Calculate Cost to here + edge weight from here to neighbor
					double estTotal = costToNeigh + calcHeuristic(to, destination);			//Calculate total estimate with heuristic
					
					FringeNode f = new FringeNode(to, node, costToNeigh, estTotal);		//Should only add once, and add only the best path, ensure that we can reach our destination!!
					f.setDistToGoal(calcHeuristic(to, destination));
					fringe.offer(f);
				}
			}	
		}

		return path;

	}
	
	/**Finds all Articulation Points on the graph - Iteratively
	 * 
	 * @return HashSet<Node> articulation points*/
	public void findArticulationPoints(){
		
		articulationPoints = new HashSet<Node>();
		activationStack = new Stack<StackElement>();
		
		//===========================INTIALIZE=============================
		for(Node n : graph.nodes.values())
			n.setDepth(INF);
		
		Node start = graph.nodes.get(14473);
		start.setDepth(0);
		int numSubTrees = 0;
		
		for(Node nhb : start.getNeighbours()){
			if(nhb.getDepth() == INF){
				iterArtPts(nhb, start);
				numSubTrees++;
			}
		}
		
		if(numSubTrees > 1)
			articulationPoints.add(start);
		
		//===========================INTIALIZE=============================
		
		//Highlight Points on Graph
		graph.setHighlightNodes(articulationPoints);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Articulation Points: \n");
		for(Node art: articulationPoints)
			sb.append("NodeID: "+art.nodeID+ "	Location: "+art.location.x+","+art.location.y+"\n");
		getTextOutputArea().setText(sb.toString());
		
	}
	
	public void iterArtPts(Node firstNode, Node root){
		
		//Push First Node
		activationStack.push(new StackElement(firstNode, root, 1, 0, null));	//DEPTH - 0, REACH - 1
		
		Node node, child = null;
		StackElement elem = null;
		while(!activationStack.isEmpty()){
			
			elem = activationStack.peek();
			node = elem.getNode();
			
			if(elem.getChildren() == null){
				
				node.setDepth(elem.getDepth());
				elem.setReach(elem.getDepth());
				elem.setChildren(new PriorityQueue<Node>());
				
				for(Node nhb: node.getNeighbours()){
					if(nhb != elem.getParent())
						elem.getChildren().add(nhb);				//FIRST TIME
				}
				
			}
			
			else if(!elem.getChildren().isEmpty()){
				
				child = elem.getChildren().poll();
				
				if(child.getDepth() < INF)
					elem.setReach(Math.min(elem.getReach(), child.getDepth()));
				else
					activationStack.push(new StackElement(child, elem.getParent(), node.getDepth()+1, 0, null));		//CHILDREN TO PROCESS
			}
		
			else
				if(node != firstNode){
					
					if(elem.getReach() >= elem.getParent().getDepth())
						articulationPoints.add(elem.getParent());
						
					elem.getParent().setReachBack(Math.min(elem.getParent().getReachBack(), elem.getReach()));
				}
				activationStack.pop();																				//LAST TIME
		}
	}
	
	private boolean canReachDest(Node node, Node destination){
		
		boolean canReach = false;
		
//		if(){
//			
//		}			//can we reach our destination from this node? - find all paths to node, articulation points
		
		return canReach;
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

	/**Returns the Euclidean distance between the current node and the end destination
	 * 
	 * @param Node current, Node destination*/
	public double calcHeuristic(Node current, Node destination) {
		
		double distance = 0;
		
		//Calculate Euclidean Distance
		distance = Math.sqrt(Math.pow(current.location.x - destination.location.x, 2) + 
				Math.pow(current.location.y - destination.location.y, 2));
		
		return distance;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}

	
}

// code for COMP261 assignments