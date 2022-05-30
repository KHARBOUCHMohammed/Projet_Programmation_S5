package classes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// @Author Dorian Vabre

/**
 * A graph containing all the useful data from a OSM XML file needed to draw it : nodes, ways, min and max lat/long
 */
public class OsmGraph {
	
	/** The factory to create a document builder. */
	private DocumentBuilderFactory factory;
	
	/** The document builder used to parse the XML file. */
	private DocumentBuilder builder;
	
	/** The document got from the XML file. */
	private Document document;
	
	/** The map linking each OsmNode Id with the associated OsmNode */
	private Map<Long, OsmNode> osmNodeMap;
	
	/** The map linking a way Id to the associated OsmWay */
	private Map<Long, OsmWay> osmWayMap;
	
	/** The map linking the name of the street to the associated Way */
	private Map<String, OsmWay> osmWayMapByName;
	
	/** The list of accessible OsmNodes (that are on a path). */
	private List<OsmNode> accessibleOsmNodes;
	
	/** The number of OsmNodes. */
	private int nbNodes;
	
	/** The number of OsmWays. */
	private int nbWays;
	
	/** The minimum latitude of all OsmNodes of the graph. */
	private double minLat;
	
	/** The maximum latitude of all OsmNodes of the graph. */
	private double maxLat;
	
	/** The minimum longitude of all OsmNodes of the graph. */
	private double minLon;
	
	/** The maximum longitude of all OsmNodes of the graph. */
	private double maxLon;
	
	/** Based on the min/max latitude, represents the amount of pixels to represent 1 lat  */
	private double pixelPerLat;
	
	/** Based on the min/max longitude, represents the amount of pixels to represent 1 lat  */
	private double pixelPerLon;

    public OsmGraph(String pathToFile) {
    	initializeDocumentBuilder(pathToFile);
    	
    	NodeList bounds = this.document.getDocumentElement().getElementsByTagName("bounds");

    	for (int temp = 0; temp < bounds.getLength(); temp++) {
            Node node = bounds.item(temp);
            
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                this.minLat = Double.parseDouble(element.getAttribute("minlat"));
                this.minLon = Double.parseDouble(element.getAttribute("minlon"));
                this.maxLat = Double.parseDouble(element.getAttribute("maxlat"));
                this.maxLon = Double.parseDouble(element.getAttribute("maxlon"));
            }
        }
    	
    	// Getting the Map NodeID->OsmNode
    	initializeOsmNodeMap();
    	
    	// Getting the Map WayID->OsmWay and 
    	initializeOsmWayMap();
    	
    	// Filling the accessibleOsmNodes list
    	this.accessibleOsmNodes = new ArrayList<OsmNode>();
    	initializeAccessibleOsmNodes();
    	
    	// Initializing the adjacent Nodes of each Node that is in a Way
    	initializeEdges();
    	
    	// Getting the Map WayName->OsmWay
    	initializeOsmWayMapByName();
	}


	/**
	 * Initialize document builder.
	 *
	 * @param pathToFile the path to file
	 */
	public void initializeDocumentBuilder(String pathToFile) {
    	this.factory = DocumentBuilderFactory.newInstance();
		try {
			this.builder = this.factory.newDocumentBuilder();
			this.document = this.builder.parse(new File(pathToFile));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Initialize the OsmNode map.
     */
    public void initializeOsmNodeMap() {
    	NodeList listOfOsmNodes = this.document.getDocumentElement().getElementsByTagName("node");
    	
    	this.osmNodeMap = new HashMap<Long, OsmNode>();
    	this.nbNodes = listOfOsmNodes.getLength();
    	
    	for (int temp = 0; temp < this.nbNodes; temp++) {
            Node node = listOfOsmNodes.item(temp);
            
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                long id = Long.parseLong(element.getAttribute("id"));
                String visible = element.getAttribute("visible");
                double lat = Double.parseDouble(element.getAttribute("lat"));
                double lon = Double.parseDouble(element.getAttribute("lon"));

                NodeList osmNodeList = element.getChildNodes();
                List<OsmTag> childOsmTagList = new ArrayList<OsmTag>();
                
                // For each child, adding it to the list of Tags of that Node
            	for (int tempChildIndex = 0; tempChildIndex < osmNodeList.getLength(); tempChildIndex++) {
                    Node child = osmNodeList.item(tempChildIndex);
                    
                    if (child.getNodeName().equals("tag")) {
                    	String type = child.getAttributes().getNamedItem("k").getNodeValue();
                    	String value = child.getAttributes().getNamedItem("v").getNodeValue();

                    	childOsmTagList.add(new OsmTag(type, value));
            		}
            	}
                
                this.osmNodeMap.put(id, new OsmNode(id, visible, lat, lon, childOsmTagList));
            }
        }
    }
    
    /**
     * Calculate the ratio coord to pixels.
     *
     * @param height the height
     * @param width the width
     */
    public void calculateRatioCoordToPixels(double height, double width) {
    	double latDiff = this.maxLat-this.minLat;
    	double lonDiff = this.maxLon-this.minLon;

    	this.setPixelPerLat(height/latDiff);
    	this.setPixelPerLon(width/lonDiff);
    }
    
    /**
     * Initialize osm way map.
     */
    public void initializeOsmWayMap() {
    	NodeList listOfOsmWays = this.document.getDocumentElement().getElementsByTagName("way");
    	
    	this.osmWayMap = new HashMap<Long, OsmWay>();
    	this.nbWays = listOfOsmWays.getLength();
    	
    	for (int tempWayIndex = 0; tempWayIndex < this.nbWays; tempWayIndex++) {
            Node way = listOfOsmWays.item(tempWayIndex);

            if (way.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) way;
                
                long id = Long.parseLong(element.getAttribute("id"));
                String visible = element.getAttribute("visible");
                NodeList osmNodeList = element.getChildNodes();
                
                List<Long> childOsmNodeList = new ArrayList<Long>();
                List<OsmTag> childOsmTagList = new ArrayList<OsmTag>();
                String name = null;
                
                // For each child, adding it to the list of Nodes or Tags of that Way
            	for (int tempChildIndex = 0; tempChildIndex < osmNodeList.getLength(); tempChildIndex++) {
                    Node child = osmNodeList.item(tempChildIndex);
                    
                    if (child.getNodeName().equals("tag")) {
                    	String type = child.getAttributes().getNamedItem("k").getNodeValue();
                    	String value = child.getAttributes().getNamedItem("v").getNodeValue();
                    	
                    	if (type.equals("name")) {
                    		name = value;
                    	}
                    	
                    	childOsmTagList.add(new OsmTag(type, value));
            		} else if (child.getNodeName().equals("nd")) {
            			Long ref = Long.parseLong(child.getAttributes().getNamedItem("ref").getNodeValue());
            			childOsmNodeList.add(ref);
            		}
            	}
            	
                OsmWay osmWay = new OsmWay(id, visible, childOsmNodeList, childOsmTagList, name);

                this.osmWayMap.put(id, osmWay);
            }
        }
    }
    
    /**
     * Initialize the list of accessible OsmNodes.
     */
    public void initializeAccessibleOsmNodes() {
    	for (Entry<Long, OsmWay> entry : this.osmWayMap.entrySet()) {
    	    if(entry.getValue().isPath()) {
    	    	List<Long> childNodesIds = entry.getValue().getChildOsmNodeList();
    	    	for (Long id : childNodesIds) {
    	    		OsmNode currentNode = this.osmNodeMap.get(id);
    	    		this.accessibleOsmNodes.add(currentNode);
    	    	}
    	    }
    	}
    }
    
	/**
	 * Initialize edges.
	 */
	public void initializeEdges() {
    	for (Entry<Long, OsmWay> entry : this.osmWayMap.entrySet()) {
    	    OsmWay currentWay = entry.getValue();
    	    
    	    if (!currentWay.isPath()) continue;
    	    
    	    boolean isOneWay = currentWay.isOneWay();
    	    boolean isPedestrianOnly = currentWay.isPedestrian();
    	   
    	    
    	    List<Long> listOfNodes = currentWay.getChildOsmNodeList();
    	    int nbNodes = listOfNodes.size();
    	    
    	    OsmNode currentNode = this.osmNodeMap.get(listOfNodes.get(0));
    	    if (!isPedestrianOnly) currentNode.addAdjacentForVehicles(this.osmNodeMap.get(listOfNodes.get(1)));
    	    currentNode.addAdjacentForPedestrians(this.osmNodeMap.get(listOfNodes.get(1)));
    	    
    	    for (int nodeIndex=1 ; nodeIndex<nbNodes-1 ; nodeIndex++) {
    	    	currentNode = this.osmNodeMap.get(listOfNodes.get(nodeIndex));
    	    	if (!isOneWay) {
    	    		if (!isPedestrianOnly) currentNode.addAdjacentForVehicles(this.osmNodeMap.get(listOfNodes.get(nodeIndex-1)));
    	    		currentNode.addAdjacentForPedestrians(this.osmNodeMap.get(listOfNodes.get(nodeIndex-1)));
    	    	}
    	    	if (!isPedestrianOnly)currentNode.addAdjacentForVehicles(this.osmNodeMap.get(listOfNodes.get(nodeIndex+1)));
        	    currentNode.addAdjacentForPedestrians(this.osmNodeMap.get(listOfNodes.get(nodeIndex+1)));
    	    }
    	    
	    	if (!isOneWay) currentNode = this.osmNodeMap.get(listOfNodes.get(nbNodes-1));
	    	if (!isPedestrianOnly) currentNode.addAdjacentForVehicles(this.osmNodeMap.get(listOfNodes.get(nbNodes-2)));
	    	currentNode.addAdjacentForPedestrians(this.osmNodeMap.get(listOfNodes.get(nbNodes-2)));
    	}
	}
    
    /**
     * Initialize osm way map by name.
     */
    public void initializeOsmWayMapByName() {
    	
    	NodeList listOfOsmWays = this.document.getDocumentElement().getElementsByTagName("way");
    	
    	this.osmWayMapByName = new HashMap<String, OsmWay>();
    	this.nbWays = listOfOsmWays.getLength();
    	
    	for (int tempWayIndex = 0; tempWayIndex < this.nbWays; tempWayIndex++) {
    		Node way = listOfOsmWays.item(tempWayIndex);
    		
            if (way.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) way;       
                
                Long id = Long.parseLong(element.getAttribute("id"));
                
                OsmWay osmWay = this.osmWayMap.get(id);
                
                if (!osmWay.isPath()) continue;
                
                this.osmWayMapByName.put(osmWay.getWayName(), osmWay);
            }
    	}
    }
    
    /**
     * Convert latitude to Y.
     *
     * @param lat the lat
     * @return the double
     */
    public double convertLatToY(double lat) {
    	return (lat-this.getMinLat())*this.getPixelPerLat();
    }
    
    public double convertYToLat(double y) {
    	return (y/this.getPixelPerLat())+this.getMinLat();
    }
    
    /**
     * Convert longitude to X.
     *
     * @param lon the lon
     * @return the double
     */
    public double convertLonToX(double lon) {
    	return (lon-this.getMinLon())*this.getPixelPerLon();
    }
    
    public double convertXToLon(double x) {
    	return (x/this.getPixelPerLon())+this.getMinLon();
    }
    
    /**
     * Find a OsmNode by number and name of the steet.
     *
     * @param number the number
     * @param street the street name
     * @return the OsmNode
     */
    public OsmNode findNodeByNumberAndName(String number, String street) {
		for (Entry<Long, OsmWay> entry : this.osmWayMap.entrySet()) {
		    OsmWay way = entry.getValue();
		    boolean rightNumber = false;
		    boolean rightStreet = false;
		    for (OsmTag tag : way.getChildOsmTagList()) {
		    	if (tag.getType().equals("addr:housenumber") && tag.getValue().equals(number)) rightNumber=true;
		    	if (tag.getType().equals("addr:street") && tag.getValue().equals(street)) rightStreet=true;
		    	if (rightNumber && rightStreet) {
		    		OsmNode anyNode = this.osmNodeMap.get(way.getChildOsmNodeList().get(0));
		    		return anyNode;
		    	}
		    }
		}
		return null;
    }
    
	/**
	 * For a given OsmNode, find the closest OsmNode that is accessible (on a path) (Used to join OsmNodes that are not connected to the roads)
	 *
	 * @param desiredStartNode the desired start node
	 * @return the osm node
	 */
	public OsmNode findClosestNodeOnPath(OsmNode desiredStartNode) {
		Double minDistance = Double.POSITIVE_INFINITY;
		OsmNode closestNode = null;
		for (OsmNode accessibleNode : this.accessibleOsmNodes) {
			Double distance = desiredStartNode.getDistanceBetweenNodes(accessibleNode);
			if (distance < minDistance) {
				minDistance = distance;
				closestNode = accessibleNode;
			}
		}
		return closestNode;
	}
    
	public OsmNode findClosestNodeOnPath(double x, double y) {
		OsmNode unexistingNode = new OsmNode(0, "false", convertYToLat(y), convertXToLon(x), null);
		return findClosestNodeOnPath(unexistingNode);
	}
	
	/**
	 * Prints the number of nodes and ways.
	 */
	public void printNbNodesAndWays() {
    	System.out.println("Nombre de nodes : " + getNbNodes());
    	System.out.println("Nombre de ways : " + getNbWays());
    }
    
    /**
     * Prints the ways.
     */
    public void printWays() {
        for (Entry<Long, OsmWay> entry : this.osmWayMap.entrySet()) {
        	System.out.println("------------------");
        	System.out.println("Way id : " + entry.getKey());
            
            OsmWay way = entry.getValue();
            String nom = way.getName();
            
            System.out.println("Nom : " + nom);
       
        }
    }
    
    /**
     * Prints the ways by name.
     */
    public void printWaysByName() {
        for (Entry<String, OsmWay> entry : this.osmWayMapByName.entrySet()) {
        	System.out.println("------------------");
        	System.out.println("Way name : " + entry.getKey());
            
            OsmWay way = entry.getValue();
            Long id = way.getId();
            
            System.out.println("Id : " + id);
       
        }
    }
    
	public DocumentBuilderFactory getFactory() {
		return this.factory;
	}

	public void setFactory(DocumentBuilderFactory factory) {
		this.factory = factory;
	}

	public DocumentBuilder getBuilder() {
		return this.builder;
	}

	public void setBuilder(DocumentBuilder builder) {
		this.builder = builder;
	}

	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Map<Long, OsmNode> getOsmNodeMap() {
		return this.osmNodeMap;
	}

	public void setOsmNodeMap(Map<Long, OsmNode> osmNodeMap) {
		this.osmNodeMap = osmNodeMap;
	}

	public Map<Long, OsmWay> getOsmWayMap() {
		return this.osmWayMap;
	}

	public void setOsmWayMap(Map<Long, OsmWay> osmWayMap) {
		this.osmWayMap = osmWayMap;
	}

	public Map<String, OsmWay> getOsmWayMapByName() {
		return this.osmWayMapByName;
	}

	public void setOsmWayMapByName(Map<String, OsmWay> osmWayMapByName) {
		this.osmWayMapByName = osmWayMapByName;
	}
	
	public List<String> getStreetList() {
		List<String> streetList = new ArrayList();
		
		for (Entry<String, OsmWay> entry : this.osmWayMapByName.entrySet()) {
		    streetList.add(entry.getKey());
		}
		return streetList;
	}
	
	public int getNbNodes() {
		return this.nbNodes;
	}

	public void setNbNodes(int nbNodes) {
		this.nbNodes = nbNodes;
	}

	public int getNbWays() {
		return this.nbWays;
	}

	public void setNbWays(int nbWays) {
		this.nbWays = nbWays;
	}

	public double getPixelPerLat() {
		return pixelPerLat;
	}

	public void setPixelPerLat(double pixelPerLat) {
		this.pixelPerLat = pixelPerLat;
	}

	public double getPixelPerLon() {
		return pixelPerLon;
	}

	public void setPixelPerLon(double pixelPerLon) {
		this.pixelPerLon = pixelPerLon;
	}

	public double getMinLat() {
		return this.minLat;
	}

	public void setMinLat(double minLat) {
		this.minLat = minLat;
	}

	public double getMaxLat() {
		return this.maxLat;
	}

	public void setMaxLat(double maxLat) {
		this.maxLat = maxLat;
	}

	public double getMinLon() {
		return this.minLon;
	}

	public void setMinLon(double minLon) {
		this.minLon = minLon;
	}

	public double getMaxLon() {
		return this.maxLon;
	}

	public void setMaxLon(double maxLon) {
		this.maxLon = maxLon;
	}
}
