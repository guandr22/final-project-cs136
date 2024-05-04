package mapnyc;

import java.util.*; 
import java.io.*; 

public class PLUTO{
	public ArrayList<TaxPlot> taxplots; //temporary data structre, to be replaced with quadtree
	public Hashtable<String, TaxPlot> symbolTable; // address ---> taxplot

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

        for (TaxPlot taxplot: taxplots){
        	symbolTable.put(taxplot.address, taxplot);
        }
	}

	//What is the nearest [park/vacant lot/multi-family walk-up building]?

	/*What is the nearest building owned by a person or corporation (same thing, really)
	whose name includes the characters [for example, “Gates” or “Apple” or “City of New York”]?*/


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

		System.out.println(map.getOwner("406 EAST 189 STREET"));
	}
}

