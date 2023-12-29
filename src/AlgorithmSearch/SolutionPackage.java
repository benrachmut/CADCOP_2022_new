package AlgorithmSearch;

import AgentsAbstract.NodeId;

import java.util.Set;

public class SolutionPackage {
    private  Set<NodeId> allNodesInTree;
    private  NodeId partnerId;
    private int lockedValue;
    private Set<NodeId> bfsChildren;
    private int partnerValue;

    public SolutionPackage(int lockedValue, Set<NodeId> bfsChildren, int partnerValue, NodeId partnerId,Set<NodeId>allNodesInTree) {
        this.lockedValue = lockedValue;
        this.bfsChildren = bfsChildren;
        this.partnerId = partnerId;
        this.partnerValue = partnerValue;
        this.allNodesInTree = allNodesInTree;
    }

    public NodeId getPartnerId() {
        return partnerId;
    }

    public int getLockedValue() {
        return lockedValue;
    }

    public Set<NodeId> getBfsChildren() {
        return bfsChildren;
    }

    public int getPartnerValue() {
        return partnerValue;
    }

    public Set<NodeId> getAllNodesInTree() {
        return allNodesInTree;
    }
}
