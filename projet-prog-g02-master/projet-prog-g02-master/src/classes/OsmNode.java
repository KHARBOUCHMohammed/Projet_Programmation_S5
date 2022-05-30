package classes;

import java.util.ArrayList;
import java.util.List;

// @Author Dorian Vabre

/**
 * An object representing a node on the map
 */
public class OsmNode implements Comparable<OsmNode> {

	/** The id of the node */
	private long id;
	
	/** The visibility of the node */
	private String visibility;
	
	/** The latitude. */
	private double lat;
	
	/** The longitude. */
	private double lon;
	
	/** The list of the children OsmTag of the node. */
	private List<OsmTag> childOsmTagList = new ArrayList<OsmTag>();
	
	/** The array of Edges pointing to OsmNodes that are accessible from this nodes for a pedestrian */
	private Edge[] adjacentsForPedestrians;
	
	/** The number of Edges pointing to OsmNodes that are accessible from this nodes for a pedestrian */
	private int nbAdjacentsForPedestrians;
	
	/** The array of Edges pointing to OsmNodes that are accessible from this nodes for a vehicle */
	private Edge[] adjacentsForVehicles;
	
	/** The number of Edges pointing to OsmNodes that are accessible from this nodes for a vehicle */
	private int nbAdjacentsForVehicles;
	
	/** The minimum distance from a given node to this node (used only for Dijkstra) */
	private double minDistance = Double.POSITIVE_INFINITY;
	
	/** The previous node of the path to be reached by a given node (used only for Dijkstra) */
	private OsmNode previousNode;
	
	public OsmNode(long theId, String theVisibility, double theLat, double theLon, List<OsmTag> theChildOsmTagList) {
		
		this.id = theId;
		this.visibility = theVisibility;
		this.lat = theLat;
		this.lon = theLon;
		this.childOsmTagList = theChildOsmTagList;
		this.adjacentsForPedestrians = new Edge[50];
		this.adjacentsForVehicles = new Edge[50];
		this.previousNode = null;
		this.nbAdjacentsForPedestrians = 0;
		this.nbAdjacentsForVehicles = 0;
	}

	/**
	 * Compare the distance to this node with the distance to another node (from a third node, used in Dijkstra algorithm)
	 *
	 * @param other the other node
	 * @return the int positive if this node is nearer, negative if it is further, 0 if same
	 */
	public int compareTo(OsmNode other) {
		return Double.compare(this.minDistance, other.getMinDistance());
	}
	
	/**
	 * Gets the distance between the given node and this node.
	 *
	 * @param other the other
	 * @return the distance between nodes
	 */
	public Double getDistanceBetweenNodes(OsmNode other) {
	    Double x2 = other.getLon();
	    Double y2 = other.getLat();
	    return Math.sqrt((x2-this.lon)*(x2-this.lon) + (y2-this.lat)*(y2-this.lat));
	}
	
	/**
	 * Adds an OsmNode in the adjacent OsmNodes list (for pedestrians).
	 *
	 * @param node the node
	 */
	public void addAdjacentForPedestrians(OsmNode node) {
		this.adjacentsForPedestrians[nbAdjacentsForPedestrians] = new Edge(node, getDistanceBetweenNodes(node));
		this.nbAdjacentsForPedestrians++;
	}
	
	/**
	 * Adds an OsmNode in the adjacent OsmNodes list (for vehicles).
	 *
	 * @param node the node
	 */
	public void addAdjacentForVehicles(OsmNode node) {
		this.adjacentsForVehicles[nbAdjacentsForVehicles] = new Edge(node, getDistanceBetweenNodes(node));
		this.nbAdjacentsForVehicles++;
	}

	/**
	 * Resets the distances used in Dijkstra algorithm
	 */
	public void flushDistances() {
		this.minDistance = Double.POSITIVE_INFINITY;
		this.previousNode = null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public double getLat() {
		return lat;
	}
	
	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public double getLon() {
		return lon;
	}
	
	public void setLon(double lon) {
		this.lon = lon;
	}

	public Edge[] getAdjacentsForPedestrians() {
		return this.adjacentsForPedestrians;
	}
	
	public Edge[] getAdjacentsForVehicles() {
		return this.adjacentsForVehicles;
	}

	public double getMinDistance() {
		return this.minDistance;
	}

	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}

	public OsmNode getPreviousNode() {
		return this.previousNode;
	}

	public void setPreviousNode(OsmNode previousNode) {
		this.previousNode = previousNode;
	}
	
	public int getNbAdjacentsForPedestrians() {
		return this.nbAdjacentsForPedestrians;
	}

	public int getNbAdjacentsForVehicles() {
		return this.nbAdjacentsForVehicles;
	}
	
	public List<OsmTag> getChildOsmTagList() {
		return childOsmTagList;
	}

	public String toString() {
		return "OsmNode [id=" + this.id + ", lat=" + this.lat + ", lon=" + this.lon + "]";
	}

}
