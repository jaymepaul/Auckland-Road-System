import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 *
 * @author tony
 */
public class Road {
	public final int roadID;
	public final String name, city;
	public final int oneWay;
	public final int speed;
	public final int roadClass;
	public final Collection<Segment> components;

	private List<Road> roads;

	public Road(int roadID, int type, String label, String city, int oneway,
			int speed, int roadclass, int notforcar, int notforpede,
			int notforbicy) {
		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.components = new HashSet<Segment>();
		this.oneWay = oneway;
		this.speed = speed;
		this.roadClass = roadclass;
		this.roads = new ArrayList<Road>();
	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}

	/**Get all Roads associated with this Road
	 *
	 * @param Map<Integer Road> mainRoads
	 * @return List<Road> - list of Roads*/
	public List<Road> getAllRoads(Map<Integer, Road> mainRoads){

		for(Road r: mainRoads.values()){					//Go through all Roads, get ones that have equal Name
			if(r.name.equals(this.name))
				roads.add(r);
		}

		return roads;
	}


}

// code for COMP261 assignments