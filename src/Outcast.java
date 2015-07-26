
public class Outcast {
	WordNet w;
	
		// constructor takes a WordNet object
		public Outcast(WordNet wordnet){
		this.w = wordnet;
			
	}
	
	// given an array of WordNet nouns, return an outcast
	public String outcast(String[] nouns){
		int n = nouns.length;
		int dist = 0;
		int longestDist = 0;

		//initialize values
		longestDist = dist2All(nouns[0], nouns, 0);
		String outcast = nouns[0];
		
		for(int i = 1; i < n; i++){
			dist = dist2All(nouns[i], nouns, i);
			if (dist>longestDist) {
				longestDist = dist; 
				outcast = nouns[i];
			}
		}
		return outcast;
	}
		
		private Integer dist2All(String a, String[] b, Integer indexAinB){
			int accumDist = 0;
			
			for(int j = 0; j< b.length; j++){;
				if(j!=indexAinB) accumDist += w.distance(a, b[j]);
			}
			return accumDist;
		}

	public static void main(String[] args){
		   
		WordNet a = new WordNet("synsets.txt", "hypernyms.txt");
		Outcast b = new Outcast(a);

		//outcast5.txt: table
		//outcast8.txt: bed
		//outcast11.txt: potato
		String[] outcast5 ={"horse","zebra", "cat", "bear","table"};
		System.out.println(b.outcast(outcast5));
		
		String [] outcast8 = {"water", "soda", "bed", "orange_juice", "milk", "apple_juice", "tea", "coffee"};
		System.out.println(b.outcast(outcast8));
		
		String [] outcast11 = {"apple", "pear", "peach", "banana", "lime", "lemon", "blueberry", "strawberry", "mango", "watermelon", "potato"};
		System.out.println(b.outcast(outcast11));
	}

}
