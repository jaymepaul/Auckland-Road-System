import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 *
 * @author tony
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;
	// restrictions
	Collection<Restriction> restrictions;
	// polygons
	List<Polygon> polygons = new ArrayList<Polygon>();

	Collection<Road> highlightedRoads = new HashSet<>();
	Collection<Segment> highlightedSegments = new ArrayList<>();
	Collection<Node> articulationPoints = new LinkedList<>();

	List<List<Node>> subNodes = new ArrayList<List<Node>>();

	Node highlightedNode, startNode, endNode;			//A* Variables
	String point = null;

	public Graph(File nodesFile, File roads, File segments, File polygonFile, File restrictions, File traffic) throws IOException {
		this.nodes = Parser.parseNodes(nodesFile, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
		this.restrictions = Parser.parseRestrictions(restrictions, this);
		this.polygons = Parser.parsePolygons(polygonFile, polygons);
		Parser.parseTrafficLights(traffic, nodes);


		findAllSubGraphs();			//Creates a List of List of Nodes that each represent a component of the graph
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		g2.setColor(Mapper.SEGMENT_COLOUR);
		for (Segment s : segments){
			s.draw(g2, origin, scale);
		}

		//Draw Polygons
		for (Polygon pol : polygons)
			pol.drawPolygons(g2, screen, origin, scale);

		//Draw Highlighted Segments
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		if(highlightedSegments!=null){
			for (Segment seg : highlightedSegments)
				seg.draw(g2, origin, scale);
		}

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		if(startNode!=null){
			g2.setColor(Color.GREEN);								//Highlight Start and End Nodes
			startNode.draw(g2, screen, origin, scale);
		}
		if(endNode!=null){
			g2.setColor(Color.RED);
			endNode.draw(g2, screen, origin, scale);
		}
		if(articulationPoints != null){
			g2.setColor(Color.GREEN);
			for(Node n : articulationPoints)
				n.draw(g2, screen, origin, scale);					//Highlight Articulation Points
		}
	}

	public void setHighlight(Node node, String point) {
		this.highlightedNode = node;

		if(point == "origin")
			startNode = node;
		else if(point == "destination")
			endNode = node;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}

	public Segment getSegmentFromPoints(Node from, Node to){

		Segment segment = null;

		for(Segment s : segments){
			if(s.start.nodeID == from.nodeID && s.end.nodeID == to.nodeID
					|| s.start.nodeID == to.nodeID && s.end.nodeID == from.nodeID){
				segment = s;
			}
		}
		return segment;
	}

	public void setHighlightPath(List<Segment> path, Node start, Node end) {
		this.startNode = start;
		this.endNode = end;
		this.highlightedSegments = path;
	}

	public void setHighlightNodes(List<Node> articulationPoints) {
		this.articulationPoints = articulationPoints;
	}

	/**Breadth First Search algo to find all disconnected Nodes on the graph*/
	public void findAllSubGraphs(){

		//Breadth First Search
		int counter = 0, MAX_NODES = nodes.size();
		int INF = (int)Double.POSITIVE_INFINITY;

		for(Node n : nodes.values()){
			n.setDist(INF);
			n.setParent(null);
		}

		while(counter != MAX_NODES){

			Node root = getNewNode();							//Get a disconnected Node, perform BFS
			Queue<Node> queue = new LinkedList<Node>();
			List<Node> subN = new ArrayList<Node>();

			root.setDist(0);
			queue.add(root);

			Node cur = null;
			while(!queue.isEmpty()){

				cur = queue.poll();
				subN.add(cur);
				counter++;

				for(Node nhb: cur.getNeighbours()){
					if(nhb.getDist() == INF){
						nhb.setDist(cur.getDist()+1);
						nhb.setParent(cur);
						queue.offer(nhb);
					}
				}
			}
			subNodes.add(subN);				//Add to Collection of SubNodes
		}
	}

	/**Gets a Node that is disconnected from the graph
	 *
	 * @return Node - disconnected Node to be processed*/
	public Node getNewNode() {

		Node node = null;

		Set<Node> tmpNodes = new HashSet<Node>();
		for(Node n : nodes.values())
			tmpNodes.add(n);						//Copy Set of Nodes

		if(subNodes.size() == 0)
			node = nodes.get(14392);				//Arbitrary Start Node
		else if(subNodes.size() > 0){
			for(List<Node> col : subNodes)
				tmpNodes.removeAll(col);			//Difference between subNodes set and full set of Nodes
		}

		for(Node nb : tmpNodes){
			node = nb;
			break;
		}

		return node;
	}

	/**Returns a list of all disconnected Nodes
	 *
	 * @return List<Node> - list of disconnected nodes*/
	public List<Node> getDisconnectedNodes() {

		List<Node> discNodes = new ArrayList<Node>();

		for(List<Node> list : subNodes)
			discNodes.add(list.get(0));

		return discNodes;
	}

	/**Checks if start Node and end Node are within the same component of the Graph
	 *
	 * @return boolean - valid/invalid route*/
	public boolean checkRoute(Node start, Node end){

		boolean valid = false;

		for(List<Node> list : subNodes){
			if(list.contains(start) && list.contains(end))		//Check if both Nodes are in one component of the graph, I.e. NOT DISCONNECTED
				valid = true;
		}

		return valid;
	}

	/**Highlights Road based on Sequence of Segments within that Road*/
	public void highlightRoads(List<Road> selectRoads){

		highlightedSegments = new ArrayList<Segment>();

		for(Road r : selectRoads){

			for(Road rd : r.getAllRoads(roads)){			//For each Road, get All Roads assoc. to it

				for(Segment s : rd.components){
					highlightedSegments.add(s);
				}
			}
		}
	}
	
	public void highlightRoad(Road r){
		
		highlightedSegments = new ArrayList<Segment>();
		
		for(Road rd : r.getAllRoads(roads)){
			for(Segment s : rd.components)
				highlightedSegments.add(s);
		}
	}

}

// code for COMP261 assignments