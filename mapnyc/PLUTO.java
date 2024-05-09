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

public class PLUTO extends JFrame{

	public ArrayList<TaxPlot> taxplots; //used to iterate through once, find the max and min xs and ys, in order to set up quadtree
	public Hashtable<String, TaxPlot> symbolTable; // address ---> taxplot
	public PointRegionQuadtree<TaxPlot> quadtree;
	public BoundingBox region;
	public final int WINDOW_WIDTH, WINDOW_HEIGHT;
	public BufferedImage bf;
	public BufferedImage nycJPG;

	//I used Liberty Island and U Thant Island as set points to calculate this these constants
	//I used screenshots to figure out their pixel position, and their coordinates are included in pluto.csv
	public final double PIXELS_TO_COORDS_RATIO = 1/270.3317839;
	//the BOTTOMLEFT_XCOORD and BOTTOMLEFT_YCOORD are the x and y cordinates cooresponding to the bottom left of the window.
	public final double BOTTOMLEFT_XCOORD = 895306.8989;
	public final double BOTTOMLEFT_YCOORD = 113704.226;

	public PLUTO(String filename, String jpgfilename){
		taxplots = new ArrayList<TaxPlot>();
		symbolTable = new Hashtable<>();
		File file = new File(filename); 
		try{
            Scanner scanner = new Scanner(file);
            scanner.nextLine(); //skip the first line of data labels
            int numTaxPlots = 10000000;
            while (scanner.hasNextLine() && numTaxPlots >=0) {
        		String line = scanner.nextLine();
        		TaxPlot nextTaxplot = new TaxPlot(line);
        		if (!nextTaxplot.corruptedData){
        			taxplots.add(nextTaxplot);
        		}
        		numTaxPlots --;
        	}
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        //iterate through to find max/min x and y, in order to set boundaries
        setRegion();

        quadtree = new PointRegionQuadtree<TaxPlot>(region);
        //iterate through points in order to insert into symbol table and quadtree
        for (TaxPlot taxplot: taxplots){
        	symbolTable.put(taxplot.address, taxplot);
        	quadtree.insert(taxplot, taxplot.xcoord, taxplot.ycoord);
        }

        //set up jpg
        File jpgFile = new File(jpgfilename);
        try{
			nycJPG = ImageIO.read(jpgFile);
		} catch (Exception e){
			System.out.println("File not found: " + e.getMessage());
		}

        WINDOW_WIDTH = nycJPG.getWidth();
        WINDOW_HEIGHT = nycJPG.getHeight();

        bf = new BufferedImage(WINDOW_WIDTH,WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        //jframe = new JFrame("mapnyc");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		this.setVisible(true);
		System.out.println(taxplots.get(0).toStringExhaustive());
	}
	public void setRegion(){
		//necessary to set defaults to numbers in the range, not 0s or +-infinities, otherwise it's not "centered"
		double minX = taxplots.get(0).xcoord;
		double maxX = taxplots.get(0).xcoord;
		double minY = taxplots.get(0).ycoord;
		double maxY = taxplots.get(0).ycoord;

		for (TaxPlot taxplot: taxplots){
			if (taxplot.xcoord<minX){
				minX = taxplot.xcoord;
			}
			else if (taxplot.xcoord>maxX){
				maxX = taxplot.xcoord;
			}
			if (taxplot.ycoord<minY){
				minY = taxplot.ycoord;
			}
			else if (taxplot.ycoord>maxY){
				maxY = taxplot.ycoord;
			}
		}
		region = new BoundingBox(minX,maxX,minY,maxY);
		//System.out.println(region.toString());
	}

	//getXPixel and getYPixel make use of the 
	public int getXPixel(TaxPlot taxplot){
		return (int)((taxplot.xcoord - BOTTOMLEFT_XCOORD) * PIXELS_TO_COORDS_RATIO);
	}
	public int getYPixel(TaxPlot taxplot){
		return WINDOW_HEIGHT - (int)((taxplot.ycoord - BOTTOMLEFT_YCOORD) * PIXELS_TO_COORDS_RATIO);
	}

	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) bf.getGraphics();
		//Set the jpg of NYC as the background
		try{
			g2.drawImage(nycJPG,0,0,null);
		}
		catch (Exception e){}

		g2.setPaint(Color.RED);

		//Put a pixel of red for each taxplot
		for (TaxPlot taxplot: taxplots){
			g2.fillRect(getXPixel(taxplot), getYPixel(taxplot), 1,1);
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

	public static void main(String[] args){
		PLUTO map = new PLUTO("mapnyc/pluto_24v1_1.csv","mapnyc/state_plane_nyc.jpg");
	}
}

