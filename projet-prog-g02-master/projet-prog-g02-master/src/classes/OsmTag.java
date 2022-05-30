package classes;

// @Author Dorian Vabre

/**
 * A tag attached to either a node or a way in the XML file, and to an OsmNode/OsmWay in the graph
 */
public class OsmTag {
	
	/** The type of the data given by the tag. */
	private String type;
	
	/** The value of the data given by the tag. */
	private String value;
	
	public OsmTag(String theType, String theValue) {
		this.type = theType;
		this.value = theValue;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
