import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a trie, used for the search box. We cheat a bit
 * and store references to the actual Road objects in each node, blowing the
 * memory efficiency but making this easy to implement.
 *
 *
 */
public class Trie {

	private TrieNode root;
	private List<String> roadNames;

	private Map<String, Road> prefixRoads;

	private List<Road> selectRoads;

    public Trie(Map<Integer, Road> roads) {

    	this.root = new TrieNode();

    	this.roadNames = new ArrayList<String>();
    	this.prefixRoads = new HashMap<String, Road>();

    	initTrie(roads);
    }

    /**Initializes the Trie based on the RoadNames and CityNames in the Road Data Set
     *
     * @param Map<Integer, Road> roads - A Map containing loaded data from the Road Data Set*/
    public void initTrie(Map<Integer, Road> roads){

    	for(Road r : roads.values()){
    		if( ! roadNames.contains(r.name))
    			roadNames.add(r.name +","+r.city);		//Get List of Unique Road Names + City

    		prefixRoads.put(r.name, r);				//Initialize Data Struct with RoadNames as indexes to Roads
    	}

    	for(String s : roadNames)
    		insert(s);										//Insert all RoadNames into Trie

    }

	/**Inserts a Word (RoadName + CityName) into a Trie
	 *
	 * @param String word*/
    public void insert(String word) {

    	HashMap<Character, TrieNode> children = root.children;

        for(int i = 0; i < word.length(); i++){

        	char c = word.charAt(i);

            TrieNode trieNode;

            if(children.containsKey(c))
            	trieNode = children.get(c);			//If it contains the current character on prefix, then set
            else{
                trieNode = new TrieNode(c);			//Else, create a new TrieNode and insert into Map
                children.put(c, trieNode);
            }

            children = trieNode.children;			//Set Children Nodes

            if(i == word.length()-1)
                trieNode.isRoad = true;
        }
    }


    /**Returns true if there is an entry in the Trie that matches the given prefix
     *
     * @param String prefix*/
    public boolean startsWith(String prefix) {

    	if(searchNode(prefix) == null)
            return false;
        else
            return true;

    }

    /**Searches the particular Node that matches the given prefix
     * A match is found if the prefix matches an entry in the trie
     * up to the ith position.
     *
     * @param String prefix*/
    public TrieNode searchNode(String prefix){

    	Map<Character, TrieNode> children = root.children;
        TrieNode trieNode = null;

        for(int i = 0; i < prefix.length(); i++){

        	char c = prefix.charAt(i);

            if(children.containsKey(c)){
                trieNode = children.get(c);
                children = trieNode.children;
            }
            else
                return null;
        }

        return trieNode;
    }


	/**Returns the list of Road objects that correspond to the given prefix
	 *
	 * @param String prefix*/
	public List<Road> getRoads(String prefix) {

		selectRoads = new ArrayList<Road>();

		for(int i = 0; i < prefix.length(); i++){

			for(Map.Entry<String, Road> entry : prefixRoads.entrySet()){

				if(i <= entry.getKey().length()-1){

					String subStr = entry.getKey().substring(0, i+1);				//Get Substring of RoadName based off ith pos on prefix

					if(prefix.equals(subStr)){										//If Prefix Matches, Add to list of Roads
						selectRoads.add(entry.getValue());
					}
				}
			}
		}

		return selectRoads;
	}

	/**Returns a String that consists of all the roadNames and cityNames
	 * that match the given prefix
	 *
	 * @param String prefix*/
	public String getRoadNames(String prefix) {

		StringBuilder sb = new StringBuilder();

		for(Road r : getRoads(prefix))
			sb.append("Street: " + r.name + "	Suburb: " + r.city + "\n");

		return sb.toString();
	}

	/**
	 * Represents a single node in the trie. It contains a collection of the
	 * Roads whose names are exactly the traversal down to this node.
	 */
	private class TrieNode {

		char c;
		boolean isRoad = false;
	    HashMap<Character, TrieNode> children = new HashMap<Character, TrieNode>();		//Children of Node


	    public TrieNode() {}

	    /**TrieNode constructor, simply takes in a character and sets it
	     *
	     * @param char c*/
	    public TrieNode(char c){
	        this.c = c;
	    }
	}
}

// code for COMP261 assignments