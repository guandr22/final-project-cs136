package mapnyc;

import java.util.ArrayList;

public class Quadtree<Item, Location> implements QuadtreeInterface<Item, Location>{

	// Instance variables
	private int numNodes;
	public Node root;

	// Constructor
	public Quadtree(){
		this.numNodes = 0;
		this.root = null;
	}

	// Inner Node Class - copied from lab 6 and adapted.
	public class Node {
		public Item data;
		public Location location;
		public Node[] branches;

		// Inner Node constructor;
		public Node(Item data, Location location){
			this.data = data;
			this.location = location;
			this.branches = new Node[4]; // Initializes each of the maximum of 4 branches as null;
		}

		// POSSIBLE AREA TO ADD STUFF -- a helper function that adds a branch to a node's branch?
	}


	// Returns true if the Quadtree is empty.  
	public boolean isEmpty(){
		return this.numNodes == 0;
	}

	// Returns the number of non-empty nodes.
	public int size(){
		return this.numNodes;
	}

	// Adds an object to the tree given a place to put it.
	// Ensures that no node has more than 4 branches.
	// Returns true if the addition is successful and false if the target location 
	// is already occupied.
	// If successful, increases the Quadtree's size.
	// - Andrew's
	public boolean insert(Item object, Location place){
		return false;
	}

	// Removes an object from the tree. Returns true if the removal is successful
	// and false if the object was not present in the tree.
	// If successful, decreases the Quadtree's size.
	// - Andrew's
	public boolean remove(Item object){
		return false;
	}

	// Returns the object at a given location if one exists. Returns null otherwise.
	// - Wyatt's
	public Item get(Location place){
		return null;
	}

	// Returns the closest object to a given location. 
	// - Wyatt's
	public Item closestObject(Location place){
		return null;
	}

	// Returns an ArrayList of all objects within a given distance from a location.
	// - Wyatt's
	public ArrayList<Item> withinDistance(Location place, float radius){
		return null;
	}

	public static void main(String[] args){
		Quadtree test = new Quadtree();
	}
}