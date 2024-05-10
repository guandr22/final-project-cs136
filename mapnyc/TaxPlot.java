package mapnyc;

public class TaxPlot{
	// New York-Long Island State Plane coordinate system;
	public int xcoord; 
	public int ycoord; 
	public String address; 
	public String ownerName; 
	public int landuse; 
	public double numFloors;
	public int yearBuilt; 
	public double assessedLandValue; 
	public double assessedTotalValue;
	public double totalBuildingArea;
	public double lotArea;
	public boolean corruptedData = false;

	public static final int ONE_TWO_FAMILY_WALK_UP = 1;
	public static final int MULTI_FAMILY_WALK_UP = 2;
	public static final int MULTI_FAMILY_ELEVATOR = 3;
	public static final int MIXED_RESIDENTIAL_COMMERCIAL = 4;
	public static final int COMMERCIAL_OFFICE = 5;	
	public static final int INDUSTRIAL_MANUFACTURING = 6;
	public static final int TRANSPORTATION_UTILITY = 7;
	public static final int PUBLIC_FACILITIES_INSTITUTIONS = 8;
	public static final int OPEN_SPACE_OUTDOOR_RECREATION = 9;
	public static final int PARKING_FACILITIES = 10;
	public static final int VACANT_LOT = 11;


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
		return ownerName + "'s" + address + ": (" + xcoord + ", " + ycoord+")";
	}
	public String toStringExhaustive(){
		return "address: "+ address + ": (" + xcoord + ", " + ycoord+")" + " ownerName: " 
		+ ownerName + " numFloors: " + numFloors + "yearBuilt: " + yearBuilt + "assessedLandValue: " +
		assessedLandValue + "assessedTotalValue: " + assessedTotalValue + "totalBuildingArea: " + totalBuildingArea + "lotArea: " + lotArea;
	}
}