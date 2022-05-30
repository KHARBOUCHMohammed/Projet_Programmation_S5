package classes;

// Author Dorian Vabre

/**
 * A link to another OsmNode, with the distance between both
 */
public class Edge {
	
	/** The OsmNode target that the Edge points towards. */
	private OsmNode target;
	
	/** The distance with the target. */
	private double weight;
	
	public Edge(OsmNode node, double weight) {
		this.target = node;
		this.weight = weight;
	}

	public String toString() {
		return "Edge [target=" + this.target + ", weight=" + this.weight + "]";
	}

	public OsmNode getTarget() {
		return this.target;
	}

	public void setTarget(OsmNode target) {
		this.target = target;
	}

	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
