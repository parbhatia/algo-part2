import java.util.ArrayList;
import java.util.List;

import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class SAP {
    private Digraph graph;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException();
        }
        // make copy of G
        graph = new Digraph(G);
    }

    private void validateNode(int node, Digraph G) {
        if (node < G.V() && node >= 0) {
            return;
        }
        throw new IllegalArgumentException("vertex argument is outside its prescribed range");
    }

    private void validateNodes(Iterable<Integer> nodes, Digraph G) {
        if (nodes == null) {
            throw new IllegalArgumentException("Null argument");
        }
        for (Integer n : nodes) {
            if (n == null) {
                throw new IllegalArgumentException("vertex argument is outside its prescribed range");
            }
            validateNode(n, G);
        }
        return;
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        validateNode(v, graph);
        validateNode(w, graph);
        BreadthFirstDirectedPaths v_bfs = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths w_bfs = new BreadthFirstDirectedPaths(graph, w);
        int min_length = Integer.MAX_VALUE;
        for (int n = 0; n < graph.V(); ++n) {
            if (w_bfs.hasPathTo(n) && v_bfs.hasPathTo(n)) {
                min_length = Math.min((v_bfs.distTo(n) + w_bfs.distTo(n)), min_length);
            }
        }
        if (min_length == Integer.MAX_VALUE) {
            return -1;
        }
        return min_length;
    }

    // a common ancestor of v and w that participates in a shortest ancestral
    // path; -1 if no such path
    public int ancestor(int v, int w) {
        validateNode(v, graph);
        validateNode(w, graph);
        int min_length = length(v, w);
        if (min_length == -1) {
            return min_length;
        }
        BreadthFirstDirectedPaths v_bfs = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths w_bfs = new BreadthFirstDirectedPaths(graph, w);
        for (int n = 0; n < graph.V(); ++n) {
            if (w_bfs.hasPathTo(n) && v_bfs.hasPathTo(n)) {
                int local_dist = v_bfs.distTo(n) + w_bfs.distTo(n);
                if (local_dist == min_length) {
                    return n;
                }
            }
        }
        return -1;
    }

    // length of shortest ancestral path between any vertex in v and any vertex
    // in w; -1 if no such path
    // note: we're doing BFS multiple times, which is still bounded by O(E + V),
    // since BFS is O(E + V)
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        validateNodes(v, graph);
        validateNodes(w, graph);
        int min_length = Integer.MAX_VALUE;
        for (int node1 : v) {
            for (int node2 : w) {
                int dist = length(node1, node2);
                if (dist != -1) {
                    min_length = Math.min(min_length, dist);
                }
            }
        }
        if (min_length == Integer.MAX_VALUE) {
            return -1;
        }
        return min_length;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no
    // such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        validateNodes(v, graph);
        validateNodes(w, graph);
        int min_length = length(v, w);
        if (min_length == -1) {
            return min_length;
        }
        for (int node1 : v) {
            for (int node2 : w) {
                int local_length = length(node1, node2);
                if (local_length == min_length) {
                    return ancestor(node1, node2);
                }
            }
        }
        return -1;
    }

    private static void test_basic(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }

    private static void test_with_iterables(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v1 = StdIn.readInt();
            int v2 = StdIn.readInt();
            int v3 = StdIn.readInt();
            int w1 = StdIn.readInt();
            int w2 = StdIn.readInt();
            int w3 = StdIn.readInt();
            List<Integer> v = new ArrayList<>();
            List<Integer> w = new ArrayList<>();
            v.add(v1);
            v.add(v2);
            v.add(v3);
            w.add(w1);
            w.add(w2);
            w.add(w3);
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
        // test_basic(args);
        test_with_iterables(args);
    }
}
