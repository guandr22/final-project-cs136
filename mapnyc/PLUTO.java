package mapnyc;

import java.util.*; 
import java.io.*; 
//graphics packages (from LAB 2)
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class PLUTO extends JFrame{
	public int MODE=-1;
	public final int WITHIN_DISTANCE_MODE =1;
	public final int NEAREST_MODE =2;
	public final int MAP_MODE = 3;

	public int MAP_TYPE = 5;
	public final int MONEY =5;
	public final int AGE =6;
	public final int FLOORS =7;

	public final static int NUM_TAXPLOTS=1000000;

	public double WITHIN_DISTANCE_RADIUS;

	public ArrayList<TaxPlot> taxplots; //used to iterate through once, find the max and min xs and ys, in order to set up quadtree
	public Hashtable<String, TaxPlot> symbolTable; // address ---> taxplot
	public PointRegionQuadtree<TaxPlot> quadtree;
	public BoundingBox region;
	public final int WINDOW_WIDTH, WINDOW_HEIGHT;
	public BufferedImage bf;
	public BufferedImage nycJPG;
	public int mouseX, mouseY;

	//  Instance variables for mapping through all Taxplots of a given land use category or owned by a given person/corporation.
	public ArrayList<TaxPlot> landuseTaxplots;
	public Hashtable<String, TaxPlot> landuseSymbolTable;
	public PointRegionQuadtree<TaxPlot> landuseQuadTree;

	public ArrayList<TaxPlot> ownerTaxplots;
	public Hashtable<String, TaxPlot> ownerSymbolTable;
	public PointRegionQuadtree<TaxPlot> ownerQuadTree;

	//Wyatt used Liberty Island and U Thant Island as set points to calculate these constants.
	/** 
	 * Wyatt used screenshots to figure out their pixel position, and their x and y coordinates (based on 
	 * the New York-Long Island State Plane coordinate system) are included in pluto_24v1_1.csv in columns BX and BY.
	 * 
	 * To figure out a rough ratio between coordinates and miles, Andrew plugged WGS84 latitude and longitude values,
	 * found in columns CL and CM of pluto_24v1_1.csv, into https://www.omnicalculator.com/other/latitude-longitude-distance 
	 * to calculate the rough distance in statute miles between Liberty and U Thant Islands (these are rows 131751 and 
	 * 838380, respectively, in the csv).
	 * Then, he used the Pythagorean Theorem (this is sketchy, given the curvature of the Earth, but the distances
	 * involved should be small enough to avoid too much distortion) to find the difference in NY-LI SP coordinates between
	 * Liberty and U Thant islands in order to calculate a coordinate-to-mile ratio.
	 * The taxplots for Liberty and U Thant islands are 5.751 statute miles or 30398.80861 NY-LI SP coordinate units apart,
	 * giving the map a coordinate-to-mile ratio of 5285.83004869, making one coordinate unit (at least close to Liberty and
	 * U Thant islands) a little smaller than a foot.
	 * 
	 * All that said, please keep in mind that these are rough approximations (you probably wouldn't want to use these for a cross-country 
	 * trip) but that these should be close enough to be practically useful within the bounds of New York City, though they will become
	 * a bit more distorted the farther you go from the center of the city.
	*/
	public final double PIXELS_TO_COORDS_RATIO = 1/270.3317839; 
	public final double COORDS_TO_MILES_RATIO = 5285.83004869;

	public final double MILES_TO_COORDS_RATIO = 1/COORDS_TO_MILES_RATIO;
	public final double COORDS_TO_PIXELS_RATIO = 1/PIXELS_TO_COORDS_RATIO;

	//the BOTTOMLEFT_XCOORD and BOTTOMLEFT_YCOORD are the x and y cordinates cooresponding to the bottom left of the window.
	public final double BOTTOMLEFT_XCOORD = 895306.8989;
	public final double BOTTOMLEFT_YCOORD = 113704.226;

	public PLUTO(String filename, String jpgfilename){
		mouseX = -1;
		mouseY = -1;
        //set up jpg
        File jpgFile = new File(jpgfilename);
        try{
			nycJPG = ImageIO.read(jpgFile);
		} catch (Exception e){
			System.out.println("File not found: " + e.getMessage());
		}

        WINDOW_WIDTH = nycJPG.getWidth();
        WINDOW_HEIGHT = nycJPG.getHeight();

		setRegion();
		//initialize data structures
		taxplots = new ArrayList<TaxPlot>();
		symbolTable = new Hashtable<>();
		quadtree = new PointRegionQuadtree<TaxPlot>(region);

		//scan data into the data structures
		File file = new File(filename); 
		try{
            Scanner scanner = new Scanner(file);
            scanner.nextLine(); //skip the first line of data labels
            int numTaxPlots = NUM_TAXPLOTS;
            while (scanner.hasNextLine() && numTaxPlots>=0) {
        		String line = scanner.nextLine();
        		TaxPlot nextTaxplot = new TaxPlot(line);
        		if (!nextTaxplot.corruptedData){//} && region.inBox(nextTaxplot.xcoord,nextTaxplot.ycoord)){
        			taxplots.add(nextTaxplot);
        			quadtree.insert(nextTaxplot,nextTaxplot.xcoord,nextTaxplot.ycoord);
        			symbolTable.put(nextTaxplot.address, nextTaxplot);
        		}
        		numTaxPlots --;
        	}
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        // Open window!
        bf = new BufferedImage(WINDOW_WIDTH,WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		this.setVisible(true);

		// Mouse listener (from LAB 2)
		MapMouseListener listener = new MapMouseListener();
		addMouseListener(listener);
		addMouseMotionListener(listener);


	}
	public void setRegion(){
		double minX = BOTTOMLEFT_XCOORD;
		double maxX = BOTTOMLEFT_XCOORD + (WINDOW_WIDTH/PIXELS_TO_COORDS_RATIO);
		double minY = BOTTOMLEFT_YCOORD;
		double maxY = BOTTOMLEFT_YCOORD + (WINDOW_HEIGHT/PIXELS_TO_COORDS_RATIO);
		region = new BoundingBox(minX,maxX,minY,maxY);
	}

	//getXPixel and getYPixel given a taxplot's coordinates. Don't forget that (0,0) is the top left!

	public int getXPixel(TaxPlot taxplot){
		return (int)((taxplot.xcoord - BOTTOMLEFT_XCOORD) * PIXELS_TO_COORDS_RATIO);
	}
	public int getYPixel(TaxPlot taxplot){
		return WINDOW_HEIGHT - (int)((taxplot.ycoord - BOTTOMLEFT_YCOORD) * PIXELS_TO_COORDS_RATIO);
	}

	//get coordinates given an X and Y pixel
	 public double getXCoordFromPixel(int xPixel){
		return (xPixel / PIXELS_TO_COORDS_RATIO) + BOTTOMLEFT_XCOORD;
	}
	public double getYCoordFromPixel(int yPixel){
		return ((WINDOW_HEIGHT - yPixel)/PIXELS_TO_COORDS_RATIO) + BOTTOMLEFT_YCOORD;
	}

	public void paint(Graphics g){
		if (MODE == -1){
			return;
		}
		Graphics2D g2 = (Graphics2D) bf.getGraphics();
		//Set the jpg of NYC as the background
		try{
			g2.drawImage(nycJPG,0,0,null);
		}
		catch (Exception e){}


		if (MODE == WITHIN_DISTANCE_MODE){
			ArrayList<TaxPlot> taxplotsWithinDistance = quadtree.withinDistance(getXCoordFromPixel(mouseX),
																				getYCoordFromPixel(mouseY),
																				WITHIN_DISTANCE_RADIUS);
			// //Put a pixel of red for each taxplot
			for (TaxPlot taxplot: taxplots){
				if (taxplotsWithinDistance.contains(taxplot)){
					g2.setPaint(Color.RED);
				}
				else{
					g2.setPaint(Color.BLUE);
				}
				g2.fillRect(getXPixel(taxplot), getYPixel(taxplot), 1,1);
			}
			//draw a green oval for the YOU ARE HERE
			g2.setPaint(Color.GREEN);
			g2.fillOval(mouseX,mouseY,10,10);
		}
		else if (MODE == NEAREST_MODE){

			// PointRegionQuadtree<TaxPlot> tempQuadtree = new PointRegionQuadtree<TaxPlot>(region);
			// for (TaxPlot taxplot: taxplots){
			// 	tempQuadtree.insert(taxplot,taxplot.xcoord,taxplot.ycoord);
			// }

			// TaxPlot curTaxplot = null;
			// while (true){
			// 	curTaxplot = tempQuadtree.closestObject(getXCoordFromPixel(mouseX),getYCoordFromPixel(mouseY),2);
			// 	if (curTaxplot.landuse == TaxPlot.VACANT_LOT){
			// 		break;
			// 	}
			// 	else{
			// 		tempQuadtree.remove(curTaxplot);
			// 	}
			// }
			//draw a green oval for the YOU ARE HERE
			g2.setPaint(Color.GREEN);
			g2.fillOval(mouseX,mouseY,10,10);
			TaxPlot nearestTaxplot = quadtree.closestObject(getXCoordFromPixel(mouseX),getYCoordFromPixel(mouseY),2);
			g2.setPaint(Color.RED);
			g2.fillOval(getXPixel(nearestTaxplot),getYPixel(nearestTaxplot),10,10);
		}

		else if (MODE == MAP_MODE){
			Color mapColor = new Color(0,0,0);
			for (TaxPlot taxplot: taxplots){
				if (MAP_TYPE == MONEY){
					double price = taxplot.assessedTotalValue;
					double capPrice = 2000000;
					if (price >capPrice){
						price = capPrice;
					}
					int red = (int)((price/capPrice) * 254);
					mapColor = new Color(red,0, 0);
				}
				else if (MAP_TYPE == AGE){
					if (taxplot.yearBuilt == -1){
						continue;
					}
					double age = 2024 - taxplot.yearBuilt;
					double capAge = 300;
					if (age >capAge){
						age = capAge;
					}
					int green = (int)((age/capAge) * 254);
					mapColor = new Color(0,green, 0);
				}
				else if (MAP_TYPE == FLOORS){
					if (taxplot.numFloors == -1){
						continue;
					}
					double floors = taxplot.numFloors;
					double capFloors = 10;
					if (floors >capFloors){
						floors = capFloors;
					}
					int blue = (int)((floors/capFloors) * 254);
					mapColor = new Color(0,0, blue);
				}
				g2.setPaint(mapColor);
				g2.fillRect(getXPixel(taxplot), getYPixel(taxplot), 1,1);
			}
		}

		g.drawImage(bf,0,0,null);
	}

	// QUERIES
	/** We've broadly implemented two types of functions: functions for answering individual questions and functions for building
	 * maps.
	 * 
	 * The functions for answering individual questions, which we have reduced to the queries "What is the nearest [park/vacant lot/multi-family 
	 * walk-up building]?", "What’s the average [age/height/square footage/land value/total value] of a building within X miles of me?", and 
	 * "Who owns the building at this address?" are better optimized for space, only focusing on a subsection or 
	 * individual point of a Quadtree at a given time.
	 * We decided to drop the functions designed to answer the question "What’s the [oldest/tallest/most spacious/highest land value/highest
	 * total value] building within X miles of me?" and "What is the nearest building owned by a person or corporation whose name includes 
	 * these characters [for example, "Gates" or "Apple" or "City of New York"]?" because the answers to these questions would be significantly
	 * when visually mapped out. Once Wyatt did the work of making all these cool maps, it seemed like a waste of time to try to provide poor
	 * answers to questions better answered visually and with less finnicky implementation.
	 * 
	 * Our second group of functions, those related to maps, involve initial setup functions which can tax a system (requiring iteration through
	 * all of the points on the map), but afterwards are easier to implement with the functions provided in PointRegionQuadtree.java and 
	 * provide more useful answers with Wyatt's wonderful maps.
	 * - Andrew
	 */  

	// **1: INDIVIDUAL QUERY FUNCTIONS.**

	// Who owns the building at this address?
	public String getOwner(String address){
		return symbolTable.get(address).ownerName;
	}

	// What’s the average [height/age/land value/total value/square footage] of a building within X miles of me? 
	public double average(double xcoord, double ycoord, double mileRadius, int choice){
		// The options, in order: 
		// 1. numFloors, 
		// 2. age (based on yearBuilt - the only int), 
		// 3. assessedLandValue, 
		// 4. assessedTotalValue, 
		// 5. totalBuildingArea.

		double coordRadius = mileRadius*COORDS_TO_MILES_RATIO;
		ArrayList<TaxPlot> nearbyBuildingsArr = quadtree.withinDistance(xcoord, ycoord, coordRadius);
		double sum = 0;

		// numFloors
		if (choice == 1){
			for (TaxPlot plot : nearbyBuildingsArr){
				sum += plot.numFloors;
			}
			return sum / nearbyBuildingsArr.size();
		}

		// age
		if (choice == 2){
			for (TaxPlot plot : nearbyBuildingsArr){
				sum += (double) (2024-plot.yearBuilt);
			}
			return sum / nearbyBuildingsArr.size();
		}

		// assessedLandValue
		if (choice == 3){
			for (TaxPlot plot : nearbyBuildingsArr){
				sum += plot.assessedLandValue;
			}
			return sum / nearbyBuildingsArr.size();
		}

		// assessedTotalValue
		if (choice == 4){
			for (TaxPlot plot : nearbyBuildingsArr){
				sum += plot.assessedTotalValue;
			}
			return sum / nearbyBuildingsArr.size();
		}

		// totalBuildingArea
		if (choice == 5){
			for (TaxPlot plot : nearbyBuildingsArr){
				sum += plot.totalBuildingArea;
			}
			return sum / nearbyBuildingsArr.size();
		}

		return 0.0; // if the input is out-of-bounds
	}

	// "What is the nearest [park/vacant lot/multi-family walk-up building]?""
	// Based on the landuse instance variable, so there are 11 valid inputs between 1 and 11 (inclusive).
	// See the top of TaxPlot.java for what each integer corresponds to.
	// Returns the lot's address and its distance from our point.
	public String nearest(double xcoord, double ycoord, int landUseValue){
		if (landUseValue > 1 || landUseValue < 11){ // Out of bounds
			return Integer.toString(landUseValue) + " is out of bounds. Please input an integer between 0 and 30, inclusive.";
		}

		TaxPlot plotResult = nearestHelper(xcoord, ycoord, landUseValue, 3);
		double dist = distanceHelper(xcoord, ycoord, plotResult.xcoord, plotResult.ycoord);

		// For distances less than 0.1 miles, convert to feet.
		if (dist < 0.1) {
			int distFeet = (int) dist*5280;
			return plotResult.address + " | " + Integer.toString(distFeet) + " feet away.";
		}

		return plotResult.address + " | " + Double.toString(dist) + " miles away.";
	}

	// Calculates the Euclidean distance in miles between two points based on their xcoords and ycoords.
	public double distanceHelper(double point1X, double point1Y, double point2X, double point2Y){
		double xDist = point1X-point2X;
		double yDist = point1Y-point2Y;

		double xMiles = xDist/COORDS_TO_MILES_RATIO;
		double yMiles = yDist/COORDS_TO_MILES_RATIO;

		return Math.hypot(xMiles, yMiles);
	}

	// Andrew's, based on Wyatt's implementation of closestObject. 
	// This was particularly finnicky because closestObject (and all functions in PointRegionQuadtree) were designed for generics, 
	// not around the particular TaxPlot objects used in Pluto, and we needed to adapt closestObject() to looking for an instance 
	// variable of a TaxPlot.
	public TaxPlot nearestHelper(double xcoord, double ycoord, int landUseValue, int exhaustiveness){
		// Internal Classes of PointRegionQuadtree like Node and LeafNode need some additional help in order to function properly
		// in PLUTO.java.
		if (!quadtree.root.box.inBox(xcoord,ycoord)){
			System.out.println("coords not in window");
			return null;
		}

		//searchNode is the node under which you check every point
		PointRegionQuadtree.Node searchNode = quadtree.getHelper(quadtree.root, null, xcoord, ycoord);
		//If searchNode is a leaf, return that leaf's data
		if (searchNode instanceof PointRegionQuadtree.LeafNode){
			PointRegionQuadtree.LeafNode leaf = (PointRegionQuadtree.LeafNode) searchNode;
			if (leaf.data instanceof TaxPlot){
				TaxPlot candidatePlot = (TaxPlot) leaf.data;
				if (candidatePlot.landuse == landUseValue){
					return candidatePlot;
				}
			}
		}

		//Move searchNode up the tree a number of levels equal to the exhaustiveness of the search
		while (exhaustiveness>0 && searchNode.parent != null){
			exhaustiveness --;
			searchNode = searchNode.parent;
		}

		//leafNodes is the list of all leafNodes below the searchNode
		ArrayList<PointRegionQuadtree<TaxPlot>.LeafNode> leafNodes = new ArrayList<PointRegionQuadtree<TaxPlot>.LeafNode>();
		quadtree.traversalHelper(searchNode, leafNodes);

		//For each leafNode, calculate the distance to (xcoord,ycoord), in order to find the minimum distance
		double minDistance = Double.POSITIVE_INFINITY;
		PointRegionQuadtree<TaxPlot>.LeafNode closestNode = null;
		for (PointRegionQuadtree<TaxPlot>.LeafNode leaf: leafNodes){
			double dist = Math.hypot(xcoord-leaf.xcoord, ycoord-leaf.ycoord);
			if (dist < minDistance){
				closestNode = leaf;
				minDistance = dist;
			}
		}

		return (TaxPlot) closestNode.data;
	}


	// **2: MAP-RELATED FUNCTIONS.**
	// FOR FUTURE (TO WYATT): I don't really know how the map works, but I'm trying to provide some tools to make mapping out 
	// just TaxPlots of a given type or with a certain owner name something we can just copy-paste.

	// Fills out a secondary ArrayList, Quadtree, and SymbolTable to help map and answer queries involving just TaxPlots of
	// a chosen land usage category.
	// Integers from 1-11 (inclusive) are valid inputs.
	// Highly memory and time-intensive for large datasets like ours. Only one land usage option can stored at a time.
	public void mapLandUse(int landUseOption){
		if (landUseOption > 1 || landUseOption < 11){ // Out of bounds
			return;
		}
		for (TaxPlot plot : this.taxplots) {
			if (plot.landuse == landUseOption) {
				landuseTaxplots.add(plot);
        		landuseQuadTree.insert(plot,plot.xcoord,plot.ycoord);
        		landuseSymbolTable.put(plot.address, plot);
			}
		}
	}


	// Fills out a tertiary group of data structures to help map and answer queries involving just TaxPlots owned by
	// a person, governmental body, or corporation whose name includes a given string of character [for example, "Gates"
	// or "Apple" or "City of New York"].
	// Also highly memory and time-intensive for large datasets. Can only store one land usage option at a time.
	public void mapOwner(String ownerInput){
		for (TaxPlot plot : this.taxplots) {
			if (plot.ownerName.contains(ownerInput)) {
				ownerTaxplots.add(plot);
				ownerQuadTree.insert(plot,plot.xcoord,plot.ycoord);
				ownerSymbolTable.put(plot.address, plot);
			}
		}
	}
	 

	// DELETE LATER - I think Wyatt answered this perfectly with the demo.
	// // What’s the [oldest/tallest/most spacious/highest land value/highest total value] building within X miles of me?
	// public TaxPlot most(double maxDistance){
	// 	return null;
	// }

	private class MapMouseListener implements MouseListener, MouseMotionListener {
		public void mousePressed(MouseEvent event) {
			mouseX = event.getX();
			mouseY = event.getY();
			//System.out.println(newX);
			repaint();
		}
		public void mouseReleased(MouseEvent event) {}
		public void mouseDragged(MouseEvent event){}
		public void mouseClicked(MouseEvent event) {}
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
		public void mouseMoved(MouseEvent event) {}
	}

	public static void main(String[] args){

		// Tests
		// PLUTO testmap = new PLUTO("mapnyc/toy.csv", "mapnyc/state_plane_nyc.jpg");


		// Actual main function for demo.
		PLUTO map = new PLUTO("mapnyc/pluto_24v1_1.csv","mapnyc/state_plane_nyc.jpg");
		//https://www.geeksforgeeks.org/ways-to-read-input-from-console-in-java/
		Scanner scanner = new Scanner(System.in);
		while (true){
			System.out.println("MapNYC: select mode");
			System.out.println("1: Within Distance");
			System.out.println("2: Nearest");
			System.out.println("3: Generate Map");
			int modeSelection = Integer.valueOf(scanner.nextLine());
			map.MODE = Integer.valueOf(modeSelection);
			if (map.MODE == map.WITHIN_DISTANCE_MODE){
				System.out.println("Set radius:");
				int withinDistanceRadius = Integer.valueOf(scanner.nextLine());
				map.WITHIN_DISTANCE_RADIUS = withinDistanceRadius;
			}
			if (map.MODE == map.MAP_MODE){
				System.out.println("Map generator: select mode");
				System.out.println("1: Assessed Total Value");
				System.out.println("2: Year Built");
				System.out.println("3: Number of Floors");
				int mapTypeSelection = Integer.valueOf(scanner.nextLine());
				map.MAP_TYPE = mapTypeSelection +map.MONEY -1;
			}
		}

		//System.out
	}
}

