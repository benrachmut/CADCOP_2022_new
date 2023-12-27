package AlgorithmSearch;

import AgentsAbstract.NodeId;

import java.util.*;

public class BFSTree {

    private NodeId root;
    Map<NodeId, Set<NodeId>> neighborsMap;


    public BFSTree(NodeId root, Set<NodeId> rootNeighbors, NodeId x, Set<NodeId> xNeighbors) {
        this.root = root;
        neighborsMap = new HashMap<>();

        // Connect root with its neighbors
        neighborsMap.put(root, rootNeighbors);

        // Connect x with its neighbors
        neighborsMap.put(x, xNeighbors);
    }

    public void bfs() {
        if (root == null) {
            return;
        }

        Queue<NodeId> queue = new LinkedList<>();
        Set<NodeId> visited = new HashSet<>();

        queue.add(root);
        visited.add(root);

        while (!queue.isEmpty()) {
            NodeId current = queue.poll();

            Set<NodeId> currentNeighbors = neighborsMap.get(current);
            if (currentNeighbors != null) {
                for (NodeId neighbor : currentNeighbors) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }
    }
}
