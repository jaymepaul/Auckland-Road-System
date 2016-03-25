import java.util.PriorityQueue;


public class StackElement {

	private Node node, parent;
	private int reach, depth;
	private PriorityQueue<Node> children;
	
	public StackElement(Node node, Node parent, int reach, int depth, PriorityQueue<Node> children){
		this.node = node;
		this.parent = parent;
		this.reach = reach;
		this.depth = depth;
		this.children = children;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
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
