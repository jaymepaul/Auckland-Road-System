import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This utility class provides three static methods for parsing each of the
 * three files we're interested in, and returning the relevant data structure.
 * Internally it uses BufferedReaders instead of Scanners to read in the files,
 * as Scanners are pathetically slow.
 * 
 * @author tony
 */
public class Parser {

	public static Map<Integer, Node> parseNodes(File nodes, Graph graph) {
		Map<Integer, Node> map = new HashMap<Integer, Node>();

		try {
			// make a reader
			BufferedReader br = new BufferedReader(new FileReader(nodes));
			String line;

			// read in each line of the file
			while ((line = br.readLine()) != null) {
				// tokenise the line by splitting it at the tabs.
				String[] tokens = line.split("[\t]+");

				// process the tokens
				int nodeID = asInt(tokens[0]);
				double lat = asDouble(tokens[1]);
				double lon = asDouble(tokens[2]);

				Node node = new Node(nodeID, lat, lon);
				map.put(nodeID, node);
			}

			br.close();
		} catch (IOException e) {
			throw new RuntimeException("file reading failed.");
		}

		return map;
	}

	public static Map<Integer, Road> parseRoads(File roads, Graph graph) {
		Map<Integer, Road> map = new HashMap<Integer, Road>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(roads));
			br.readLine(); // throw away the top line of the file.
			String line;

			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("[\t]+");

				int roadID = asInt(tokens[0]);
				int type = asInt(tokens[1]);
				String label = tokens[2];
				String city = tokens[3];
				int oneway = asInt(tokens[4]);
				int speed = asInt(tokens[5]);
				int roadclass = asInt(tokens[6]);
				int notforcar = asInt(tokens[7]);
				int notforpede = asInt(tokens[8]);
				int notforbicy = asInt(tokens[8]);

				Road road = new Road(roadID, type, label, city, oneway, speed,
						roadclass, notforcar, notforpede, notforbicy);
				map.put(roadID, road);
			}

			br.close();
		} catch (IOException e) {
			throw new RuntimeException("file reading failed.");
		}

		return map;
	}

	public static Collection<Segment> parseSegments(File segments, Graph graph) {
		Set<Segment> set = new HashSet<Segment>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(segments));
			br.readLine(); // throw away the top line of the file.
			String line;

			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("[\t]+");

				int roadID = asInt(tokens[0]);
				double length = asDouble(tokens[1]);
				int node1ID = asInt(tokens[2]);
				int node2ID = asInt(tokens[3]);

				double[] coords = new double[tokens.length - 4];
				for (int i = 4; i < tokens.length; i++)
					coords[i - 4] = asDouble(tokens[i]);

				Segment segment = new Segment(graph, roadID, length, node1ID,
						node2ID, coords);
				
				segment.start.getOutNeighbours().add(segment);			
				segment.end.getInNeighbours().add(segment);				//Initialize Neighbours
				segment.end.getOutNeighbours().add(segment);
				
			
				
				set.add(segment);
			}

			br.close();
		} catch (IOException e) {
			throw new RuntimeException("file reading failed.");
		}

		return set;
	}
	
	public static Collection<Restriction> parseRestrictions(File restrictions, Graph graph){
		
		Set<Restriction> rest = new HashSet<Restriction>();
		
		if(restrictions!=null){
			try {
				BufferedReader br = new BufferedReader(new FileReader(restrictions));
				br.readLine(); // throw away the top line of the file.
				String line;

				while ((line = br.readLine()) != null) {
					String[] tokens = line.split("[\t]+");

					Node N1 = graph.nodes.get(asInt(tokens[0]));
					Road R1 = graph.roads.get(asInt(tokens[1]));
					Node N = graph.nodes.get(asInt(tokens[2]));
					Road R2 = graph.roads.get(asInt(tokens[3]));
					Node N2 = graph.nodes.get(asInt(tokens[4]));

					Restriction restriction = new Restriction(N1, R1, N, R2, N2);
					rest.add(restriction);

					N.getRestrictions().add(restriction);				//Add to set of Restrictions
				}

				br.close();
			} catch (IOException e) {
				throw new RuntimeException("file reading failed.");
			}
		}
		
		return rest;
	}
	
	public static void parseTrafficLights(File lights, Map<Integer, Node> nodes){
		
		if(lights!=null){
			try {
				BufferedReader br = new BufferedReader(new FileReader(lights));
				String line;

				while ((line = br.readLine()) != null) {
					String[] tokens = line.split("[\t]+");

					double lat = asDouble(tokens[0]);
					double lon = asDouble(tokens[1]);
					
					Location loc = Location.newFromLatLon(lon, lat);
					Node node = null;
					double locDiff = 1000;
					//Lets find closest match of location to Node
					for(Node n : nodes.values()){
						if(n.location.distance(loc) <= locDiff){
							locDiff = n.location.distance(loc);
							node = n;
						}
					}
					
					if(node!=null)
						node.setHasLights(true);  	//Set Node to have lights
				}

				br.close();
			} catch (IOException e) {
				throw new RuntimeException("file reading failed.");
			}
		}
	} 

	private static int asInt(String str) {
		return Integer.parseInt(str);
	}

	private static double asDouble(String str) {
		return Double.parseDouble(str);
	}
}

// code for COMP261 assignments