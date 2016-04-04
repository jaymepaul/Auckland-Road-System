
public class Restriction {

	private Node N1;
	private Road R1;
	private Node N;
	private Road R2;
	private Node N2;
	
	public Restriction(Node N1, Road R1, Node N, Road R2, Node N2) {
		this.N1 = N1;
		this.R1 = R1;
		this.N = N;
		this.R2 = R2;
		this.N2 = N2;
	}

	public Node getN1() {
		return N1;
	}

	public void setN1(Node n1) {
		N1 = n1;
	}

	public Road getR1() {
		return R1;
	}

	public void setR1(Road r1) {
		R1 = r1;
	}

	public Node getN() {
		return N;
	}

	public void setN(Node n) {
		N = n;
	}

	public Road getR2() {
		return R2;
	}

	public void setR2(Road r2) {
		R2 = r2;
	}

	public Node getN2() {
		return N2;
	}

	public void setN2(Node n2) {
		N2 = n2;
	}

	

}
