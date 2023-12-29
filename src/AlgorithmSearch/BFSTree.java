package AlgorithmSearch;

import AgentsAbstract.NodeId;

import java.util.*;

public class BFSTree {

    private NodeId root;
    private Map<NodeId, Set<NodeId>> neighborsMap;
    private Map<NodeId, Set<NodeId>> fatherAndSons;
    private Map<NodeId, Boolean> visited;
    private Map<NodeId, Boolean> creators;


    public BFSTree(Map<NodeId, Set<NodeId>> neighborsMap, NodeId root) {

        if (!neighborsMap.containsKey(root)) {
            throw new RuntimeException();
        }

        this.root = root;
        this.neighborsMap = neighborsMap;
        fatherAndSons = new HashMap<NodeId, Set<NodeId>>();
        this.visited = new HashMap<NodeId,Boolean>();
        this.creators = new HashMap<NodeId,Boolean>();
        createFatherAndSon();
    }


    @Override
    public String toString() {
        return fatherAndSons.toString();
    }

    public Set<NodeId> getSonsInTree(NodeId father){
        return this.fatherAndSons.get(father);
    }

    public void createFatherAndSon() {
        createVisited();
        createCreators();
        this.creators.put(root,true);
        Set<NodeId> sons = this.neighborsMap.get(this.root);
        this.fatherAndSons.put(this.root,sons);
        this.visited.put(root,true);
        markVisited(sons);

        while (!isAllVisited() || !isAllCreators()){
            NodeId creator = getNextCreator();
            sons = getNoneVisitedSons(creator);
            this.fatherAndSons.put(creator,sons);

        }
    }

    private Set<NodeId> getNoneVisitedSons(NodeId creator) {
        Set<NodeId> ans = new HashSet<NodeId>();
        Set<NodeId> allSons = this.neighborsMap.get(creator);
        for (NodeId nId:allSons) {
            if (!this.visited.get(nId)){
                ans.add(nId);
                this.visited.put(nId,true);
            }
        }
        return ans;
    }

    private NodeId getNextCreator() {
        for (NodeId creator: this.creators.keySet()) {
            if (!this.creators.get(creator)) {
                this.creators.put(creator, true);
                return creator;
            }
        }
        return null;
    }

    private void markVisited(Set<NodeId> sons) {
        for (NodeId nId:sons) {
            this.visited.put(nId,true);
        }
    }

    private void createCreators() {
        for (NodeId nId:this.neighborsMap.keySet()) {
            this.creators.put(nId,false);
        }

    }

    private boolean isAllVisited() {
        for (NodeId nId: this.visited.keySet()) {
            if (!this.visited.get(nId)) {
                return false;
            }
        }
        return true;
    }



    private boolean isAllCreators() {
        for (NodeId nId: this.creators.keySet()) {
            if (!this.creators.get(nId)) {
                return false;
            }
        }
        return true;
    }



    private void createVisited() {
        for (NodeId nId:this.neighborsMap.keySet()) {
            Set<NodeId> neighbors = this.neighborsMap.get(nId);
            for (NodeId neighbor : neighbors) {
                this.visited.put(neighbor, false);
            }
        }
    }

    public Set<NodeId> getAllNodesInTree() {
        Set<NodeId>ans = new HashSet<NodeId>();
        for (NodeId nId:
             this.fatherAndSons.keySet()) {
            ans.add(nId);
            for (NodeId nId2:
                 this.fatherAndSons.get(nId)) {
                ans.add(nId2);
            }
        }
        return ans;

    }
}
