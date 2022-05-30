package application;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import classes.Dijkstra;
import classes.OsmDownloader;
import classes.OsmGraph;
import classes.OsmNode;
import classes.OsmTag;
import classes.OsmWay;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// @Authors Lou Favre, Dorian Vabre

public class MainController{
	
	/** The city name or coordinates input */
	@FXML 
	private TextField city;
	
	/** The searchCity button. */
	@FXML 
	private Button searchCity;
	
	/** The cancel button. */
	@FXML 
	private Button cancel;
	
	/** The starting number input. */
	@FXML
	private TextField startingNumber;
	
	/** The starting street input. */
	@FXML 
	private TextField startingStreet;

	/** The autocompletion bind for the start street. */
	AutoCompletionBinding<String> startBind;
	
	/** The destination number input. */
	@FXML
	private TextField destinationNumber;
	
	/** The destination street input. */
	@FXML 
	private TextField destinationStreet;
	
	/** The autocompletion bind for the destination street. */
	AutoCompletionBinding<String> destBind;
	
	/** The searchRoute button. */
	@FXML 
	private Button searchRoute;

	/**
	 * The node to start the route
	 */
	private OsmNode startNode;
	
	/**
	 * The destination node for the route
	 */
	private OsmNode destinationNode;
	
	/** The canvas of the application to draw on. */
	@FXML 
	private static Canvas canvas;

	/** The graph of the current city. */
	private static OsmGraph graph;
	
	/** The scene of the application. */
	private Scene scene;

	/**
	 * The stage of the application
	 */
	private Stage stage;
	
	/** The graphics context of the canvas to draw on. */
	private static GraphicsContext gc;
	
	/**
	 * Button to zoom
	 */
	@FXML
	private Button zoombtn;
	
	/** Button to unzoom */
	@FXML
	private Button unzoombtn;
	
	/**
	 * x coordonate of the mouse at the start of the dragging action
	 */
	private static double xBeforeDrag;
	
	/**
	 * y coordinate of the mouse at the start of the dragging action
	 */
	private static double yBeforeDrag;
	
	/**
	 * x coordinate of the mouse at the end of the dragging action
	 */
	private static double xAfterDrag;
	
	/**
	 * y coordinate of the mouse at the end of the dragging action
	 */
	private static double yAfterDrag;
	 
	
	public MainController() {
	}

	/**
	 * Adds a scrolling handler when the scroll is performed with the mouse
	 * @param node
	 */
	 @SuppressWarnings("unchecked")
	public static void addMouseScrolling(Node node) {
		 node.setOnScroll(new EventHandler() {
				@Override
				public void handle(Event event) {
					
					ScrollEvent se = (ScrollEvent) event;
					
					double zoomFactor = 1.05;
	                double deltaY = se.getDeltaY();

	                if (deltaY < 0){
	                    zoomFactor = 0.95;
	                }
	                setZoom(zoomFactor);
				}
		    });
	 }
	 
	 /**
	  * Defines the zoom of the canvas
	  * @param zoomFactor
	  */
	public static void setZoom(double zoomFactor) {
		 canvas.setScaleX(canvas.getScaleX() * zoomFactor);
		 canvas.setScaleY(canvas.getScaleY() * zoomFactor);
	 }
	
	/**
	 * Defines the action of dragging the mouse
	 * @param node
	 */
	public static void addMouseDragging(Node node) {
		 node.setOnMousePressed(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {
				xBeforeDrag = event.getX();
				yBeforeDrag = event.getY();

			}
			 
		 });
		 node.setOnMouseReleased(new EventHandler<MouseEvent>() {
			 	@Override
				public void handle(MouseEvent event) {
			 		xAfterDrag = event.getX();
			 		yAfterDrag = event.getY();
			 		
					node.setTranslateX(node.getTranslateX() + (xAfterDrag - xBeforeDrag));
					node.setTranslateY(node.getTranslateY() + (yAfterDrag - yBeforeDrag));
			 }
		 });
	 }
	 
	/**
	 * Zooms with the button
	 */
	public void zoomOnClick() {
		 double zoomFactor = 1.05;
		 setZoom(zoomFactor);
	 }
	 
	/**
	 * Unzooms with the button
	 */
	public void unzoomOnClick() {
		 double zoomFactor = 0.95;
		 setZoom(zoomFactor);
	 }
	 
	 
	/**
	 * Method called on searchRoute button press, drawing the desired route on the map
	 *
	 * @param event the event
	 */
	public void searchRoute(ActionEvent event) {

		if (graph == null) return;
		
		String startingNumber = this.getStartingNumber();
		String src = this.getStartingStreet();
		
		// Getting the start node of the route
		if (!this.startingStreet.getText().equals("Custom start")) {
			if (!startingNumber.equals("") && !src.equals("")) {
				// Search with number and street
				// Finding the node using the number and the street name
				OsmNode desiredStartNode = graph.findNodeByNumberAndName(startingNumber, src);
				if (desiredStartNode == null) {
					alert("Starting point not found (try not using the number, it may not be existing in the database)");
					return;
				}
				this.startNode = graph.findClosestNodeOnPath(desiredStartNode);
			} else if (!src.equals("")) {
				// Search only with street
				OsmWay startWay = graph.getOsmWayMapByName().get(src);
				if (startWay == null) {
					alert("Starting street not found, please use the autocomplete");
					return;
				}
				Long startNodeId = startWay.getChildOsmNodeList().get(0);
				this.startNode = graph.getOsmNodeMap().get(startNodeId);
			} else {
				alert("Please enter a starting street name");
				return;
			}
		}
		
		// Getting the destination node of the route
		if (!this.destinationStreet.getText().equals("Custom destination")) {
			String destinationNumber = this.getDestinationNumber();
			String dest = this.getDestinationStreet();
			if (!destinationNumber.equals("") && !dest.equals("")) {
				// Search with number and street
				// Finding the node using the number and the street name
				OsmNode desiredDestinationNode = graph.findNodeByNumberAndName(destinationNumber, dest);
				if (desiredDestinationNode == null) {
					alert("Destination point not found (try not using the number, it may not be existing in the database)");
					return;
				}
				this.destinationNode = graph.findClosestNodeOnPath(desiredDestinationNode);
			} else if (!dest.equals("")) {
				// Search only with street
				OsmWay endWay = graph.getOsmWayMapByName().get(dest);
				if (endWay == null) {
					alert("Destination street not found, please use the autocomplete");
					return;
				}
				Long endNodeId = endWay.getChildOsmNodeList().get(0);
				this.destinationNode = graph.getOsmNodeMap().get(endNodeId);
			} else {
				alert("Please enter a destination street name");
				return;
			}
		}

		// Search the route
		searchRouteBetweenNodes(this.startNode, this.destinationNode);
	}

	/**
	 * Alerts the user with a specific message.
	 *
	 * @param string the string
	 */
	private void alert(String string) {
		Alert alert = new Alert(AlertType.ERROR, string, ButtonType.OK);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.show();
	}

	/**
	 * Defines the popup action after a right click
	 * @param event
	 */
	public void canvasClickPopUp(MouseEvent event) {
		MouseButton button = event.getButton();
		if (button==MouseButton.SECONDARY) {
		double x = event.getX(), y = event.getY();
		y = canvas.getHeight()-y;

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setHeaderText("Modification de l'itinéraire");
		alert.setContentText("Voulez-vous définir ce point comme départ ou arrivée ?");

		ButtonType buttonStart = new ButtonType("Départ");
		ButtonType buttonDestination = new ButtonType("Arrivée");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonStart, buttonDestination, buttonTypeCancel);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonStart){
		    this.setAsStart(graph.findClosestNodeOnPath(x, y));
		} else if (result.get() == buttonDestination) {
			this.setAsDestination(graph.findClosestNodeOnPath(x, y));
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
	}
	}
	
	/**
	 * Sets a custom node as start in the text field
	 * @param node
	 */
	public void setAsStart(OsmNode node) {
		this.startingStreet.setText("Custom start");
		this.startNode = graph.findClosestNodeOnPath(node);
	}
	
	/**
	 * Sets a custom node as destination in the text field
	 * @param node
	 */
	public void setAsDestination(OsmNode node) {
		this.destinationStreet.setText("Custom destination");
		this.destinationNode = graph.findClosestNodeOnPath(node);
	}
	
	/**
	 * Search and draws the route between nodes using Dijkstra algorithm.
	 *
	 * @param startNode the start node
	 * @param endNode the end node
	 */
	private void searchRouteBetweenNodes(OsmNode startNode, OsmNode endNode) {
		Dijkstra dj = new Dijkstra();
		// @TODO change brut mode of transport to the value of the button
		dj.UpdatePaths(startNode, "vehicle");
		List<OsmNode> path = dj.getShortestPathTo(endNode);	
		// Resets the map
		resetMap();
		// Draws the map
		drawPath(path);
		displayStartingAndDestinationPoint(startNode,endNode);
		// Flush all node distances to use Dijkstra again later
		flushDistancesOfEveryNode();
	}
	
	/**
	 * Cancel the current route by redrawing the map
	 */
	public void cancelRoute() {
		resetMap();
		this.startingStreet.setText("");
		this.destinationStreet.setText("");
		this.startingNumber.setText("");
		this.destinationNumber.setText("");
	}
	
	/**
	 * Resets the map by redrawing it (used to remove any drawing applied after, such as routes)
	 */
	public void resetMap() {
		gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
		drawWays();
		
		flushDistancesOfEveryNode();
	}
	
	/**
	 * Resets the distances of every node (used before looking for a new route)
	 */
	public void flushDistancesOfEveryNode() {
		// Resets back the info about the distances between the nodes for the shortest path
		for (Entry<Long, OsmNode> entry : graph.getOsmNodeMap().entrySet()) {
			entry.getValue().flushDistances();
		}
	}
	
    /**
     * Draw points (deprecated)
     *
     * @param canvas the canvas
     * @param gc the GraphicalContext
     */
	public void drawPoints() {
    	double height = canvas.getHeight();
    	double width = canvas.getWidth();
    	graph.calculateRatioCoordToPixels(height, width);
    	for (Entry<Long, OsmNode> entry : graph.getOsmNodeMap().entrySet()) {
    		
    		double x = graph.convertLonToX(entry.getValue().getLon());
    		double y = graph.convertLatToY(entry.getValue().getLat());
    	
            gc.moveTo(x, height-y);
            gc.lineTo(x+0.1, height-y+0.1);
            gc.stroke();
    	}
    }

	/**
	 * Sets the tooltip (popup) when hovering start/end of the route
	 * @param startNode
	 * @param endNode
	 */
	public void setTooltips(OsmNode startNode, OsmNode endNode){
		
		Double startNodeLat = startNode.getLat();
		Double startNodeLon = startNode.getLon();
		Double startNodeY = canvas.getHeight()-graph.convertLatToY(startNodeLat);
		Double startNodeX = graph.convertLonToX(startNodeLon);
		
		Double endNodeLat = endNode.getLat();
		Double endNodeLon = endNode.getLon();
		Double endNodeY = canvas.getHeight()-graph.convertLatToY(endNodeLat);
		Double endNodeX = graph.convertLonToX(endNodeLon);
		
		Circle startCircle = new Circle(startNodeX, startNodeY, 10.0f);
		Circle endCircle = new Circle(endNodeX, endNodeY, 10.0f);
	
		Tooltip tooltip = new Tooltip();
       
		canvas.setOnMouseMoved(e -> {
	        if (startCircle.contains(e.getX(), e.getY())) {
	    		Tooltip.install(canvas, tooltip);
	    		tooltip.setText("Start\nLatitude : " + startNodeLat + "\nLongitude : " + startNodeLon);
	        } else if (endCircle.contains(e.getX(), e.getY())) {
	    		Tooltip.install(canvas, tooltip);
	    		tooltip.setText("Destination\nLatitude : " + endNodeLat + "\nLongitude : " + endNodeLon);
	        }
	        else {
	    		Tooltip.uninstall(canvas, tooltip);
	        }
	       
        });
	}
	
    // Draws the map and color the components
	public static void drawWays() {
    	double height = canvas.getHeight();
    	double width = canvas.getWidth();
		addMouseScrolling(canvas);
		addMouseDragging(canvas);
    	graph.calculateRatioCoordToPixels(height, width);
    	
    	// For each OsmWay
    	for (Entry<Long, OsmWay> entry : graph.getOsmWayMap().entrySet()) {
    	    OsmWay currentWay = entry.getValue();
    	    
    	    if (currentWay.getVisibility().equals("false")) continue;
    	    
    	    // Getting each node in the way
    	    List<Long> listOfNodes = currentWay.getChildOsmNodeList();
    	    int nbNodes = listOfNodes.size();
    	    int nodesDone = 0;
    	    double[] listOfX = new double[nbNodes];
    	    double[] listOfY = new double[nbNodes];
    	    
    	    // Getting the list of all coordinates
    	    for (Long id : listOfNodes) {
    	    	OsmNode currentNode = graph.getOsmNodeMap().get(id);
    	    	listOfX[nodesDone] = graph.convertLonToX(currentNode.getLon());
    	    	listOfY[nodesDone] = height-graph.convertLatToY(currentNode.getLat());
    	    	nodesDone++;
    	    }
    	    
    	    // Defining the color of the type of object that we are drawing
    	    boolean canFill = defineStrokeColor(currentWay.getChildOsmTagList(), gc);
    	    
    	    // Drawing a polygon or a polyline, depending if the shape is a loop
    	    if (listOfNodes.get(0).equals(listOfNodes.get(nbNodes-1))) {
    	    	gc.strokePolygon(listOfX, listOfY, nbNodes);
    	    	if (canFill) gc.fillPolygon(listOfX, listOfY, nbNodes);
    	    } else {
    	    	gc.strokePolyline(listOfX, listOfY, nbNodes);
    	    }
    		
    		gc.setStroke(Color.BLACK);
    		gc.setFill(Color.BLACK);
    	}
	}
    
	/**
	 * Draw the path
	 *
	 * @param path the list of nodes in the correct order
	 */
	public void drawPath(List<OsmNode> path) {
    	double height = canvas.getHeight();
    	double width = canvas.getWidth();
    	graph.calculateRatioCoordToPixels(height, width);
		
	    int nodesDone = 0;
	    int nbNodes = path.size();
	    double[] listOfX = new double[nbNodes];
	    double[] listOfY = new double[nbNodes];
	    
		for (OsmNode currentNode : path) {
	    	listOfX[nodesDone] = graph.convertLonToX(currentNode.getLon());
	    	listOfY[nodesDone] = height-graph.convertLatToY(currentNode.getLat());
	    	nodesDone++;
		}
		gc.setLineWidth(4);
		gc.setStroke(Color.RED);
		gc.strokePolyline(listOfX, listOfY, nbNodes);
	}
	
    /**
	 * Defines the color to use to draw and fill shapes.
	 *
	 * @param list the tag list
	 * @param gc the GraphicalContext
	 * @return true, if you can fill it, false otherwise
	 */
	private static boolean defineStrokeColor(List<OsmTag> list, GraphicsContext gc) {
    	
    	gc.setLineWidth(1.0);
    	
		for (OsmTag tag : list) {
	
			if (gc.getStroke() != Color.BLACK || gc.getFill() != Color.BLACK) return true;

			switch (tag.getType()) {
				case "building":
					gc.setStroke(Color.GREY);
					gc.setFill(Color.DARKGREY);
					return true;
				case "way":
					gc.setStroke(Color.BROWN);
					break;
				case "natural":
					switch (tag.getValue()) {
						case "water":
							gc.setStroke(Color.CADETBLUE);
							break;
						case "tree":
							gc.setStroke(Color.DARKOLIVEGREEN);
							break;
						case "treerow":
							gc.setStroke(Color.DARKOLIVEGREEN);
							break;
						case "wetland":
							gc.setStroke(Color.DODGERBLUE);
							break;
						case "beach":
							gc.setStroke(Color.SEASHELL);
							break;
						case "sand":
							gc.setStroke(Color.SEASHELL);
							break;
						case "coastline":
							gc.setStroke(Color.SEASHELL);
							break;
						default: 
							gc.setStroke(Color.DARKGREEN);
							break;
					}
					break;
				case "amenity":
					switch (tag.getValue()) {
						case "parking":
							gc.setStroke(Color.DARKBLUE);
							break;
						default: 
							gc.setStroke(Color.GREY);
							gc.setFill(Color.DARKGREY);
							return true;
					}
					break;
				case "leisure":
					switch (tag.getValue()) {
						case "park":
							gc.setStroke(Color.DARKSEAGREEN);
							break;
						default: 
							break;
					}
					break;
				case "highway":
					gc.setLineWidth(2.0);
					gc.setStroke(Color.rgb(30,30,30));
					return false;
				default:
					break;
			}
			gc.setFill(gc.getStroke());
		}
		return false;
	}
	
	/**
	 * Displays circles on the map to show where are the start and destination points
	 * @param startNode the start OsmNode
	 * @param endNode the destination OsmNode
	 */
	private void displayStartingAndDestinationPoint(OsmNode startNode, OsmNode endNode) {
		
		Double startNodeLat = startNode.getLat();
		Double startNodeLon = startNode.getLon();
		
		Double endNodeLat = endNode.getLat();
		Double endNodeLon = endNode.getLon();
		
		Double startNodeY = canvas.getHeight()-graph.convertLatToY(startNodeLat);
		Double startNodeX = graph.convertLonToX(startNodeLon);
		
		Double endNodeY = canvas.getHeight()-graph.convertLatToY(endNodeLat);
		Double endNodeX = graph.convertLonToX(endNodeLon);
		
		//Marker for starting point
		gc.setLineWidth(2);
		gc.setFill(Color.BLUE);
		gc.fillOval(startNodeX-10, startNodeY-10, 20, 20);
		
		//Marker for destination point
		gc.setFill(Color.GREEN);
		gc.fillOval(endNodeX-10, endNodeY-10, 20, 20);
		
		
		gc.setFont(Font.font("Tahoma", 18));
		gc.setFill(Color.WHITE);
		gc.fillText("D", startNodeX-5, startNodeY+6);
		gc.fillText("A", endNodeX-5, endNodeY+6);
		
		setTooltips(startNode, endNode);
		
	}

	public String getCity() {
		return this.city.getText();
	}
	
	public void searchCity(ActionEvent event) {
		Thread t = new Thread() {
			public void run() {
				OsmDownloader downloader = new OsmDownloader();
				downloader.makeRequest(getCity());
		    	graph = new OsmGraph("src/application/XMLFile"); 
		    	if (MainController.this.startBind != null ) MainController.this.startBind.dispose();
		    	MainController.this.startBind = TextFields.bindAutoCompletion(MainController.this.startingStreet, graph.getStreetList());
		    	if (MainController.this.destBind != null) MainController.this.destBind.dispose();
		    	MainController.this.destBind = TextFields.bindAutoCompletion(MainController.this.destinationStreet, graph.getStreetList());
		    	resetMap();
		    	drawWays();
		    }
		};
		t.start();
	}
	
	public String getStartingStreet() {
		return this.startingStreet.getText();
	}
	
	public String getStartingNumber() {
		return this.startingNumber.getText();
	}
	
	public String getDestinationStreet() {
		return this.destinationStreet.getText();
	}
	
	public String getDestinationNumber() {
		return this.destinationNumber.getText();
	}
	
	public void setCanvas(Canvas theCanvas) {
		canvas = theCanvas;
		canvas.setOnMouseClicked(this::canvasClickPopUp);
	}
	
	public void setGraphicsContext(GraphicsContext theGc) {
		gc = theGc;
	}

	public void setStage(Stage theStage) {
		this.stage = theStage;
	}

}
