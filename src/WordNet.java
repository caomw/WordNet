import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class WordNet {

	Digraph G;
	Hashtable<Integer, Queue<String>> index2Synset;
	TreeMap<String,Set<Integer>> noun2Index;
	int size = 0;
	
	   // constructor takes the name of the two input files
	   public WordNet(String synsets, String hypernyms){
		   if (synsets==null || hypernyms ==null) throw new NullPointerException("synset/hypernym file is null");
		   //read in Synsets from file: create hashtable and bst
		   this.index2Synset = readSynsets(synsets);
		   //construct Digraph from hypernym file
		   this.G = createGraph(hypernyms);
		   
		   
	   }
	   
	private Hashtable<Integer, Queue<String>> readSynsets(String fileName){
			//Hashtable maps integers to synsets - each synset (group of nouns) its own bag in hashtable
			// ht: f(Integer index) --> Bag<String> synset
		    Hashtable<Integer, Queue<String>> index2Synset = new Hashtable<Integer, Queue<String>>();
		    //BST allows us to quickly  - each noun its own node in BST
		    	//search if there is a noun using the noun
		    	// return all indeces of synsets that contain noun
		    // bst: f(String noun) ---> Integer index
		    TreeMap<String,Set<Integer>> noun2Index = new TreeMap<String,Set<Integer>>(); 
		    
		    String line = "";
			String cvsSplitBy = ",";
			String synsetSplitBy = " ";
			BufferedReader br = null;
			int count  = 0;
			
			try {
				br = new BufferedReader(new FileReader(fileName));
		    	while( (line = br.readLine()) != null )
		    		{
		    		// use comma as separator
		    		//	d: 0th is index, 1st is synsets, 2nd is def
		    		String[] d = line.split(cvsSplitBy);
		    		int index = Integer.valueOf(d[0]);
		    		String[] words = d[1].split(synsetSplitBy);
		    		
		    		//construct BST and hashtable
		    		Queue<String> synset = new Queue<String>();
		    		for(int i = 0; i <words.length; i++){
		    			String s = words[i];
		    			//add words in the synset to the bag
		    			synset.enqueue(s);
		    			
		    			//if noun already in tree, add index to location of set
		    			Set<Integer> sameWords = noun2Index.get(s);
		    			if (sameWords != null){sameWords.add(index);}
		    			else {
		    				Set<Integer> noun2Synset = new TreeSet<Integer>();
		    				noun2Synset.add(index);
		    				noun2Index.put(s, noun2Synset);
		    				}
		    		}
		    		
		    		//place the bag of words in hashtable
		    		index2Synset.put(index, synset);
		    		count++;
		    		}
			}
			  catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (br != null) {
					try {br.close();}
					catch (IOException e) {e.printStackTrace();}
				}
			}
			setSize(count);
			this.noun2Index = noun2Index;
			return index2Synset;
			
	   }
	
	private Digraph createGraph(String fileName){
		    String line = "";
			String cvsSplitBy = ",";
			BufferedReader br = null;
			
			//create Digraph
			Digraph DG = new Digraph(this.size);

			try {
				br = new BufferedReader(new FileReader(fileName));
		    	while( (line = br.readLine()) != null )
		    		{
		    		// use comma as separator
		    		String[] vert2Edges = line.split(cvsSplitBy);
		    		// edge goes from vertex ==> vert2Edges[i]
		    		int vertex = Integer.valueOf(vert2Edges[0]);
		    		// add edge to Digraph
		    		for(int i = 1; i < vert2Edges.length; i++){ 
		    			DG.addEdge(vertex, Integer.valueOf(vert2Edges[i])); }
		    		}
			}
			  catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (br != null) {
					try {br.close();}
					catch (IOException e) {e.printStackTrace();}
				}
			}
			return DG;
			
	   }

	private void setSize(int size){
		this.size = size;
	}
	   
	   // all WordNet nouns
	public Iterable<String> nouns(){
		LinkedList<String> nouns = new LinkedList<String>();
		//Iterate over BST
		for(String a: noun2Index.keySet()){nouns.add(a);}
		return nouns;
	}
	   // is the word a WordNet noun?
	public boolean isNoun(String word){
		if (word==null) throw new NullPointerException("isNoun: null is not acceptable argument");
		//search bst
		return noun2Index.containsKey(word);
	}
	
	// a synset (second field of synsets.txt) that is a shortest common ancestor
    // of noun1 and noun2 (defined below)
	public String sca(String noun1, String noun2){
		   Set<Integer> a = noun2Index.get(noun1);
		   Set<Integer> b = noun2Index.get(noun2);
		   if (a==null) throw new IllegalArgumentException("noun not found: " + a);
		   if (b==null) throw new IllegalArgumentException("noun not found: " + b);
		
		   ShortestCommonAncestor sca = new ShortestCommonAncestor(G);
		   
		   int ancIndex = sca.ancestor(a, b);
		   
		   Queue<String> ancSyns = index2Synset.get(ancIndex);
		   
		   String synset = "";
		   for(String c: ancSyns){ synset = synset + " " + c; }

		   return synset;
	   }
	   
	// distance between noun1 and noun2 (defined below)
	public int distance(String noun1, String noun2){
		Set<Integer> a = noun2Index.get(noun1);
		Set<Integer> b = noun2Index.get(noun2);
		if (a==null) throw new IllegalArgumentException("noun not found: " + a);
		if (b==null) throw new IllegalArgumentException("noun not found: " + b);
		ShortestCommonAncestor sca = new ShortestCommonAncestor(G);
		return sca.length(a, b);
	}

	public static void main(String[] args) {
		//testing
		
		WordNet a = new WordNet("synsets.txt", "hypernyms.txt");
		//Nouns
		for(String b: a.nouns()){
			System.out.println(b);
		}
		
		//isNoun
		System.out.println("Should contain ''hood': " + a.isNoun("'hood"));
		System.out.println("Should contain 'Abramis': " + a.isNoun("Abramis"));
		System.out.println("Should contain '1530s': " + a.isNoun("1530s"));
		System.out.println("Should not contain 'kjhvkvkuyc': " + !a.isNoun("kjhvkvkuyc"));
		
		WordNet b = new WordNet("synsets_custom.txt", "hypernyms_custom.txt");
		
		//Nouns
		for(String c: b.nouns()){
			System.out.println(c);
		}
		//isNoun
		System.out.println("Should contain 'hello' : " + b.isNoun("hello"));
		System.out.println("Should contain 'bye' : " + b.isNoun("bye"));
		System.out.println("Should not contain 'kjhvkvkuyc': " + !b.isNoun("kjhvkvkuyc"));
		
		//Shortest Common Ancestor
		System.out.println("SCA of 'see_you_later' and 'hi' is 'Greetings' is 3 away: " + b.sca("see_you_later", "hi") + " , " + b.distance("see_you_later", "hi"));
		System.out.println("SCA of 'Goodbye' and 'hi' is 'Greetings' is 2 away: " + b.sca("goodbye", "hi") + " , " + b.distance("goodbye", "hi"));
		
		
		
		
	
	}
}
