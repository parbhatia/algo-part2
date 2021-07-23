'''This problem is the same as the diameter of the n-ary tree. We have discussed a simple solution here. 
In this post, an efficient solution is discussed. We can find the longest path using two BFSs. The idea is based on the following fact: If we start BFS from any node x and find a node with the longest distance from x, it must be an endpoint of the longest path. It can be proved using contradiction. So our algorithm reduces to simple two BFSs. First BFS to find an endpoint of the longest path and second BFS from this endpoint to find the actual longest path. For the proof of why does this algorithm works, there is a nice explanation here Proof of correctness: Algorithm for the diameter of a tree in graph theory 
As we can see in the above diagram, if we start our BFS from node-0, the node at the farthest distance from it will be node-5, now if we start our BFS from node-5 the node at the farthest distance will be node-7, finally, the path from node-5 to node-7 will constitute our longest path.'''


'''
Design a linear-time algorithm to find a vertex such that its maximum distance from any other vertex is minimize.
Get the middle node of the longest path! that way, the max distance of the vertex to any other node is minimized.

'''
from collections import deque
 
 
class Graph:
 
    # Intialisation of graph
    def __init__(self, vertices):
 
        # No. of vertices
        self.vertices = vertices
 
        # adjacency list
        self.adj = {i: [] for i in range(self.vertices)}
 
    def addEdge(self, u, v):
        # add u to v's list
        self.adj[u].append(v)
        # since the graph is undirected
        self.adj[v].append(u)
 
    # method return farthest node and its distance from node u
    def BFS(self, u):
        # marking all nodes as unvisited
        visited = [False for i in range(self.vertices + 1)]
        # mark all distance with -1
        distance = [-1 for i in range(self.vertices + 1)]
 
        # distance of u from u will be 0
        distance[u] = 0
        # in-built library for queue which performs fast oprations on both the ends
        queue = deque()
        queue.append(u)
        # mark node u as visited
        visited[u] = True
 
        while queue:
 
            # pop the front of the queue(0th element)
            front = queue.popleft()
            # loop for all adjacent nodes of node front
 
            for i in self.adj[front]:
                if not visited[i]:
                    # mark the ith node as visited
                    visited[i] = True
                    # make distance of i , one more than distance of front
                    distance[i] = distance[front]+1
                    # Push node into the stack only if it is not visited already
                    queue.append(i)
 
        maxDis = 0
 
        # get farthest node distance and its index
        for i in range(self.vertices):
            if distance[i] > maxDis:
 
                maxDis = distance[i]
                nodeIdx = i
 
        return nodeIdx, maxDis
 
    # method prints longest path of given tree
    def LongestPathLength(self):
 
        # first DFS to find one end point of longest path
        node, Dis = self.BFS(0)
 
        # second DFS to find the actual longest path
        node_2, LongDis = self.BFS(node)
 
        print('Longest path is from', node, 'to', node_2, 'of length', LongDis)
 
 
# create a graph given in the example
 
G = Graph(10)
G.addEdge(0, 1)
G.addEdge(1, 2)
G.addEdge(2, 3)
G.addEdge(2, 9)
G.addEdge(2, 4)
G.addEdge(4, 5)
G.addEdge(1, 6)
G.addEdge(6, 7)
G.addEdge(6, 8)
 
G.LongestPathLength()