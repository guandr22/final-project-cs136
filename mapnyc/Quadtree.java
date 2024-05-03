package mapnyc;

import java.util.ArrayList;

public class Quadtree<Item> implements QuadtreeInterface<Item>{

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
		public double xcoord;
		public double ycoord;
		public Node upperLeft;
		public Node upperRight;
		public Node lowerLeft;
		public Node lowerRight;

		// Inner Node constructor;
		public Node(Item data, double xcoord, double ycoord){
			this.data = data;
			this.xcoord = xcoord;
			this.ycoord = ycoord;
			this.upperLeft = null;
			this.upperRight = null;
			this.lowerLeft = null;
			this.lowerRight = null;
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
	public boolean insert(Item object, double xcoord, double ycoord){
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
	public Item get(double xcoord, double ycoord){
		return null;
	}

	// Returns the closest object to a given location. 
	// - Wyatt's
	public Item closestObject(double xcoord, double ycoord){
		return null;
	}

	// Returns an ArrayList of all objects within a given distance from a location.
	// - Wyatt's
	public ArrayList<Item> withinDistance(double xcoord, double ycoord, double radius){
		return null;
	}

	public static void main(String[] args){
		Quadtree test = new Quadtree();
	}
}
