package mapnyc;

import java.util.ArrayList;

// this is a point-region quadtree, based off https://www.cs.cmu.edu/~ckingsf/bioinfo-lectures/quadtrees.pdf
public class PointRegionQuadtree<Item> implements Quadtree<Item>{

	// Instance variables
	public int numLeaves;
	public int numInternalNodes;
	public InternalNode root;

	// Constructor
	public PointRegionQuadtree(double minX,double maxX,double minY,double maxY){
		this.numLeaves = 0;
		this.numInternalNodes = 1;
		this.root = new InternalNode(minX,maxX,minY,maxY);
	}
	public PointRegionQuadtree(BoundingBox box){
		this.numLeaves = 0;
		this.numInternalNodes = 1;
		this.root = new InternalNode(box);
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

	// Returns true if the Quadtree is empty.  
	public boolean isEmpty(){
		return this.numLeaves == 0;
	}

	// Returns the number of non-empty nodes.
	public int size(){
		return this.numLeaves;
	}

	// Adds an object to the tree given a place to put it.
	// Ensures that no node has more than 4 branches.
	// Returns true if the addition is successful and false if the target location 
	// is already occupied.
	// If successful, increases the Quadtree's size.
	// - Andrew's ---

	public boolean insert(Item object, double xcoord, double ycoord){
		//root.upperLeft = new InternalNode(0,10,0,5);
		if (!root.box.inBox(xcoord,ycoord)){
			System.out.println("("+xcoord +", " + ycoord +")"+" not in region");
			return false;
		}
		root = (InternalNode) insertHelper((Node) root, object, xcoord, ycoord,root.box);
		return true;
	}

	public Node insertHelper(Node curNode,Item object, double xcoord,double ycoord,BoundingBox box){
		//the syntax "PointRegionQuadtree.EmptyNode" was explained to me by Claude.ai, as I was getting an error regarding generics
		//base case:
		if (curNode instanceof PointRegionQuadtree.EmptyNode){
			numLeaves ++;
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
				//this should never happen
				System.out.println("something broke");
			}

			return cell;
		}
		//if your at a leaf, crack it open---make a new internalnode, then insert the displaced and new data into it
		else if (curNode instanceof PointRegionQuadtree.LeafNode){
			//create a new internal node
			InternalNode newInternalNode = new InternalNode(box);
			numInternalNodes ++;
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
	// - Wyatt's
	public Item get(double xcoord, double ycoord){
		return getHelper(this.root, xcoord, ycoord);
	}

	public Item getHelper(Node curNode, double xcoord, double ycoord){
		if (curNode instanceof PointRegionQuadtree.EmptyNode){
			return null;
		}
		else if (curNode instanceof PointRegionQuadtree.LeafNode){
			LeafNode leaf = (LeafNode) curNode;
			return leaf.data;
		}
		//if we're at an internal node...
		Item data = null;
		InternalNode cell = (InternalNode) curNode;
		if (cell.box.upperLeftBox().inBox(xcoord,ycoord)){
			data = getHelper(cell.upperLeft,xcoord,ycoord);
		}
		else if (cell.box.upperRightBox().inBox(xcoord,ycoord)){
			data = getHelper(cell.upperRight,xcoord,ycoord);
		}
		else if (cell.box.lowerLeftBox().inBox(xcoord,ycoord)){
			data = getHelper(cell.lowerLeft,xcoord,ycoord);
		}
		else if (cell.box.lowerRightBox().inBox(xcoord,ycoord)){
			data = getHelper(cell.lowerRight,xcoord,ycoord);
		}
		return data;
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

	// Returns an ArrayList of all the objects in the tree
	// - Wyatt's
	public ArrayList<Item> traversal(){
		ArrayList<Item> items = new ArrayList<Item>();
		traversalHelper(root, items);
		return items;
	}
	public void traversalHelper(Node curNode, ArrayList<Item> items){
		if (curNode instanceof PointRegionQuadtree.LeafNode){
			LeafNode leaf = (LeafNode) curNode;
			items.add(leaf.data);
		}
		//if we're at an internal node...
		else if (curNode instanceof PointRegionQuadtree.InternalNode){
			InternalNode cell = (InternalNode) curNode;		
			traversalHelper(cell.upperLeft,items);
			traversalHelper(cell.upperRight,items);
			traversalHelper(cell.lowerLeft,items);
			traversalHelper(cell.lowerRight,items);
		}
	}

	public static void main(String[] args){
		PointRegionQuadtree test = new PointRegionQuadtree<Integer>(0,16,0,16);
		test.insert(0,5,5);
		test.insert(0,4,4);
		test.insert(0,3,3);
		test.insert(0,2,2);
		test.insert(0,1,1);
		test.insert(0,3.00003,1);
		test.insert(0,.5,.5);
		test.insert(0,10,5);
		test.insert(0,11,5);
		test.insert(0,12,5);
		test.insert(0,13,5);
		test.insert(0,13.0000002,5);
		test.insert(0,13.00001,5);
		
		ArrayList<Integer> ints = test.traversal();
		System.out.println(ints.toString());
		System.out.println(test.get(135,5));
		System.out.println(test.numInternalNodes);
	}	
}