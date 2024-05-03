package mapnyc;
import java.util.ArrayList;

public interface QuadtreeInterface<Item>{

	// Returns true if the Quadtree is empty.  
	public abstract boolean isEmpty();

	// Returns the number of non-empty nodes.
	public abstract int size();

	// Adds an object to the tree given a place to put it.
	// Rebalances the quadtree to ensure that no node has more than 4 children.
	// Returns true if the addition is successful and false if the target location 
	// is already occupied.
	// If successful, increases the Quadtree's size.
	public abstract boolean insert(Item object, double xcoord, double ycoord);

	// Removes an object from the tree. Returns true if the removal is successful
	// and false if the object was not present in the tree.
	// If successful, decreases the Quadtree's size.
	public abstract boolean remove(Item object);

	// Returns the object at a given location if one exists. Returns null otherwise/
	public abstract Item get(double xcoord, double ycoord);

	// Returns the closest object to a given location. 
	public abstract Item closestObject(double xcoord, double ycoord);

	// Returns an ArrayList of all objects within a given distance from a location.
	public abstract ArrayList<Item> withinDistance(double xcoord, double ycoord, double radius); 
} 