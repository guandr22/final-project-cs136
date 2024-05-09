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
	public int MODE=1;
	public final int WITHIN_DISTANCE_MODE =0;
	public final int NEAREST_MODE =1;
	public final int MONEYMAP_MODE =2;
	public final int OLDMAP_MODE =3;
	public final int FLOORSMAP_MODE =4;

	public final static int NUM_TAXPLOTS=200000;

	public ArrayList<TaxPlot> taxplots; //used to iterate through once, find the max and min xs and ys, in order to set up quadtree
	public Hashtable<String, TaxPlot> symbolTable; // address ---> taxplot
	public PointRegionQuadtree<TaxPlot> quadtree;
	public BoundingBox region;
	public final int WINDOW_WIDTH, WINDOW_HEIGHT;
	public BufferedImage bf;
	public BufferedImage nycJPG;
	public int mouseX, mouseY;

	//I used Liberty Island and U Thant Island as set points to calculate this these constants
	//I used screenshots to figure out their pixel position, and their coordinates are included in pluto.csv
	public final double PIXELS_TO_COORDS_RATIO = 1/270.3317839;
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
		Graphics2D g2 = (Graphics2D) bf.getGraphics();
		//Set the jpg of NYC as the background
		try{
			g2.drawImage(nycJPG,0,0,null);
		}
		catch (Exception e){}


		if (MODE == WITHIN_DISTANCE_MODE){
			ArrayList<TaxPlot> taxplotsWithinDistance = quadtree.withinDistance(getXCoordFromPixel(mouseX),getYCoordFromPixel(mouseY),10000);
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
			g2.fillOval(mouseX-5,mouseY-5,10,10);
		}
		else if (MODE == NEAREST_MODE){
			PointRegionQuadtree<TaxPlot> tempQuadtree = new PointRegionQuadtree<TaxPlot>(region);
			for (TaxPlot taxplot: taxplots){
				tempQuadtree.insert(taxplot,taxplot.xcoord,taxplot.ycoord);
			}
			TaxPlot curTaxplot = null;
			while (true){
				curTaxplot = tempQuadtree.closestObject(getXCoordFromPixel(mouseX),getYCoordFromPixel(mouseY),2);
				if (curTaxplot.landuse == TaxPlot.VACANT_LOT){
					break;
				}
				else{
					tempQuadtree.remove(curTaxplot);
				}
			}
			//draw a green oval for the YOU ARE HERE
			g2.setPaint(Color.GREEN);
			g2.fillOval(getXPixel(curTaxplot),getYPixel(curTaxplot),10,10);
			System.out.println("The nearest vacant lot is:" + curTaxplot.toString());
			g2.fillOval(mouseX-5,mouseY-5,10,10);
		}

		else if (MODE == MONEYMAP_MODE){
			for (TaxPlot taxplot: taxplots){
				double price = taxplot.assessedTotalValue;
				double capPrice = 2000000;
				if (price >capPrice){
					price = capPrice;
				}
				int red = (int)((price/capPrice) * 254);
				Color priceColor = new Color(red,255-red, 0);
				g2.setPaint(priceColor);
				g2.fillRect(getXPixel(taxplot), getYPixel(taxplot), 1,1);
			}
		}
		else if (MODE == OLDMAP_MODE){
			for (TaxPlot taxplot: taxplots){
				if (taxplot.yearBuilt == -1){
					continue;
				}
				double age = 2024 - taxplot.yearBuilt;

				double capAge = 300;
				if (age >capAge){
					age = capAge;
				}

				int green = (int)((age/capAge) * 254);

				Color ageColor = new Color(0,green, 0);
				g2.setPaint(ageColor);
				g2.fillRect(getXPixel(taxplot), getYPixel(taxplot), 1,1);
			}
		}

		else if (MODE == FLOORSMAP_MODE){
			for (TaxPlot taxplot: taxplots){
				if (taxplot.numFloors == -1){
					continue;
				}
				double floors = taxplot.numFloors;

				double capFloors = 10;
				if (floors >capFloors){
					floors = capFloors;
				}

				int blue = (int)((floors/capFloors) * 254);

				Color floorsColor = new Color(0,0, blue);
				g2.setPaint(floorsColor);
				g2.fillRect(getXPixel(taxplot), getYPixel(taxplot), 1,1);
			}
		}

		g.drawImage(bf,0,0,null);
	}


	/** PLANNING - ANDREW
	 * Won't be able to lift functions from the Quadtree wholesale - two options
	 * 	1. create another tree of stuff that fits just this landUseValue and search through that, lifting the Quadtree function
	 *  2. modify Wyatt's closestObject function here to only add leaves where the data's landUseValue matches up.
	 * 		the second option probably makes the most sense in terms of space and time tradeoffs --> maybe do this adjustment by adding 
	 * 		another function in PointRegionQuadtree.java.
	 */

	//What is the nearest [park/vacant lot/multi-family walk-up building]?
	// Andrew's, based on Wyatt's implementation of closestObject.
	// Should return the name of the lot and its distance from our point.
	public String nearest(double xcoord, double ycoord, int landUseValue){
		// Possibility of type being part of a user input from the prompt --> would require some kind of hashtable later on to map each 
		// user input (I'm guessing we're going to have users look for parking spaces, not land use value 10) to a land use value.
		// TaxPlot plotResult = nearestHelper(xcoord, ycoord, landUseValue);
		return "";
	}

	// public TaxPlot nearestHelper(double xcoord, double ycoord, int landUseValue){
	// 	//searchNode is the node under which you check every point
	// 	Node searchNode = quadtree.getHelper(quadtree.root, null, xcoord, ycoord);
	// 	//If searchNode is a leaf, return that leaf's data
	// 	if (searchNode instanceof PointRegionQuadtree.LeafNode){
	// 		LeafNode leaf = (LeafNode) searchNode;
	// 		if (leaf.data.landuse == landUseValue){
	// 			return leaf.data;
	// 		}
	// 	}
	// }

	/*What is the nearest building owned by a person or corporation (same thing, really)
	whose name includes the characters [for example, "Gates" or "Apple" or "City of New York"]?*/
	public String ownerOfNearest(double xcoord, double ycoord, String owner){
		// Type will be provided by Type
		return "";
	}
	 


	//Who owns the building at this address?
	public String getOwner(String address){
		return symbolTable.get(address).ownerName;
	}

	//What’s the [oldest/tallest/most spacious/highest land value/highest total value] building within X miles of me?
	public TaxPlot most(double maxDistance){
		return null;
	}

	//What’s the average [age/height/square footage/land value/total value] of a building within X miles of me? 

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
		PLUTO map = new PLUTO("mapnyc/pluto_24v1_1.csv","mapnyc/state_plane_nyc.jpg");
		//System.out
	}
}

