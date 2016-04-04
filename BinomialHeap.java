package BinomialHeap;
import java.util.HashSet;

/**
 * BinomialHeap
 *
 * An implementation of binomial heap for non-negative integers.
 * 
 */
public class BinomialHeap
{
	int size;
	HeapNode root; //the root of the smallest key. This is the minimum of the heap. 
													//It is not necessarily the smallest order tree
	
	public BinomialHeap(){
		this.size = 0;
		this.root = null;
	}
	public BinomialHeap(HeapNode root){
		this.size = 1;
		this.root = root;
	}

   /**
    * public boolean empty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
    public boolean empty()
    {
    	return (this.root == null); //if the smallest key tree is null than the whole tree is empty
    }
	
   /**
    * public void insert(int value)
    *
    * Insert value into the heap 
    *
    */
    public void insert(int value) 
    {    
    	HeapNode newNode = new HeapNode(value);
    	if (this.empty()) {
    		this.root = newNode;
    		size++; 
    	} else {
    	    BinomialHeap newSingeltonHeap = new BinomialHeap(newNode);
    	    this.meld(newSingeltonHeap);
    	    return;
    	}
    }

   /**
    * public void deleteMin()
    *
    * Delete the minimum value
    *
    */
    public void deleteMin()
    {
    	if (this.empty()) {
    		return;
    	}
    	this.size--;
    	//if this is the last node in the heap:
    	if (size == 0) { 
    		this.root = null;
    		return;
    	}
    	HeapNode nodeToDelete = this.root;
    	
    	//find minimum of the deleted root's sons
    	HeapNode newMinSons = null;
    	if (nodeToDelete.leftMostSon != null) {
    		HeapNode currentSon = nodeToDelete.leftMostSon;
        	newMinSons = currentSon;
        	while (currentSon.rightBrother != null) {
        		if (currentSon.rightBrother.key < newMinSons.key) {
        			newMinSons = currentSon.rightBrother;
        		}
        		currentSon = currentSon.rightBrother;
        	}
    	} else {
    		//the deleted node had no sons
    	}
    	
    	//find new minimum in remaining old roots
    	HeapNode newMinOldRoots = null;
    	//find the right-most root in the linked list of roots
    	HeapNode rightMostRoot = this.rightMostRoot();
    	
    	//traverse the linked list of old roots to find the new minimum among them
    	if (rightMostRoot.leftBrother != null) {
    		if (rightMostRoot == nodeToDelete) {
    			newMinOldRoots = rightMostRoot.leftBrother;
    		} else {
    			newMinOldRoots = rightMostRoot;
    		}
    		HeapNode currentNode = newMinOldRoots;
    		while (currentNode.leftBrother != null) {
        		if (currentNode.leftBrother.key < newMinOldRoots.key && currentNode.leftBrother != nodeToDelete) {
        			newMinOldRoots = currentNode.leftBrother;
        		}
        		currentNode = currentNode.leftBrother;
        	}
    	} else {
    		//the heap had only one tree
    	}
    	
    	//meld deleted root's sons with remaining roots
    	if (newMinSons != null && newMinOldRoots != null) {
    		BinomialHeap sonsHeap = new BinomialHeap(newMinSons);
    		BinomialHeap remainingRootsHeap = new BinomialHeap(newMinOldRoots);
    		sonsHeap.meld(remainingRootsHeap);
    		this.root = sonsHeap.root;
    	}
    	else if (newMinSons != null && newMinOldRoots == null) {
    		this.root = newMinSons;
    	}
    	else if (newMinOldRoots != null && newMinSons == null) {
    		this.root = newMinOldRoots;
    	}
    	//go over the linked list of roots from right to left and make sure none of them has a pointer to a father
    	//at the same time, disconnect the pointers from the brothers of the deleted root to the deleted root 
    	HeapNode rightMostNode = this.rightMostRoot();
    	HeapNode currentRoot = rightMostNode;
    	while (currentRoot != null) {
    		currentRoot.father = null;
    		if (currentRoot != nodeToDelete) {
    			if (currentRoot.leftBrother == nodeToDelete) {
    				currentRoot.leftBrother = nodeToDelete.leftBrother;
    			}
    			if (currentRoot.rightBrother == nodeToDelete) {
    				currentRoot.rightBrother = nodeToDelete.rightBrother;
    			}    			
    		}
    		currentRoot = currentRoot.leftBrother;
    	}
    }

   /**
    * public int findMin()
    *
    * Return the minimum value
    *
    */
    public int findMin()
    {
    	return this.root.key;
    } 
    
   /**
    * public void meld (BinomialHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld (BinomialHeap heap2)
    {
    	//deal with empty heaps
    	if (this.empty() && heap2.empty()) {
    		return;
    	} else if (!this.empty() && heap2.empty()) {
    		return;
    	} else if (this.empty() && !heap2.empty()) {
    		this.root = heap2.root;
    		this.size = heap2.size;
    		return;
    	}
    	
    	this.size = this.size + heap2.size;
    	
    	//first: union the two heaps
    	HeapNode newHead = unionAll(this, heap2);
    	
    	this.root = newHead; //change the heap in-place by re-routing the pointer from the previous root to the new root
        heap2.root = null; //disconnect the pointer to the second heap
        
        //second: deal with the duplicates (link trees of same order)
        HeapNode previous = null;
        HeapNode current = this.rightMostRoot();
        HeapNode next = current.leftBrother;
    	
    	//traverse the linked list of roots and link trees that are of the same order
    	  while (next != null) {
              if (current.rank != next.rank || (next.leftBrother != null &&
                      next.leftBrother.rank == current.rank)) { 
            	//if the current root and the next one are not the same order, move on
            	  previous = current;
                  current = next;
              } else { //the current root and its left brother are of the same order
                  if (current.key <= next.key) { //if the current root's key is smaller
                      current.linkTrees(next); //link the two trees
                  } else { //the next root's key is the smaller
                      //link the two trees
                      next.linkTrees(current);
                      //move on
                      current = next;
                  }
              }
              //make sure we don't point to a deleted root
              if (current.leftBrother != null) {
            	  next = ((current.leftBrother.key < this.root.key) ? current.leftBrother.leftBrother : current.leftBrother);
              } else {
            	  next = current.leftBrother;
              }
          }
    }
    
    //this method union two heaps to one heap with possibly more than one tree per order
    private HeapNode unionAll(BinomialHeap heap1, BinomialHeap heap2) {
    	
    	//if one of the heaps is actually empty, return the other heap's root
    	if (heap1.empty()) {
            return heap2.root;
        } else if (heap2.empty()) {
            return heap1.root;
            
        } else {
        	HeapNode newRoot;
        	
        	//decide which root has the smaller key, and it will be the new, united heap's root
        	if (heap1.root.key <= heap2.root.key) {
        		newRoot = heap1.root;
            } else {
            	newRoot = heap2.root;
            }
        	
        	HeapNode heap1MinRankRoot = heap1.minRankRoot();
        	HeapNode heap2MinRankRoot = heap2.minRankRoot();
        	HeapNode heap1Next = heap1MinRankRoot;
        	HeapNode heap2Next = heap2MinRankRoot;
        	
        	HeapNode lastRoot = null;
        	if (heap1MinRankRoot.rank <= heap2MinRankRoot.rank) {
        		lastRoot = heap1MinRankRoot;
        		heap1Next = heap1Next.leftBrother;
        	} else {
        		lastRoot = heap2MinRankRoot;
        		heap2Next = heap2Next.leftBrother;
        	}
        	
            while (heap1Next != null && heap2Next != null) { //as long as there are more roots in both roots lists 
            	//go over both roots lists and add roots to the new heap list in order 
                if (heap1Next.rank <= heap2Next.rank) {
                	lastRoot.leftBrother = heap1Next;
                	lastRoot.leftBrother.rightBrother = lastRoot;
                    heap1Next = heap1Next.leftBrother;
                } else {
                	lastRoot.leftBrother = heap2Next;
                	lastRoot.leftBrother.rightBrother = lastRoot;
                    heap2Next = heap2Next.leftBrother;
                }
                lastRoot = lastRoot.leftBrother;
            }
            
            //find out which list still has roots left and connect them to the new heap's roots list 
            if (heap1Next != null) {
            	lastRoot.leftBrother = heap1Next;
            	lastRoot.leftBrother.rightBrother = lastRoot;
            } else {
            	lastRoot.leftBrother = heap2Next;
            	lastRoot.leftBrother.rightBrother = lastRoot;
            }
            return newRoot;
        }
    }
    
    private HeapNode rightMostRoot() {
    	HeapNode currentRoot = this.root;
    	if (this.root == null) {
    		return null;
    	}
    	while (currentRoot.rightBrother != null) {
    		currentRoot = currentRoot.rightBrother;
    	}
    	return currentRoot;
    }
    
    private HeapNode minRankRoot()
    {
    	//find the root with the minimum rank
    	HeapNode currentRoot = this.rightMostRoot();
    	HeapNode currentMinRank = currentRoot;
    	while (currentRoot.leftBrother != null) { 
    		if (currentMinRank.leftBrother.rank < currentMinRank.rank) {
    			currentMinRank = currentRoot.leftBrother;
    		}
    		currentRoot = currentRoot.leftBrother;
    	}
    	return currentMinRank;
    }
    
   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size()
    {
    	return this.size;
    }
    
   /**
    * public int minTreeRank()
    *
    * Return the minimum rank of a tree in the heap.
    * 
    */
    public int minTreeRank()
    {
    	boolean[] arr = this.binaryRep();
    	boolean[] revArr = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
        	revArr[i] = arr[arr.length-1-i];
        }
        int currentRank = 0;
        while (!revArr[currentRank]) {
        	currentRank++;
        }
        return currentRank;
    }
	
	   /**
    * public boolean[] binaryRep()
    *
    * Return an array containing the binary representation of the heap.
    * 
    */
    public boolean[] binaryRep()
    {
    	String strRep = Integer.toBinaryString(this.size);
    	boolean[] arr = new boolean[strRep.length()];
    	for (int b = 0; b < strRep.length(); b++) {
    		if (Character.getNumericValue(strRep.charAt(b)) == 1) {
    			arr[b] = true;
    		} else {
    			arr[b] = false;
    		}
    	}
        return arr;  	
    }

   /**
    * public void arrayToHeap()
    *
    * Insert the array to the heap. Delete previous elements in the heap.
    * 
    */
    public void arrayToHeap(int[] array)
    {
        this.reset();
         for (int j : array) {
    	     this.insert(j);
         }   
    }
    private void reset(){
    	this.root = null;
    	this.size = 0;
    }
	
   /**
    * public boolean isValid()
    *
    * Returns true if and only if the heap is valid.
    *   
    */
    public boolean isValid() 
    {
    	//make sure there is no more than one tree for each order
    	HashSet<Integer> orderValues = new HashSet<>();
    	HeapNode current = this.rightMostRoot();
    	while (current != null) {
    		if (orderValues.contains(current.rank)) {
    			return false;
    		} else {
    			orderValues.add(current.rank);
    		}
    		//make sure each tree is a valid binomial tree: sons have larger keys than parent
    		if (binomialTreeValidation(current) == false) {
    			return false;
    		}
    		current = current.leftBrother;
    	}
    		
    	return true;
    }
    //helper function: traverse a binomial tree and validate each son's key is no less than the parent's key
    private boolean binomialTreeValidation(HeapNode root) {
    	//first check if there are any sons at all
    	if (root.leftMostSon != null) {
    	    HeapNode father = root;
    	    int fatherKey = father.key;
    	    HeapNode leftSon = root.leftMostSon;
    	    int leftSonKey = leftSon.key;
    	    if (leftSonKey < fatherKey) {
    		    return false;
    	    } else {
    		    while (leftSon.rightBrother != null) {
    			    if (leftSon.rightBrother.key < fatherKey) {
    				    return false;
    			    } else {
    				    binomialTreeValidation(leftSon.rightBrother);
    			    }
    			    leftSon = leftSon.rightBrother;
    		    }
    	    }
    	    return true;  
    	} else {
    		return true;
    	}
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than BinomialHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    
    public class HeapNode{
    	
  	    private int key;
  	    private int rank;
  	    private HeapNode father;
  	    private HeapNode leftMostSon;
  	    private HeapNode rightBrother;
  	    private HeapNode leftBrother;
  	
  	    public HeapNode(int key) {
  		    this.key = key;
  		    this.rank = 0;
  		    this.father = null;
  		    this.leftMostSon = null;
  		    this.rightBrother = null;
  		    this.leftBrother = null;
  	    }
  	    //a set of getters for the node's properties
  	    public int key() { return this.key; }
  	    public int order() { return this.rank; }
  	    public HeapNode father() { return this.father; }
  	    public HeapNode leftMostSon() { return this.leftMostSon; }
  	    public HeapNode rightBrother() { return this.rightBrother; }
  	    public HeapNode leftBrother() { return this.leftBrother; }
  	
  	    public void linkTrees(HeapNode root2) {
  	    	if (this.rank == root2.rank) {
  	    		HeapNode newFather;
  	    		HeapNode newSon;
  	    		if (this.key < root2.key) { //this should be the father of root2
  	    			newFather = this;
  	    			newSon = root2;
  	    		}
  	    		else { //root2 should be the father of this
  	    			newFather = root2;
  	    			newSon = this;
  	    		}
	    			if (newFather.rightBrother == newSon) {
	    				newFather.rightBrother = newSon.rightBrother;
  	    				if (newSon.rightBrother != null) {
  	    					newSon.rightBrother.leftBrother = newFather;
  	    				}
  	    			}
  	    			if (newFather.leftBrother == newSon) {
  	    				newFather.leftBrother = newSon.leftBrother;
  	    				if (newSon.leftBrother != null) {
  	    					newSon.leftBrother.rightBrother = newFather;
  	    				}
  	    			}
  	    			newSon.rightBrother = newFather.leftMostSon;
  	    			if (newFather.leftMostSon != null) { //the left most son of root1 is not null
  	    				newFather.leftMostSon.leftBrother = newSon;
  	    			}
  	    			newFather.leftMostSon = newSon;
  	    			newSon.father = newFather;
  	    			newSon.leftBrother = null;
  	    			newFather.rank++;
  	    	}
  	    	else {
  	    		System.out.println("You are trying to link trees of different order!");
  	    	}	
  	    }
    }
}
