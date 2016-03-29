
public class Restriction {

	private int nodeID1;
	private int roadID1;
	private int nodeID;
	private int roadID2;
	private int nodeID2;
	
	public Restriction(int nodeID1, int roadID1, int nodeID, int roadID2, int nodeID2) {
		this.nodeID1 = nodeID1;
		this.roadID1 = roadID1;
		this.nodeID = nodeID;
		this.roadID2 = roadID2;
		this.nodeID2 = nodeID2;
	}

	public int getNodeID1() {
		return nodeID1;
	}

	public void setNodeID1(int nodeID1) {
		this.nodeID1 = nodeID1;
	}

	public int getRoadID1() {
		return roadID1;
	}

	public void setRoadID1(int roadID1) {
		this.roadID1 = roadID1;
	}

	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public int getRoadID2() {
		return roadID2;
	}

	public void setRoadID2(int roadID2) {
		this.roadID2 = roadID2;
	}

	public int getNodeID2() {
		return nodeID2;
	}

	public void setNodeID2(int nodeID2) {
		this.nodeID2 = nodeID2;
	}
	
	

}
