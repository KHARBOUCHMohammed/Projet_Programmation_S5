package classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.Normalizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// @Author Jeremie Bouilhol

/**
 * A downloader for the Osm XML file
 */
public class OsmDownloader {
	
	/** The size of the box is set by a shift of the actual coordinates**/
	private static final double BOX_SIZE_LONGITUDE = 0.0118;

	/** The size of the box is set by a shift of the actual coordinates**/
	private static final double BOX_SIZE_LATITUDE = 0.0067;

	/** The XML file to store data collected from the API. */
	static File XMLFile = new File("src/application/XMLFile");
	
	/** The path of the file to store data collected from the API. */
	static String pathToFile = "src/application/XMLFile";
	
	/**
	 * Make a request for the Overpass API
	 *
	 * @param info the input from the user to search a city (name or coordinates)
	 */
	public void makeRequest(String info) {
			double[] bbox = new double[4];
			try {
				emptyFile(XMLFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (is_alpha(info)) {
				info = info.replaceAll("\\s+", "+").toLowerCase();
				info = Normalizer.normalize(info, Normalizer.Form.NFD);
			    info = info.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
				sendRequest("https://nominatim.openstreetmap.org/search?q=%22"+info+"%22&format=xml&polygon=1&addressdetails=1");
				Document documentFile = getDocumentFromPath(pathToFile);
				String[] longLatTab = new String[2];
				longLatTab = getLatLongFromCityName(documentFile);
				bbox = createBbox(longLatTab);
				
			}
			else {
				String delims = "[,]";
				String[] tabCoordinates = info.split(delims);
				bbox = createBbox(tabCoordinates);
			}
			String south = String.valueOf(bbox[0]);
			String west = String.valueOf(bbox[1]);
			String north = String.valueOf(bbox[2]);
			String est = String.valueOf(bbox[3]);
			String box = south + "," + west + "," + north + "," + est;
			String request = "https://overpass-api.de/api/interpreter?data="
					+ "[bbox:"+box+"];" 
					+ "node("+box+")[~\".\"~\".\"];"
					+ "way("+box+")[~\".\"~\".\"];"
					+ "(._;>;);out%20meta;";
			sendRequest(request);
		}

	/**
	 * Initialize document builder.
	 *
	 * @param pathToFile the path to file
	 */
	public Document getDocumentFromPath(String pathToFile) {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(pathToFile));
			return document;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
    }
	
	/**
	 * Returns the longitude and latitude of a city from a xml file
	 *
	 * @param xmlfile the file containing the information of a city (to get coordinates)
	 * @return the string[] the array that with the latitude and longitude of a city
	 */
	public String[] getLatLongFromCityName(Document xmlfile) {
		NodeList listOfOsmNodes = xmlfile.getDocumentElement().getElementsByTagName("place");
		String[] tabRes = new String[2];
		
        Node node = listOfOsmNodes.item(0);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            tabRes[0] = element.getAttribute("lat");
            tabRes[1] = element.getAttribute("lon");
        }
        return tabRes;
	}
	
	/**
	 * Creates the bounding box (bbox) used in the request (the square of data to request)
	 *
	 * @param tabCoordinates the coordinates
	 * @return the float[] the array that defines the coordinates of the 4 corners of the square to request
	 */
	public static double[] createBbox(String[] tabCoordinates) {
		double[] tabResult;
		tabResult = new double[4];
		float latitude = Float.parseFloat(tabCoordinates[0]);
		float longitude = Float.parseFloat(tabCoordinates[1]);
		tabResult[0] = (double) (latitude - BOX_SIZE_LATITUDE);	
		tabResult[1] = (double) (longitude - BOX_SIZE_LONGITUDE);
		tabResult[2] = (double) (latitude + BOX_SIZE_LATITUDE);
		tabResult[3] = (double) (longitude + BOX_SIZE_LONGITUDE);
		return tabResult;
	}

	/**
	 * Checks if a string is alphabetic
	 *
	 * @param str the string to check
	 * @return true, if it is alphabetic
	 */
	public static boolean is_alpha(final String str) {
		  if (!Character.isAlphabetic(str.charAt(0))) {
		   return false;
		  }
		 return true;
	}

	/**
	 * Sends the request to the Overpass api
	 *
	 * @param request the request
	 */
	public static void sendRequest(String request) {
		URL url;

		try {
			//System.out.println("Try to send request"); //Keep to debug
			url = new URL(request);
	        ReadableByteChannel inputChannel = Channels.newChannel(url.openStream());
	        FileOutputStream fileOutputStream = new FileOutputStream(XMLFile);
	        FileChannel outputChannel = fileOutputStream.getChannel();
		    outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
		    //System.out.println("Request finished"); //Keep to debug

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Empties the file.
	 *
	 * @param file the file
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static public void emptyFile(
	     File file) throws FileNotFoundException, IOException {
	    if (file == null) {
	      throw new IllegalArgumentException("File should not be null.");
	    }
	    if (!file.exists()) {
	      throw new FileNotFoundException ("File does not exist: " + file);
	    }
	    if (!file.isFile()) {
	      throw new IllegalArgumentException("Should not be a directory: " + file);
	    }
	    if (!file.canWrite()) {
	      throw new IllegalArgumentException("File cannot be written: " + file);
	    }
	    
	    Writer output = new BufferedWriter(new FileWriter(file));
	    try {
	      output.write( "" );
	    }
	    finally {
	      output.close();
	    }
	  }
}