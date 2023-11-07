package AlgorithmSearch;

import AgentsAbstract.NodeId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TokenTree {

    HashSet<NodeId> agentsInTheTree;

    public TokenTree(NodeId rootId){
        this.agentsInTheTree = new HashSet<NodeId>();
        this.agentsInTheTree.add(rootId);
    }

    public boolean addNodeIdToTree(NodeId nodeId){
        return agentsInTheTree.add(nodeId);
    }

}
