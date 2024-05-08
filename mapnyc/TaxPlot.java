package mapnyc;

public class TaxPlot{
	// New York-Long Island State Plane coordinate system;
	public int xcoord; //76
	public int ycoord; //77
	public String address; //19
	public String ownerName; //35
	public int landuse; //32
	public double numFloors; //48
	public int yearBuilt; //63
	public double assessedLandValue; //60
	public double assessedTotalValue; //61
	public double totalBuildingArea; //37
	public double lotArea; //36
	public boolean corruptedData = false;

	public TaxPlot(String lineOfData){
		//There are two issues with the data. First, some names have commas
		//Usually this takes the form of "FIRST, LAST" so I tried temporarily excising ", " before splitting
		//However, some addresses start with a space, so I ended up excising those too! :(
		//There are always 18 commas before an address: addresses are the 19th entry
		lineOfData = lineOfData.replace(", ", "~");
		String[] data = lineOfData.split(",");

		try{
			//no try and catch, because these are essential facts
			xcoord = Integer.valueOf(data[75]);
			ycoord = Integer.valueOf(data[76]);
			address = data[18];

			//combines first and last names
			String[] ownerNames = data[34].split("~");
			ownerName = ownerNames[0];
			if (ownerNames.length==2){
				ownerName += ", " + ownerNames[1];
			}

			try{
				landuse = Integer.valueOf(data[31]);
			} catch (Exception q){
				landuse = -1;
			}
			try{
				numFloors = Double.valueOf(data[47]);
			} catch (Exception q){
				numFloors = -1;
			}
			try{
				yearBuilt = Integer.valueOf(data[62]);
			} catch (Exception q){
				yearBuilt = -1;
			}
			try{
				assessedLandValue = Double.valueOf(data[59]);
			} catch (Exception q){
				assessedLandValue = -1;
			}
			try{
				assessedTotalValue = Double.valueOf(data[60]);
			} catch (Exception q){
				assessedTotalValue = -1;
			}
			try{
				totalBuildingArea = Double.valueOf(data[36]);
			} catch (Exception q){
				totalBuildingArea = -1;
			}
			try{
				lotArea = Double.valueOf(data[35]);
			} catch (Exception q){
				lotArea = -1;
			}

		}
		catch(Exception e){
			corruptedData = true;
			//System.out.print("*");
		}
		
		
	}

	public String toString(){
		return address + ": (" + xcoord + ", " + ycoord+")";
	}
	public String toStringExhaustive(){
		return "address: "+ address + ": (" + xcoord + ", " + ycoord+")" + " ownerName: " 
		+ ownerName + " numFloors: " + numFloors + "yearBuilt: " + yearBuilt + "assessedLandValue: " +
		assessedLandValue + "assessedTotalValue: " + assessedTotalValue + "totalBuildingArea: " + totalBuildingArea + "lotArea: " + lotArea;
	}
}