package AlgorithmSearch;

import AgentsAbstract.NodeId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TokenTree {

    HashMap<NodeId,Integer> agentsInTheTree;

    public TokenTree(NodeId rootId){
        this.agentsInTheTree = new HashMap<NodeId,Integer>();
        this.agentsInTheTree.put(rootId,0);
    }

    public Map<NodeId,Integer> getMyNeighborsInTheToken(Set<NodeId>yourNeighbors){
        Map<NodeId,Integer> ans = new HashMap<NodeId,Integer>();
        for (NodeId nId: yourNeighbors) {
            if (this.agentsInTheTree.containsKey(nId)){
                ans.put(nId,this.agentsInTheTree.get(nId));
            }
        }
        return ans;
    }
}
