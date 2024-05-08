package mapnyc;

import java.util.ArrayList;
import java.util.*; 
import java.io.*; 
// This is a point-region quadtree (which we refer to as a PR quadtree), based on https://www.cs.cmu.edu/~ckingsf/bioinfo-lectures/quadtrees.pdf.
/**
 * A PR quadtree helps us do functions like withinDistance(), as the quadrants on a given layer of the quadtree are of equal size.
 */

public class PointRegionQuadtree<Item> implements Quadtree<Item>{

	// Instance variables
	public int numLeaves;
	public int numInternalNodes;
	public InternalNode root;

	// Constructor
	public PointRegionQuadtree(double minX,double maxX,double minY,double maxY){
		this.numLeaves = 0;
		this.numInternalNodes = 1;
		this.root = new InternalNode(null,minX,maxX,minY,maxY);
	}
	public PointRegionQuadtree(BoundingBox box){
		this.numLeaves = 0;
		this.numInternalNodes = 1;
		this.root = new InternalNode(null,box);
	}

	public class Node{
		public Node parent;
	}

	/** 
	 * In a PR quadtree, there are two kinds of nodes, internal nodes (nodes with children), and leaves (nodes which store a given point in
	 * two-dimension space but do not have children). We included a third type of node (the Empty Node) to represent areas without points.
	 * Each internal node has 4 children, and so we can imagine each subtree of the PR quadtree dividing a two-dimensional space into 4 equally
	 * -sized quadrants, which either contain a point (a LeafNode), another four quadrants (an InternalNode), or nothing (an Empty Node).
	 */

	// An inner InternalNode class - copied from lab 6 and adapted.
	// Divides a space into four equal-sized regions.
	public class InternalNode extends Node{
		public Node upperLeft;
		public Node upperRight;
		public Node lowerLeft;
		public Node lowerRight;
		public BoundingBox box;

		// Inner Node constructor;
		public InternalNode(Node parent, double minX,double maxX,double minY,double maxY){
			this.parent = parent;
			this.upperLeft = new EmptyNode(this);
			this.upperRight = new EmptyNode(this);
			this.lowerLeft = new EmptyNode(this);
			this.lowerRight = new EmptyNode(this);
			this.box = new BoundingBox(minX,maxX,minY,maxY);
		}

		public InternalNode(Node parent, BoundingBox box){
			this.parent = parent;
			this.upperLeft = new EmptyNode(this);
			this.upperRight = new EmptyNode(this);
			this.lowerLeft = new EmptyNode(this);
			this.lowerRight = new EmptyNode(this);
			this.box = box;
		}
		public String toString(){
			return "[" + upperLeft.toString() + ", "
			+ upperRight.toString() + ", "
			+ lowerLeft.toString() + ", "
			+ lowerRight.toString() + "]";
		}
	}

	// A LeafNode stores a point in space and its associated information.
	public class LeafNode extends Node{
		public Item data;
		public double xcoord;
		public double ycoord;

		public LeafNode(Node parent, Item data, double xcoord, double ycoord){
			this.parent = parent;
			this.data = data;
			this.xcoord = xcoord;
			this.ycoord = ycoord;
		}
		public String toString(){
			return "("+xcoord + ","+ycoord +")-->" + data; 
		}
	}

	// An EmptyNode denotes a space without points or further subdivisions.
	public class EmptyNode extends Node{
		public EmptyNode(Node parent){
			this.parent = parent;
		}
		public String toString(){
			return "emptyNode";
		}
	} 

	// METHODS
	// Returns true if the Quadtree is empty.  
	public boolean isEmpty(){
		return this.numLeaves == 0;
	}

	// Returns the number of non-empty leaf nodes.
	public int size(){
		return this.numLeaves;
	}

	// Adds an object to the tree given a place to put it.
	// Ensures that no node has more than 4 branches.
	// Returns true if the addition is successful and false if the target location 
	// is already occupied.
	// If successful, increases the Quadtree's size.

	public boolean insert(Item object, double xcoord, double ycoord){
		//root.upperLeft = new InternalNode(0,10,0,5);
		if (!root.box.inBox(xcoord,ycoord)){
			System.out.println("("+xcoord +", " + ycoord +")"+" not in region");
			return false;
		}
		root = (InternalNode) insertHelper((Node) root, null, object, xcoord, ycoord,root.box);
		numLeaves ++;
		return true; //this isn't foolproof. Returns true, even when the spot is taken!
	}

	public Node insertHelper(Node curNode, Node prevNode, Item object, double xcoord,double ycoord,BoundingBox box){
		//the syntax "PointRegionQuadtree.EmptyNode" was explained to me by Claude.ai, as I was getting an error regarding generics
		//base case:
		if (curNode instanceof PointRegionQuadtree.EmptyNode){
			return new LeafNode(prevNode, object,xcoord,ycoord);
		}
		//if you're at an InternalNode, call insert helper on the subtree which contains the location you want to insert a node at.
		if (curNode instanceof PointRegionQuadtree.InternalNode){
			InternalNode cell = (InternalNode) curNode;

			//upper left
			if (box.upperLeftBox().inBox(xcoord,ycoord)){
				cell.upperLeft = insertHelper(cell.upperLeft,curNode, object,xcoord,ycoord,box.upperLeftBox());
			}
			//upper right		
			else if (box.upperRightBox().inBox(xcoord,ycoord)){
				cell.upperRight = insertHelper(cell.upperRight,curNode,object,xcoord,ycoord,box.upperRightBox());
			}
			//lower left
			else if (box.lowerLeftBox().inBox(xcoord,ycoord)){
				cell.lowerLeft = insertHelper(cell.lowerLeft,curNode,object,xcoord,ycoord,box.lowerLeftBox());
			}
			//lower right
			else if (box.lowerRightBox().inBox(xcoord,ycoord)){
				cell.lowerRight = insertHelper(cell.lowerRight,curNode,object,xcoord,ycoord,box.lowerRightBox());
			}
			else{
				//this should never happen
				System.out.println("something broke");
			}

			return cell;
		}
		//if you're at a leaf, crack it open---make a new internalnode, then insert the displaced leaf and new data into it
		else if (curNode instanceof PointRegionQuadtree.LeafNode){
			//check for a "collision". If there is one, don't add the new object.
			LeafNode oldLeaf = (LeafNode) curNode;
			if (oldLeaf.xcoord ==xcoord && oldLeaf.ycoord==ycoord){
				//System.out.println("There is already an object at ("+xcoord + ", "+ycoord+ "). Insertion failed.");
				return curNode;
			}
			else {
				//create a new internal node
				InternalNode newInternalNode = new InternalNode(prevNode, box);
				numInternalNodes ++;

				// add old object to the new internal node //
				newInternalNode = (InternalNode) insertHelper(newInternalNode,null,oldLeaf.data,oldLeaf.xcoord,oldLeaf.ycoord,box);
				// add new object to the new internal node
				newInternalNode = (InternalNode) insertHelper(newInternalNode,null,object,xcoord,ycoord,box);

				return newInternalNode;
			}
		}

		else{
			System.out.println("something broke");
			return null;
		}
	}

	// Remove all instances of an object from the tree. Returns true if the removal is successful
	// and false if the object was not present in the tree.
	// If successful, decreases the Quadtree's size.
	// - Andrew's
	public boolean remove(Item object){
		ArrayList<Item> objectsInTreeArr = traversal();
		if (!objectsInTreeArr.contains(object)) return false; 
		else{
			removeHelper(root, object);
			return true;
		}
	}

	// A helper method for remove() using in-order traversal (inspired by Wyatt's traversalHelper()).
	public void removeHelper(Node pointer, Item object){
		if (pointer instanceof PointRegionQuadtree.LeafNode){
			LeafNode leaf = (LeafNode) pointer;
			if (leaf.data.equals(object)){
				// Replace the leaf with an EmptyNode. Due to Java's class requirements, this requires going to 
				// the leaf's parent in order to conduct a deletion. 
				InternalNode parentPointer = (InternalNode) leaf.parent; // All parent nodes in practice should be Internal Nodes
				if (parentPointer.upperLeft.equals(leaf)) {
					parentPointer.upperLeft = new EmptyNode(parentPointer);
				}
				if (parentPointer.upperRight.equals(leaf)) {
					parentPointer.upperRight = new EmptyNode(parentPointer);
				}
				if (parentPointer.lowerLeft.equals(leaf)) {
					parentPointer.lowerLeft = new EmptyNode(parentPointer);
				}
				if (parentPointer.lowerRight.equals(leaf)) {
					parentPointer.lowerRight = new EmptyNode(parentPointer);
				}
				numLeaves--;
			}
		}

		else if (pointer instanceof PointRegionQuadtree.InternalNode){
			InternalNode cell = (InternalNode) pointer;		
			removeHelper(cell.upperLeft, object);
			removeHelper(cell.upperRight, object);
			removeHelper(cell.lowerLeft, object);
			removeHelper(cell.lowerRight, object);
		}
	}

	// Returns the object at a given location if one exists. Otherwise, returns null
	// - Wyatt's
	public Item get(double xcoord, double ycoord){
		Node closestNode = getHelper(this.root, null, xcoord, ycoord);
		if (closestNode instanceof PointRegionQuadtree.LeafNode){
			LeafNode leaf = (LeafNode) closestNode;
			return leaf.data;
		}
		return null;
	}

	//Returns the node at a given location if one exists. Otherwise, returns the internal node that bounds that location.
	public Node getHelper(Node curNode, Node prevNode, double xcoord, double ycoord){
		if (curNode instanceof PointRegionQuadtree.EmptyNode){
			return prevNode;
		}
		else if (curNode instanceof PointRegionQuadtree.LeafNode){
			LeafNode leaf = (LeafNode) curNode;
			//If this leaf node is at the search coordinates, then return its data
			if (leaf.xcoord == xcoord && leaf.ycoord == ycoord){
				return leaf;
			}
			//If this leaf node isn't, then where the sought-after leaf node ought to be, there is nothing, so return null
			else{
				return prevNode;
			}
		}
		//if we're at an internal node...
		Node nextNode = null;
		InternalNode cell = (InternalNode) curNode;
		if (cell.box.upperLeftBox().inBox(xcoord,ycoord)){
			nextNode = getHelper(cell.upperLeft,cell,xcoord,ycoord);
		}
		else if (cell.box.upperRightBox().inBox(xcoord,ycoord)){
			nextNode = getHelper(cell.upperRight,cell,xcoord,ycoord);
		}
		else if (cell.box.lowerLeftBox().inBox(xcoord,ycoord)){
			nextNode = getHelper(cell.lowerLeft,cell,xcoord,ycoord);
		}
		else if (cell.box.lowerRightBox().inBox(xcoord,ycoord)){
			nextNode = getHelper(cell.lowerRight,cell,xcoord,ycoord);
		}
		return nextNode;
	}

	// Returns the approximately closest object to a given location. 
	// The higher the exhaustiveness, the more likely it is to return the truly closest object
	// The exhaustiveness represents each additional box above the box in which the coordinates are that you check
	// - Wyatt's
	public Item closestObject(double xcoord, double ycoord, int exhaustiveness){
		//searchNode is the node under which you check every point
		Node searchNode = getHelper(this.root, null, xcoord, ycoord);
		//If searchNode is a leaf, return that leaf's data
		if (searchNode instanceof PointRegionQuadtree.LeafNode){
			LeafNode leaf = (LeafNode) searchNode;
			return leaf.data;
		}

		//Move searchNode up the tree a number of levels equal to the exhaustiveness of the search
		while (exhaustiveness>0 && searchNode.parent != null){
			exhaustiveness --;
			searchNode = searchNode.parent;
		}

		//leafNodes is the list of all leafNodes below the searchNode
		ArrayList<LeafNode> leafNodes = new ArrayList<LeafNode>();
		traversalHelper(searchNode, leafNodes);

		//For each leafNode, calculate the distance to (xcoord,ycoord), in order to find the minimum distance
		double minDistance = Double.POSITIVE_INFINITY;
		LeafNode closestNode = null;

		for (LeafNode leaf: leafNodes){
			double dist = Math.hypot(xcoord-leaf.xcoord, ycoord-leaf.ycoord);
			if (dist < minDistance){
				closestNode = leaf;
				minDistance = dist;
			}
		}

		//return the closestNode's data
		return closestNode.data;
	}

	// Returns an ArrayList of all objects within a given distance from a location.
	// - Wyatt's
	public ArrayList<Item> withinDistance(double xcoord, double ycoord, double radius){
		//Find the node either at the coordinates, or that bounds the coordinates
		Node getHelped = getHelper(this.root, null, xcoord, ycoord);
		//If that node is a leafNode, go up to its parent
		if (getHelped instanceof PointRegionQuadtree.LeafNode){
			getHelped = getHelped.parent;
		}
		//Type cast that parent to an InternalNode (so we can access its bounding box)
		InternalNode searchNode = (InternalNode) getHelped;

		//Move searchNode up the tree until you hit a boundingBox that has a width/height > radius
		while (searchNode.parent != null){
			// The approximation behind this logic is:
			// if the width and height of the box you are searching is greater than the radius,
			// there is a good chance any object within that radius will also be within the box
			if (searchNode.box.width > radius && searchNode.box.height > radius){
				System.out.println(searchNode.box.width + " " + searchNode.box.height + ", radius:" + radius);
				break;
			}
			searchNode = (InternalNode) searchNode.parent;
		}
		//leafNodes is the list of all leafNodes below the searchNode
		ArrayList<LeafNode> leafNodes = new ArrayList<LeafNode>();
		traversalHelper(searchNode, leafNodes);

		//make an arrayList of outputs. Then add the data of every leaf below searchNode that is within the radius.
		ArrayList<Item> output = new ArrayList<Item>();

		for (LeafNode leaf: leafNodes){
			double dist = Math.hypot(xcoord-leaf.xcoord, ycoord-leaf.ycoord);
			if (dist <= radius){
				output.add(leaf.data);
			}
		}

		//return that list
		return output;
	}

	// Returns an ArrayList of all the objects in the tree
	// - Wyatt's
	public ArrayList<Item> traversal(){
		ArrayList<LeafNode> leafNodes = new ArrayList<LeafNode>();
		traversalHelper(this.root, leafNodes);
		ArrayList<Item> items = new ArrayList<Item>();
		for (LeafNode leaf: leafNodes){
			items.add(leaf.data);
		}
		return items;
	}

	//Starting from some InternalNode, this adds all the leafNodes below it to some preexisting arraylist
	public void traversalHelper(Node curNode, ArrayList<LeafNode> leafNodes){
		//base case: if you're at a leaf, add it to the list
		if (curNode instanceof PointRegionQuadtree.LeafNode){
			LeafNode leaf = (LeafNode) curNode;
			leafNodes.add(leaf);
		}
		//if we're at an internal node, call the helper on all children nodes
		else if (curNode instanceof PointRegionQuadtree.InternalNode){
			InternalNode cell = (InternalNode) curNode;		
			traversalHelper(cell.upperLeft,leafNodes);
			traversalHelper(cell.upperRight,leafNodes);
			traversalHelper(cell.lowerLeft,leafNodes);
			traversalHelper(cell.lowerRight,leafNodes);
		}
	}

	public static void main(String[] args){
		PointRegionQuadtree test = new PointRegionQuadtree<Integer>(0.0,5.0,0.0,16.0);
		assert test.isEmpty() == true;
		test.insert(0,5.0,5.0);
		test.insert(1,4.0,4.0);
		test.insert(2,3.0,3.0);
		test.insert(3,2.0,2.0);
		test.insert(4,1.0,1.0);
		test.insert(5,1.0,1.1);
		assert test.size() == 6;
		assert test.isEmpty() == false;


		System.out.println(test.root.toString());
		System.out.println(test.withinDistance(1,1.11,1).toString());

	}	
}