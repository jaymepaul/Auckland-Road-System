import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

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
	public static final double MIN_ZOOM = 0.5, MAX_ZOOM = 200;

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
	private List<Node> articulationPoints;


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

			if(source == "Origin"){
				getSearchOrigin().setText(Integer.toString(closest.nodeID));
				graph.setHighlight(closest, "origin");
				redraw();													//Highlight Origin
			}
			else if(source == "Destination"){
				getSearchDestination().setText(Integer.toString(closest.nodeID));
				graph.setHighlight(closest, "destination");					//Highlight Destination
				redraw();
			}
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
	protected void onLoad(File nodes, File roads, File segments, File polygons, File restrictions, File traffic) {
		
		try {
			graph = new Graph(nodes, roads, segments, polygons, restrictions, traffic);
		} catch (IOException e) {
			e.printStackTrace();
		}
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}
	
	@Override
	protected void onScroll(MouseWheelEvent e) {

		int zoomFactor = e.getWheelRotation();			//Neg - AWAY, Pos - TOWARDS
		
		if(zoomFactor > 0){
			if(scale > MIN_ZOOM){
				scaleOrigin(true);
				scale /= ZOOM_FACTOR;
			}
		
		}else if(zoomFactor < 0){
			if(scale < MAX_ZOOM){
				scaleOrigin(false);
				scale *= ZOOM_FACTOR;								//Zooming
			}
		}
	}

	/**Uses the A* search algorithm to find the Shortest Path 
	 * from the given origin and destination Node.
	 * Highlights the path on the map and prints out the
	 * roads along the path and its respective distances
	 * or time.
	 * 
	 *  @param boolean distTime - toggle to choose between distance/time heuristic
	 * */
	@Override
	protected void findShortestPath(String origin, String destination, boolean distTime) {

		Node start = graph.nodes.get(Integer.parseInt(origin));
		Node end = graph.nodes.get(Integer.parseInt(destination));

		AStarSearch aStar = new AStarSearch(graph, start, end);

		if(!distTime){														//PATH BASED ON DISTANCE
			//Exception: Check for Disconnected Route/Path
			if(graph.checkRoute(start, end)){

				aStar.setOrigin(start); aStar.setDestination(end);
				List<Segment> path = aStar.searchDist();				//Perform AStar Search - DIST

				getTextOutputArea().setText(getPathTextOutput(path, start, end));
				graph.setHighlightPath(path, start, end);						//Highlight and Display Path

			}
			else if(!graph.checkRoute(start, end)){
				StringBuilder sb = new StringBuilder();
				sb.append("Invalid Route! " + "	Start: "+start.nodeID + "	End: "+end.nodeID+"\n");
				sb.append("Disconnected Path! \n");
				getTextOutputArea().setText(sb.toString());
			}
		}
		else if(distTime){												//PATH BASED ON TIME

			if(graph.checkRoute(start, end)){

				aStar.setOrigin(start); aStar.setDestination(end);		//TEST: 2707 - 3068, DIST V TIME, 2707-30126
				List<Segment> timePath = aStar.searchPathTime();

				getTextOutputArea().setText(getTimePathTextOutput(timePath, start, end));
				graph.setHighlightPath(timePath, start, end);						//Highlight and Display Path
			}
			else if(!graph.checkRoute(start, end)){
				StringBuilder sb = new StringBuilder();
				sb.append("Invalid Route! " + "	Start: "+start.nodeID + "	End: "+end.nodeID+"\n");
				sb.append("Disconnected Path! \n");
				getTextOutputArea().setText(sb.toString());
			}
		}

	}

	/**Finds all Articulation Points on the graph - Iteratively
	 * Considers sub components of the graph
	 * 
	 **/
	public void findArticulationPoints(){

		IterArtPts artPts = new IterArtPts(graph);
		articulationPoints = artPts.getArticulationPoints();			//Find Articulation Points

		//===========================INTIALIZE=============================

		//Highlight Points on Graph
		graph.setHighlightNodes(articulationPoints);

		StringBuilder sb = new StringBuilder();
		sb.append("Articulation Points: \n");
		for(Node art: articulationPoints)
			sb.append("NodeID: "+art.nodeID+ "	Location: "+art.location.x+","+art.location.y+"\n");
		sb.append("Total Number of Articulation Points: "+articulationPoints.size());
		getTextOutputArea().setText(sb.toString());

	}

	/**Returns the text output of the path
	 * Eliminates Duplicate Road Names, Gets Max Distance for each Road
	 * 
	 * @return String - text output of path*/
	public String getPathTextOutput(List<Segment> path, Node start, Node end){

		StringBuilder sb = new StringBuilder();
		sb.append("START: "+start.nodeID+"	END: "+end.nodeID+"\n"+
		"Start At Intersection: "+start.nodeID+"	Total Distance To Goal: "+ AStarSearch.calcDistHeuristic(start, end)+" km \n");

		String name = null;
		double dist = 0;
		Map<String, Double> textPath = new LinkedHashMap<String, Double>();

		name = path.get(path.size()-1).road.name;

		for(int i = path.size()-1; i >= 0; i--){

			if(path.get(i).road.name == name){

				if(i == 0)
					textPath.put(name, dist);

				if(path.get(i).getPathDistance() > dist)
					dist = path.get(i).getPathDistance();
			}
			else if(path.get(i).road.name != name){

				textPath.put(name, dist);

				name = path.get(i).road.name;
				dist = path.get(i).getPathDistance();

				if(i == 0)
					textPath.put(name, dist);

			}

		}

		for(Map.Entry<String, Double> e : textPath.entrySet())
			sb.append("Street: " + e.getKey() + "	Distance To Goal: " + e.getValue() +" km \n");
		
		sb.append("Total Time Taken : "+getTimeElapsed((long) (AStarSearch.calcTotalTime(path) * 1000))+"\n");
		sb.append("REACHED END GOAL!");

		return sb.toString();
	}

	/**Returns the text output of the path
	 * Omits duplicate road names and includes the max time for each road
	 * 
	 * @return String - text output of path*/
	public String getTimePathTextOutput(List<Segment> path, Node start, Node end){

		StringBuilder sb = new StringBuilder();
		sb.append("START: "+start.nodeID+"	END: "+end.nodeID+"\n"+
		"Start At Intersection: "+start.nodeID+ "	Total Estimated Time To Goal: "+ getTimeElapsed((long)(AStarSearch.calcTimeHeuristic(start, end)*3600))+" \n");

		String name = null;
		double time = 0;
		Map<String, Double> textPath = new LinkedHashMap<String, Double>();

		name = path.get(path.size()-1).road.name;

		for(int i = path.size()-1; i >= 0; i--){

			if(path.get(i).road.name == name){

				if(i == 0)
					textPath.put(name, time);

				if(path.get(i).getPathTime() > time)
					time = path.get(i).getPathTime();
			}
			else if(path.get(i).road.name != name){

				textPath.put(name, time);

				name = path.get(i).road.name;
				time = path.get(i).getPathTime();

				if(i == 0)
					textPath.put(name, time);

			}

		}

		for(Map.Entry<String, Double> e : textPath.entrySet())
			sb.append("Street: " + e.getKey() + "	Time To Goal: " + getTimeElapsed( (long) (e.getValue()*3600)) +" \n");

		sb.append("REACHED END GOAL!");

		return sb.toString();
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

	public void reset(){
		graph.articulationPoints = null;
	}
	
	public String getTimeElapsed(long l){
		
		final long hr = TimeUnit.MILLISECONDS.toHours(l);
		final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec);
		
		return String.format("%02d:%02d:%02d", hr, min, sec);
		
	}

}

// code for COMP261 assignments