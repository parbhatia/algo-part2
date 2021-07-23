import java.util.ArrayList;
import java.util.HashMap;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class WordNet {
    private int num_of_synsets;
    private int num_of_nouns;
    private int num_of_edges;
    private Digraph graph;
    private ArrayList<String> list_of_synsets;
    private HashMap<String, ArrayList<Integer>> words; // maps each noun to synsetId
    private SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException();
        }
        init_synsets(synsets);
        init_hypernyms(hypernyms);
        sap = new SAP(graph);
    }

    private void init_synsets(String synsets) {
        list_of_synsets = new ArrayList<String>();
        words = new HashMap<String, ArrayList<Integer>>();
        In synsetIn = new In(synsets);
        while (synsetIn.hasNextLine()) {
            String line = synsetIn.readLine();
            String[] splitLine = line.split(",");
            Integer synsetId = Integer.parseInt(splitLine[0]);
            String synset = splitLine[1];
            add_nouns(synset, synsetId);
        }
    }

    private void add_nouns(String synset, Integer synsetId) {
        ++num_of_synsets;
        list_of_synsets.add(synset);
        String[] list = synset.split(" ");
        for (String noun : list) {
            // check repeated nouns, since multiple synsets can have the same noun
            if (isNoun(noun)) {
                words.get(noun).add(synsetId);
            } else {
                ++num_of_nouns;
                ArrayList<Integer> newlist = new ArrayList<Integer>();
                newlist.add(synsetId);
                words.put(noun, newlist);
            }
        }
    }

    private void init_hypernyms(String hypernyms) {
        graph = new Digraph(num_of_synsets);
        In hypernymIn = new In(hypernyms);
        while (hypernymIn.hasNextLine()) {
            String line = hypernymIn.readLine();
            String[] splitLine = line.split(",");
            Integer synsetId = Integer.parseInt(splitLine[0]);
            // iterate over synset's hypernyms
            for (int i = 1; i < splitLine.length; ++i) {
                Integer hypernym = Integer.parseInt(splitLine[i]);
                add_hypernym(synsetId, hypernym);
            }
        }
    }

    private void add_hypernym(Integer synsetId, Integer hypernym) {
        graph.addEdge(synsetId, hypernym);
        ++num_of_edges;
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return words.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return words.containsKey(word);
    }

    private void validate_nouns(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException("Non nouns detected");
        }
    }

    // friendly reminder that the synsetId of a noun is always an array list
    // a bit of an overkill, but since our SAP module handles iterables, why not?
    private ArrayList<Integer> synset_id(String noun) {
        return words.get(noun);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        validate_nouns(nounA, nounB);
        return sap.length(synset_id(nounA), synset_id(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA
    // and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        validate_nouns(nounA, nounB);
        Integer synset_id = sap.ancestor(synset_id(nounA), synset_id(nounB));
        return list_of_synsets.get(synset_id);

    }

    private void debug() {
        StdOut.println("num of synsets: " + num_of_synsets);
        StdOut.println("num of nouns: " + num_of_nouns);
        StdOut.println("num of edges: " + num_of_edges);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        wordnet.debug();
        StdOut.println(wordnet.distance("AND_circuit", "entity"));
        StdOut.println(wordnet.sap("Adams", "Sam_Adams"));
        StdOut.println(wordnet.sap("Adam", "Robert_Adam"));
        StdOut.println(wordnet.distance("bird", "worm"));
        StdOut.println(wordnet.sap("worm", "bird"));
    }
}