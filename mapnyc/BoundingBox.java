package mapnyc;

public class BoundingBox{
	public double minX,maxX,minY,maxY;
	public double centerX, centerY;

	public BoundingBox(double minX,double maxX,double minY,double maxY){
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		centerX = (minX + maxX)/2;
		centerY = (minY + maxY)/2;
	}

	public BoundingBox upperLeftBox(){
		return new BoundingBox(minX,centerX,centerY,maxY);
	}
	public BoundingBox upperRightBox(){
		return new BoundingBox(centerX,maxX,centerY,maxY);
	}
	public BoundingBox lowerLeftBox(){
		return new BoundingBox(minX,centerX,minY,centerY);
	}
	public BoundingBox lowerRightBox(){
		return new BoundingBox(centerX,maxX,minY,centerY);
	}

	public boolean inBox(double xcoord, double ycoord){
		//If the coordinates are on the edge of the box, that counts
		return xcoord>=minX && xcoord<=maxX && ycoord>=minY && ycoord<=maxY;
	}
	public String toString(){
		return "x: " + minX + " to " + maxX + ", y: " + minY + " to " + maxY;
	}
}