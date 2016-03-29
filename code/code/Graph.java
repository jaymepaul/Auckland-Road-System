import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	Collection<Segment> highlightedSegments = new HashSet<>();
	Collection<Node> articulationPoints = new HashSet<>();
	
	Node highlightedNode, startNode, endNode;			//A* Variables
	
	public Graph(File nodes, File roads, File segments, File polygons, File restrictions) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
		this.restrictions = Parser.parseRestrictions(restrictions, this);
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

	public void setHighlightPath(LinkedHashMap<Segment, Double> path, Node start, Node end) {
		
		HashSet<Segment> segs = new HashSet<Segment>();
		
		for(Segment s : path.keySet())
			segs.add(s);
		
		this.startNode = start;
		this.endNode = end;
		this.highlightedSegments = segs;
	}

	public void setHighlightNodes(HashSet<Node> articulationPoints) {
		this.articulationPoints = articulationPoints;
	}
	
}

// code for COMP261 assignments