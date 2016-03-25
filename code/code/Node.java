import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author tony
 */
public class Node {

	public final int nodeID;
	public final Location location;
	public final Collection<Segment> segments;

	private Collection<Segment> inNeighbours;
	private Collection<Segment> outNeighbours;
	
	private boolean visited;
	private Node pathFrom; 
	private double cost;
	
	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<Segment>();
		
		this.visited = false;					//Initially set to false
		this.pathFrom = null;					//Initially null
		
		this.inNeighbours = new ArrayList<Segment>();
		this.outNeighbours = new ArrayList<Segment>();
	}

	public void addSegment(Segment seg) {
		segments.add(seg);
	}

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
	
	public double estimate(Node origin, Node destination, Collection<Segment> segments){
		
		double estimate = 0;
		
		int originID = origin.nodeID;
		int destID = destination.nodeID;
		
		for(Segment s : segments){
			
		}
		
		return estimate;
	}

	public Collection<Segment> getInNeighbours() {
		return inNeighbours;
	}

	public Collection<Segment> getOutNeighbours() {
		
	
		
		return outNeighbours;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public Node getPathFrom() {
		return pathFrom;
	}

	public void setPathFrom(Node pathFrom) {
		this.pathFrom = pathFrom;
	}

	public double getCostToHere() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
	
	
}

// code for COMP261 assignments