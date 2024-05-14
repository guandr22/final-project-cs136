# Final Project: Mapping New York City
By Wyatt Smith and Andrew Gu, in Prof. Katie Keith's CSCI 136 class.

## Overview
We used Point-Region (PR) Quadtrees to organize data from New York City's [PLUTO dataset](https://www.nyc.gov/site/planning/data-maps/open-data/dwn-pluto-mappluto.page), a database of over 850,000 taxplots (roughly corresponding to individual buildings) in NYC and information on each taxplot's location, address, land use type, owner, and more. We answered two-dimensional spatial queries, such as "Where is the nearest parking structure to a given point?", and generated maps to make PLUTO's wealth of data more legible to our users.

## Quadtree ADT
Our interface resembles a two-dimensional extension of the Symbol Table, with isEmpty(), size(), insert(), remove(), and get(), as our project associates points in space with data. We also included the space-specific functions closestObject() and withinDistance().
In regards to naming, we understand that Quadtrees are a data structure rather than an ADT, but in our research on Quadtrees, we didn't find a name for an ADT that matched up with the functions we wanted to implement, so we named our ADT after the general Quadtree data structure and named our implementation, PointRegionQuadtree after the specific type of Quadtree we used.

## Point-Region Quadtree implementation and BoundingBox.java
A Quadtree is a tree designed for 2-dimensional space in which each Node links to up to four children. Traversing and adding to a Quadtree operates along similar lines to a Binary Search Tree, only with two "key" factors rather than one. For example, in a BST, if I were to add a Node with the key 58 to a tree with a root whose key was 20, I would compare my Node's key with the root's key, and as 58>20, I would move to the root's right child. In one type of Quadtree, a Point Quadtree, I would compare a Node's x and y coordinates to a root Node's coordinates. For example, if I wanted to add a Node at (5, 3) to a tree whose root coordinate was (6, 2), seeing that my Node was to the left (with a lower x value) of and above (with a higher y value) the root, I would navigate to the root's upper-left child.
We implemented a Point-Region Quadtree, which based coordinate comparisons around equally-sized Bounding Boxes, or quadrants. For example, if (0, 0) is the center of a space, then we would access a point at (1, -2) by moving to the child corresponding to the space's bottom-right quadrant.
See explanations of Point-Region and Point Quadtrees starting at slides 37 and 13, respectively, of [this presentation](https://www.cs.cmu.edu/~ckingsf/bioinfo-lectures/quadtrees.pdf) from CMU. See an [excellent interactive explanation of a Point-Region Quadtree by Jim Kang](https://jimkang.com/quadtreevis/). 

We chose a PR Quadtree in part due to Jim Kang's suggestion that PR Quadtrees were perfect for identifying nearest neighbors by excluding certain regions from being possible nearest neighbors, but in practice, the closestObject() and withinDistance() functions as we implemented them were not guaranteed to return nearest neighbors or all of the objects within a given distance, as points under the same subtrees were not guaranteed to be closer than points in other subtrees (see an example of this on slide 7 of [our presentation](https://docs.google.com/presentation/d/1Zk5axN5m-yCPuxg8xJpuzqym7CFcBICT97DbWptIQD8/edit?usp=sharing). Instead, we implemented an "exhaustiveness" level (n), wherein we checked every node within n layers of the quadtree. Jim Kang linked an implementation that guaranteed finding a nearest point, but looking at that code would have broken the 50-foot rule, and so we made do without using anyone else's algorithms. In the context of our New York City map, the closestObject() being somewhat probabilistic wasn't all that much of a problem given how densely-packed objects were; the difference between a close object and the closest object usually wouldn't be that noticeable to a pedestrian on the ground. 

Note: Implementing withinDistance() and in-order-traversal involved using ArrayLists.

PR Quadtrees are also distinguished by having three types of nodes (see the CMU presentation): 
1. Internal Nodes, which for us used Bounding Boxes, organize space with their children but do not contain data points themselves.
2. Leaf Nodes, which contain points with data.
3. Empty Nodes, which denote a quadrant of empty space.

This organization highlights one of the benefits of Quadtrees compared to, for example, two-dimensional arrays, especially in less dense datasets; a single Empty Node can store a huge amount of empty space that could take many, many cells of a two-dimensional array. 
Our particular implementation, however, which involved treating Internal Nodes, Leaf Nodes, and Empty Nodes as subclasses of a broader Node class, did not gel very well with Java's class declaration requirements; many of our approximately 1,000 lines of code are class checks and declarations. We overcame the problems that resulted with the power of trial-and-error and liberal copying and pasting, but in retrospect, this may have been much simpler if we stored Internal Nodes, Leaf Nodes, and Empty Nodes all as Nodes with certain instance variables being null (for example, giving Internal Nodes exampleNode.data = null).

## The Dataset and TaxPlot.java
The dataset itself, pluto_24_v1_1, was a CSV file produced by the NYC Department of City Planning, with 858,578 entries, each containing a taxplot (a small area of land usually corresponding to a building) and 91 variables per entry, including x and y coordinates alongside latitude and longitude. The dictionary for making sense of all of those variables is available [here](https://s-media.nyc.gov/agencies/dcp/assets/files/pdf/data-tools/bytes/pluto_datadictionary.pdf). The csv is too large to put in GitHub Classroom directly, so it's available on the NYC Department of City Planning website [here](https://www.nyc.gov/site/planning/data-maps/open-data/dwn-pluto-mappluto.page).
We did not clean the csv directly. Instead, Wyatt reduced the number of variables to 11 and processed each line of the csv in TaxPlot.java through some very creative finagling given the idiosyncracies of some of the data's formatting, but he ended up needing to throw out around 20,000 entries as corrupted (usually because of some unfortunate commas).

## PLUTO.java
PLUTO.java makes all of our work legible to users. We used Hashtables, ArrayLists, PR Quadtrees, and the graphics packages from Lab 2 to store and map the dataset.

In order to run PLUTO.java, download and move the file "pluto_24v1_1.csv" into the mapnyc directory. 

Before you compile and run this program, make sure you are in the same directory this `README.md` lives in on your terminal.

First, make a bin directory where Java will store and read the .class files.

```
mkdir bin
```
Then run the following command to compile your programs 

```
javac -d bin mapnyc/*.java
```
java -cp bin mapnyc.PLUTO
```

You will be prompted with four features to choose from.

## Sources
https://www.cs.cmu.edu/~ckingsf/bioinfo-lectures/quadtrees.pdf
https://people.scs.carleton.ca/~maheshwa/courses/5703COMP/16Fall/quadtrees-paper.pdf
https://people.scs.carleton.ca/~maheshwa/courses/5703COMP/16Fall/quadtrees-paper.pdf
https://jimkang.com/quadtreevis/
