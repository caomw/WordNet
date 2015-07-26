import java.util.Hashtable;
import java.util.LinkedList;

public class ShortestCommonAncestor {
	
	protected Digraph G;
	private cycle a;
	//Constructor: takes rooted DAG as argument
	public ShortestCommonAncestor(Digraph G) {
		if (G==null) throw new NullPointerException("ShortestCommonAncestor Constructor called with null argument");
		//check for cycles
		cycle a = detectCycle(G);
		this.a = a;
		if(a.isCyclic) {
			System.out.println("     Cycle detected! Back Edge Occurs at: [" + a.t.getFirst() + "," + a.t.getLast() + "]");
			throw new IllegalArgumentException("Cyclic graph detected");
		}
		this.G = G;
	}
	
	// length of shortest ancestral path between v and w
	public int length (Integer v, Integer w){
		if (v==null || w ==null) throw new NullPointerException("Length called with null argument: arg1 =" + v + " arg2 =" + w);
		if(v>G.V() || v<0) throw new IllegalArgumentException("Length called with node(" + v +") not in Digraph");
		if(w>G.V() || w<0) throw new IllegalArgumentException("Length called with node(" + w +") not in Digraph");
		LinkedList<Integer> a = new LinkedList<Integer>();
		LinkedList<Integer> b = new LinkedList<Integer>();
		a.add(v);
		b.add(w);
		
		return DeluxeBFS(a,b).length();
	}	
	// a shortest common ancestor of vertices v and w
	public int ancestor(Integer v, Integer w) {
		if (v==null || w ==null) throw new NullPointerException("ancestor called with null argument: arg1 =" + v + " arg2 =" + w);
		if(v>G.V() || v<0) throw new IllegalArgumentException("Length called with node(" + v +") not in Digraph");
		if(w>G.V() || w<0) throw new IllegalArgumentException("Length called with node(" + w +") not in Digraph");
		LinkedList<Integer> a = new LinkedList<Integer>();
		LinkedList<Integer> b = new LinkedList<Integer>();
		a.add(v);
		b.add(w);
		
		return DeluxeBFS(a,b).ancestor();
	}
	// length of shortest ancestral path between sets v and w
	public int length (Iterable<Integer> v, Iterable<Integer>  w){
		if (v==null || w ==null) throw new NullPointerException("Length called with null argument");
		if(!v.iterator().hasNext() || !w.iterator().hasNext()) throw new IllegalArgumentException("Length called with empty collection");
		return DeluxeBFS(v,w).length();
	}
	// a shortest common ancestor of vertices v and w
	public int ancestor(Iterable<Integer> v, Iterable<Integer>  w) {
		if (v==null || w ==null) throw new NullPointerException("ancestor called with null argument");
		return DeluxeBFS(v,w).ancestor();
	}
	
	// helper method to calculate single shortest path between two sets of points
	private pathInfo DeluxeBFS(Iterable<Integer> v, Iterable<Integer> w){
		//create hashtable for the ones already visited by either set
		Hashtable<Integer, Point> stV = new Hashtable<Integer, Point>();
		Hashtable<Integer, Point> stW = new Hashtable<Integer, Point>();
		
		//create queue of Points(Integer , last point) for the ones to visit
		Queue<Point> q = new Queue<Point>();
		for(Integer i: v){ q.enqueue(new Point(i,null, "v")); }
		for(Integer i: w){ q.enqueue(new Point(i,null, "w")); }
		
		 while (!q.isEmpty()) {
	            Point p = q.dequeue();
	            int pKey = p.val();
	            String pSet = p.whichSet();
        		//Given a connected graph, a node will be found 2 times: 
        		// 	First time the node is found
	            //		- a Point object is created when enqueued
	            //		- pops off queue -> added to the symbol table of Points indexed by their node int value
        		//	Second time the node is found, we check how we are getting to it. 
	            //		- If exploring from a node of the same set, we do nothing,
	            //		- If from the other set, then we have found our shortest common ancestor (sac)
	            if(pSet.equals("v")){
	            	// If it is in either Symbol Table then this is the second time we've seen this node
	            	if(stV.containsKey(pKey)){} // explored by node in SAME set. Do nothing.	
	            	else if(stW.containsKey(pKey)){return processPath(p, stW);} // explored by node in OTHER set.
	            	else{
	            	// Node not in either Symbol Table - first time node explored
	            		// add it to correct set and add its neighbors to queue
	            		stV.put(pKey, p );
	            		for (Integer n : G.adj(pKey)) q.enqueue(new Point(n, p, pSet));
	            		}
	            }
	           
	            else{ // must be member of w
	            	if(stW.containsKey(pKey)){} // explored by node in SAME set. Do nothing.	
	            	else if(stV.containsKey(pKey)){return processPath(p, stV);} // explored by node in OTHER set.
	            	else{
	            	// Node not in either Symbol Table - first time node explored
	            		// add it to correct set and add its neighbors to queue
	            		stW.put(pKey, p);
	            		for (Integer n : G.adj(pKey)) q.enqueue(new Point(n, p, pSet));
	            		}
	            }
		 }
		 throw new RuntimeException("DeluxeBFS: no path found");
		 
		}
	//helper method for DeluxeBFS
	private pathInfo processPath(Point p, Hashtable<Integer,Point> st){
		// find path length:
		// 		trace from Symbol Table (first time we found it) and from current memory (second time we found it)
		int length = 0;
		//Symbol Table Trace
		Point stP = st.get(p.val());
		Trace a = chasePointer(stP, length);

		//Current Memory Trace
		Trace b = chasePointer(p, a.length());
		length = b.length();
		
		//find total path: end of trace a to end of trace b
		Queue<Integer> totalPath = new Queue<Integer>();
			//for each loop with iterate enqueue from bottom ==>top (ancestor ==> end)
			// thus first element in for each loop will be the common ancestor
		// put elements on totalPath Queue: first element: ancestor, last element: end
		for(Integer l: a.path()){ totalPath.enqueue(l);}
		
		// dequeue the ancestor (first element), will be re-added by other path
		totalPath.dequeue();
		
		// iterate over Stack b and enqueue onto totalPath: this will add first: ancestor, last: other end
		for(Integer k: b.path()){ totalPath.enqueue(k);}
		
		// totalPath Queue now in order from end ... ancestor ... other end
		return new pathInfo(length, p.val(), a.end(), b.end(), totalPath);
		
	}
	//helper method for BFS length
	private Trace chasePointer(Point p, int count){
		//path will go from ancestor ==> endOfPath
		Stack<Integer> path = new Stack<Integer>();
		if (p == null) return new Trace(0, null, path);
		
		Integer endOfPath = p.val();
		path.push(endOfPath);
		while (p.prev != null) {
			endOfPath = p.val();
			p = p.prev;
			path.push(p.val());
			count++;
		}
		
		return new Trace(count, endOfPath, path);
	}
	//helper classes for BFS
		private class Point{
			private Integer val;
			private Point prev;
			private String whichSet;
			public Point(Integer val, Point prev){
				this.val = val;
				// edge from prev to current: how we got here
					// null of a source
				this.prev = prev;
			}
			public Point(Integer val, Point prev, String whichSet){
				this.val = val;
				// edge from prev to current: how we got here
					// null of a source
				this.prev = prev;
				this.whichSet = whichSet;
			}
			public int val() {return val;}
			public Point prev() {return prev;}
			public String whichSet() { return whichSet;}
			public String toString() {return "Current: " + val;}
			
		}
		private class Trace {
			private Integer pathLength;
			private Integer endNode;
			private Stack<Integer> path;
			public Trace(Integer pathLength, Integer endNode, Stack<Integer> path){
				this.endNode = endNode;
				this.pathLength = pathLength;
				this.path = path;
			}
			public Integer length() { return this.pathLength;}
			public Integer end() { return this.endNode;}
			public Stack<Integer> path(){ 
				Stack<Integer> toReturn = new Stack<Integer>();
				for(Integer a: path){ toReturn.push(a); }
				return toReturn;
				}
		}
		private class pathInfo{
			private Integer pathLength;
			private Integer commonAncestor;
			private Integer end1;
			private Integer end2;
			private Queue<Integer> path;
			public pathInfo(Integer pathLength, Integer commonAncestor, Integer end1, Integer end2, Queue<Integer> path){
				this.commonAncestor = commonAncestor;
				this.pathLength = pathLength;
				this.end1 = end1;
				this.end2 = end1;
				this.path = path;
			}
			public Integer length() { return this.pathLength;}
			public Integer ancestor() { return this.commonAncestor;}
			public Queue<Integer> path() {
				Queue<Integer> toReturn = new Queue<Integer>();
				for(Integer a: path){ toReturn.enqueue(a); }
				return toReturn;
			}
			public Integer end1() { return this.end1;}
			public Integer end2() { return this.end2;}
		}
	//helper method for detectCycle	
		public enum Status{ visiting, visited, notVisited;}
		private class cycle{
			public boolean isCyclic;
			public LinkedList<Integer> t;
			public cycle(boolean isCyclic,  LinkedList<Integer> t){
				this.isCyclic = isCyclic;
				this.t = t;
			}
			
		}
	//helper method to ensure graph has no cycles
	public cycle detectCycle(Digraph G){
		Status[] s = new Status[G.V()];
		for(int i = 0; i < s.length; i++){ s[i] = Status.notVisited;}
		
		//keep track of where cycle is if it exists
		LinkedList<Integer> t = new LinkedList<Integer>();
		//perform DFS to find cycles
		//start at first node, explore graph with DFS, check for next unvisited Node, repeat...
		for(int i = 0; i < s.length; i++){
			if (s[i] == Status.visited) continue;
			else if( isCyclic(i,s,G,t) ) return new cycle(true, t);
		}
		return new cycle(false, t);
		
	}
	public Iterable<Integer> cycleNodes() {return a.t;}
	public boolean isCyclic() {return a.isCyclic;}
	private boolean isCyclic(Integer V, Status[] s, Digraph G, LinkedList<Integer> t){
		//Base Cases
		//are we currently visiting this node?
			// if yes then we are in a cycle, if not this 
			// else if we already visited this node then no cycle here
			// else mark this node as currently being visited
		if(s[V] == Status.visiting) return true;
		else if (s[V] == Status.visited) return false;
		else s[V] = Status.visiting;
		
		Iterable<Integer> adjVs = G.adj(V);

		//reduction case: if we find cycle in any adjacent vertices return true
		for(Integer a: adjVs){ 
			if(isCyclic(a,s,G,t)){
			t.add(a);
			//System.out.println(t);
			return true; 
			}
		}
		
		//once all recursive calls are done change status to visited and return that no cycle found
		s[V] = Status.visited;
		return false;
		
		
	}
	public static void main(String[] args) {
		In in = new In("digraph1.txt");
	    Digraph G = new Digraph(in);
	    ShortestCommonAncestor sca = new ShortestCommonAncestor(G);
	 
	    	//single source
	    	int v = 3;
	        int w = 9;
	        System.out.println(v + ", " + w + ": Length 3 == " +  sca.length(v, w) + ", Ancestor 1 == " +sca.ancestor(v, w));
	        
	        v = 4;
	        w = 5;
	        System.out.println(v + ", " + w + ": Length 2 == " +  sca.length(v, w) + ", Ancestor 1 == " +sca.ancestor(v, w));
	        
	        v = 4;
	        w = 6;
	        System.out.println(v + ",  " + w + ": Length 3 == " +  sca.length(v, w) + ", Ancestor 1 == " +sca.ancestor(v, w));

	        //multi source
	        LinkedList<Integer> a = new LinkedList<Integer>();
	        LinkedList<Integer> b = new LinkedList<Integer>();
	        a.add(3); a.add(4);
	        b.add(2); b.add(8);
	        System.out.println(a + ",  " + b + ": Length 3 == " +  sca.length(a, b) + ", Ancestor 0 or 1 == " +sca.ancestor(a, b));
	       
	        LinkedList<Integer> c = new LinkedList<Integer>();
	        LinkedList<Integer> d = new LinkedList<Integer>();
	        c.add(6);
	        d.add(10);
	        System.out.println(c + ",  " + d + ": Length 5 == " +  sca.length(c, d) + ", Ancestor 1 == " +sca.ancestor(c, d));
		      
	        // non-cyclic graph
	        System.out.println("digraph1.txt - should be no cycles");
	        try{new ShortestCommonAncestor(G);}
		    catch (IllegalArgumentException e){System.out.println("cycle detected!" );}
	        
	        // cyclic graph
	        In in1 = new In("digraph1_cycle_1_6.txt");
		    Digraph G1 = new Digraph(in1);
		    System.out.println("digraph1_cycle_1_6.txt - should be cycle at [1,6]");
		    try{new ShortestCommonAncestor(G1);}
		    catch (IllegalArgumentException e){//System.out.println("     cycle detected!");
		    }

		    // cyclic graph
		    In in2 = new In("digraph1_cycle_5_10.txt");
		    Digraph G2 = new Digraph(in2);
		    System.out.println("digraph1_cycle_5_10.txt - should be cycle at [5,10]");
		    try{new ShortestCommonAncestor(G2);}
		    catch (IllegalArgumentException e){//System.out.println("     cycle detected!");
		    }
		    
		    
		    // cyclic graph - self cycle
		    In in3 = new In("digraph1_cycle_5_5.txt");
		    Digraph G3 = new Digraph(in3);
		    System.out.println("digraph1_cycle_5_5.txt - should be cycle at [5,5]");
		    try{new ShortestCommonAncestor(G3);}
		    catch (IllegalArgumentException e){//System.out.println("     cycle detected!");
		    }
		    
	        
	        
	}
}
