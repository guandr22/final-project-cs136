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

	public TaxPlot(String lineOfData){
		lineOfData = lineOfData.replace(", ", "~");//to deal with the problem of commas in names

		String[] data = lineOfData.split(",");

		
		xcoord = Integer.valueOf(data[75]);
		ycoord = Integer.valueOf(data[76]);

		address = data[18];

		//combines first and last names
		String[] ownerNames = data[34].split("~");
		ownerName = ownerNames[0];
		if (ownerNames.length==2){
			ownerName += ", " + ownerNames[1];
		}

		landuse = Integer.valueOf(data[31]);

		if (data[47].equals("")){
			numFloors = 0;
		} else {
			numFloors = Double.valueOf(data[47]);
		}

		yearBuilt = Integer.valueOf(data[62]);
		assessedLandValue = Double.valueOf(data[59]);
		assessedTotalValue = Double.valueOf(data[60]);
		totalBuildingArea = Double.valueOf(data[36]);
		lotArea = Double.valueOf(data[35]);
		
	}
	public String toString(){
		return address + ": (" + xcoord + ", " + ycoord+")";
	}
}