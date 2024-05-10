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

	//Wyatt used Liberty Island and U Thant Island as set points to calculate these constants.
	/** 
	 * Wyatt used screenshots to figure out their pixel position, and their x and y coordinates (based on 
	 * the New York-Long Island State Plane coordinate system) are included in pluto.csv in columns BX and BY.
	 * 
	 * To figure out a rough ratio between coordinates and miles, Andrew plugged WGS84 latitude and longitude values,
	 * found in columns CL and CM of pluto.csv, into https://www.omnicalculator.com/other/latitude-longitude-distance 
	 * to calculate the rough distance in statute miles between Liberty and U Thant Islands (these are rows 131751 and 
	 * 838380, respectively).
	 * Then, he used the Pythagorean Theorem (this is sketchy, given the curvature of the Earth, but the distances
	 * involved should be small enough to avoid too much distortion) to find the difference in NY-LI SP coordinates between
	 * Liberty and U Thant islands in order to calculate a coordinate-to-mile ratio.
	*/
	public final double PIXELS_TO_COORDS_RATIO = 1/270.3317839;
	public final double COORDS_TO_MILES_RATIO = 0;

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

	//getXPixel and getYPixel make use of the 
	//getXPixel and getYPixel given a taxplots coordinates. Don't forget that (0,0) is the top left!

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

	/** Because closestObject() was designed with abstract Objects in mind rather than specific TaxPlots, implementing nearest() 
	 * presented a problem: how do we look for a specific type of TaxPlot when PointRegionQuadtree's closestObject() method
	 * can't process specific TaxPlot data?
	 * We had two options: 
	 * 1. Use a function which creates an empty quadtree, iterates through this.quadtree and adds to the empty quadtree only those 
	 * elements which matched the desired type of TaxPlot, then calls closestObject() on the new quadtree.
	 * 2. Adapt the underlying code behind closestObject() to work with TaxPlot's landuse instance variable.
	 * 
	 * The first choice would have been significantly easier to implement, but we decided on the second choice because the first choice was
	 * so memory and space-intensive.
	 */
	

	// What is the nearest [park/vacant lot/multi-family walk-up building]?
	// Should return the lot's address and its distance from our point.
	public String nearest(double xcoord, double ycoord, int landUseValue){
		// Possibility of type being part of a user input from the prompt --> would require some kind of hashtable later on to map each 
		// user input (I'm guessing we're going to have users look for parking spaces, not land use value 10) to a land use value.
		TaxPlot plotResult = nearestHelper(xcoord, ycoord, landUseValue, 3);
		return plotResult.address + ", ";
	}

	// Andrew's, based on Wyatt's implementation of closestObject.
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

	/*What is the nearest building owned by a person or corporation (same thing, really)
	whose name includes the characters [for example, "Gates" or "Apple" or "City of New York"]?*/
	public String ownerOfNearest(double xcoord, double ycoord, String owner){
		// Type will be provided by Type
		return "";
	}
	 


	// Who owns the building at this address?
	public String getOwner(String address){
		return symbolTable.get(address).ownerName;
	}

	// What’s the [oldest/tallest/most spacious/highest land value/highest total value] building within X miles of me?
	public TaxPlot most(double maxDistance){
		return null;
	}

	// What’s the average [age/height/square footage/land value/total value] of a building within X miles of me? 

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

