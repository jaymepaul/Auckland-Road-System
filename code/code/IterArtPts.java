import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

public class IterArtPts {

	private Stack<StackElement> activationStack;
	private List<Node> articulationPoints;
	private Node firstNode;
	private Node root;
	private Graph graph;
	
	private final int INF = (int)Double.POSITIVE_INFINITY;
	
	public IterArtPts(Graph graph){
		this.graph = graph;
		
		initArtPts();
	}
	
	public void initArtPts(){
		
		articulationPoints = new ArrayList<Node>();
		activationStack = new Stack<StackElement>();
		
		//===========================INTIALIZE=============================
		for(Node n : graph.nodes.values()){
			n.setVisited(false);
			n.setDepth(INF);
		}
		
		for(Node start : graph.getDisconnectedNodes()){		//Find All SubGraphs/SubNodes - Deals with Disconnected
			
			start.setDepth(0);
			int numSubTrees = 0;
			
			for(Node nhb : start.getNeighbours()){		//For each neighbor of the subcomponent Node find the art pts
				if(nhb.getDepth() == INF){
					findArtPts(nhb, start);
					numSubTrees++;
				}
			}
			if(numSubTrees > 1)
				articulationPoints.add(start);
			
		}
		
	}
	
	public void findArtPts(Node firstNode, Node root){
		
		// Push First Node
		StackElement parent = new StackElement(root, 0, null, true);
		activationStack.push(new StackElement(firstNode, 0, parent));

		Node node, child = null;
		StackElement elem = null;
		while (!activationStack.isEmpty()) {

			elem = activationStack.peek();
			node = elem.getNode();

			if (elem.getChildren() == null) {

				node.setDepth(elem.getDepth());
				elem.setReach(elem.getDepth());
				elem.setChildren(new PriorityQueue<Node>());

				for (Node nhb : node.getNeighbours()) {
					if (nhb != elem.getParent().getNode()) // EXCLUDE FROM NODE
						elem.getChildren().add(nhb); // FIRST TIME
				}
			}

			else if (!elem.getChildren().isEmpty()) { // IF WE HAVE CHILDREN TO
														// PROCESS

				child = elem.getChildren().poll();

				if (child.getDepth() < INF)
					elem.setReach(Math.min(elem.getReach(), child.getDepth()));
				else
					activationStack.push(new StackElement(child, elem.getDepth() + 1,
							new StackElement(elem.getNode(), 0, null, true))); // CHILDREN
																				// TO
																				// PROCESS
			}

			else {
				if (node != firstNode) {

					if (elem.getReach() >= elem.getParent().getDepth())
						articulationPoints.add(elem.getParent().getNode());

					elem.getParent().setReach(Math.min(elem.getParent().getReach(), elem.getReach()));
				}
				activationStack.pop();
			} // LAST TIME
		}
	}

	public List<Node> getArticulationPoints() {
		return articulationPoints;
	}
	
	
	
}
