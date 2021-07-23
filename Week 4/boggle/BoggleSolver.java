import java.util.HashSet;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BoggleSolver {
    private BoggleBoard board;
    private HashSet<String> validWords;
    private Node root;
    private Cell[][] cells;
    private Node recentPrefixNode;

    private class Node {
        private int score;
        private int depth;
        private char c;
        private Node left, mid, right, parent;
    }

    // Initializes the data structure using the given array of strings as the
    // dictionary.
    // (You can assume each word in the dictionary contains only the uppercase
    // letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        for (String word : dictionary) {
            int wordScore = wordToScore(word);
            if (wordScore > 0) { // only store words with correct score (based on length)
                put(word, wordScore);
            }
        }
    }

    // get score of word
    private int get(String key) {
        Node x = get(root, key, 0);
        if (x == null) {
            return 0;
        }
        return x.score;
    }

    private Node get(Node x, String key, int d) {
        // ++prefixChecks;
        if (x == null) {
            return null;
        }
        char c = key.charAt(d);
        if (c < x.c)
            return get(x.left, key, d);
        else if (c > x.c)
            return get(x.right, key, d);
        else if (d < key.length() - 1)
            return get(x.mid, key, d + 1);
        else
            return x;
    }

    private void put(String key, int score) {
        root = put(root, null, key, score, 0, 0);
    }

    private Node put(Node x, Node parent, String key, int score, int d, int depth) {
        char c = key.charAt(d);
        if (x == null) {
            x = new Node();
            x.c = c;
            x.parent = parent;
            x.depth = depth;
        }
        if (c < x.c) {
            x.left = put(x.left, x, key, score, d, depth);
        } else if (c > x.c) {
            x.right = put(x.right, x, key, score, d, depth);
        } else if (d < key.length() - 1) {
            x.mid = put(x.mid, x, key, score, d + 1, depth + 1);
        } else {
            x.score = score;
        }
        return x;
    }

    private boolean validCell(int row, int col) {
        return row >= 0 && row < board.rows() && col < board.cols() && col >= 0;
    }

    // returns if a string is a prefix in our trie
    private Node validPrefixFromNode(Node n, String prefix) {
        // StdOut.println(prefix + " : " + n.depth);
        if (n == null)
            return null;
        Node x = get(n, prefix, n.depth);
        return x;
    }

    private class Cell {
        private int row, col;
        private char c;
        private Bag<Cell> neighbours;
        private boolean visited;

        public Cell(int row, int col, char c) {
            this.row = row;
            this.col = col;
            this.c = c;
            this.visited = false;
            this.neighbours = new Bag<Cell>();
        }

        public String getFilteredChar() {
            if (c == 'Q') {
                return "QU";
            }
            return Character.toString(c);
        }

        public boolean visited() {
            return this.visited;
        }

        public void visit() {
            this.visited = true;
        }

        public void unVisit() {
            this.visited = false;
        }

        public Bag<Cell> neighbours() {
            return neighbours;
        }

    }

    private void precomputeCells() {
        for (int row = 0; row < board.rows(); ++row) {
            for (int col = 0; col < board.cols(); ++col) {
                Cell newCell = new Cell(row, col, board.getLetter(row, col));
                cells[row][col] = newCell;
            }
        }
        for (int row = 0; row < board.rows(); ++row) {
            for (int col = 0; col < board.cols(); ++col) {
                precomputeCellNeighbours(cells[row][col]);
            }
        }
    }

    private void precomputeCellNeighbours(Cell c) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0)
                    continue;
                int newrow = c.row + i;
                int newcol = c.col + j;
                if (validCell(newrow, newcol)) {
                    c.neighbours.add(cells[newrow][newcol]);
                }
            }
        }
    }

    private Node getParent(Node n) {
        if (n == root)
            return n;
        else
            return n.parent;
    }

    private void dfs(Cell c, String prefixSoFar) {
        c.visit();
        Node startingNode = root;
        if (recentPrefixNode != null) {
            int recentPrefixNodeLength = recentPrefixNode.depth + 2;
            if (prefixSoFar.length() == recentPrefixNodeLength) {
                startingNode = getParent(recentPrefixNode);
            } else if (prefixSoFar.length() - 1 == recentPrefixNodeLength) {
                startingNode = recentPrefixNode;
            }
        }
        Node validPrefixNode = validPrefixFromNode(startingNode, prefixSoFar);
        recentPrefixNode = validPrefixNode;
        if (validPrefixNode != null) {
            if (validPrefixNode.score > 0) {
                validWords.add(prefixSoFar);
            }
            for (Cell n : c.neighbours()) {
                if (!n.visited()) {
                    String newPrefix = prefixSoFar + n.getFilteredChar();
                    dfs(n, newPrefix);
                }
            }
        }
        c.unVisit();
    }

    // // Node based dfs version without storing/creating new strings
    // private void dfs(Cell c, Node curr, String prefixSoFar) {
    // c.visit();
    // Node startingNode = root;
    // if (recentPrefixNode != null) {
    // int recentPrefixNodeLength = recentPrefixNode.depth + 2;
    // if (prefixSoFar.length() == recentPrefixNodeLength) {
    // startingNode = getParent(recentPrefixNode);
    // } else if (prefixSoFar.length() - 1 == recentPrefixNodeLength) {
    // startingNode = recentPrefixNode;
    // }
    // }
    // Node validPrefixNode = validPrefixFromNode(startingNode, prefixSoFar);
    // recentPrefixNode = validPrefixNode;
    // if (validPrefixNode != null) {
    // if (validPrefixNode.score > 0) {
    // validWords.add(prefixSoFar);
    // }
    // for (Cell n : c.neighbours()) {
    // if (!n.visited()) {
    // String newPrefix = prefixSoFar + n.getFilteredChar();
    // dfs(n, newPrefix);
    // }
    // }
    // }
    // c.unVisit();
    // }

    private void enumerateAllStrings() {
        for (int row = 0; row < board.rows(); ++row) {
            for (int col = 0; col < board.cols(); ++col) {
                Cell cell = cells[row][col];
                recentPrefixNode = null;
                dfs(cell, cell.getFilteredChar());
            }
        }
    }

    // Returns the set of all valid words in the given Boggle board, as an
    // Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        this.board = board;
        cells = new Cell[board.rows()][board.cols()];
        precomputeCells();
        validWords = new HashSet<String>();
        enumerateAllStrings();
        return validWords;
    }

    // Raw word scores map , with no prior assumptions
    private int wordToScore(String word) {
        int len = word.length();
        if (len == 3 || len == 4)
            return 1;
        else if (len == 5)
            return 2;
        else if (len == 6)
            return 3;
        else if (len == 7)
            return 5;
        else if (len >= 8)
            return 11;
        else
            return 0;
    }

    // Returns the score of the given word if it is in the dictionary, zero
    // otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        return get(word);
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        StdOut.println("ALL VALID WORDS:");
        int count = 0;
        for (String word : solver.getAllValidWords(board)) {
            count++;
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        // StdOut.println("SKIPPED PREFIXES # = " + solver.skippedPrefixChecks);
        // StdOut.println("EVALUTED PREFIXES # = " + solver.prefixChecks);
        StdOut.println("Score = " + score);
        StdOut.println("Total words = " + count);
    }

}
