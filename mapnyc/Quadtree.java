package mapnyc;

import java.util.ArrayList;

// this is a point-region quadtree, based off https://www.cs.cmu.edu/~ckingsf/bioinfo-lectures/quadtrees.pdf
public class PointRegionQuadtree<Item> implements Quadtree<Item>{

	// Instance variables
	private int numNodes;
	public InternalNode root;

	// Constructor
	public PointRegionQuadtree(double minX,double maxX,double minY,double maxY){
		this.numNodes = 0;
		this.root = new InternalNode(minX,maxX,minY,maxY);
	}

	public class Node{
	}

	// Inner Node Class - copied from lab 6 and adapted.
	public class InternalNode extends Node{
		public Node upperLeft;
		public Node upperRight;
		public Node lowerLeft;
		public Node lowerRight;
		public BoundingBox box;

		// Inner Node constructor;
		public InternalNode(double minX,double maxX,double minY,double maxY){
			this.upperLeft = new EmptyNode();
			this.upperRight = new EmptyNode();
			this.lowerLeft = new EmptyNode();
			this.lowerRight = new EmptyNode();
			this.box = new BoundingBox(minX,maxX,minY,maxY);
		}

		public InternalNode(BoundingBox box){
			this.upperLeft = new EmptyNode();
			this.upperRight = new EmptyNode();
			this.lowerLeft = new EmptyNode();
			this.lowerRight = new EmptyNode();
			this.box = box;
		}
	}

	public class LeafNode extends Node{
		public Item data;
		public double xcoord;
		public double ycoord;

		public LeafNode(Item data, double xcoord, double ycoord){
			this.data = data;
			this.xcoord = xcoord;
			this.ycoord = ycoord;
		}
		public String toString(){
			return "("+xcoord + ","+ycoord +")-->" + data; 
		}
	}

	public class EmptyNode extends Node{} 

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
	// - Andrew's ---

	public boolean insert(Item object, double xcoord, double ycoord){
		//root.upperLeft = new InternalNode(0,10,0,5);
		root = (InternalNode) insertHelper((Node) root, object, xcoord, ycoord,root.box);
		return true;
	}

	public Node insertHelper(Node curNode,Item object, double xcoord,double ycoord,BoundingBox box){
		//the syntax "PointRegionQuadtree.EmptyNode" was explained to me by Claude.ai, as I was getting an error regarding generics
		//base case:
		if (curNode instanceof PointRegionQuadtree.EmptyNode){
			return new LeafNode(object,xcoord,ycoord);
		}
		//if you're at a internalnode, call insert helper on the correct subtree
		if (curNode instanceof PointRegionQuadtree.InternalNode){
			InternalNode cell = (InternalNode) curNode;

			//upperleft
			if (box.upperLeftBox().inBox(xcoord,ycoord)){
				cell.upperLeft = insertHelper(cell.upperLeft,object,xcoord,ycoord,box.upperLeftBox());
			}
			//upperright		
			else if (box.upperRightBox().inBox(xcoord,ycoord)){
				cell.upperRight = insertHelper(cell.upperRight,object,xcoord,ycoord,box.upperRightBox());
			}
			//lowerleft
			else if (box.lowerLeftBox().inBox(xcoord,ycoord)){
				cell.lowerLeft = insertHelper(cell.lowerLeft,object,xcoord,ycoord,box.lowerLeftBox());
			}
			//lowerright
			else if (box.lowerRightBox().inBox(xcoord,ycoord)){
				cell.lowerRight = insertHelper(cell.lowerRight,object,xcoord,ycoord,box.lowerRightBox());
			}
			else{
				System.out.println("your coordinates" + xcoord + ","+ycoord+" are right on a edge of regions");
			}

			return cell;
		}
		//if your at a leaf, crack it open---make a new internalnode, then insert the displaced and new data into it
		else if (curNode instanceof PointRegionQuadtree.LeafNode){
			//create a new internal node
			InternalNode newInternalNode = new InternalNode(box);
			//temporarily store the old information
			LeafNode oldLeaf = (LeafNode) curNode;
			//check for a "collision"
			if (oldLeaf.xcoord ==xcoord && oldLeaf.ycoord==ycoord){
				System.out.println("the coordinates are already taken!--"+xcoord + ","+ycoord);
			}
			// add old object to the new internal node
			newInternalNode = (InternalNode) insertHelper(newInternalNode,oldLeaf.data,oldLeaf.xcoord,oldLeaf.ycoord,box);
			// add new object to the new internal node
			newInternalNode = (InternalNode) insertHelper(newInternalNode,object,xcoord,ycoord,box);

			return newInternalNode;
		}

		else{
			System.out.println("something broke");
			return null;
		}
	}

	// Removes an object from the tree. Returns true if the removal is successful
	// and false if the object was not present in the tree.
	// If successful, decreases the Quadtree's size.
	// - Andrew's
	public boolean remove(Item object){
		return false;
	}

	// Returns the object at a given location if one exists. Returns null otherwise.
	// - Wyatt's. A variation on the get method for binary search trees, as written by Prof. Keith on slide "30-wrap-up"
	public Item get(double xcoord, double ycoord){
		return getHelper(this.root, xcoord, ycoord);
	}

	public Item getHelper(Object x, double xcoord, double ycoord){
		/*
		//base case
		if (x ==null){return null;}
		//determine the next subquadrant to search
		Node subQuadrant = getQuadrant(x, xcoord, ycoord);
		//if that subquadrant is x, that means the search coords are smack dab on the coords of x, so return x's data
		if (subQuadrant ==x){
			return x.data;
		}
		//else, recursively call getHelper to look through the subquadrant
		else{
			return getHelper(subQuadrant, xcoord, ycoord);
		}
		*/
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
		PointRegionQuadtree test = new PointRegionQuadtree<Integer>(0,16,0,16);
		test.insert(0,2,9);
		test.insert(0,2,6);
		test.insert(0,1,7);
		//test.insert(0,6,8);
		//test.insert(0,9,6);
		//test.insert(0,8,1);
		//test.insert(0,7.5,1);
		//test.insert(0,2,8);

		//System.out.println(test.root.lowerLeft);
		//System.out.println(((PointRegionQuadtree.InternalNode)test.root.upperLeft).upperLeft);
		//System.out.println(((PointRegionQuadtree.InternalNode)(((PointRegionQuadtree.InternalNode)test.root.upperLeft).lowerLeft)).upperLeft);
		//System.out.println(((PointRegionQuadtree.InternalNode)test.root.upperLeft).lowerRight);
		//System.out.println(((PointRegionQuadtree.InternalNode)test.root.upperLeft).lowerLeft);

		//System.out.println(((PointRegionQuadtree.InternalNode)((PointRegionQuadtree.InternalNode)(((PointRegionQuadtree.InternalNode)test.root.lowerLeft).upperLeft)).upperLeft).lowerRight);
		//System.out.println(test.root.lowerRight);
		//	System.out.println(((PointRegionQuadtree.InternalNode)test.root.lowerRight).lowerLeft);
	}	
}
