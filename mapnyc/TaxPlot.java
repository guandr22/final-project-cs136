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

		//String[] data = lineOfData.split(",");
		//data = Arrays.copyOfRange(data, 18, data.length);

		try{
			xcoord = Integer.valueOf(data[75]);
			ycoord = Integer.valueOf(data[76]);
			//System.out.println(data.length);
		}
		catch(Exception e2){
			try{
				xcoord = Integer.valueOf(data[74]);
			 	ycoord = Integer.valueOf(data[75]);
			 	//System.out.println("Failed once: " +lineOfData + data.length);
			}
			catch(Exception e){
				//System.out.println("Doubly failed: " +lineOfData);
				corruptedData = true;
			}
		}

		address = data[18];
		/*
		//combines first and last names
		String[] ownerNames = data[34].split("~");
		ownerName = ownerNames[0];
		if (ownerNames.length==2){
			ownerName += ", " + ownerNames[1];
		}
		*/
		//System.out.println(this.toString());
		/*
		if (data[31].equals("")){
			landuse = 0;
		} else {
			landuse = Integer.valueOf(data[31]);
		}

		if (data[47].equals("")){
			numFloors = 0;
		} else {
			numFloors = Double.valueOf(data[47]);
		}

		yearBuilt = Integer.valueOf(data[62]);
		assessedLandValue = Double.valueOf(data[59]);
	
		assessedTotalValue = Double.valueOf(data[60])

		totalBuildingArea = Double.valueOf(data[36]);
		lotArea = Double.valueOf(data[35]);
		*/
		
	}
	public String toString(){
		return address + ": (" + xcoord + ", " + ycoord+")";
	}
}