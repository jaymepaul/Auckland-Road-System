import java.util.PriorityQueue;


public class StackElement {

	private Node node;
	private StackElement parent;
	private int reach, depth;
	private PriorityQueue<Node> children;

	/**Primary Constructor, creates a new StackElement that contains..
	 *
	 * @param Node node - graph Node to be processed
	 * @param int reach - reachBack value of this Node
	 * @param StackElement parent - parent StackElement that contains Node to not be visited & reach update*/
	public StackElement(Node node, int depth, StackElement parent){
		this.node = node;
		this.depth = depth;
		this.parent = parent;
	}


	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public StackElement getParent() {
		return parent;
	}

	public void setParent(StackElement parent) {
		this.parent = parent;
	}

	public int getReach() {
		return reach;
	}

	public void setReach(int reach) {
		this.reach = reach;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public PriorityQueue<Node> getChildren() {
		return children;
	}

	public void setChildren(PriorityQueue<Node> children) {
		this.children = children;
	}






}
