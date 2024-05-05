package mapnyc;

import java.util.*; 
import java.io.*; 

public class PLUTO{

	public ArrayList<TaxPlot> taxplots; //used to iterate through once, find the max and min xs and ys, in order to set up quadtree
	public Hashtable<String, TaxPlot> symbolTable; // address ---> taxplot
	public PointRegionQuadtree<TaxPlot> quadtree;
	public BoundingBox region;

	public PLUTO(String filename){
		taxplots = new ArrayList<TaxPlot>();
		symbolTable = new Hashtable<>();

		File file = new File(filename); 
		try{
            Scanner scanner = new Scanner(file);
            scanner.nextLine(); //skip the first line of data labels
            while (scanner.hasNextLine()) {
        		String line = scanner.nextLine();
        		TaxPlot nextTaxplot = new TaxPlot(line);
        		taxplots.add(nextTaxplot);
        		
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

	//What is the nearest [park/vacant lot/multi-family walk-up building]?

	/*What is the nearest building owned by a person or corporation (same thing, really)
	whose name includes the characters [for example, "Gates" or "Apple" or "City of New York"]?*/



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
		PLUTO map = new PLUTO("mapnyc/toy.csv");
		System.out.println(map.quadtree.traversal().toString());
		System.out.println(map.getOwner("406 EAST 189 STREET"));
	}
}

