import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private WordNet wordnet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordnet = wordnet;
    }

    private static int getMaxIndex(int[] inputArray) {
        int maxValue = inputArray[0];
        int maxValueIndex = 0;
        for (int i = 1; i < inputArray.length; i++) {
            if (inputArray[i] > maxValue) {
                maxValue = inputArray[i];
                maxValueIndex = i;
            }
        }
        return maxValueIndex;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        if (nouns == null || nouns.length == 0) {
            throw new IllegalArgumentException("Null/Empty arguments");
        }
        int total_nouns = nouns.length;
        int[] distances = new int[total_nouns];
        for (int i = 0; i < total_nouns; ++i) {
            int total_dist = 0;
            for (int j = 0; j < total_nouns; ++j) {
                total_dist += wordnet.distance(nouns[i], nouns[j]);
            }
            distances[i] = total_dist;
        }
        return nouns[getMaxIndex(distances)];
    }

    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}