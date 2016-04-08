import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * A Segment is the most interesting class making up our graph, and represents
 * an edge between two Nodes. It knows the Road it belongs to as well as the
 * Nodes it joins, and contains a series of Locations that make up the length of
 * the Segment and can be used to render it.
 *
 * @author tony
 */
public class Segment {

	public final Road road;
	public final Node start, end;
	public final double length;
	public final Location[] points;

	private double pathDistance;
	private double pathTime;
	private Color color;
	
	private boolean nameVis;

	public Segment(Graph graph, int roadID, double length, int node1ID,
			int node2ID, double[] coords) {

		this.road = graph.roads.get(roadID);
		this.start = graph.nodes.get(node1ID);
		this.end = graph.nodes.get(node2ID);
		this.length = length;

		points = new Location[coords.length / 2];
		for (int i = 0; i < points.length; i++) {
			points[i] = Location
					.newFromLatLon(coords[2 * i], coords[2 * i + 1]);
		}

		this.road.addSegment(this);
		this.start.addSegment(this);
		this.end.addSegment(this);
		this.color = Mapper.SEGMENT_COLOUR;
	}

	public void draw(Graphics g, Location origin, double scale) {
		for (int i = 1; i < points.length; i++) {
			Point p = points[i - 1].asPoint(origin, scale);
			Point q = points[i].asPoint(origin, scale);
			g.drawLine(p.x, p.y, q.x, q.y);
		}
	}

	public Node getStart() {
		return start;
	}

	public Node getEnd() {
		return end;
	}

	public double getPathDistance() {
		return pathDistance;
	}

	public void setPathDistance(double pathDistance) {
		this.pathDistance = pathDistance;
	}

	public double getPathTime() {
		return pathTime;
	}

	public void setPathTime(double pathTime) {
		this.pathTime = pathTime;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isNameVis() {
		return nameVis;
	}

	public void setNameVis(boolean nameVis) {
		this.nameVis = nameVis;
	}

	

}

// code for COMP261 assignments