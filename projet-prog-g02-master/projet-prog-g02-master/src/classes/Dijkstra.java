package classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

// @Author Lou Favre
// Inspired then adapted from https://stackoverflow.com/questions/27454084/dijkstras-algorithm-in-java
// Reason : this dijkstra algorithm seemed appropriated for our project and easy enough for us to understand it and modify it for our use
// Modifications done : Vertex class replaced with our own OsmNode class, used our own variables and methods to get and set variables at the right places


public class Dijkstra {
	
	/**
	 * Update paths and distances for a given node and mode of transport, ie using the path that that mode can use.
	 *
	 * @param sourceNode the source node
	 * @param modeOfTransport the mode of transport
	 */
	public void UpdatePaths(OsmNode sourceNode, String modeOfTransport) {		

		sourceNode.setMinDistance(0.);
		PriorityQueue<OsmNode> nodeQueue = new PriorityQueue<OsmNode>();
		nodeQueue.add(sourceNode);
		
		// While theere is a node in the queue
		while(!nodeQueue.isEmpty()) {
			OsmNode currentNode = nodeQueue.poll();
			
			Edge[] edges;
			if (modeOfTransport.equals("pedestrian")) {
				edges = currentNode.getAdjacentsForPedestrians();
			} else {
				edges = currentNode.getAdjacentsForVehicles();
			}
			
			// For each edge (link to another node)
			for(Edge e : edges) {
				if (e == null) continue;
				OsmNode currentTarget = e.getTarget();
				double weight = e.getWeight();
				// Updating the distance
				double distanceThroughCurrentNode = currentNode.getMinDistance() + weight;
				
				// If the distance is the minimum known, we use it
				if(distanceThroughCurrentNode < currentTarget.getMinDistance()) {
					nodeQueue.remove(currentTarget);
					
					currentTarget.setMinDistance(distanceThroughCurrentNode);
					currentTarget.setPreviousNode(currentNode);
					nodeQueue.add(currentTarget);
				}
			}
		}
	}
	
	 /**
 	 * Gets the shortest path to a given node. Requires a previous use of UpdatePaths() from the starting node.
 	 *
 	 * @param target the target
 	 * @return the list of OsmNode that is the shortest path to the given OsmNode
 	 */
 	public List<OsmNode> getShortestPathTo(OsmNode target) {
	        List<OsmNode> path = new ArrayList<OsmNode>();
	        
	        for(OsmNode node = target; node != null; node = node.getPreviousNode()) {
	        	path.add(node);
	        }
	        
	        Collections.reverse(path);
	        return path;
	 }
}
