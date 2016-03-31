import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
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
	
	Collection<Road> highlightedRoads = new HashSet<>();
	Collection<Segment> highlightedSegments = new ArrayList<>();
	Collection<Node> articulationPoints = new LinkedList<>();
	
	List<List<Node>> subNodes = new ArrayList<List<Node>>();
	
	Node highlightedNode, startNode, endNode;			//A* Variables
	
	public Graph(File nodes, File roads, File segments, File polygons, File restrictions) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
		this.restrictions = Parser.parseRestrictions(restrictions, this);
		
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
		for (Segment s : segments)
			s.draw(g2, origin, scale);

		// draw the segments of all highlighted roads.
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
			for (Segment seg : road.components) {
				seg.draw(g2, origin, scale);
			}
		}
		
		//Draw Highlighted Segments
		for (Segment seg : highlightedSegments) 
			seg.draw(g2, origin, scale);
		

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}
		if(startNode!=null || endNode!=null){
			g2.setColor(Color.GREEN);								//Highlight Start and End Nodes
			startNode.draw(g2, screen, origin, scale);
			g2.setColor(Color.RED);
			endNode.draw(g2, screen, origin, scale);
		}
		if(articulationPoints != null){
			g2.setColor(Color.GREEN);
			for(Node n : articulationPoints)
				n.draw(g2, screen, origin, scale);					//Highlight Articulation Points
		}
	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
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
	
}

// code for COMP261 assignments