package classes;

import java.util.ArrayList;
import java.util.List;

// @Author Dorian Vabre

/**
 * An object representing a way on the map
 */
public class OsmWay {

	/** The id of the way. */
	private long id;
	
	/** The visibility of the way. */
	private String visibility;
	
	/** The list of the children OsmNode of the way. */
	private List<Long> childOsmNodeList = new ArrayList<Long>();
	
	/** The list of the children OsmTag of the way. */
	private List<OsmTag> childOsmTagList = new ArrayList<OsmTag>();
	
	/** The name of the way. */
	private String name;
	
	public OsmWay(long theId, String theVisibility, List<Long> childOsmNodeList, List<OsmTag> theChildOsmTagList, String theName) {
		this.id = theId;
		this.visibility = theVisibility;
		this.childOsmNodeList = childOsmNodeList;
		this.childOsmTagList = theChildOsmTagList;
		this.name = theName;
	}
	
	/**
	 * Returns true if the way is a oneway
	 * @return
	 */
	public boolean isOneWay() {
		for (OsmTag tag : this.childOsmTagList) {
			if (tag == null) continue;
			if (tag.getType().equals("oneway") && tag.getValue().equals("yes")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the way is pedestrian-only
	 * @return
	 */
	public boolean isPedestrian() {
		for (OsmTag tag : this.childOsmTagList) {
			if (tag == null) continue;
			if (tag.getType().equals("highway") && tag.getValue().equals("path")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the way is usable to reach the road network
	 * @return
	 */
	public boolean isPath() {
		for (OsmTag tag : this.childOsmTagList) {
			if (tag == null) continue;
			if (tag.getType().equals("highway")) {
				return true;
			}
		}
		return false;
	}
	
	public String getWayName( ) {
		for (OsmTag tag : this.childOsmTagList) {
			if (tag == null) continue;
			
			if (tag.getType().equals("name")) {
				return tag.getValue();
			}
		}
		return "";
	}
	
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getVisibility() {
		return this.visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public List<Long> getChildOsmNodeList() {
		return this.childOsmNodeList;
	}

	public void setChildOsmNodeList(List<Long> childOsmNodeList) {
		this.childOsmNodeList = childOsmNodeList;
	}

	public List<OsmTag> getChildOsmTagList() {
		return this.childOsmTagList;
	}

	public void setChildOsmTagList(List<OsmTag> childOsmTagList) {
		this.childOsmTagList = childOsmTagList;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
